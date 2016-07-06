package com.jpttrindade.testlib;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.sql.Time;
import java.util.Objects;

import br.com.jpttrindade.mmomlib.mmomserver.BrokerEventCallback;
import br.com.jpttrindade.mmomlib.mmomserver.IMMomServer;
import br.com.jpttrindade.mmomlib.mmomserver.MMomMessage;
import br.com.jpttrindade.mmomlib.mmomserver.MMomServer;

public class MainActivity extends AppCompatActivity implements BrokerEventCallback {

    private static final String RESPONDER_ID = "RESPONDER_1";
    private IMMomServer momServer;
    private Button bt_connect;
    private Button bt_sendtext;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count = 0;
        Log.d("DEBUG", "onCreate ");
        bt_connect = (Button) findViewById(R.id.bt_connect);
        bt_sendtext = (Button) findViewById(R.id.bt_sendtext);
        bt_sendtext.setText(count + "");

        momServer = MMomServer.createMMomServer(this, "172.22.68.46", 5678);

    }

    @Override
    protected void onResume() {
        super.onResume();
        momServer.connect(RESPONDER_ID, this);
    }

    public void connect(View view) {
        momServer.connect(RESPONDER_ID, this);

    }

    @Override
    protected void onPause() {
        Log.d("DEBUG", "onPause");
        momServer.closeConnection();
        super.onPause();

    }

    public void count(View view) {
        count++;
        bt_sendtext.setText(count + "");
    }

    @Override
    public void onReceiveRequest(String request) {

        Log.d("DEBUG", "onReceiveRequest()");
        bt_sendtext.setText(request);

        if (Objects.equals("get_hour()", request)) {
            sendHour();
        } else if (request.contains("get_gps()")) {
            sendGPS();
        }

    }

    private void sendGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location l = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        momServer.response("{longitude: " + l.getLongitude() + ",\nlatitude: " + l.getLatitude() + "}");

    }

    private void sendImage(String fileName) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        Log.d("Files", "Path: " + path + "/" + fileName);
        File f = new File(path + "/" + fileName);
        Log.d("Files", "File: " + f.exists());
        momServer.response(f);
    }

    private void sendHour() {
        Time time = new Time(System.currentTimeMillis());
        Log.d("DEBUG", time.toString());
        momServer.response(time.toString());
    }

    @Override
    public void onConnectionEstablished() {
        Log.d("DEBUG", "onConnectionEstablished()");
        Log.d("DEBUG", "waiting for Broker message!");
        bt_connect.setEnabled(false);
        bt_sendtext.setEnabled(true);
        bt_connect.setText("CONNECTED");
        bt_connect.setBackgroundColor(getResources().getColor(R.color.connected));
    }

    @Override
    public void onConnectionClosed() {
        Log.d("DEBUG", "onConnectionClosed()");
        bt_connect.setEnabled(true);
        bt_sendtext.setEnabled(false);
        bt_connect.setText("CONNECT");
        bt_connect.setBackgroundColor(getResources().getColor(R.color.disconnected));
    }

}