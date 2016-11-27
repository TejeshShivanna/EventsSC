package application.eventssc;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDescription extends FragmentActivity {

    static String eventsJsonStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_description);

        eventsJsonStr = getIntent().getStringExtra("eventObject");
        try {
            JSONObject eventObj = new JSONObject(eventsJsonStr);
            TextView eventNameTv = (TextView)findViewById(R.id.idEventName);
            eventNameTv.setText(eventObj.getString("eventName"));

            TextView eventLocationTv = (TextView)findViewById(R.id.idEventLocation);
            eventLocationTv.setText(eventObj.getString("address"));

            TextView eventDateTv = (TextView)findViewById(R.id.idEventDate);
            eventDateTv.setText(eventObj.getString("eventDate"));

            TextView eventTimeTv = (TextView)findViewById(R.id.idEventTime);
            SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
            SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm a");
            Date startTime = sdf1.parse(eventObj.getString("startTime"));
            Date endTime = sdf1.parse(eventObj.getString("endTime"));
            eventTimeTv.setText(sdf2.format(startTime) + " - " + sdf2.format(endTime));

            TextView eventDescriptionTv = (TextView)findViewById(R.id.idEventDescription);
            eventDescriptionTv.setText(eventObj.getString("eventDescription"));

        } catch (JSONException e){
            e.printStackTrace();
        } catch (ParseException e){
            e.printStackTrace();
        }
    }
}
