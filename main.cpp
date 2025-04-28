#include <DHT.h>
#include<Wire.h>
#include<SPI.h>

#define DHTPIN 2      
#define DHTTYPE DHT11 

DHT dht(DHTPIN, DHTTYPE);

void setup() {
  Serial.begin(9600);
  dht.begin();
}

void loop() {
  float temp = dht.readTemperature();
  float hum = dht.readHumidity();
  int airQuality = analogRead(A0); 

  if (isnan(temp) || isnan(hum)) {
    Serial.println("Error reading sensor");
    return;
  }

  Serial.print(temp);
  Serial.print(",");
  Serial.print(hum);
  Serial.print(",");
  Serial.println(airQuality);
  
  delay(2000);
}
