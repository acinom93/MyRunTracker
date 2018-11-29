package monash.sydney.com.mytracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    public static final double ACCURACY_CONSTANT = 10000.0;
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION};
    LatLng myLocation;
    LocationManager myLocMgr;
    MarkerOptions myMarkerOps;
    Marker myMarker;
    boolean startTracking = false;
    ArrayList<LatLng> listTracking;
    Polyline line;
    Location previousLocation;
    Date previousLocalTime;
    Double currentDistance;
    Double currentTime;
    Double currentSpeed;
    Double currentPace;
    String runName;

    RunItemDAO runItemDAO;
    RunItem runItem;
    private GoogleMap myGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        myLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        myMarkerOps = new MarkerOptions().position(new LatLng(-33.8882, 151.1941)).title("Current Location");

        listTracking = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else {
            requestLocation();
        }

        if (!isLocationEnabled()) {
            showAlert(1);
        }

        //read data from database

        runItemDAO = RunItemDB.getDatabase(this).runItemDao();


        setUI("Status", false);
    }

    private void showAlert(final int status) {

        String message, title, btnText;

        if (status == 1) {
            message = "Your location setting are 'Off'. Please enable to use the App";
            title = "Enable Location";
            btnText = "Location Settings";
        } else {
            message = "Please allow the App to use Location";
            title = "Location Permission";
            btnText = "Grant Permission";
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(btnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (status == 1) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                } else {
                    requestPermissions(PERMISSIONS, PERMISSION_ALL);
                }
            }
        });
    }

    private boolean isLocationEnabled() {
        return myLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) || myLocMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        myGoogleMap = googleMap;
        myMarker = myGoogleMap.addMarker(myMarkerOps);

        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myMarkerOps.getPosition(), 17.0f));
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng myCords = new LatLng(location.getLatitude(), location.getLongitude());
        myMarker.setPosition(myCords);
        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCords, 17.0f));


        if (startTracking == true) {
            listTracking.add(myCords);
            calculateParams(location, myCords);
            setTextViews();
        }

    }

    private void calculateParams(Location location, LatLng myCords) {
        Date currentLocalTime = new Date();
        if (previousLocation != null) {
            // distance in meters
            currentDistance += location.distanceTo(previousLocation);
        }

        if (previousLocalTime != null) {
            // time in seconds
            long duration = currentLocalTime.getTime() - previousLocalTime.getTime();
            currentTime += Long.valueOf(duration / 1000).doubleValue();
        }

        if (currentDistance > 0.0 & currentTime > 0.0) {
            // speed in km/hr
            currentSpeed = (currentDistance / 1000) / (currentTime / 3600);

            // pace in s/km
            currentPace = currentTime / (currentDistance / 1000);
        } else {
            currentSpeed = 0.0;
            currentPace = 0.0;
        }

        previousLocalTime = currentLocalTime;
        previousLocation = location;
    }

    private void setTextViews() {

        String distance, time, speed, pace;

        distance = "Distance : " + Math.round(currentDistance * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "m";
        time = "Duration : " + getHourMinSecString(currentTime.intValue()) + "";
        speed = "Speed : " + Math.round(currentSpeed * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "km/hr";
        pace = "Pace : " + getHourMinSecString(currentPace.intValue()) + "/km";

        TextView distanceView = (TextView) findViewById(R.id.Distance);
        distanceView.setText(distance);
        TextView timeView = (TextView) findViewById(R.id.Time);
        timeView.setText(time);
        TextView speedView = (TextView) findViewById(R.id.Speed);
        speedView.setText(speed);
        TextView paceView = (TextView) findViewById(R.id.Pace);
        paceView.setText(pace);
    }

    private String getHourMinSecString(Integer currentTime) {
        String time = "";

        Integer hours = currentTime / 3600;
        Integer remainder = currentTime - (hours * 3600);
        Integer mins = remainder / 60;
        remainder = remainder - (mins * 60);
        Integer secs = remainder;

        time = hours + ":" + mins + ":" + secs;
        return time;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void requestLocation() {
        Criteria myCriteria = new Criteria();
        myCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        myCriteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = myLocMgr.getBestProvider(myCriteria, true);

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

        myLocMgr.requestLocationUpdates(provider, 1000, 10, this);
    }

    public void onStartClick(View view) {

        runName = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Name Your Run");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runName = input.getText().toString();

                if (runName == null | runName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Cannot Start the Run without a name", Toast.LENGTH_LONG).show();
                } else {
                    listTracking.clear();
                    resetParams();

                    startTracking = true;

                    if (line != null) {
                        myGoogleMap.clear();
                    }
                    setUI("Current : " + runName + " Running ", true);
                    setTextViews();
                }
            }
        });
        builder.show();

    }

    private void resetParams() {
        previousLocation = null;
        previousLocalTime = null;
        currentDistance = 0.0;
        currentTime = 0.0;
        currentSpeed = 0.0;
        currentPace = 0.0;
    }

    public void onStopClick(View view) {

        startTracking = false;

        drawPolyLine();

        addRunToDB();
        listTracking.clear();

        setUI("Last : " + runName + " Stopped ", false);
    }

    private void drawPolyLine() {
        if (listTracking != null && !listTracking.isEmpty()) {
            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(false);

            for (int i = 0; i < listTracking.size(); i++) {
                LatLng point = listTracking.get(i);
                options.add(point);
            }

            line = myGoogleMap.addPolyline(options);
        }
    }

    private void addRunToDB() {
        runItem = new RunItem(runName, new Date(), currentDistance, currentTime, currentSpeed, currentPace, new ArrayList<LatLng>(listTracking),0.0,0.0);
        databaseInsert();
    }

    public void onStatsClick(View view) {
        Intent intent = new Intent(this, StatsActivity.class);

        startActivity(intent);

    }

    public void onBestClick(View view) {
        Intent intent = new Intent(this, BestActivity.class);

        startActivity(intent);

    }

    public void onMusicClick(View view) {
        Intent intent = new Intent(this, MusicPlayerActivity.class);

        startActivity(intent);

    }


    private void setUI(String status, boolean run) {

        TextView textView = (TextView) findViewById(R.id.Status);
        textView.setText(status);

        Button buttonRun = (Button) findViewById(R.id.buttonRun);
        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        if (run) {
            buttonRun.setVisibility(View.GONE);
            buttonStop.setVisibility(View.VISIBLE);
        } else {
            buttonRun.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.GONE);
        }
    }


    private void databaseInsert() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                //save to database
                runItemDAO.insert(runItem);
                return null;
            }
        }.execute();
    }

    public void onFalatuClick(View view) {
        Intent intent = new Intent(this, PaceActivity.class);

        startActivity(intent);

    }
}
