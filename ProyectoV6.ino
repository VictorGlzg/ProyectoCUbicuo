#include <DHT.h>
#include <HTTPClient.h>
#include <WiFi.h>
#include <WiFiServer.h>

// Definiciones y configuración
const char* ssid = "Dormitorio";        // Nombre de la red WiFi
const char* password = "Carlos0317";    // Contraseña de la red WiFi
String serverUrl = "http://192.168.0.122:3000"; // URL del servidor
const int serverPort = 3000;             // Puerto del servidor
const int numSensors = 1;                // Número de sensores (dejar solo 1)
const int sensorPins[] = {33};           // Pines de los sensores (dejar solo el pin 33)
#define DHTPIN 27                        // Pin del sensor DHT11
#define DHTTYPE DHT11                    // Tipo de sensor DHT11
DHT dht(DHTPIN, DHTTYPE);                 // Instancia del sensor DHT11
bool sensorsEnabled = true;              // Estado de los sensores
const int pinControl = 13;               // Pin que controla la alimentación de los sensores

// Variables para el control del tiempo
unsigned long previousMillis = 0;
const long interval = 60000;  // Intervalo de un minuto en milisegundos
bool immediateSend = false;  // Bandera para enviar de forma inmediata

// Servidor web para escuchar solicitudes
WiFiServer server(80);

#define RX_PIN 16 // Pin RX
#define TX_PIN 17 // Pin TX

void setup() {
  Serial.begin(115200); // Inicializa la comunicación serial USB
  Serial.begin(9600, SERIAL_8N1, RX_PIN, TX_PIN); // Inicializa la comunicación serial en los pines RX y TX
  WiFi.begin(ssid, password); // Conéctate a la red WiFi
  while (WiFi.status() != WL_CONNECTED) delay(1000); // Espera a que la conexión WiFi se establezca
  dht.begin(); // Inicializa el sensor DHT11
  
  // Inicializa el servidor web en el puerto 80
  server.begin();
  pinMode(pinControl, OUTPUT); // Configura el pin de control como salida
  digitalWrite(pinControl, LOW); // Asegura que el pin 13 esté desactivado al inicio
}

void sendDataToServer(float h, float t, float hd) {
  // Crea la URL para enviar datos al servidor
  String url = serverUrl + "/data?h=" + String(h) +
               "&t=" + String(t) +
               "&hd=" + String(hd);
  HTTPClient http; // Instancia para realizar solicitudes HTTP
  http.begin(url); // Inicia la solicitud HTTP
  if (http.GET() == HTTP_CODE_OK) {
    Serial.println("Datos enviados al servidor con éxito.");
  } else {
    Serial.println("Error al enviar datos al servidor.");
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
    // Ha pasado un minuto o se debe enviar de forma inmediata
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

  // Maneja solicitudes entrantes en el servidor web
  WiFiClient client = server.available();
  if (client) {
    if (client.available()) {
      String request = client.readStringUntil('\r');
      if (request.indexOf("/solicitar-datos") != -1) {
        // Procesa la solicitud de datos en tiempo real aquí
        if (sensorsEnabled) {
          // Activa los sensores, captura datos y responde con los datos
          // Asegúrate de responder con el formato adecuado (por ejemplo, JSON).
          String response = "{\"data1\": " + String(123) + ", \"data2\": " + String(456) + "}";
          client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: application/json");
          client.println("Connection: close");
          client.println();
          client.println(response);
        } else {
          // Los sensores están deshabilitados, responde con un mensaje adecuado.
          client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: text/plain");
          client.println("Connection: close");
          client.println();
          client.println("Sensores deshabilitados.");
        }
        delay(1);
        client.stop();
      }
    }
  }
}