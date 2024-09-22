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
const unsigned long LOG_INTERVAL_IN_MILLISECONDS = 300000;
const unsigned long HEARTBEAT_INTERVAL_IN_MILLISECONDS = 10000;
const unsigned long EVENT_SIZE_IN_BYTES = 1024;
const unsigned long MEGABYTE_IN_BYTES = 1000000;
unsigned long millisecondsSinceLastHeartbeat = 0;
unsigned long timeOfDisconnect = 0;
unsigned long currentTime = 0;

void setup()
{
  WiFi.begin(USER_SSID, USER_SSID_TOKEN);
  WiFi.onEvent(WiFiEventHandler);
  configTime(0, 0, "pool.ntp.org");
  timeOfLastLog = millis();
}

void WiFiEventHandler(WiFiEvent_t event)
{
  if (event == SYSTEM_EVENT_STA_DISCONNECTED)
  {
    logEvent(false, "WiFi disconnected");
    hasDisconnected = true;
    hasReconnected = false;
    timeOfDisconnect = millis();
  }
  else if (event == SYSTEM_EVENT_STA_CONNECTED)
  {
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
    unsigned long downtime = (millis() - timeOfDisconnect) / 1000;
    String message = "Heartbeat: WiFi has been down for " + String(downtime) + " seconds";
    logEvent(false, message);
  }
  else
  {
    logEvent(true, "Heartbeat: WiFi is up");
  }
}

void logEvent(bool connected, String message)
{
  String timestamp = getCurrentTime();

  DynamicJsonDocument jsonDoc(EVENT_SIZE_IN_BYTES);
  jsonDoc["Timestamp"] = timestamp;
  jsonDoc["Connected"] = connected;
  jsonDoc["Message"] = message;

  String logEntry;
  serializeJson(jsonDoc, logEntry);
  logs.push_back(logEntry);
}

String getCurrentTime()
{
  time_t now = time(nullptr);
  struct tm timeInfo;
  localtime_r(&now, &timeInfo);

  char buffer[25];
  strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S", &timeInfo);
  return String(buffer);
}

void sendLogsToServer()
{
  if (WiFi.status() == WL_CONNECTED && !logs.empty())
  {
    DynamicJsonDocument jsonDoc(MEGABYTE_IN_BYTES);
    JsonArray events = jsonDoc.createNestedArray("events");

    for (String log : logs)
    {
      DynamicJsonDocument eventJson(EVENT_SIZE_IN_BYTES);
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

void loop()
{
  currentTime = millis();
  checkHeartbeat();
  checkLogs();
}
