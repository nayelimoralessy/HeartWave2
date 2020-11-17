package com.example.heartwave;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.*;
import java.lang.ref.*;

public class ECGSignal extends AppCompatActivity {
    private XYPlot plot;
    private Redrawer redrawer;
    BleService bleService;
    boolean isBound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_c_g_signal);
        plot = findViewById(R.id.plot);
        ECGModel ecgSeries = new ECGModel(2000, 200);
        MyFadeFormatter formatter =new MyFadeFormatter(2000);
        formatter.setLegendIconEnabled(false);
        plot.addSeries(ecgSeries, formatter);
        plot.setRangeBoundaries(0, 10, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 2000, BoundaryMode.FIXED);
        plot.setLinesPerRangeLabel(3);
        // start generating ecg data in the background:
        ecgSeries.start(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)));
        // set a redraw rate of 30hz and start immediately:
        redrawer = new Redrawer(plot, 30, true);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    protected static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

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

    protected static class ECGModel implements XYSeries {

        private final Number[] data;
        private final long delayMs;
        private final int blipInteral;
        private final Thread thread;
        private boolean keepRunning;
        private int latestIndex;

        private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

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
                            if (latestIndex % blipInteral == 0) {
                                data[latestIndex] = ((int)BleService.voltageADC * 10) + 3;
                            } else {
                                data[latestIndex] = BleService.voltageADC * 2;
                            }
                            if(latestIndex < data.length - 1) {
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
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BleService.LocalBinder binder = (BleService.LocalBinder) service;
            BleService bleService = binder.getService();
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
}