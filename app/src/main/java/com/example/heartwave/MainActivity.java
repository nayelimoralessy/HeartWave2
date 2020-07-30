package com.example.heartwave;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 4000;
    private BluetoothAdapter ba;
    private Set<BluetoothAdapter> pairedDevice;
    private Set<BluetoothDevice>pairedDevices;
    ImageView mBlue, mNoblue;
    ImageButton mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = findViewById(R.id.buttonbt);
        mBlue = findViewById(R.id.bt);
        mNoblue = findViewById(R.id.nobt);
        ba = BluetoothAdapter.getDefaultAdapter();
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, 0);
        pairedDevices = ba.getBondedDevices();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { //since requires newer versions
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent homeIntent = new Intent(MainActivity.this, Home.class);
                    startActivity(homeIntent);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }
        if (ba.isEnabled()) {
            //mBlue.setImageResource();
            mBtn.setImageResource(R.drawable.blue);
        }
        else {
            mBtn.setImageResource(R.drawable.noblue);
            //mNoblue.setImageResource(R.drawable.noblue);
        }

        // On Btn click
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ba.isEnabled()) {
                    showToast("Turning on bluetooth...");
                } else {
                    showToast("Bluetooth is already on");
                }
            }
        });
    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}