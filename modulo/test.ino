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

#define SEALEVELPRESSURE_HPA (1013.00)


// GYM8511
int UVOUT = 13; //Output from the sensor
int REF_3V3 = 26; //3.3V power on the ESP32 board


//Para GY-302
#include <BH1750.h>
BH1750 GY302(0x23); // initialize BH1750 object, con direccion 0x23


//Para el sensor tenedor
#define sensorPower 2 // Cambiar al pin GPIO 4 en el ESP32
#define sensorPin 33 


//Para sensor de ppm
#define TdsSensorPin 27
#define VREF 3.3      // analog reference voltage(Volt) of the ADC
#define SCOUNT  30           // sum of sample point 
int analogBuffer[SCOUNT];    // store the analog value in the array, read from ADC
int analogBufferTemp[SCOUNT];
int analogBufferIndex = 0;
int copyIndex = 0;
float averageVoltage = 0;
float tdsValue = 0;
float temperatureTDS = 0;


void setup() {
  Wire.begin();
  
 
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

   //Sensor ppm
  pinMode(TdsSensorPin,INPUT);

  
  //nombre del dispositivo bluetooth
  BTSerial.begin("Borde Costero"); //nombre del esp32


  //sensor temperatura
  sensors.begin();


  //sensor BME280
    if (!bme.begin(0x76)) {
    Serial.println(F("No se ha encontrado el sensor BME280"));
    //while (1) delay(10);
  }


  //recibir input
    pinMode(UVOUT, INPUT);
    pinMode(REF_3V3, INPUT);

  //iniciar GY-302
  GY302.begin(); // initialize GY-302 module


  //Sensor tenedor
    pinMode(sensorPower, OUTPUT);
  // Initially keep the sensor OFF
  digitalWrite(sensorPower, LOW);


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
  //BTSerial.print(temp);
  //BTSerial.println("C");
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
  
  //Serial.print("output: ");
  //Serial.print(refLevel);
 
  //Serial.print("ML8511 output: ");
  //Serial.print(uvLevel);
 
  //Serial.print(" / ML8511 voltage: ");
  //Serial.print(outputVoltage);
 
  //Serial.print(" / UV Intensity (mW/cm^2): ");
  //Serial.println(uvIntensity);
  //delay(200);


   //Sensor GY-302 intensidad luminosa
   // get reading from module
  uint16_t lux = GY302.readLightLevel();
  
  // display to Serial Monitor
  Serial.print("Light: ");
  Serial.print(lux);
  Serial.println(" lux");
  // Convert lux to float
  float luxFloat = static_cast<float>(lux) /1.0; // Assuming lux is in hundredths
  Serial.println(luxFloat);

  //Sensor tenedor
   //get the reading from the function below and print it
  //Serial.print("Analog output: ");
  //Serial.println(readSensor());
  float tenedorData = readSensor();

  //sensor ppm
  sensors.requestTemperatures();
  float temperatureTDS = sensors.getTempCByIndex(0);
 
  static unsigned long analogSampleTimepoint = millis();
  if (millis() - analogSampleTimepoint > 40U)  //every 40 milliseconds,read the analog value from the ADC
  {
    analogSampleTimepoint = millis();
    analogBuffer[analogBufferIndex] = analogRead(TdsSensorPin);    //read the analog value and store into the buffer
    analogBufferIndex++;
    if (analogBufferIndex == SCOUNT)
      analogBufferIndex = 0;
  }
  static unsigned long printTimepoint = millis();
  if (millis() - printTimepoint > 800U)
  {
    printTimepoint = millis();
    for (copyIndex = 0; copyIndex < SCOUNT; copyIndex++)
      analogBufferTemp[copyIndex] = analogBuffer[copyIndex];
    averageVoltage = getMedianNum(analogBufferTemp, SCOUNT) * (float)VREF / 4096.0; // read the analog value more stable by the median filtering algorithm, and convert to voltage value
    float compensationCoefficient = 1.0 + 0.02 * (temperatureTDS - 25.0); //temperature compensation formula: fFinalResult(25^C) = fFinalResult(current)/(1.0+0.02*(fTP-25.0));
    float compensationVolatge = averageVoltage / compensationCoefficient; //temperature compensation
    tdsValue = (133.42 * compensationVolatge * compensationVolatge * compensationVolatge - 255.86 * compensationVolatge * compensationVolatge + 857.39 * compensationVolatge) * 0.5; //convert voltage value to tds value
 
    //Serial.print("TDS Value:");
    //Serial.print(tdsValue, 0);
    //Serial.println("ppm");
 

 }
  //Formato para enviar
  // Imprime los datos en una sola línea
  char datos[150];
  sprintf(datos, "tempSonda:%.2f,tempAmbiente:%.2f,presion:%.2f,humedadAmbiente:%.2f,altitud:%.2f,UV:%.2f,lightIntensity:%.2f,humedadSuelo:%.2f,ppm:%.2f", temp,temperature,pressure,humidity,altitude,uvIntensity,luxFloat,tenedorData,tdsValue);
  //Serial.println(datos);
  BTSerial.println(datos);
  //tiene que ser un println o no lo recibe la app, tiene que existir salto de linea


 
}

int getMedianNum(int bArray[], int iFilterLen)
{
  int bTab[iFilterLen];
  for (byte i = 0; i < iFilterLen; i++)
    bTab[i] = bArray[i];
  int i, j, bTemp;
  for (j = 0; j < iFilterLen - 1; j++)
  {
    for (i = 0; i < iFilterLen - j - 1; i++)
    {
      if (bTab[i] > bTab[i + 1])
      {
        bTemp = bTab[i];
        bTab[i] = bTab[i + 1];
        bTab[i + 1] = bTemp;
      }
    }
  }
  if ((iFilterLen & 1) > 0)
    bTemp = bTab[(iFilterLen - 1) / 2];
  else
    bTemp = (bTab[iFilterLen / 2] + bTab[iFilterLen / 2 - 1]) / 2;
  return bTemp;
}

//  This function returns the analog soil moisture measurement
int readSensor() {
  digitalWrite(sensorPower, HIGH);  // Turn the sensor ON
  delay(10);              // Allow power to settle
  int val = analogRead(sensorPin);  // Read the analog value form sensor
  digitalWrite(sensorPower, LOW);   // Turn the sensor OFF
  return val;             // Return analog moisture value
}
