package com.example.heartwave;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    private BluetoothAdapter ba;
    FloatingActionButton mactBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mBtn = findViewById(R.id.buttonbt);
        mactBT = findViewById(R.id.actBT);
        //mBlue = findViewById(R.id.bt);
        //mNoblue = findViewById(R.id.nobt);
        ba = BluetoothAdapter.getDefaultAdapter();

       // pairedDevices = ba.getBondedDevices();

        if (ba.isEnabled()) {
            //mBlue.setImageResource();
            mactBT.setImageResource(R.drawable.blue);
        } else {
            mactBT.setImageResource(R.drawable.noblue);
            //mNoblue.setImageResource(R.drawable.noblue);
        }

        // On Btn click
        mactBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ba.isEnabled()) {
                    showToast("Turning on bluetooth...");
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);
                } else {
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
                    mactBT.setImageResource(R.drawable.blue);
                    showToast("Bluetooth is on");
                } else {
                    // User denied to turn on bluetooth
                    showToast("Couldn't turn on bluetooth");
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}