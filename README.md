# Air Quality Monitoring and AI-Driven Recommendations

This project demonstrates a real-time air quality monitoring system using Arduino, Java, and AI integration. The system uses sensors like DHT11 (for temperature and humidity) and MQ-135 (for air quality) to collect data, visualize it on a dashboard, and provide insights through AI-driven queries.

## Features

- **Real-Time Data Monitoring:** Continuously collects sensor data (Temperature, Humidity, Air Quality) and displays them as interactive graphs.
- **AI Integration:** Uses Gemini API to process sensor data and answer natural language questions like "What is the current temperature?" or "How is the air quality?"
- **Email Alerts:** Automatically sends email notifications when air quality levels exceed predefined thresholds (e.g., when AQI is dangerously high).
- **Route Suggestions:** Based on air quality levels, AI suggests alternate routes for employees or users to avoid highly polluted areas.

## Hardware Components

- **Arduino Uno**
- **DHT11 Sensor** (Temperature and Humidity)
- **MQ-135 Sensor** (Air Quality)
- **Jumper Wires and Breadboard** (for connections)

## Software Stack

- **Arduino IDE** (for Arduino code development)
- **Java (IntelliJ IDEA)** (for real-time data visualization)
- **Gemini API** (for AI-driven natural language queries)
- **SMTP Email Setup** (for sending email notifications)

## Getting Started

### Prerequisites

1. Install the necessary Arduino libraries for DHT11 and MQ-135.
2. Set up the Gemini API by obtaining your API key from [Google Gemini API](https://cloud.google.com/generative-language).
3. Install Java (latest version) and IntelliJ IDEA.
4. Install the necessary libraries for real-time communication with Arduino in Java (e.g., jSerialComm for serial communication).

### How to Use

1. **Connect the Sensors to Arduino:** 
   - Connect the DHT11 sensor to the Arduino for temperature and humidity readings.
   - Connect the MQ-135 sensor to measure air quality.
   
2. **Upload the Arduino Code:**
   - Use Arduino IDE to upload the code that reads sensor data and sends it to the Java application.

3. **Run the Java Application:**
   - Launch the Java application which will read data from Arduino and display it on a graphical dashboard.
   - The user can interact with the dashboard to switch between different types of data (temperature, humidity, air quality).
   - The system will also send email alerts if air quality levels exceed a threshold.

4. **Ask AI Questions:**
   - Use the AI-driven Q&A feature to ask questions about the collected sensor data (e.g., "What is the current temperature?").

### Example Use Cases

- **Real-time Monitoring:** Continuously track temperature, humidity, and air quality.
- **Critical Alerts:** Receive instant email notifications when air quality deteriorates.
- **Route Optimization:** AI suggests alternate routes for employees based on real-time air quality data.

## Future Enhancements

- Integration with more sensors (e.g., CO2, PM2.5).
- Mobile application support for remote monitoring.
- Advanced AI analytics for detailed environmental reports.
- 
## 🏗️ Installation & Setup  
1. **Clone the repository**  
   ```sh
   git clone https://github.com/utej8553/AirQualityMonitoring.git  
