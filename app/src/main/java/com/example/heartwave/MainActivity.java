package com.example.heartwave;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
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

        if (ba.isEnabled()) {
            mBtn.setImageResource(R.drawable.blue);
            //pairingDev();
        }
        else {
            mBtn.setImageResource(R.drawable.noblue);
        }

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