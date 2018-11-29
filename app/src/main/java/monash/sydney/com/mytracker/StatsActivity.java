package monash.sydney.com.mytracker;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StatsActivity extends AppCompatActivity implements OnMapReadyCallback {
    ArrayList<RunItem> runItemList;

    List<RunItem> runItemDailyList;
    List<RunItem> runItemWeeklyList;
    private GoogleMap myGoogleMap;

    RunItemDAO runItemDAO;
    RunItemAdapter adapter;
    ListView listView;
    GraphView graph;

    Map<String, List<RunItem>> statList;
    int typeOfList;

    private static Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private static String getKey(Calendar itemDate) {
        int year = itemDate.get(itemDate.YEAR);
        int week = itemDate.get(itemDate.WEEK_OF_YEAR);

        return "Year " + year + ", Week " + week;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        listView = (ListView) findViewById(R.id.listStat);

        runItemWeeklyList = new ArrayList<>();
        runItemDailyList = new ArrayList<>();
        runItemList = new ArrayList<>();
        statList = new HashMap<>();
        typeOfList = 0;

        runItemDAO = RunItemDB.getDatabase(this).runItemDao();
        adapter = new RunItemAdapter(this, runItemList);
        listView.setAdapter(adapter);
        databaseRead();

        graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.INVISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                graph.setVisibility(View.INVISIBLE);
                if (typeOfList == 0) {

                    List<RunItem> runItems = new ArrayList<>();
                    runItems.add(runItemList.get(position));
                    updatemap(runItems);

                } else if (typeOfList == 1) {
                    updatemap(statList.get(runItemDailyList.get(position).getLabel()));

                } else {
                    updatemap(statList.get(runItemWeeklyList.get(position).getLabel()));

                }
            }

        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                graph.setVisibility(View.VISIBLE);
                if (typeOfList == 0) {

                    List<RunItem> runItems = new ArrayList<>();
                    runItems.add(runItemList.get(position));

                    drawgraph(runItems);
                } else if (typeOfList == 1) {

                    drawgraph(statList.get(runItemDailyList.get(position).getLabel()));
                } else {

                    drawgraph(statList.get(runItemWeeklyList.get(position).getLabel()));
                }
                return true;
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        TextView typeView = (TextView) findViewById(R.id.type);
        typeView.setText("All Runs");
    }

    private void updatemap(List<RunItem> runItemList) {

        myGoogleMap.clear();
        for(RunItem runItem : runItemList) {
            drawPolyLine(runItem.getPointList());
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
                        adapter.notifyDataSetChanged();
                        generateDailyList();
                        generateWeeklyList();
                    }
                    return null;
                }
            }.execute().get();
        } catch (Exception ex) {
            Log.e("Read", ex.getStackTrace().toString());
        }
    }

    private void generateWeeklyList() {

        Map<String, List<RunItem>> weeklyList = new HashMap<>();

        for (RunItem item : runItemList) {

            Calendar itemDate = toCalendar(item.getDate());
            String itemKey = getKey(itemDate);

            List<RunItem> tempList;
            if (weeklyList.containsKey(itemKey)) {
                tempList = weeklyList.get(itemKey);
            } else {
                tempList = new ArrayList<RunItem>();
            }
            tempList.add(item);
            weeklyList.put(itemKey, tempList);
        }

        runItemWeeklyList = generateList(weeklyList);
        statList.putAll(weeklyList);
    }

    private void generateDailyList() {
        Map<String, List<RunItem>> dailyMap = new HashMap<>();
        for (RunItem runItem : runItemList) {
            String stringDate = getDateInString(runItem);
            if (dailyMap.containsKey(stringDate)) {
                dailyMap.get(stringDate).add(runItem);
            } else {
                List<RunItem> list = new ArrayList<>();
                list.add(runItem);
                dailyMap.put(stringDate, list);
            }

        }

        runItemDailyList = generateList(dailyMap);
        statList.putAll(dailyMap);
    }

    private List<RunItem> generateList(Map<String, List<RunItem>> dailyMap) {

        List<RunItem> generatedList = new ArrayList<>();
        Iterator it = dailyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<RunItem>> pair = (Map.Entry) it.next();

            String label = pair.getKey();
            Double distance = 0.0;
            Double duration = 0.0;
            for (RunItem temp : pair.getValue()) {
                distance = distance + temp.getDistance();
                duration = duration + temp.getTime();

            }
            Double speed = (distance / 1000) / (duration / 3600);
            Double pace = duration / (distance / 1000);
            RunItem runItem = new RunItem(label, null, distance, duration, speed, pace, null, distance/pair.getValue().size(), duration/pair.getValue().size());
            generatedList.add(runItem);
        }

        return generatedList;
    }

    private String getDateInString(RunItem runItem) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMMM d, yyyy");
        return formatter.format(runItem.getDate());
    }

    public void onEveryRunClick(View view) {
        typeOfList = 0;
        adapter = new RunItemAdapter(this, runItemList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        TextView typeView = (TextView) findViewById(R.id.type);
        typeView.setText("All Runs");
    }

    public void onEveryDayClick(View view) {
        typeOfList = 1;
        adapter = new RunItemAdapter(this, runItemDailyList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        TextView typeView = (TextView) findViewById(R.id.type);
        typeView.setText("All Days");
    }

    public void onEveryWeekClick(View view) {
        typeOfList = 2;
        adapter = new RunItemAdapter(this, runItemWeeklyList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        TextView typeView = (TextView) findViewById(R.id.type);
        typeView.setText("All Weeks");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myGoogleMap = googleMap;
        myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8882, 151.1941), 17.0f));
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


            Polyline line = myGoogleMap.addPolyline(options);
            myGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(listTracking.get(0), 17.0f));
        }
    }

    private void drawgraph(List<RunItem> runItemList) {

        graph.removeAllSeries();
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>();

        int i=0;
        int max = 0;
        for(RunItem runItem : runItemList) {
            i+=1;
            DataPoint dp = new DataPoint(i,runItem.getDistance().intValue());
            if(max<runItem.getDistance().intValue())
            {
                max = runItem.getDistance().intValue();
            }
            series.appendData(dp,true,runItemList.size());
        }
        // styling
        series.setColor(Color.rgb((int) 102, (int) 153, 0));
        series.setSpacing(50);

        // draw values on top
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLUE);
        //series.setValuesOnTopSize(50);

        BarGraphSeries<DataPoint> series2 = new BarGraphSeries<>();

        i=0;
        for(RunItem runItem : runItemList) {
            i+=1;
            DataPoint dp = new DataPoint(i,runItem.getSpeed().intValue());
            if(max<runItem.getSpeed().intValue())
            {
                max = runItem.getSpeed().intValue();
            }
            series2.appendData(dp,true,runItemList.size());
        }

        // styling
        series2.setColor(Color.rgb((int) 0, (int) 153, 204));
        series2.setSpacing(50);

        // draw values on top
        series2.setDrawValuesOnTop(true);
        series2.setValuesOnTopColor(Color.BLACK);
        //series.setValuesOnTopSize(50);

        // set manual X bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(max+50);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(i+1);

        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        // legend
        series.setTitle("Distance");
        series2.setTitle("Speed");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph.addSeries(series);
        graph.addSeries(series2);
    }


}
