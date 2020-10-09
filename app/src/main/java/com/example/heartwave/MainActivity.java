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
import android.graphics.Paint;
import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.*;
import java.lang.ref.*;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;

    private BluetoothAdapter ba;
    FloatingActionButton mBtn;
    ListView mPairedList;
    ArrayAdapter mAdapter;
    List list = new ArrayList();
    Dialog popup;
    Button mOK, mPairedBtn, mSig;
    ImageView mIcon;
    TextView mMsg;
    XYPlot plot;
    Redrawer redrawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtn = findViewById(R.id.actBT);
        mPairedBtn = findViewById(R.id.pairBtn);
        mSig = findViewById(R.id.realSig);

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

        mSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.signal);
                plot = findViewById(R.id.plot);
                ECGModel ecgSeries = new ECGModel(2000, 200);
                // add a new series' to the xyplot:
                MyFadeFormatter formatter =new MyFadeFormatter(2000);
                formatter.setLegendIconEnabled(false);
                plot.addSeries(ecgSeries, formatter);
                plot.setRangeBoundaries(0, 10, BoundaryMode.FIXED);
                plot.setDomainBoundaries(0, 2000, BoundaryMode.FIXED);

                // reduce the number of range labels
                plot.setLinesPerRangeLabel(3);
                // start generating ecg data in the background:
                ecgSeries.start(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)));

                // set a redraw rate of 30hz and start immediately:
                redrawer = new Redrawer(plot, 30, true);

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
    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private int trailSize;

        MyFadeFormatter(int trailSize) {
            this.trailSize = trailSize;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset;
            if(thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset =  latestIndex - thisIndex;
            }

            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
            return getLinePaint();
        }
    }

    /**
     * Primitive simulation of some kind of signal.  For this example,
     * we'll pretend its an ecg.  This class represents the data as a circular buffer;
     * data is added sequentially from left to right.  When the end of the buffer is reached,
     * i is reset back to 0 and simulated sampling continues.
     */
    public static class ECGModel implements XYSeries {

        private final Number[] data;
        private final long delayMs;
        private final int blipInteral;
        private final Thread thread;
        private boolean keepRunning;
        private int latestIndex;

        private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

        /**
         *
         * @param size Sample size contained within this model
         * @param updateFreqHz Frequency at which new samples are added to the model
         */
        ECGModel(int size, int updateFreqHz) {
            data = new Number[size];
            for(int i = 0; i < data.length; i++) {
                data[i] = 0;
            }

            // translate hz into delay (ms):
            delayMs = 1000 / updateFreqHz;

            // add 7 "blips" into the signal:
            blipInteral = size / 7;

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (keepRunning) {
                            if (latestIndex >= data.length) {
                                latestIndex = 0;
                            }

                            // generate some random data:
                            if (latestIndex % blipInteral == 0) {
                                // insert a "blip" to simulate a heartbeat:
                                data[latestIndex] = (Math.random() * 10) + 3;
                            } else {
                                // insert a random sample:
                                data[latestIndex] = Math.random() * 2;
                            }

                            if(latestIndex < data.length - 1) {
                                // null out the point immediately following i, to disable
                                // connecting i and i+1 with a line:
                                data[latestIndex +1] = null;
                            }

                            if(rendererRef.get() != null) {
                                rendererRef.get().setLatestIndex(latestIndex);
                                Thread.sleep(delayMs);
                            } else {
                                keepRunning = false;
                            }
                            latestIndex++;
                        }
                    } catch (InterruptedException e) {
                        keepRunning = false;
                    }
                }
            });
        }

        void start(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
            this.rendererRef = rendererRef;
            keepRunning = true;
            thread.start();
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data[index];
        }

        @Override
        public String getTitle() {
            return "Signal";
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        redrawer.finish();
    }
}