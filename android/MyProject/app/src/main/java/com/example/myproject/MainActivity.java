package com.example.myproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
    EditText etMessage;
    String result = "procesando"; // Valor predeterminado
    Handler handler;
    TextView tvResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etMessage = (EditText) findViewById(R.id.etMessage);
        tvResponse = (TextView) findViewById(R.id.tvResponse);

        etMessage.setFocusable(false);
        etMessage.setClickable(false);
        etMessage.setCursorVisible(false);

        IntentFilter intentFilter2 = new IntentFilter("com.example.myproject.RUN_RUNNABLE_ACTION");
        BroadcastReceiver runRunnableReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Ejecutar el método actualizarEditTextRunnable cuando se recibe la señal
                handler.post(actualizarEditTextRunnable);
            }
        };
        registerReceiver(runRunnableReceiver, intentFilter2);

        IntentFilter intentFilter = new IntentFilter("ACTUALIZAR_EDITTEXT_ACTION");
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String resultado = intent.getStringExtra("RESULTADO");
                // Actualizar el EditText con el resultado
                etMessage.setText(resultado);
                // Actualizar la variable result
                result = resultado;
            }
        };

        registerReceiver(broadcastReceiver, intentFilter);

        // Iniciar el temporizador para actualizar cada 30 segundos
        handler = new Handler();
        handler.postDelayed(actualizarEditTextRunnable, 60000);

    }

    public void onService(View v) {
        Intent demon = new Intent(this, MyService.class);
        startService(demon);
        Intent intent = new Intent(this, MyWebService.class);
        startService(intent);

        //finish();
    }


    private Runnable actualizarEditTextRunnable = new Runnable() {
        @Override
        public void run() {
            // Actualizar el EditText con la información de la variable result
            etMessage.setText(result);
            // Ejecutar la petición GET-SEND cada vez que se actualiza el EditText
            MiPeticionREST peticion = new MiPeticionREST(tvResponse);
            peticion.execute("GET-SEND",etMessage.getText().toString());
            Log.d("EnvioTelegram", "La información se envió a Telegram");
            // Programar la próxima ejecución después de 30 segundos
            handler.postDelayed(this, 30000);
        }
    };



    protected void onDestroy() {
        super.onDestroy();
        // Detener el temporizador cuando se destruye la actividad
        handler.removeCallbacks(actualizarEditTextRunnable);

    }
}
