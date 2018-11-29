package monash.sydney.com.mytracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pace);
    }

    public void onFindOutClick(View view) {

        EditText distance = findViewById(R.id.distance);
        EditText duration = findViewById(R.id.duration);

        Double distanceValue = 0.0;
        if(distance.getText()!=null & distance.getText().length()!=0) {
            distanceValue = Double.parseDouble(distance.getText().toString());
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please enter a valid value of Distance in kms", Toast.LENGTH_LONG).show();
        }

        Double durationValue = 0.0;
        if(duration.getText()!=null & duration.getText().length()!=0) {
            durationValue = Double.parseDouble(duration.getText().toString());
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Please enter a valid value of Duration in minutes", Toast.LENGTH_LONG).show();
        }

        Double speedVal = 0.0;
        Double paceVal = 0.0;
        if(distanceValue>0 & durationValue>0) {

            speedVal = distanceValue / (durationValue/60);
            paceVal = (durationValue*60) / distanceValue;
        }

        TextView speed = (TextView) findViewById(R.id.speed);
        TextView speedUnit = (TextView)findViewById(R.id.speedUnit);
        TextView speedValue = (TextView)findViewById(R.id.speedValue);
        TextView pace = (TextView)findViewById(R.id.pace);
        TextView paceUnit = (TextView)findViewById(R.id.paceUnit);
        TextView paceValue = (TextView)findViewById(R.id.paceValue);

        speedValue.setText(Math.round(speedVal * 100.00) / 100.00 + "");
        paceValue.setText(getHourMinSecString(paceVal.intValue()) + "");
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
