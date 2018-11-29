package monash.sydney.com.mytracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class RunItemAdapter extends ArrayAdapter<RunItem> {

    public static final double ACCURACY_CONSTANT = 10000.0;
    private Context mContext;
    private List<RunItem> runItemList;

    public RunItemAdapter(@NonNull Context context, List<RunItem> list) {
        super(context, 0, list);
        mContext = context;
        runItemList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.runitemlayout, parent, false);

        RunItem currentRunItem = runItemList.get(position);

        ((TextView) listItem.findViewById(R.id.label)).setText(currentRunItem.getLabel());
        ((TextView) listItem.findViewById(R.id.distance)).setText(Math.round(currentRunItem.getDistance() * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "m");
        ((TextView) listItem.findViewById(R.id.duration)).setText(getHourMinSecString(currentRunItem.getTime().intValue()) + "");
        ((TextView) listItem.findViewById(R.id.speed)).setText(Math.round(currentRunItem.getSpeed() * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "km/hr");
        ((TextView) listItem.findViewById(R.id.pace)).setText(getHourMinSecString(currentRunItem.getPace().intValue()) + "/km");

        Date date = currentRunItem.getDate();

        if(date==null)
        {
            ((TextView) listItem.findViewById(R.id.dateValue)).setText("");
            ((TextView) listItem.findViewById(R.id.dateLabel)).setText("");
            ((TextView) listItem.findViewById(R.id.dateValue)).setVisibility(View.GONE);
            ((TextView) listItem.findViewById(R.id.dateLabel)).setVisibility(View.GONE);

            ((TextView) listItem.findViewById(R.id.distance2)).setText(Math.round(currentRunItem.getAvgDistance() * ACCURACY_CONSTANT) / ACCURACY_CONSTANT + "m");
            ((TextView) listItem.findViewById(R.id.duration2)).setText(getHourMinSecString(currentRunItem.getAvgTime().intValue()) + "");

            ((TextView) listItem.findViewById(R.id.durationLabel)).setText("Total Duration");
            ((TextView) listItem.findViewById(R.id.distanceLabel)).setText("Total Distance");
        }
        else
        {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMMM d, yyyy");
            ((TextView) listItem.findViewById(R.id.dateValue)).setText(dateFormatter.format(date));

            ((TextView) listItem.findViewById(R.id.distance2)).setText("");
            ((TextView) listItem.findViewById(R.id.textView14)).setText("");
            ((TextView) listItem.findViewById(R.id.distance2)).setVisibility(View.GONE);
            ((TextView) listItem.findViewById(R.id.textView14)).setVisibility(View.GONE);


            ((TextView) listItem.findViewById(R.id.duration2)).setText("");
            ((TextView) listItem.findViewById(R.id.textView15)).setText("");
            ((TextView) listItem.findViewById(R.id.duration2)).setVisibility(View.GONE);
            ((TextView) listItem.findViewById(R.id.textView15)).setVisibility(View.GONE);
        }

        return listItem;
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
}
