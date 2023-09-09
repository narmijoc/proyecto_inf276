//para tajeta sd
#include <SPI.h>
#include <SD.h>

#define SSpin 5

File archivo;

//biblioteca de bluetooth
#include "BluetoothSerial.h"

//Verificar que el bluetooth se ha habilitado correctamente 
# if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
# error Bluetooth is not enabled! Please run ´make menuconfig´ to and enable it 
# endif

//cosas sonda temperatura
#include <OneWire.h>                
#include <DallasTemperature.h>
OneWire ourWire(4);                //Se establece el pin 2  como bus OneWire
DallasTemperature sensors(&ourWire); //Se declara una variable u objeto para nuestro sensor


BluetoothSerial BTSerial;

//Para BME280
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BME280.h>


Adafruit_BME280 bme; // use I2C interface
float pressure;
float temperature;
float humidity;
float altitude;

#define SEALEVELPRESSURE_HPA (1018.00)


// GYM8511
int UVOUT = 13; //Output from the sensor
int REF_3V3 = 26; //3.3V power on the ESP32 board


void setup() {

  //Tarjeta sd
  Serial.println("Iniciando Tarjeta SD");
  if (!SD.begin(SSpin)){
    Serial.println("fallo en iniciar.");
  }
  Serial.println("fallo en iniciar SD");  
  archivo = SD.open("prueba.txt", FILE_WRITE);

  if(archivo){
    archivo.println("probando");
    archivo.close();
  }

  Serial.begin(115200);
  //nombre del dispositivo bluetooth
  BTSerial.begin("Borde Costero"); //nombre del esp32


  //sensor temperatura
  sensors.begin();


  //sensor BME280
    if (!bme.begin(0x76)) {
    Serial.println(F("No se ha encontrado el sensor BME280"));
    while (1) delay(10);
  }


  //recibir input
    pinMode(UVOUT, INPUT);
    pinMode(REF_3V3, INPUT);
}


//Takes an average of readings on a given pin
//Returns the average
int averageAnalogRead(int pinToRead)
{
  byte numberOfReadings = 8;
  unsigned int runningValue = 0; 
 
  for(int x = 0 ; x < numberOfReadings ; x++)
    runningValue += analogRead(pinToRead);
  runningValue /= numberOfReadings;
 
  return(runningValue);
}
 
float mapfloat(float x, float in_min, float in_max, float out_min, float out_max)
{
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
 

void loop() {

  if(Serial.available())
  {
    BTSerial.write(Serial.read());
  }
  if (BTSerial.available())
  {
    Serial.write(BTSerial.read());
  }

  //sonda
  sensors.requestTemperatures();   //Se envía el comando para leer la temperatura
  float temp= sensors.getTempCByIndex(0); //Se obtiene la temperatura en ºC

  //mandar por terminal
  //Serial.print("Temperatura= ");
  //Serial.print(temp);
  //Serial.println(" C");

  //mandar por bluetooth
  //BTSerial.println(temp);
  //delay(100);  



  //bmp 280
  pressure = bme.readPressure() / 100.0F ;
  temperature = bme.readTemperature();
  humidity = bme.readHumidity();
  altitude = bme.readAltitude(SEALEVELPRESSURE_HPA);
  
  //Serial.print(F("Temperatura = "));
  //Serial.print(temperature);
  //Serial.println(" *C");

  //Serial.print(F("Presion = "));
  //Serial.print(pressure );
  //Serial.println(" hPa");

  //Serial.print(F("Humedad = "));
  //Serial.print(humidity);
  //Serial.println(" %");

  //Serial.print(F("Altitud = "));
  //Serial.print(altitude);
  //Serial.println(" m");

  //Serial.println();


  // GYM8511
  int uvLevel = averageAnalogRead(UVOUT);
  int refLevel = averageAnalogRead(REF_3V3);

   //Use the 3.3V power pin as a reference to get a very accurate output value from sensor
  float outputVoltage = 3.3 / refLevel * uvLevel;
  float uvIntensity = mapfloat(outputVoltage, 0.99, 2.8, 0.0, 15.0); //Convert the voltage to a UV intensity level

  if (uvIntensity < 0){
    uvIntensity = 0;
  }
  
  Serial.print("output: ");
  Serial.print(refLevel);
 
  Serial.print("ML8511 output: ");
  Serial.print(uvLevel);
 
  Serial.print(" / ML8511 voltage: ");
  Serial.print(outputVoltage);
 
  Serial.print(" / UV Intensity (mW/cm^2): ");
  Serial.println(uvIntensity);
  delay(200);
}
