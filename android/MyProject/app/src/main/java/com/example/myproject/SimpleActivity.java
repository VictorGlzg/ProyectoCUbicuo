package com.example.myproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import com.example.myproject.bluetooth.BluetoothAdmin;
import com.example.myproject.bluetooth.BluetoothDeviceArrayAdapter;
import com.example.myproject.bluetooth.BluetoothService;

import org.w3c.dom.Text;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class SimpleActivity extends Activity {
    private Button btnBluetooth;
    private BluetoothAdapter bAdapter;
    BluetoothService obj;
    BluetoothDevice bluetoothDevice;
    private static final int REQUEST_ENABLE_BT = 1;
    private Button btnBuscarDispositivo;
    private ArrayList<BluetoothDevice> arrayDevices;
    private ListView lvDispositivos;
    private TextView tvResponse;
    private EditText etMessage;
    private Button btnEncender,btnApagar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        btnBluetooth  = (Button)findViewById(R.id.btnBluetooth);
        btnBuscarDispositivo = (Button)findViewById(R.id.btnBuscarDispositivo);
        lvDispositivos = (ListView)findViewById(R.id.lvDispositivos);
        btnEncender = (Button) findViewById(R.id.btnEncender);
        btnApagar = (Button) findViewById(R.id.btnApagar);

        // Obtenemos el adaptador Bluetooth. Si es NULL, significara que el
        // dispositivo no posee Bluetooth, por lo que deshabilitamos el boton
        // encargado de activar/desactivar esta caracteristica.
        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bAdapter == null)
        {
            btnBluetooth.setEnabled(false);
            Toast.makeText(this,"Device does not support Bluettoth", Toast.LENGTH_SHORT);
            return;
        }
        // Comprobamos si el Bluetooth esta activo y cambiamos el texto del
        // boton dependiendo del estado.
        if(bAdapter.isEnabled())
            btnBluetooth.setText("Desactivar");
        else
            btnBluetooth.setText("Activar");

        String[] perms = {"android.permission.ACCESS_FINE_LOCATION","android.permission.INTERNET","android.permission.BLUETOOTH_ADVERTISE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH_SCAN","android.permission.BLUETOOTH_CONNECT"};

        BluetoothAdmin.getPermissions(perms, this);

        BluetoothAdmin.registrarEventosBluetooth(this, bReceiver);

        obj = new BluetoothService(bAdapter, handler);

        lvDispositivos.setOnItemClickListener(new ListView.OnItemClickListener(){
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothDevice = arrayDevices.get(position);

                Toast.makeText(getApplicationContext(), "" + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                conectarDispositivoSeleccionado(position);
            }
        });

        btnEncender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obj.escribir("1".getBytes());
                Log.e("Encender sensor","Boton de encendido presionado");
            }
        });

        btnApagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obj.escribir("0".getBytes());
                Log.e("Apagar sensor","Boton de encendido apagado presionado");
            }
        });

    }

    public void conectarDispositivoSeleccionado(int position) {
        if (arrayDevices != null && position >= 0 && position < arrayDevices.size()) {
            BluetoothDevice device = arrayDevices.get(position);
            obj.iniciarCliente(device); // Inicia la conexión con el dispositivo seleccionado
        }
    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        Log.e("OR-PERMISSIONS", "Respuesta");

        switch (permsRequestCode) {
            case 200:
                if (grantResults.length >= 2) {
                    boolean fineLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean coarseLocationAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    Log.e("OR-PERMISSIONS-200", "Respuesta a solicitud FINE: [" + fineLocationAccepted + "] + COARSE: [" + coarseLocationAccepted + "] + ");

                    if (fineLocationAccepted && coarseLocationAccepted) {
                        // Both permissions are granted, you can proceed with your logic here
                    } else {
                        // Handle the case when one or both permissions are not granted
                    }
                } else {
                    // Handle the case when the grantResults array is not of the expected length
                    Log.e("OR-PERMISSIONS-200", "Unexpected length of grantResults array: " + grantResults.length);
                }
                break;
        }
    }

    private void registrarEventosBluetooth() {
        IntentFilter filter;

        // Registramos el BroadcastReceiver que instanciamos previamente para
        // detectar los distintos eventos que queremos recibir.
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(bReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(bReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(bReceiver, filter);
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        btnBluetooth.setText("Activar");
                        break;

                    case BluetoothAdapter.STATE_ON:
                        btnBluetooth.setText("Desactivar");
                        break;

                    default:
                        break;
                }
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (arrayDevices == null)
                    arrayDevices = new ArrayList<>();

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayDevices.add(device);

                @SuppressLint("MissingPermission") String deviceDescription = device.getName() + " [" + device.getAddress() + "]";
                Toast.makeText(getBaseContext(), "Dispositivo Detectado: " + deviceDescription, Toast.LENGTH_SHORT).show();

            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ArrayAdapter arrayAdapter = new BluetoothDeviceArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, arrayDevices);
                lvDispositivos.setAdapter(arrayAdapter);
                Toast.makeText(getBaseContext(), "Fin de la búsqueda", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void onClick(View v) {
        if (v.getId() == R.id.btnBluetooth) {
            if (bAdapter.isEnabled()) {
                bAdapter.disable();
                btnBuscarDispositivo.setEnabled(false);
            } else {
                if (ActivityCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    btnBuscarDispositivo.setEnabled(true);
                }
            }
        } else if (v.getId() == R.id.btnBuscarDispositivo) {
            if (arrayDevices != null)
                arrayDevices.clear();

            if (bAdapter.isDiscovering())
                bAdapter.cancelDiscovery();

            if (bAdapter.startDiscovery()) {
                Toast.makeText(this, "Iniciando búsqueda de dispositivos Bluetooth", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al iniciar búsqueda de dispositivos Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
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

    public void conectarBluetooth(int position) {
        if (arrayDevices != null && position >= 0 && position < arrayDevices.size()) {
            BluetoothDevice device = arrayDevices.get(position);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(bReceiver);
    }

    // Handler que obtendrá informacion de BluetoothService
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



    public void onServer(View v) {
        obj.iniciarServidor();
    }

    public void onClient(View v) {
        obj.iniciarCliente(bluetoothDevice);
    }
}
