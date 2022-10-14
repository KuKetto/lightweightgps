package pe.saydomonkos.lightweightgps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    //declare ui element variables
    TextView location;
    Button track, history, syncOnline;
    boolean trackingInProgress;

    Utils utils;
    Intent locationIntent;

    private LocationService locationService;
    private boolean locationBound;

    static final int PERMISSION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        //init ui element variables
        location = findViewById(R.id.locationText);
        track = findViewById(R.id.trackBtn);
        history = findViewById(R.id.historyBtn);
        syncOnline = findViewById(R.id.onlineSyncBtn);

        //restore session if there was any
        if (savedInstanceState != null) {
            trackingInProgress = savedInstanceState.getBoolean("trackingInProgress", false);
            if (trackingInProgress) {
                track.setText("STOP TRACKING");
                location.setText("Tracking in progress!");
            }
        }

        //check and request for necessary permissions

        new PermissionHandler()
                .permissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }).handle(MainActivity.this, MainActivity.this, PERMISSION);

        new PermissionHandler()
                .permissions(new String[] {
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE
                }).handle(MainActivity.this, MainActivity.this, PERMISSION);

        utils = new Utils();

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    new PermissionHandler()
                            .permissions(new String[]{
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                    Manifest.permission.FOREGROUND_SERVICE
                            }).handle(MainActivity.this, MainActivity.this, PERMISSION);

                } else {
                    final LocationManager manager = (LocationManager) getSystemService( MainActivity.LOCATION_SERVICE );

                    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        buildAlertMessageNoGps();
                    } else {
                        if (!trackingInProgress) {
                            //start tracking process

                            //NOTE we cannot ask for permission inside a service (which is a context)
                            //  because it cannot use MainActivity's activity.
                            //  Before we do anything check and request for permission here.
                            if (new PermissionHandler().permission(Manifest.permission.ACCESS_FINE_LOCATION).handle(MainActivity.this, MainActivity.this, PERMISSION)) {
                                //permission denied
                            }
                            handleLocation();
                            trackingInProgress = true;
                            track.setText("STOP TRACKING");
                            location.setText("Tracking in progress!");
                        } else {
                            //stop tracking progress
                            locationService.stop();
                            try {
                                unbindService(locationServiceConnection);
                            } catch (Exception e) {
                                Log.i("error", e.getMessage());
                            }
                            location.setText("Start tracking to show your location");
                            track.setText("START TRACKING");
                            trackingInProgress = false;
                            locationIntent = null;
                        }
                    }
                }
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                MainActivity.this.startActivity(historyIntent);
            }
        });

        syncOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(MainActivity.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent syncOnlineIntent = new Intent(MainActivity.this, LoginRegisterActivity.class);
                    MainActivity.this.startActivity(syncOnlineIntent);
                }
                else buildAlertMessageNoInternet();
            }
        });
    }

    private void handleLocation() {
        Log.i("handle","event");
        locationIntent = new Intent(this, LocationService.class);
        bindService(locationIntent, locationServiceConnection, BIND_AUTO_CREATE);
        startService(locationIntent);
        Log.i("bound","done");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        /* on screen rotation application is recreated or
        while the app is in the background, it could be killed to free up ram
        so we need to save our application state to be able to restore it later
        */
        super.onSaveInstanceState(outState);
        if (trackingInProgress) {
            outState.putBoolean("trackingInProgress", true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Request must be granted to " + permissions[i] + " in order to application to work.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from service
        if (locationBound) {
            try {
                unbindService(locationServiceConnection);
            } catch (Exception e) {
                Log.i("error",e.getMessage());
            }
            locationBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationBound) locationService.stop();
    }

    private ServiceConnection locationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i("bound","started");
            // cast the IBinder and get MyService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
            locationBound = true;
            Log.i("bound","finished");
            onLocationServiceReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            locationBound = false;
            locationService = null;
        }
    };

    private void onLocationServiceReady() {
        Log.i("service","ready");
        locationService.locate();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    };

    private void buildAlertMessageNoInternet() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Your WIFI seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    };
}