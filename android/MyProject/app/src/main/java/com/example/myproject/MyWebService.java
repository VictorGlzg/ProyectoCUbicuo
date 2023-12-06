package com.example.myproject;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.os.Handler;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyWebService extends Service {
    private static final String TAG = "MyWebService";
    private static final String URL_SERVIDOR = "https://jsonplaceholder.typicode.com/todos/1";
    private static final int INTERVALO_CONEXION = 30000; // Intervalo en milisegundos (1 minuto)
    private boolean conectado = false;
    private ConnectionTask connectionTask;
    private Handler handler;

    private static final String TELEGRAM_BOT_TOKEN = "6953803560:AAF-ejNSd4tqqVo4LjSleTYvrWqkN__8e2U";
    private static final String TELEGRAM_CHAT_ID = "6953803560";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Iniciar la conexión aquí
        conectarConServidor();

        // Iniciar el temporizador para la conexión automática cada 1 minuto
        handler = new Handler();
        handler.postDelayed(tareaConexionAutomatica, INTERVALO_CONEXION);

        // Si el servicio se cierra por alguna razón, reiniciar
        return START_STICKY;
    }

    private void conectarConServidor() {
        if (!conectado) {
            // Iniciar la tarea en segundo plano para la conexión
            connectionTask = new ConnectionTask();
            connectionTask.execute(URL_SERVIDOR);
        }
    }

    private Runnable tareaConexionAutomatica = new Runnable() {
        @Override
        public void run() {
            // Realizar la conexión automáticamente cada 1 minuto
            conectarConServidor();

            // Programar la próxima ejecución del temporizador
            handler.postDelayed(this, INTERVALO_CONEXION);
        }
    };

    private void detenerTemporizador() {
        if (handler != null) {
            handler.removeCallbacks(tareaConexionAutomatica);
        }
    }

    private void desconectarDelServidor() {
        if (conectado) {
            // Detener la tarea en segundo plano
            if (connectionTask != null) {
                connectionTask.cancel(true);
            }
            conectado = false;
        }
    }

    private class ConnectionTask extends AsyncTask<String, Void, String> {
        private static final int CONNECTION_TIMEOUT = 15000; // 5 segundos
        private static final int READ_TIMEOUT = 15000; // 10 segundos
        @Override
        protected String doInBackground(String... params) {
            String resultado = null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Configuraciones de la conexión (puedes personalizar según tus necesidades)
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(CONNECTION_TIMEOUT); // Tiempo de espera para la conexión
                urlConnection.setReadTimeout(READ_TIMEOUT);
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    resultado = stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error en la conexión: " + e.getMessage());
            }
            return resultado;
        }

        private class TelegramTask extends AsyncTask<String, Void, Void> {

            @Override
            protected Void doInBackground(String... params) {
                enviarDatosATelegram(params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // Puedes realizar alguna acción después de completar la operación en segundo plano
            }
        }

        private void enviarDatosATelegram(String data) {
            try {
                // Construir la URL de la API de Telegram para enviar un mensaje
                String telegramApiUrl = "https://api.telegram.org/bot6953803560:AAF-ejNSd4tqqVo4LjSleTYvrWqkN__8e2U/sendMessage";
                URL url = new URL(telegramApiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                // Configuraciones de la conexión para enviar un mensaje a un chat específico
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Crear los parámetros del mensaje
                String messageParams = "chat_id=6953803560&text=" + data;

                // Escribir los parámetros en el cuerpo de la solicitud
                urlConnection.getOutputStream().write(messageParams.getBytes());

                // Verificar si la conexión fue exitosa (código de respuesta HTTP 200)
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Obtener la respuesta del servidor de Telegram (puedes procesarla si es necesario)
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Imprimir la respuesta del servidor de Telegram
                    Log.d(TAG, "Respuesta del servidor de Telegram: " + response.toString());
                } else {
                    // Imprimir un mensaje de error si la conexión no fue exitosa
                    if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        Log.e(TAG, "Error al enviar datos a Telegram. Permiso denegado (HTTP 403)");
                    } else {
                        Log.e(TAG, "Error al enviar datos a Telegram. Código de respuesta: " + responseCode);
                    }
                }


                // Cerrar la conexión
                urlConnection.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Error al enviar datos a Telegram: " + e.getMessage());
            }
        }


        @Override
        protected void onPostExecute(String result) {
            // Procesar el resultado de la conexión aquí
            if (result != null) {
                if (result != null) {
                    Log.d(TAG, "Respuesta del servidor: " + result);

                    // Aquí puedes llamar a un método para enviar los datos a la API de Telegram
                    TelegramTask telegramTask = new TelegramTask();
                    telegramTask.execute(result);

                    // Envía el resultado a MainActivity
                    enviarBroadcast(result);
                } else {
                    // Si result es nulo, asigna el texto "procesando" al EditText
                    enviarBroadcast("procesando");
                }

                // Aquí puedes llamar a un método para enviar los datos a la API de Telegram
                //TelegramTask telegramTask = new TelegramTask();
                //telegramTask.execute(result);

                // Envía el resultado a MainActivity
                enviarBroadcast(result);
            }
        }

        private void enviarBroadcast(String result) {
            Intent intent = new Intent("ACTUALIZAR_EDITTEXT_ACTION");
            intent.putExtra("RESULTADO", result);
            sendBroadcast(intent);
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            // La tarea ha sido cancelada (por ejemplo, cuando se detiene el servicio)
            Log.d(TAG, "Tarea de conexión cancelada.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // No necesitas un enlace para este ejemplo
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Lógica para cerrar la conexión cuando el servicio se destruye
        detenerTemporizador();
        desconectarDelServidor();
    }

}
