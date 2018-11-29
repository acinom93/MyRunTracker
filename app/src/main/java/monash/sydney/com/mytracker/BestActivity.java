package monash.sydney.com.mytracker;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class BestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap longGoogleMap;
    public static final double ACCURACY_CONSTANT = 100.0;
    RunItem runlongItem;
    RunItem runfastItem;
    ArrayList<RunItem> runItemList;
    RunItemDAO runItemDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best);

        runItemDAO = RunItemDB.getDatabase(this).runItemDao();
        runItemList = new ArrayList<>();
        databaseRead();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

    }

    private void setBestStats() {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMMM d, yyyy");

        if(runlongItem!=null) {
            ((TextView) findViewById(R.id.longName)).setText(runlongItem.getLabel());
            ((TextView) findViewById(R.id.longdistance)).setText(Math.round(runlongItem.getDistance() * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "m");
            ((TextView) findViewById(R.id.longspeed)).setText(Math.round(runlongItem.getSpeed() * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "km/hr");
            Date date = runlongItem.getDate();
            ((TextView) findViewById(R.id.longdate)).setText(dateFormatter.format(date));
        }

        if(runfastItem!=null) {
            ((TextView) findViewById(R.id.fastname)).setText(runfastItem.getLabel());
            ((TextView) findViewById(R.id.fastdistance)).setText(Math.round(runfastItem.getDistance() * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "m");
            ((TextView) findViewById(R.id.fastspeed)).setText(Math.round(runfastItem.getSpeed() * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "km/hr");
            Date date = runfastItem.getDate();
            ((TextView) findViewById(R.id.fastdate)).setText(dateFormatter.format(date));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        longGoogleMap = googleMap;
        longGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8882, 151.1941), 12.0f));
        setBestStats();
        updatemap();
    }

    private void updatemap() {

        //longGoogleMap.clear();
        if(runlongItem!=null) {
            drawPolyLine(runlongItem.getPointList());
        }
        if(runfastItem!=null) {
            drawPolyLine(runfastItem.getPointList());
        }
    }

    private void drawPolyLine(ArrayList<LatLng> listTracking) {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        if (listTracking != null && !listTracking.isEmpty()) {
            PolylineOptions options = new PolylineOptions().width(10).color(color).geodesic(false);

            for (int i = 0; i < listTracking.size(); i++) {
                LatLng point = listTracking.get(i);
                options.add(point);
            }
            Polyline line = longGoogleMap.addPolyline(options);
            longGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(listTracking.get(0), 12.0f));
        }
    }

    private void databaseRead() {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {

                    //read from database
                    List<RunItem> itemsFromDB = runItemDAO.listAll();
                    if (itemsFromDB != null) {
                        runItemList.addAll(itemsFromDB);
                        getBestStats();
                    }
                    return null;
                }
            }.execute().get();
        } catch (Exception ex) {
            Log.e("Read", ex.getStackTrace().toString());
        }
    }

    private void getBestStats() {
        double longestRun = 0.0;
        double maxSpeed = 0.0;
        for (RunItem runItem : runItemList) {

            if(runItem.getDistance()>longestRun) {
                longestRun = runItem.getDistance();
                runlongItem = runItem;
            }

            if(runItem.getSpeed()>maxSpeed) {
                maxSpeed = runItem.getSpeed();
                runfastItem = runItem;
            }
        }
    }
}
