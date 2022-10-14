package pe.saydomonkos.lightweightgps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.sql.Timestamp;

public class LocationHandler {

    LocationManager locationManager;
    LogHandler logHandler;
    Utils utils;
    LocationListener locationListener;
    String filename = "";
    String locationState;
    Context context;

    public LocationHandler(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        utils = new Utils();
        this.logHandler = new LogHandler(context);
        this.context = context;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                onLocationUpdate(location);
            }
        };
    }

    public LocationHandler filename(String filename) {
        if (!filename.equals("")) this.filename = filename;
        return this;
    }

    @SuppressLint("MissingPermission") //Permission-checking is not handled before calling this class
    public boolean start(Timestamp startTime) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        //started
        locationState = "Looking for GPS signal...";
        Log.i("location", locationState);
        logHandler.filename("log_" + String.valueOf(new Timestamp(System.currentTimeMillis()).getTime()) + ".csv").open();
        logHandler.log("INIT;STARTED;" + utils.getCurrentTimeFormat(startTime) + ";\n");
        return true;
    }

    public void stop(Timestamp startTime) {
        Timestamp stopTime = utils.getTimestamp();
        logHandler.log("EOF;STOPPED;" + utils.getCurrentTimeFormat(stopTime));
        long duration = Math.abs(stopTime.getTime() - startTime.getTime());
        logHandler.close();
        logHandler.renameLogFile(utils.getFilenameFormat(startTime, stopTime, duration));
        locationManager.removeUpdates(locationListener);
    }

    public String getCurrentState() {
        return locationState;
    }

    public String getLogFileName() {
        return logHandler.getFilename();
    }

    public boolean isFileStreamClosed() {
        return logHandler.isClosed();
    }

    boolean inited = false;
    private void onLocationUpdate(Location location) {
        locationState = utils.getCurrentTimeFormat(utils.getTimestamp()) + ";" + location.getLatitude() + ";" + location.getLongitude();
        Log.i("location", locationState);
        if (!inited) {
            Toast.makeText(context, "location found . . .", Toast.LENGTH_SHORT).show();
            inited = true;
        }
        Log.i("accuracy", String.valueOf(location.getAccuracy()));
        logHandler.log(locationState + ";\n");
    }
}
