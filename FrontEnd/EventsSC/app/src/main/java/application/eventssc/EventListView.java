package application.eventssc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EventListView extends AppCompatActivity {

    private String webServerUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/all_events";

    static String eventsJsonStr = "";
    static JSONArray jsonArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list_view);

        eventsJsonStr = getIntent().getStringExtra("eventsJsonString");
        final ArrayList<String> list = new ArrayList<String>();

        try{
            jsonArr = new JSONArray(eventsJsonStr);
            for(int i = 0; i < jsonArr.length(); i++){
                JSONObject obj = (JSONObject) jsonArr.get(i);
                list.add(obj.optString("eventName", "") + " \n " + obj.optString("eventDate", ""));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }



        final ListView listview = (ListView) findViewById(R.id.listview);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

        listview.setClickable(true);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = listview.getItemAtPosition(position);
                System.out.print(position);
                Toast.makeText(getApplicationContext(), item.toString(), Toast.LENGTH_SHORT).show();
                try {
                    JSONObject obj = (JSONObject) jsonArr.get(position);
                    Intent resultsIntent = new Intent();
                    resultsIntent.setClass(getApplicationContext(), EventDescription.class);
                    resultsIntent.putExtra("eventObject", obj.toString());
                    startActivity(resultsIntent);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
        listview.setAdapter(adapter);


    }

}
