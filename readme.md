# Network Connectivity Monitor
This project is built around an ESP32 microcontroller that logs network events and interacts with a custom backend server hosted on a Raspberry Pi. With a PostgreSQL database handling the logs, and a Java-based REST API bridging the communication, it provides detailed tracking of network connectivity issues over time. The idea is to spot problems and have data to back up claims when troubleshooting.

For more details, you can jump to the following sections:

- [Project Inspiration](#project-inspiration)
- [Key Features](#key-features)
- [Technologies and Tools Used](#technologies-and-tools-used)
- [Setup Instructions](#setup-instructions)
- [What I Learned](#what-i-learned)

## Project Inspiration
In September of 2024, I was experiencing some network connectivity issues after my internet service provider rolled and out a new Wi-Fi 7 system in my city. The first few weeks were a bit of a nightmare with frequent loss of signal and repeated timeouts from the VPN used for work. Sure, I could have always just spent my own money to get my own mesh network, but I had a perfectly working system prior to the mandatory upgrade and wasn't in a place where I could justify the cost. So I started a google sheet where I would manually log this information.

While the sheet was useful in proving to the customer service team that it was an issue on their end and not on my end, it became obvious that this was not going to be a quick solution. When the ISP sent a technician out to my house, he made it clear that while he has faith the company would eventually iron out their issues, there was no way he could move me back on to my old system and that I'd have to either spend money I didn't have or be patient. So I did what any software developer would do and thought "I could easily automate this process over the weekend" and this project was born.


## Key Features

### 1. **Wi-Fi Event Handling**
   - **Connection and Disconnection Events**: The ESP32 listens for Wi-Fi connection (`SYSTEM_EVENT_STA_CONNECTED`) and disconnection (`SYSTEM_EVENT_STA_DISCONNECTED`) events. Upon disconnection, a log is generated with the exact downtime, and reconnection triggers a "reconnect" log.
   - **Heartbeat Monitoring**: A heartbeat log is generated every 30 seconds, reflecting either a stable connection or the total downtime since the last disconnection (formatted as "X minutes and Y seconds"). This also triggers a reconnection in the event of a disconnect.
   - **Internet Connectivity Monitoring**: Every 15 minutes, a ping to `8.8.8.8` is sent out to verify that the internet is still connected. Additionally, if a Wi-Fi disconnect event occurs, the system will send a ping to check upon reconnect. To prevent excessive pings, a failed ping needs to wait at least 15 seconds before attempting again.

### 2. **Memory Management and Log Handling**
   - **JSON Log Storage**: Each log entry is serialized into JSON format and stored in a `std::list<String>`. Logs contain:
     - A timestamp in ISO 8601 format.
     - The connection status (`true` or `false`).
     - A detailed message (e.g., "Wi-Fi has been down for 10 minutes and 30 seconds").
   - **Log Trimming**: To prevent memory overflows, when the log list exceeds 180 entries, it trims the middle logs, retaining the first 5 and last 5 logs, ensuring critical logs are preserved.
   - **Payload Batching**: Logs are sent in batches to reduce the frequency of HTTP requests. The ESP32 sends logs either after 5 minutes of uptime or immediately upon reconnection after a downtime.

### 3. **API Communication**
   - **HTTP POST Requests**: Batches of logs are serialized into JSON and sent to a Java-based API running on a Raspberry Pi. The payload is structured as an unnamed JSON array of events.
   - **Error Handling**: HTTP response codes are checked for success (201) or failure (400), and logs are cleared only on a successful transmission.
   - **Dynamic JSON Allocation**: Memory-efficient handling of JSON using `ArduinoJson` to ensure the ESP32 can process a large number of events without exceeding memory limits.

### 4. **Backend API and Database Integration**
   - **Java-Based Web API**: Deployed on a Raspberry Pi with a DHCP reserved IP address, the API is built using Spring Boot. The API handles log data sent from the ESP32 and stores it in a PostgreSQL database.
   - **PostgreSQL Integration**: The PostgreSQL database, also hosted on the Raspberry Pi, is accessed by Java Data Access Objects (DAOs) using JDBC, enabling persistent storage of logs and efficient querying.
   - **Integration Testing**: Integration tests ensure the proper functionality of the JDBC DAOs, verifying that logs are correctly stored and retrieved from the database.

### 5. **Time Synchronization**
   - **NTP Integration**: The ESP32 synchronizes its system time with an NTP server (`pool.ntp.org`) to ensure accurate timestamps for event logs. Logs generated before time synchronization are discarded to prevent invalid timestamps.
   
## Technologies and Tools Used

- **Hardware**: ESP32, Raspberry Pi
- **Programming Languages**: C++ (ESP32), Java (API)
- **Frameworks & Libraries**:
  - Spring Boot (for API)
  - `ArduinoJson` (for JSON serialization)
  - WiFi and HTTPClient libraries (ESP32)
  - PostgreSQL (Database)
  - JDBC (Java DAOs)
- **Testing**: Integration testing of Java DAOs using JUnit

## Setup Instructions

1. **ESP32 Setup**:
   - Clone the ESP32 project and deploy it using PlatformIO or Arduino IDE.
   - Ensure the `credentials.h` file contains the correct WiFi SSID, password, and API endpoint.

2. **Java API Setup**:
   - Deploy the Spring Boot web API on a Raspberry Pi with a reserved DHCP IP.
   - Configure PostgreSQL and ensure the database is accessible by the API.

3. **Testing**:
   - Run integration tests on the server-side API to ensure proper communication with the PostgreSQL database.

## What I Learned
While there are a lot of aspects to this project that were familiar, there was certainly some new aspects for me as well. First of all, deploying to a Raspberry Pi for the first time took me well outside of my comfort zone in the best way. This proved to be the trickiest part of the entire process simply due to having so many ways to approach it, which was exactly why I was excited. In the process, I had to learn a number of new skills which include, but are not limited to, setting up environment variables in Linux, creating PostgreSQL users on Linux (which is slightly different from doing it on Windows), granting permissions and modifying privileges, and setting up a `systemctl` service to make sure the server automatically stars up after a power failure.

All of that directly feeds into the second major learning moment for me: deploying Spring Boot projects in IntelliJ. While I can't say for certain that my approach is the most efficient, it certainly was the one that worked. In a self-taught environment, you don't get the luxury of having code reviews to learn from your peers. You have to make executive decisions and go with them. For me, this meant creating a Tomcat Servlet and adding the ability to export as a `.war` file. This took a lot of troubleshooting to get my settings right. And while I'd love to nerd out on this topic, I have kept these changes offline for peace of mind. 

The last thing I learned is that I'm either getting better at estimating how long a project will take me, I'm getting faster at developing projects, or, most likely, a combination of both. When I started out, I felt pretty confident that I could get this knocked out in an afternoon. But it's not my first rodeo. The part of me that wasn't speaking from a place of pure excitement said that it would be closer to about a week. I split the difference and convinced myself (and anyone who was around) that it was a going to be my weekend project. Either way, it felt great knowing that I could have an idea, set myself a schedule, and actually accomplish my goals in that time. It might not be a technology, but self awareness is still an important part of the creative process!
