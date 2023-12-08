package com.example.myproject;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyWebService extends Service {
    private static final String TAG = "MyWebService";
    private static final String URL_SERVIDOR = "http://74f6-187-254-100-32.ngrok-free.app/android";
    private static final int INTERVALO_CONEXION = 60000; // Intervalo en milisegundos (1 minuto)
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


        @Override
        protected void onPostExecute(String result) {
            // Procesar el resultado de la conexión aquí
            if (result != null) {
                if (result != null) {
                    Log.d("Servidor", "Respuesta del servidor: " + result);
                    // Convierte el JSON a una cadena (String)
                    //String jsonString = convertirJSONaString(result);
                    //result = jsonString;
                    // Elimina las llaves y comillas del JSON
                    //result = result.replace("{", "").replace("}", "").replace("\"", "").replace(",", " , ").replace(",", ",\n");
                    //Log.d(TAG, "JSON convertido a String: " + result);

                    // Envía el resultado a MainActivity
                    enviarBroadcast(result);
                } else {
                    // Si result es nulo, asigna el texto "procesando" al EditText
                    enviarBroadcast("procesando");
                }
                // Envía el resultado a MainActivity
                enviarBroadcast(result);
            }
        }

        private String convertirJSONaString(String json) {
            try {
                // Utiliza Gson para convertir el JSON a una cadena
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                return jsonObject.toString();
            } catch (Exception e) {
                // Maneja cualquier excepción que pueda ocurrir durante la conversión
                e.printStackTrace();
                return null;
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
