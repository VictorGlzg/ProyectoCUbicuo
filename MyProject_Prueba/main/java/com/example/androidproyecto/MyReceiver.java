package com.example.androidproyecto;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.androidproyecto.bluetooth.BluetoothService;

public class MyReceiver extends BroadcastReceiver {
    //NOTA: Esta clase se debe registrar en el Manifest
    BluetoothService obj;

    public MyReceiver(BluetoothService obj) {
        this.obj = obj;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if( intent.getAction().equals("com.example.myproject.BROADCAST_ACTION")) {
            Log.e("MY-SERVICE", "ON RECEIVE CALLED");

            BluetoothDevice device = intent.getParcelableExtra(ConnectWith.Constantes.TAG_STATUS);

            obj.iniciarCliente(device);
        }


    }
}
