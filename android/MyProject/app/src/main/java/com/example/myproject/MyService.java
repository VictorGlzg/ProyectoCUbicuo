package com.example.myproject;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.myproject.bluetooth.BluetoothService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



/* REFERENCES
 * url: Service vs IntentService https://www.geeksforgeeks.org/difference-between-service-and-intentservice-in-android/
 * URL: Using Service: https://www.geeksforgeeks.org/services-in-android-with-example/
 *
 * java.lang.IllegalAccessException : la causa es un constructor que no se puede invocar (el del IntentService) asegurate que sea declarado "public"
 */
public class MyService extends IntentService {
    MyReceiver myReceiver = null;
    MyJSONParser parser = null;
    int offset = 989527494;
    String res;

    public MyService() {
        super("MyService");
    }
    @Override
    protected void onHandleIntent(Intent workIntent) {
        MyData data = null;
        do {
            data = this.get_updates();
            Log.i("PRUEBA DO WHILE","EN CICLO");
        }while( data == null || this.process(data) );
    }

    private void send(String info) {
        try {
            //Tiene como objetivo hacer que el BOT envíe un mensaje al chat con ID definido
            URL url = new URL("https://api.telegram.org/bot6800975183:AAHalCaQ9weXmKX_i6i9_r6ZqReoQlygCoc/sendMessage?chat_id=-4055829170&text=" + info);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-length", "0");
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.connect();

            int status = conn.getResponseCode();

            if (status == 200) {
                res = "Message Send as  BOT";
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e("ENVIOREST", "[MalformedURLException]=>" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("ENVIOREST", "[IOException]=>" + e.getMessage());
            e.printStackTrace();
        }
    }

    private MyData get_updates() {
        MyData data = null;
        try {
            String my_url = "https://api.telegram.org/bot6800975183:AAHalCaQ9weXmKX_i6i9_r6ZqReoQlygCoc/getUpdates?offset=" + offset + "&timeout=5";
            URL url = new URL(my_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-length", "0");
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.connect();

            int status = conn.getResponseCode();

            if ( status == 200 ) {
                //InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8);
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(reader);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                res = sb.toString();

                //obtener mensajes y update_id
                this.parser = new MyJSONParser("[" + res +"]");

                String msg = "";
                data = this.parser.getValue();
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e("ENVIOREST", "[MalformedURLException]=>" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("ENVIOREST", "[IOException]=>" + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

    private boolean process(MyData data){
        int update_id = -1;
        String msg = "";
        for (int i = 0; i < data.msg.size(); ++i) {
            update_id = data.update_id.get(i);
            msg = data.msg.get(i);
            //msg = msg + " " + m;
            Log.i( "PRUEBA MENSAJE",msg);
            this.offset = update_id + 1;
            //interpretar cada mensaje
            if( msg.contains("on_off")){
                //Solicitud de encendido
                try {
                    Intent act = new Intent(getBaseContext(), SimpleActivity.class);
                    act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(act);
                }catch(Exception e){
                    Log.e("INIT_MY_ACTIVITY", "" + e.getMessage());
                }

                this.send("Sensor Encendido " + this.offset);
            }

            if (msg.contains("Checar")) {
                // Enviar una señal a MainActivity para ejecutar el método actualizarEditTextRunnable
                Intent broadcastIntent = new Intent("com.example.myproject.RUN_RUNNABLE_ACTION");
                sendBroadcast(broadcastIntent);
            }


            if( msg.contains("APAGAR") ) {
                //Solicitud de apagado
                this.send("Sensor Apagado " + + this.offset);
            }

            if( msg.contains("TERMINAR") ) {
                return false;
            }
        }

        return true;
    }

    public void onCreate(){
        super.onCreate();

        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothService obj = new BluetoothService(bAdapter, handler);
        myReceiver = new MyReceiver(obj);
        IntentFilter myFilter = new IntentFilter("com.example.myproject.BROADCAST_ACTION");

        registerReceiver(
                myReceiver,
                myFilter
        );


    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            byte[] buffer = null;
            String mensaje = null;

            // Atendemos al tipo de mensaje
            switch (msg.what) {
                // Mensaje de lectura: se mostrara en un TextView
                case BluetoothService.MSG_LEER: {
                    buffer = (byte[]) msg.obj;
                    mensaje = new String(buffer, 0, msg.arg1);
                    //tvMensaje.setText(mensaje);
                    break;
                }

                // Mensaje de escritura: se mostrara en el Toast
                case BluetoothService.MSG_ESCRIBIR: {
                    buffer = (byte[]) msg.obj;
                    mensaje = new String(buffer);
                    mensaje = "Enviando mensaje: " + mensaje;
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                    break;
                }

                default:
                    break;
            }
        }
    };
}
