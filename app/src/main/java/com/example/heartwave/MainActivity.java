package com.example.heartwave;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtn = findViewById(R.id.actBT);
        mPairedList = findViewById(R.id.pairedList);

        ba = BluetoothAdapter.getDefaultAdapter();

        if (ba.isEnabled()) {
            mBtn.setImageResource(R.drawable.blue);
            Set<BluetoothDevice> devices = ba.getBondedDevices();
            for (BluetoothDevice device : devices) {
                list.add(device.getName()); //+ "," + device);
            }
            mAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, list);
            mPairedList.setAdapter(mAdapter);
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
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // Bluetooth is on
                    mBtn.setImageResource(R.drawable.blue);
                    showToast("Bluetooth is on");
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