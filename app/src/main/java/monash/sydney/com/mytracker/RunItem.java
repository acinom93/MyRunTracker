package monash.sydney.com.mytracker;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

@Entity(tableName = "runitems")
public class RunItem {

    //variables
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "label")
    private String label;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "distance")
    private Double distance;

    @ColumnInfo(name = "time")
    private Double time;

    @ColumnInfo(name = "speed")
    private Double speed;

    @ColumnInfo(name = "pace")
    private Double pace;

    @Ignore
    private Double avgDistance;

    @Ignore
    private Double avgTime;

    @ColumnInfo(name = "pointList")
    private ArrayList<LatLng> pointList;

    //constructor

    public RunItem(String label, Date date, Double distance, Double time, Double speed, Double pace, ArrayList<LatLng> pointList) {
        this.label = label;
        this.date = date;
        this.distance = distance;
        this.time = time;
        this.speed = speed;
        this.pace = pace;
        this.pointList = pointList;
    }

    public RunItem(String label, Date date, Double distance, Double time, Double speed, Double pace, ArrayList<LatLng> pointList, Double avgDistance, Double avgTime) {
        this.label = label;
        this.date = date;
        this.distance = distance;
        this.time = time;
        this.speed = speed;
        this.pace = pace;
        this.pointList = pointList;
        this.avgDistance = avgDistance;
        this.avgTime = avgTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getPace() {
        return pace;
    }

    public void setPace(Double pace) {
        this.pace = pace;
    }

    public ArrayList<LatLng> getPointList() {
        return pointList;
    }

    public Double getAvgDistance() {
        return avgDistance;
    }

    public void setAvgDistance(Double avgDistance) {
        this.avgDistance = avgDistance;
    }

    public Double getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(Double avgTime) {
        this.avgTime = avgTime;
    }

    @Override
    public String toString() {
        return this.label;
    }
}