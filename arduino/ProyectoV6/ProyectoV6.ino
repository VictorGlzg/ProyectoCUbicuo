#include <DHT.h>
#include <HTTPClient.h>
#include <WiFi.h>

// Definiciones y configuración
const char* ssid = "Pruebas";        // Nombre de la red WiFi
const char* password = "Carlos0317"; // Contraseña de la red WiFi
String serverUrl = "http://74f6-187-254-100-32.ngrok-free.app/arduino"; // URL del servidor
const int numSensors = 1;            // Número de sensores (dejar solo 1)
const int sensorPins[] = {33};       // Pines de los sensores (dejar solo el pin 33)
#define DHTPIN 27                    // Pin del sensor DHT11
#define DHTTYPE DHT11                // Tipo de sensor DHT11
DHT dht(DHTPIN, DHTTYPE);             // Instancia del sensor DHT11
bool sensorsEnabled = true;          // Estado de los sensores
const int pinControl = 13;           // Pin que controla la alimentación de los sensores

// Variables para el control del tiempo
unsigned long previousMillis = 0;
const long interval = 180000;  // Cambiar a 10000 milisegundos (10 segundos)
bool immediateSend = false;  // Bandera para enviar de forma inmediata

#define RX_PIN 16 // Pin RX
#define TX_PIN 17 // Pin TX

void setup() {
  Serial.begin(115200); // Inicializa la comunicación serial USB
  Serial.begin(9600, SERIAL_8N1, RX_PIN, TX_PIN); // Inicializa la comunicación serial en los pines RX y TX
  WiFi.begin(ssid, password); // Conéctate a la red WiFi
  while (WiFi.status() != WL_CONNECTED) delay(1000); // Espera a que la conexión WiFi se establezca
  dht.begin(); // Inicializa el sensor DHT11
  pinMode(pinControl, OUTPUT); // Configura el pin de control como salida
  digitalWrite(pinControl, LOW); // Asegura que el pin 13 esté desactivado al inicio
}

void sendDataToServer(float h, float t, float hd) {
  // Crea la URL para enviar datos al servidor
  String url = serverUrl + "?h=" + String(h) +
               "&t=" + String(t) +
               "&hd=" + String(hd);
  HTTPClient http; // Instancia para realizar solicitudes HTTP
  http.begin(url); // Inicia la solicitud HTTP
  http.addHeader("Content-Type", "application/json"); // Establece el encabezado como JSON
  String payload = String("{\"h\":") + String(h) +
                   String(",\"t\":") + String(t) +
                   String(",\"hd\":") + String(hd) +
                   String("}");
  int httpResponseCode = http.POST(payload); // Realiza una solicitud POST con datos JSON
  if (httpResponseCode == HTTP_CODE_OK) {
    Serial.println("Datos enviados al servidor con éxito.");
  } else {
    Serial.print("Error al enviar datos al servidor. Código de respuesta: ");
    Serial.println(httpResponseCode);
  }
  http.end(); // Finaliza la solicitud HTTP
}

void loop() {
  unsigned long currentMillis = millis();

  if (Serial.available() > 0) {
    char command = Serial.read();
    if (command == '1') {
      sensorsEnabled = true; // Habilita los sensores
      Serial.println("Sensores habilitados.");
      digitalWrite(pinControl, HIGH); // Activa 3.3V en el pin 13

      float h = map(analogRead(sensorPins[0]), 4092, 0, 0, 100); // Leer datos del sensor activo
      float t = dht.readTemperature();
      float hd = dht.readHumidity();

      Serial.print("Sensor (Suelo): ");
      Serial.print("H: "); Serial.print(h); Serial.println("%");
      Serial.print("Sensor DHT11 (Ambiente): "); Serial.print("T: ");
      Serial.print(t); Serial.print("°C, H: "); Serial.print(hd);
      Serial.println("%");

      sendDataToServer(h, t, hd); // Envía datos al servidor
      Serial.println("Datos enviados al servidor.");

      digitalWrite(pinControl, LOW); // Desactiva el pin 13
      immediateSend = true;
    } else if (command == '0') {
      sensorsEnabled = false; // Deshabilita los sensores
      Serial.println("Sensores deshabilitados.");
      digitalWrite(pinControl, LOW); // Desactiva 3.3V en el pin 13
      immediateSend = false;
    }
  }
  
  if (sensorsEnabled && (currentMillis - previousMillis >= interval || immediateSend)) {
    digitalWrite(pinControl, HIGH); // Activa el pin 13
    delay(2000); // Espera 2 segundos

    for (int i = 0; i < numSensors; i++) {
      float h = map(analogRead(sensorPins[i]), 4092, 0, 0, 100);
      float t = dht.readTemperature();
      float hd = dht.readHumidity();
      Serial.print("Sensor "); Serial.print(i + 1); Serial.print(" (Suelo): ");
      Serial.print("H: "); Serial.print(h); Serial.println("%");
      Serial.print("Sensor DHT11 (Ambiente): "); Serial.print("T: ");
      Serial.print(t); Serial.print("°C, H: "); Serial.print(hd);
      Serial.println("%");
      sendDataToServer(h, t, hd); // Envía datos al servidor
      delay(1000); // Espera 1 segundo
    }
    digitalWrite(pinControl, LOW); // Desactiva el pin 13
    previousMillis = currentMillis;
    immediateSend = false;
  }
}

