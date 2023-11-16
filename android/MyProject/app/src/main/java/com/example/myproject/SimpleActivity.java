package com.example.myproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myproject.bluetooth.BluetoothAdmin;
import com.example.myproject.bluetooth.BluetoothDeviceArrayAdapter;
import com.example.myproject.bluetooth.BluetoothService;

import java.util.ArrayList;

public class SimpleActivity extends Activity {
    private Button btnBluetooth;
    private BluetoothAdapter bAdapter;

    private static final int REQUEST_ENABLE_BT = 1;

    private Button btnBuscarDispositivo;
    private ArrayList<BluetoothDevice> arrayDevices;

    private ListView lvDispositivos;


    BluetoothService obj;
    BluetoothDevice bluetoothDevice;

    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        btnBluetooth = (Button) findViewById(R.id.btnBluetooth);
        btnBuscarDispositivo = (Button) findViewById(R.id.btnBuscarDispositivo);
        lvDispositivos = (ListView) findViewById(R.id.lvDispositivos);

        String[] perms = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_SCAN"};

        BluetoothAdmin.getPermissions(perms, this);

        lvDispositivos.setOnItemClickListener(new ListView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothDevice = arrayDevices.get(position);

                Toast.makeText(getApplicationContext(), "" + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();

                Intent localIntent = new Intent("com.example.myproject.BROADCAST_ACTION"); // Puts the status into the  Broadcasts

                localIntent.putExtra(ConnectWith.Constantes.TAG_STATUS, bluetoothDevice); // the Intent to receivers in this app
                //LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(localIntent);

                sendBroadcast(localIntent);
                Intent act = new Intent(getBaseContext(), ConnectWith.class);
                startActivity(act);
                //finish();

            }
        });

        // Acciones a realizar al finalizar el proceso de descubrimiento
        // Instanciamos un nuevo adapter para el ListView mediante la clase que acabamos de crear
        arrayDevices =arrayDevices = new ArrayList<BluetoothDevice>();
        arrayAdapter = new BluetoothDeviceArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, arrayDevices);

        lvDispositivos.setAdapter(arrayAdapter);

        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bAdapter == null)
        {
            btnBluetooth.setEnabled(false);
            Toast.makeText(this,"Device does not support Bluettoth", Toast.LENGTH_SHORT);
            return;
        }

        if(bAdapter.isEnabled()) {
            btnBluetooth.setText("Desactivar");
            btnBuscarDispositivo.setEnabled(true);
        }
        else {
            btnBluetooth.setText("Activar");
            btnBuscarDispositivo.setEnabled(false);
        }

        BluetoothAdmin.registrarEventosBluetooth(this, bReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(bReceiver);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch(requestCode)
        {
            case REQUEST_ENABLE_BT:
            {
                if(resultCode == RESULT_OK)
                {
                    // Acciones adicionales a realizar si el usuario activa el Bluetooth
                    Log.e("ON-ACTIVITY-RESULT", "RESULT_OK");
                    btnBuscarDispositivo.setEnabled(true);
                }
                else
                {
                    // Acciones adicionales a realizar si el usuario no activa el Bluetooth
                    Log.e("ON-ACTIVITY-RESULT", "RESULT_NO_OK");
                    btnBuscarDispositivo.setEnabled(false);
                }
                break;
            }

            default:
                break;
        }
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver()
    {

        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            Log.e("ON-RECEIVE", "CALLED");

            // Filtramos por la accion. Nos interesa detectar BluetoothAdapter.ACTION_STATE_CHANGED
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                Log.e("ON-RECIEVE", "ACTION_STATE_CHANGED");

                // Solicitamos la informacion extra del intent etiquetada como BluetoothAdapter.EXTRA_STATE
                // El segundo parametro indicara el valor por defecto que se obtendra si el dato extra no existe
                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                switch (estado)
                {
                    // Apagado
                    case BluetoothAdapter.STATE_OFF:
                    {
                        ((Button)findViewById(R.id.btnBluetooth)).setText("Activar");
                        break;
                    }

                    // Encendido
                    case BluetoothAdapter.STATE_ON:
                    {
                        ((Button)findViewById(R.id.btnBluetooth)).setText("Desactivar");
                        break;
                    }
                    default:
                        break;
                }
            }
            // Cada vez que se descubra un nuevo dispositivo por Bluetooth, se ejecutara
            // este fragmento de codigo
            else if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                if (arrayDevices == null)
                    arrayDevices = new ArrayList<>();

                Log.e("ON-RECIEVE", "ACTION_FOUND");
                // Acciones a realizar al descubrir un nuevo dispositivo

                // Extraemos el dispositivo del intent mediante la clave BluetoothDevice.EXTRA_DEVICE
                BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayDevices.add(dispositivo);

                // Le asignamos un nombre del estilo NombreDispositivo [00:11:22:33:44]
                String descripcionDispositivo = dispositivo.getName() + " [" + dispositivo.getAddress() + "]";

                // Mostramos que hemos encontrado el dispositivo por el Toast
                Toast.makeText(getBaseContext(), "" + descripcionDispositivo, Toast.LENGTH_SHORT).show();

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ArrayAdapter arrayAdapter = new BluetoothDeviceArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, arrayDevices);
                lvDispositivos.setAdapter(arrayAdapter);
                Toast.makeText(getBaseContext(), "Fin de la búsqueda", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void onEnd(View v) {
        Intent intent = new Intent("com.example.myproject.BROADCAST_ACTION"); // Puts the status into the  Broadcasts
        intent.putExtra(ConnectWith.Constantes.TAG_STATUS, bluetoothDevice); // the Intent to receivers in this app
        sendBroadcast(intent);  //ya no se utiliza LocalBroadcastManager, ahora se usa send broadcast

        finish();
    }

    @SuppressLint("MissingPermission")
    public void onBuscarDispositivo(View v) {
//        if(arrayDevices != null)
 //           arrayDevices.clear();
        Log.i("DISP BLUETOOTH","Buscar dispositivo");
        // Comprobamos si existe un descubrimiento en curso. En caso afirmativo, se cancela.
        if(bAdapter.isDiscovering())
            bAdapter.cancelDiscovery();

        // Iniciamos la busqueda de dispositivos y mostramos el mensaje de que el proceso ha comenzado
        if(bAdapter.startDiscovery()) {
            Toast.makeText(this, "Iniciando búsqueda de dispositivos bluetooth", Toast.LENGTH_SHORT).show();
            // Si el array no ha sido aun inicializado, lo instanciamos
            if(arrayDevices == null){
                arrayDevices = new ArrayList<BluetoothDevice>();
            }
            else
                arrayDevices.clear();
        }
        else
            Toast.makeText(this, "Error al iniciar búsqueda de dispositivos bluetooth", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    public void onBluetooth(View v) {
        if (bAdapter.isEnabled()) {
            bAdapter.disable();
            btnBluetooth.setText("Activar");
            btnBuscarDispositivo.setEnabled(false);
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }



}