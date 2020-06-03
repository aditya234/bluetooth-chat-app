package com.example.bluetoothchatapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button listenButton, listButton, sendButton;
    ListView deviceList;
    EditText writeMessage;
    TextView status, message;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btDevices;
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String APP_NAME = "BtChat";
    private static final UUID MY_UUID = UUID.fromString("20585adb-d260-445e-934b-032a2c8b2e14");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        declareVars();
        findViewsByIds();
        // enable bluetooth just after on create is called
        enableBluetooth();
        implementListeners();
    }

    private void implementListeners() {
        //When List devices is clicked
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                String[] strings = new String[devices.size()];
                int counter = 0;
                for (BluetoothDevice device : devices) {
                    strings[counter] = device.getName();
                    btDevices[counter] = device;
                    counter++;
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                deviceList.setAdapter(arrayAdapter);
            }
        });
        //When Listen is clicked
        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;

                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;

                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;

                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;

                case STATE_MESSAGE_RECEIVED:
                    //We will write it later
                    break;
            }
            return true;
        }
    });

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    private void declareVars() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void findViewsByIds() {
        listenButton = findViewById(R.id.listen);
        listButton = findViewById(R.id.listDevices);
        sendButton = findViewById(R.id.send);
        deviceList = findViewById(R.id.devicesList);
        writeMessage = findViewById(R.id.message);
        status = findViewById(R.id.status);
        message = findViewById(R.id.chatMessage);
    }

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;
            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    // write some code for send recieve message here
                    break;
                }
            }
        }
    }
}
