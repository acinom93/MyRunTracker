package monash.sydney.com.mytracker;

import android.arch.persistence.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class RunItemConverter {

    @TypeConverter
    public static Long getTimestamp(Date date) {
        if (date != null) {
            return date.getTime();
        }
        return null;
    }

    @TypeConverter
    public static Date getDate(Long value) {
        if (value != null) {
            return new Date(value);
        }
        return null;
    }

    @TypeConverter
    public static ArrayList<LatLng> fromString(String value) {
        Type listType = new TypeToken<ArrayList<LatLng>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<LatLng> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

}
