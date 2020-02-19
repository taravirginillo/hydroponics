package com.example.cannaponics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class AutoActivity extends AppCompatActivity {
    Button connect, read;
    ImageButton home;
    TextView text, read_data;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice mDevice;
    BluetoothSocket mmSocket;
    InputStream mmInStream;
    OutputStream mmOutStream;
    boolean status = false;
    String deviceHardwareAddress,TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

        setUp();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    deviceHardwareAddress = device.getAddress(); // MAC address
                    text.setText(deviceName);
                }

                mDevice = mBluetoothAdapter.getRemoteDevice(deviceHardwareAddress);

                if(status==false) {
                    ConnectThread ct = new ConnectThread(mDevice);      //socket connection two devices
                    ct.start();

                    ConnectedThread cet = new ConnectedThread(mmSocket);  //Stream connection for two devices
                }

                else{
                    Toast.makeText(AutoActivity.this,"If you want to connect again close app and agin open it",Toast.LENGTH_SHORT).show();
                }


            }
        });

        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                byte[] buffer;
                buffer = new byte[256];
                if(status == true) {
                    try {
                        mmOutStream.write('r');
                        int length = mmInStream.read(buffer);
                        String text = new String(buffer, 0, length);
                        read_data.setText(text);
                    } catch (IOException e) {
                        Toast.makeText(AutoActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AutoActivity.this, "Connect to bluetooth device first.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AutoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });



    }

    private void setUp(){
        connect = (Button)findViewById(R.id.connect_button);
        text = (TextView)findViewById(R.id.bluetooth_device_text);
        read = (Button) findViewById(R.id.read_button);
        read_data = (TextView) findViewById(R.id.read_data);
        home = (ImageButton) findViewById(R.id.home_button);
        // on = (Button)findViewById(R.id.button2);
        // off = (Button)findViewById(R.id.button3);


    }

    private class ConnectThread extends Thread {

        private final BluetoothDevice mmDevice;
        private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "Socket's create() sucess");
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }



        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                status = true;
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(status==true){
                        Toast.makeText(AutoActivity.this,"Sucessfully connected",Toast.LENGTH_SHORT).show();
                    }

                    else{
                        Toast.makeText(AutoActivity.this,"connection failed",Toast.LENGTH_SHORT).show();

                    }

                }
            });

        }


    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d(TAG, "Input Output stream create sucessfully");

            } catch (IOException e) {

                Toast.makeText(AutoActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

    }

  /*  public void on_led(int a) {
        try {
            mmOutStream.write('1');
            Log.d(TAG, "on signal send sucessfully");
            Toast.makeText(ManualActivity.this, "on signal send sucessfully" ,Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
            Toast.makeText(ManualActivity.this, "Error occurred when sending data",Toast.LENGTH_SHORT).show();
            Toast.makeText(ManualActivity.this, e.toString(),Toast.LENGTH_SHORT).show();

        }
    }

    public void off_led(int b) {
        try {
            mmOutStream.write('0');
            Log.d(TAG, "off signal send sucessfully");
            Toast.makeText(ManualActivity.this, "off signal send sucessfully" ,Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
            Toast.makeText(ManualActivity.this, "Error occurred when sending data",Toast.LENGTH_SHORT).show();


        }
    }
*/
    /**
     *
     * @param a depending on what you want to turn on/off
     *          'g' : rgb on
     *          '0' : rgb off
     *          'w' : white on
     *          '1' : white off
     *          'f' : fan on
     *          '2' : fan off
     */
    public void send_token(char a) {

        if(status == true) {
            try {
                mmOutStream.write(a);
                Log.d(TAG, "signal sent sucessfully");
                Toast.makeText(AutoActivity.this, "signal sent sucessfully", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                Toast.makeText(AutoActivity.this, "Error occurred when sending data", Toast.LENGTH_SHORT).show();
                Toast.makeText(AutoActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

            }
        } else{
            Toast.makeText(AutoActivity.this, "Connect to bluetooth device first.", Toast.LENGTH_SHORT).show();
        }
    }
}



