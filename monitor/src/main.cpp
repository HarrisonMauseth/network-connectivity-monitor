#include <Arduino.h>
#include <credentials.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ESP32Ping.h>
#include <list>
#include <ArduinoJson.h>
#include <time.h>

const IPAddress PING_IP(8, 8, 8, 8);
const int MAX_LOGS = 180;
const unsigned long LOG_INTERVAL_IN_MILLISECONDS = 300000;
const unsigned long HEARTBEAT_INTERVAL_IN_MILLISECONDS = 30000;
const unsigned long PING_INTERVAL_IN_MILLISECONDS = 900000;
const unsigned long PING_DELAY_IN_MILLISECONDS = 15000;
const unsigned long JSON_EVENT_SIZE_IN_BYTES = 256;
const unsigned long MAX_JSON_SIZE_IN_BYTES = (MAX_LOGS + 20) * JSON_EVENT_SIZE_IN_BYTES;
bool wifiIsConnected = false;
bool internetIsConnected = false;
bool wifiHasDisconnectedFlag = false;
bool wifiHasReconnectedFlag = false;
bool timeIsSynced = false;
unsigned long currentTime = 0;
unsigned long lastHeartbeatTime = 0;
unsigned long lastPingTime = 0;
unsigned long timeOfLastLog = 0;
unsigned long timeOfDisconnect = 0;
std::list<String> logs;

String getCurrentTime()
{
  time_t now = time(nullptr);
  struct tm timeInfo;
  localtime_r(&now, &timeInfo);

  char buffer[25];
  strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S", &timeInfo);
  return String(buffer);
}

void checkAndTrimLogs()
{
  if (logs.size() >= MAX_LOGS)
  {
    std::list<String> trimmedLogs;

    for (int i = 0; i < 5 && !logs.empty(); ++i)
    {
      trimmedLogs.push_back(logs.front());
      logs.pop_front();
    }

    for (int i = 0; i < 5 && !logs.empty(); ++i)
    {
      trimmedLogs.push_back(logs.back());
      logs.pop_back();
    }

    logs.clear();
    logs = trimmedLogs;
  }
}

void checkInternetConnection()
{
  if (Ping.ping(PING_IP))
  {
    Serial.println("Successful Ping!");
    internetIsConnected = true;
  }
  else
  {
    Serial.println("Ping Failed!");
    internetIsConnected = false;
  }
}

void logEvent(bool isConnectedToWifi, bool isConnectedToInternet, String message)
{
  if (!timeIsSynced)
    return;

  String timestamp = getCurrentTime();

  DynamicJsonDocument jsonDoc(JSON_EVENT_SIZE_IN_BYTES);
  jsonDoc["eventTime"] = timestamp;
  jsonDoc["isConnectedToWifi"] = isConnectedToWifi;
  jsonDoc["isConnectedToInternet"] = isConnectedToInternet;
  jsonDoc["message"] = message;

  String logEntry;
  serializeJson(jsonDoc, logEntry);
  logs.push_back(logEntry);
  Serial.println(logEntry);
  checkAndTrimLogs();
}

void WiFiEventHandler(WiFiEvent_t event)
{
  if (event == SYSTEM_EVENT_STA_DISCONNECTED && !wifiHasDisconnectedFlag)
  {
    wifiHasDisconnectedFlag = true;
    wifiHasReconnectedFlag = false;
    wifiIsConnected = false;
    internetIsConnected = false;
    logEvent(wifiIsConnected, internetIsConnected, "WiFi disconnected");
    timeOfDisconnect = millis();
  }
  else if (event == SYSTEM_EVENT_STA_CONNECTED)
  {
    wifiIsConnected = true;
    if (wifiHasDisconnectedFlag)
    {
      checkInternetConnection();
      logEvent(wifiIsConnected, internetIsConnected, "WiFi reconnected");
      wifiHasReconnectedFlag = true;
    }
    else
    {
      checkInternetConnection();
      logEvent(wifiIsConnected, internetIsConnected, "WiFi connected");
    }
  }
}

void sendLogsToServer()
{
  if (WiFi.status() == WL_CONNECTED && !logs.empty())
  {
    DynamicJsonDocument jsonDoc(MAX_JSON_SIZE_IN_BYTES);
    JsonArray events = jsonDoc.to<JsonArray>();

    for (String log : logs)
    {
      DynamicJsonDocument eventJson(JSON_EVENT_SIZE_IN_BYTES);
      deserializeJson(eventJson, log);
      events.add(eventJson);
      Serial.println("Log added to payload: " + log);
    }

    String payload;
    serializeJson(jsonDoc, payload);

    HTTPClient http;
    http.begin(API_ENDPOINT);
    http.addHeader("Content-Type", "application/json");

    int httpResponseCode = http.POST(payload);
    if (httpResponseCode == 201)
    {
      Serial.println("Success!");
      logs.clear();
      wifiHasDisconnectedFlag = false;
      wifiHasReconnectedFlag = false;
    }
    else
    {
      Serial.println("HTTP Request failed. Response code: " + String(httpResponseCode));
      String response = http.getString(); // Capture the server's response
      Serial.println("Response from server: " + response);
    }
    http.end();
  }
}

void logHeartbeatStatus()
{
  if (WiFi.status() != WL_CONNECTED)
  {
    wifiIsConnected = false;
    internetIsConnected = false;
    unsigned long downtime = (millis() - timeOfDisconnect);
    unsigned long downtimeSeconds = downtime / 1000;
    unsigned long downtimeMinutes = downtimeSeconds / 60;
    downtimeSeconds %= 60;

    String message = "Heartbeat: WiFi has been down for " + String(downtimeMinutes) + " minutes and " + String(downtimeSeconds) + " seconds";
    Serial.println(message);
    logEvent(wifiIsConnected, internetIsConnected, message);
    Serial.println("WiFi attempting to reconnect...");
    WiFi.reconnect();
  }
  else
  {
    wifiIsConnected = true;

    unsigned long timeSinceLastPing = currentTime - lastPingTime;
    unsigned long pingSeconds = timeSinceLastPing / 1000;
    unsigned long pingMinutes = pingSeconds / 60;
    pingSeconds %= 60;

    String message;
    if (internetIsConnected)
    {
      message = "Heartbeat: WiFi is up. Last ping to the internet was " + String(pingMinutes) + " minutes and " + String(pingSeconds) + " seconds ago.";
      logEvent(wifiIsConnected, internetIsConnected, message);
    }
    else
    {
      message = "Heartbeat: WiFi is up, but no internet connection. Last ping to the internet was " + String(pingMinutes) + " minutes and " + String(pingSeconds) + " seconds ago.";
      logEvent(wifiIsConnected, internetIsConnected, message);
      checkInternetConnection();
    }
  }
}

void monitorHeartbeat()
{
  if (currentTime - lastHeartbeatTime >= HEARTBEAT_INTERVAL_IN_MILLISECONDS)
  {
    lastHeartbeatTime = currentTime;
    logHeartbeatStatus();
  }
}

void monitorPingInterval()
{
  if (wifiHasReconnectedFlag ||
      (currentTime - lastPingTime >= PING_INTERVAL_IN_MILLISECONDS) ||
      (!wifiHasDisconnectedFlag && !internetIsConnected && currentTime - lastPingTime >= PING_DELAY_IN_MILLISECONDS))
  {
    lastPingTime = currentTime;
    checkInternetConnection();
  }
}

void checkLogs()
{
  if ((currentTime - timeOfLastLog >= LOG_INTERVAL_IN_MILLISECONDS && WiFi.status() == WL_CONNECTED) || (wifiHasDisconnectedFlag && wifiHasReconnectedFlag))
  {
    sendLogsToServer();
    timeOfLastLog = currentTime;
  }
}

void setup()
{
  Serial.begin(9600);

  WiFi.begin(USER_SSID, USER_SSID_TOKEN);
  WiFi.onEvent(WiFiEventHandler);
  configTime(0, 0, "pool.ntp.org");

  while (time(nullptr) < 100000)
  {
    delay(500);
  }

  timeIsSynced = true;
  timeOfLastLog = millis();

  if (WiFi.status() == WL_CONNECTED)
  {
    wifiIsConnected = true;
    logEvent(wifiIsConnected, internetIsConnected, "WiFi connected");
    checkInternetConnection();
    if (internetIsConnected)
    {
      logEvent(wifiIsConnected, internetIsConnected, "Internet connected");
    }
  }
}

void loop()
{
  currentTime = millis();
  monitorHeartbeat();
  monitorPingInterval();
  checkLogs();
}
