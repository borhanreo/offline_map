package com.peterlaurence.trekme.ui.mapview;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.peterlaurence.trekme.MainActivity;
import com.peterlaurence.trekme.service.UsbService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Created by MD Borhan Uddin on 09-Aug-19.
 */
public class ReceiveRfData {
    public  Activity activity;
    public  Context context;
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private CheckBox box9600, box38400;
    private MyHandler mHandler;
    private String TAG= "Borhan ReceiveRFData";

    public ReceiveRfData(Activity activity, Context context){
        this.activity=activity;
        this.context=context;
    }
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    public void enable(){
        mHandler = new MyHandler((MainActivity) activity,context);
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
        setFilters();
    }
    public void writeMessage(String msg){
        try {
            usbService.write(msg.getBytes());
        } catch (Exception ex) {
            Log.d(TAG, ex.toString());
        }
    }
    public void disable()
    {
        activity.unregisterReceiver(mUsbReceiver);
        activity.unbindService(usbConnection);
    }
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(activity, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            activity.startService(startService);
        }
        Intent bindingIntent = new Intent(activity, service);
        activity.bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        activity.registerReceiver(mUsbReceiver, filter);
    }



    public static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        private Context mContext;


        public MyHandler(MainActivity activity,Context context) {
            mActivity = new WeakReference<>(activity);
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    Log.d("SendData2", data);
                    //mActivity.get().display.append(data);
                    //Toast.makeText(mContext,data.toString(),Toast.LENGTH_SHORT).show();
                    // double lat=0.0;
                    //double lng=0.0;
                    //String set_time =  "{\"type\":\"BUS\",\"lat\":\""+lat+"\",\"lat\":" + lng +  "}";
                    //mRfCalbackListener.back(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    //telemetry read
                    String buffer = (String) msg.obj;
                    Log.d("SendData3", buffer);
                    Toast.makeText(mContext,buffer,Toast.LENGTH_SHORT).show();
                    //mActivity.get().display.append(buffer);
                    //Toast.makeText(mContext, buffer.toString(), Toast.LENGTH_LONG).show();
                    //mRfCalbackListener.back(buffer);
                    break;
            }
        }
        private String encodeString(String s) {
            byte[] data = new byte[0];

            try {
                data = s.getBytes("UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } finally {
                String base64Encoded = Base64.encodeToString(data, Base64.DEFAULT);

                return base64Encoded;

            }
        }
        private String decodeString(String encoded) {
            byte[] dataDec = Base64.decode(encoded, Base64.DEFAULT);
            String decodedString = "";
            try {

                decodedString = new String(dataDec, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();

            } finally {

                return decodedString;
            }
        }
    }


}
