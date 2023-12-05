package com.example.myproject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    EditText etMessage;
    TextView tvResponse;

    String data = null;
    MyJSONParser parser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //etMessage = (EditText) findViewById(R.id.etMessage);
        //tvResponse = (TextView) findViewById(R.id.tvResponse);
    }

    public void onSend(View v){
        MiPeticionREST obj = new MiPeticionREST(tvResponse);

        obj.execute("GET-SEND", etMessage.getText().toString());

    }

    public void onUpdate(View v){
        MiPeticionREST obj = new MiPeticionREST(tvResponse);

        obj.execute("GET-UPDATES");

        //Por que aquí no puede ir
        //data = this.tvResponse.getText().toString();

        //this.parser = new MyJSONParser(data);

    }

    public void onPost(View v){
        MiPeticionREST obj = new MiPeticionREST(tvResponse);

        obj.execute("POST", "A", "B", "C");
    }

    public void onJSON(View v) {
        if( data == null ) {
            data = this.tvResponse.getText().toString();
            this.parser = new MyJSONParser(data);
        }

        String msg = "";
        MyData data = this.parser.getValue();
        for (String i : data.msg) {
            msg = msg + " " + i;

            //interpretar cada mensaje
            if( msg.contains("ENCENDER") ){
                //Solicitud de encendido

                //responder apropiadamente al usuario cada mensaje
                MiPeticionREST obj = new MiPeticionREST(tvResponse);
                obj.execute("GET-SEND", "Sensor Encendido");
            }

            if( msg.contains("APAGAR") ) {
                //Solicitud de apagado

                //responder apropiadamente al usuario cada mensaje
                MiPeticionREST obj = new MiPeticionREST(tvResponse);
                obj.execute("GET-SEND", "Sensor Apagado");
            }
        }

        //Interpretar y actuar


        //Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show();
    }

    public void onService(View v) {
        Intent demon = new Intent(this, MyService.class);
        startService(demon);
        Intent intent = new Intent(this, MyWebService.class);
        startService(intent);
        finish();
    }

    public void onServer(View v) {
        //new ConnectToServerTask().execute();
        //Intent intent = new Intent(this, MyWebService.class);
        //startService(intent);
    }
}
