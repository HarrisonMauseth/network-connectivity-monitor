#include <Arduino.h>
#include "../lib/credentials.h"
#include <WiFi.h>
#include <HTTPClient.h>
#include <list>
#include <ArduinoJson.h>
#include <time.h>

std::list<String> logs;
bool hasDisconnected = false;
bool hasReconnected = false;
unsigned long timeOfLastLog = 0;
const int MAX_LOGS = 180;
const unsigned long LOG_INTERVAL_IN_MILLISECONDS = 300000;
const unsigned long HEARTBEAT_INTERVAL_IN_MILLISECONDS = 10000;
const unsigned long JSON_EVENT_SIZE_IN_BYTES = 256;
const unsigned long MAX_JSON_SIZE_IN_BYTES = (MAX_LOGS + 20) * JSON_EVENT_SIZE_IN_BYTES;
unsigned long millisecondsSinceLastHeartbeat = 0;
unsigned long timeOfDisconnect = 0;
unsigned long currentTime = 0;

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

void logEvent(bool connected, String message)
{
  String timestamp = getCurrentTime();
  Serial.println("attempting log at: " + timestamp);

  DynamicJsonDocument jsonDoc(JSON_EVENT_SIZE_IN_BYTES);
  jsonDoc["eventId"] = timestamp;
  jsonDoc["isConnected"] = connected;
  jsonDoc["message"] = message;

  String logEntry;
  serializeJson(jsonDoc, logEntry);
  logs.push_back(logEntry);
  Serial.println("event logged! Total logs: " + String(logs.size()));
  checkAndTrimLogs();
}

void WiFiEventHandler(WiFiEvent_t event)
{
  if (event == SYSTEM_EVENT_STA_DISCONNECTED)
  {
    Serial.println("WiFi disconnected");
    logEvent(false, "WiFi disconnected");
    hasDisconnected = true;
    hasReconnected = false;
    timeOfDisconnect = millis();
  }
  else if (event == SYSTEM_EVENT_STA_CONNECTED)
  {
    Serial.println("WiFi connected");
    logEvent(true, "WiFi connected");
    if (hasDisconnected)
    {
      hasReconnected = true;
    }
  }
}

void heartbeatCheck()
{
  if (WiFi.status() != WL_CONNECTED)
  {
    unsigned long downtime = (millis() - timeOfDisconnect);
    unsigned long seconds = downtime / 1000;
    unsigned long minutes = seconds / 60;
    seconds %= 60;

    String message = "Heartbeat: WiFi has been down for " + String(minutes) + " minutes and " + String(seconds) + " seconds";
    Serial.println(message);
    logEvent(false, message);
  }
  else
  {
    Serial.println("Heartbeat: WiFi is up");
    logEvent(true, "Heartbeat: WiFi is up");
  }
}

void sendLogsToServer()
{
  if (WiFi.status() == WL_CONNECTED && !logs.empty())
  {
    DynamicJsonDocument jsonDoc(MAX_JSON_SIZE_IN_BYTES);
    JsonArray events = jsonDoc.createNestedArray("events");

    for (String log : logs)
    {
      DynamicJsonDocument eventJson(JSON_EVENT_SIZE_IN_BYTES);
      deserializeJson(eventJson, log);
      events.add(eventJson);
    }

    String payload;
    serializeJson(jsonDoc, payload);

    HTTPClient http;
    http.begin(API_ENDPOINT);
    http.addHeader("Content-Type", "application/json");

    int httpResponseCode = http.POST(payload);
    if (httpResponseCode == 200)
    {
      logs.clear();
      hasDisconnected = false;
      hasReconnected = false;
    }
    http.end();
  }
}

void checkHeartbeat()
{
  if (currentTime - millisecondsSinceLastHeartbeat >= HEARTBEAT_INTERVAL_IN_MILLISECONDS)
  {
    millisecondsSinceLastHeartbeat = currentTime;
    heartbeatCheck();
  }
}

void checkLogs()
{
  if ((currentTime - timeOfLastLog >= LOG_INTERVAL_IN_MILLISECONDS && WiFi.status() == WL_CONNECTED) || (hasDisconnected && hasReconnected))
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
  timeOfLastLog = millis();
}

void loop()
{
  currentTime = millis();
  checkHeartbeat();
  checkLogs();
}
