package com.example.heartwave;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;

    private BluetoothAdapter ba;
    FloatingActionButton mBtn;
    ListView mPairedList;
    ArrayAdapter mAdapter;
    List list = new ArrayList();
    Dialog popup;
    Button mOK, mPairedBtn;
    ImageView mIcon;
    TextView mMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtn = findViewById(R.id.actBT);
        mPairedBtn = findViewById(R.id.pairBtn);

        popup = new Dialog(this);
        msgPrompt();
        ba = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ba.isEnabled()) {
                    showToast("Turning on bluetooth...");
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);
                }
                else {
                    showToast("Bluetooth is already on");
                }
            }
        });

        mPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ba.isEnabled()) {
                    pairingDev();
                }
                else {
                    showToast("Bluetooth has to be enabled");
                }
            }
        });

    }
    public void msgPrompt(){
        popup.setContentView(R.layout.popup_msg);
        mMsg = popup.findViewById(R.id.msg);
        mIcon = popup.findViewById(R.id.icon);
        mOK = popup.findViewById(R.id.okBtn);
        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
        popup.show();
    }
    public void pairingDev(){
        popup.setContentView(R.layout.pair_device);
        mPairedList = popup.findViewById(R.id.pairedList);
        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Set<BluetoothDevice> devices = ba.getBondedDevices();

        for (BluetoothDevice device : devices) {
            if(list.contains(device.getName()))
                list.clear();
            list.add(device.getName()); //+ "," + device);
        }
        mAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, list);
        mPairedList.setAdapter(mAdapter);

//      CHANGE THIS TO IF A DEVICE IS PICKED THEN ALLOW ACCESS TO WAVEFORMS
//        mPairedList.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popup.dismiss();
//            }
//        });
        popup.show();
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        mBtn.setImageResource(R.drawable.noblue);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        mBtn.setImageResource(R.drawable.blue);
                        break;
                }
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // Bluetooth is on
                    mBtn.setImageResource(R.drawable.blue);
                    showToast("Bluetooth is on");
                    //pairingDev();
                } else {
                    // User denied to turn on bluetooth
                    mBtn.setImageResource(R.drawable.noblue);
                    showToast("Couldn't turn on bluetooth");
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
//package com.example.heartwave;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.app.Dialog;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothServerSocket;
//import android.bluetooth.BluetoothSocket;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//import android.os.ParcelUuid;
//import android.util.Log;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//
//public class MainActivity extends AppCompatActivity {
//    private static final int REQUEST_ENABLE_BT = 0;
//    private static final int REQUEST_DISCOVER_BT = 1;
//    private static final List<UUID> uuids_dev = new ArrayList<UUID>();
//    private static final List<String> a_dev = new ArrayList<String>();
//    public static int pos;
//    //private static final String TAG = "MainActivity";
//    private BluetoothAdapter ba;
//    private BluetoothDevice mDevice;
//    private SuccessConnect mConnected;
//    private ConnectThread connectThread;
//    private AcceptThread accThread;
//    FloatingActionButton mBtn;
//    ListView mPairedList;
//    ArrayAdapter mAdapter;
//    List list = new ArrayList();
//    Dialog popup;
//    Button mOK, mPairedBtn;
//    ImageView mIcon;
//    TextView mMsg;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        mBtn = findViewById(R.id.actBT);
//        mPairedBtn = findViewById(R.id.pairBtn);
//
//        popup = new Dialog(this);
//        msgPrompt();
//        ba = BluetoothAdapter.getDefaultAdapter();
//
//        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(receiver, filter);
//
//        mBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!ba.isEnabled()) {
//                    showToast("Turning on bluetooth...");
//                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    startActivityForResult(turnOn, 0);
//                }
//                else {
//                    showToast("Bluetooth is already on");
//                }
//            }
//        });
//
//        mPairedBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ba.isEnabled()) {
//                    pairingDev();
//                }
//                else {
//                    showToast("Bluetooth has to be enabled");
//                }
//            }
//        });
//
//    }
//    public void msgPrompt(){
//        popup.setContentView(R.layout.popup_msg);
//        mMsg = popup.findViewById(R.id.msg);
//        mIcon = popup.findViewById(R.id.icon);
//        mOK = popup.findViewById(R.id.okBtn);
//        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//        mOK.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popup.dismiss();
//            }
//        });
//        popup.show();
//    }
//    public void pairingDev(){
//        popup.setContentView(R.layout.pair_device);
//        mPairedList = popup.findViewById(R.id.pairedList);
//        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        final Set<BluetoothDevice> devices = ba.getBondedDevices();
//
//        for (BluetoothDevice dev : devices) {
//            ParcelUuid[] parcel_uuid = dev.getUuids();
//            list.add(dev.getName());
//            a_dev.add(dev.getAddress());
//            uuids_dev.add(UUID.fromString(parcel_uuid[0].toString()));
//        }
//        mAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, list);
//        mPairedList.setAdapter(mAdapter);
//      //CHANGE THIS TO IF A DEVICE IS PICKED THEN ALLOW ACCESS TO WAVEFORMS
//        mPairedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //pair item
//                pos = position;
//                if (ba.isEnabled()) {
//                    if (connectThread != null) {
//                        connectThread.cancel();
//                        connectThread = null;
//                    }
//                    if (accThread == null) {
//                        accThread = new AcceptThread();
//                        accThread.start();
//                    }
//                    connectThread = new ConnectThread(ba.getRemoteDevice("00:B4:F5:29:44:FC"));//a_dev.get(pos)));
//                    connectThread.start();
//                }
//                popup.dismiss();
//            }
//        });
//        popup.show();
//    }
//
//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
//                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
//                switch (state){
//                    case BluetoothAdapter.STATE_OFF:
//                        mBtn.setImageResource(R.drawable.noblue);
//                        break;
//                    case BluetoothAdapter.STATE_ON:
//                        mBtn.setImageResource(R.drawable.blue);
//                        break;
//                }
//            }
//        }
//    };
//
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        switch (requestCode) {
//            case REQUEST_ENABLE_BT:
//                if (resultCode == RESULT_OK) {
//                    // Bluetooth is on
//                    mBtn.setImageResource(R.drawable.blue);
//                    showToast("Bluetooth is on");
//                    //pairingDev();
//                } else {
//                    // User denied to turn on bluetooth
//                    mBtn.setImageResource(R.drawable.noblue);
//                    showToast("Couldn't turn on bluetooth");
//                }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private void showToast(String msg) {
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//    }
//    private class AcceptThread extends Thread {
//        private final BluetoothServerSocket mmServerSocket;
//        public AcceptThread() {
//            // Use a temporary object that is later assigned to mmServerSocket
//            // because mmServerSocket is final.
//            BluetoothServerSocket tmp = null;
//            // MY_UUID is the app's UUID string, also used by the client code.
//            try {
//                tmp = ba.listenUsingRfcommWithServiceRecord(getApplicationContext().getPackageName(),UUID.fromString("00001108-0000-1000-8000-00805f9b34fb")); //uuids_dev.get(pos));
//                //black headphones 00001101-0000-1000-8000-00805f9b34fb"));
//                //pink headphones uuid : 00001108-0000-1000-8000-00805f9b34fb"));
//            } catch (IOException e) {
//                //Log.e(TAG, "Error", e);
//            }
//            mmServerSocket = tmp;
//        }
//        public void run() {
//            BluetoothSocket socket = null;
//            // Keep listening until exception occurs or a socket is returned.
//            try {
//                socket = mmServerSocket.accept();
//            } catch (IOException e) {
//                //Log.e(TAG, "Error", e);
//            }
//            if (socket != null) {
//                Connected(socket, mDevice);
//            }
//        }
//        // Closes the connect socket and causes the thread to finish.
//        public void cancel() {
//            try {
//                mmServerSocket.close();
//            } catch (IOException e) {
//               // Log.e(TAG, "Error: Could not close the connect socket", e);
//            }
//        }
//    }
//    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    private class ConnectThread extends Thread {
//        private final BluetoothSocket mmSocket;
//
//        public ConnectThread(BluetoothDevice device) {
//            // Use a temporary object that is later assigned to mmSocket
//            // because mmSocket is final.
//            mDevice = device;
//            BluetoothSocket tmp = null;
//
//            // Get a BluetoothSocket to connect with the given BluetoothDevice.
//            // MY_UUID is the app's UUID string, also used in the server code.
//            try {
//
//                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001108-0000-1000-8000-00805f9b34fb"));//(uuids_dev.get(pos));00001101-0000-1000-8000-00805f9b34fb"));
//            } catch (IOException e) {
//                //Log.e(TAG, "Error", e);
//            }
//            mmSocket = tmp;
//        }
//        public void run() {
//            // Cancel discovery because it otherwise slows down the connection.
//            //ba.cancelDiscovery(); //its buggy so recommended to take it off
//            try {
//                mmSocket.connect();
//            } catch (IOException connectException) {
//                try {
//                    mmSocket.close();
//                } catch (IOException e) {
//                    //Log.e(TAG, "Error:Could not close the connect socket", e);
//                }
//                return;
//            }
//            //data that is being sent to phone
//
//            Connected(mmSocket, mDevice);
//            //showToast("Connected");
//        }
//        // Closes the client socket and causes the thread to finish.
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//                //Log.e(TAG, "Error:Could not close socket", e);
//            }
//        }
//    }
//    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    private class SuccessConnect extends Thread {
//        private final BluetoothSocket mmSocket;
//        private final InputStream mmInput;
//        //private final OutputStream mmOutput;
//
//        public SuccessConnect(BluetoothSocket socket) {
//            mmSocket = socket;
//            InputStream tmpIn = null;
//            //OutputStream tmpOut = null;
//            try {
//                tmpIn = mmSocket.getInputStream();
//                //tmpOut = mmSocket.getOutputStream();
//            } catch (IOException e) {
//                //Log.e(TAG, "Error", e);
//            }
//            mmInput = tmpIn;
//            // mmOutput = tmpOut;
//        }
//        public void run() {
//            //Should get data here
//            showToast("Success");
//        }
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//                //Log.e(TAG, "Error: Could not close socket", e);
//            }
//        }
//    }
//        private void Connected(BluetoothSocket mmSocket, BluetoothDevice mDevice) {
//            mConnected = new SuccessConnect(mmSocket);
//            mConnected.start();
//        }
//}