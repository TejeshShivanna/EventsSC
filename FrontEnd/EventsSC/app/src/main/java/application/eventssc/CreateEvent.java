package application.eventssc;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.TimeZone;

public class CreateEvent extends AppCompatActivity {

    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;

    private TextView tvDisplayStartTime;
    private TextView tvDisplayEndTime;
    private TimePicker timePicker1;
    private Button btnChangeTime;

    private int hour;
    private int minute;

    static final int DATE_DIALOG_ID = 999;
    static final int START_TIME_DIALOG_ID = 998;
    static final int END_TIME_DIALOG_ID = 997;

    String createEventUrl = "";

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        userId = getIntent().getIntExtra("UserId", -1);

        dateView = (TextView) findViewById(R.id.dateLabel);
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        calendar = Calendar.getInstance(tz);
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month + 1, day);

        setCurrentTimeOnView();

    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(DATE_DIALOG_ID);
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            showDate(arg1, arg2 + 1, arg3);
        }
    };

    private void showDate(int year, int month, int day) {
        String monthStr = new DateFormatSymbols().getMonths()[month - 1];
        dateView.setText(new StringBuilder().append(day).append(" ").append(monthStr.substring(0, 3)).append(" ").append(year));
    }

    @SuppressWarnings("deprecation")
    public void setCurrentTimeOnView() {

        tvDisplayStartTime = (TextView) findViewById(R.id.startTimeLabel);
        tvDisplayEndTime = (TextView) findViewById(R.id.endTimeLabel);
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        final Calendar c = Calendar.getInstance(tz);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        // set current time into textview
        tvDisplayStartTime.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));
        tvDisplayEndTime.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));

    }

    @SuppressWarnings("deprecation")
    public void setStartTime(View view) {
        showDialog(START_TIME_DIALOG_ID);
    }

    @SuppressWarnings("deprecation")
    public void setEndTime(View view) {
        showDialog(END_TIME_DIALOG_ID);
    }


    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case END_TIME_DIALOG_ID:
                return new TimePickerDialog(this, endTimePickerListener, hour, minute, false);
            case START_TIME_DIALOG_ID:
                return new TimePickerDialog(this, timePickerListener, hour, minute, false);
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
            hour = selectedHour;
            minute = selectedMinute;
            // set current time into textview
            tvDisplayStartTime.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));

        }
    };

    @SuppressWarnings("deprecation")
    private TimePickerDialog.OnTimeSetListener endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
            hour = selectedHour;
            minute = selectedMinute;
            // set current time into textview
            tvDisplayEndTime.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));

        }
    };

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    public void generatePostUrlParams() {
        EditText name = (EditText) this.findViewById(R.id.etEventName);
        TextView date = (TextView) this.findViewById(R.id.dateLabel);
        TextView startTime = (TextView) this.findViewById(R.id.startTimeLabel);
        TextView endTime = (TextView) this.findViewById(R.id.endTimeLabel);
        EditText description = (EditText) this.findViewById(R.id.etDescription);
        EditText address = (EditText) this.findViewById(R.id.etAddress);
        TextView errorMsg = (TextView) this.findViewById(R.id.errorStr);

        String jsonObjString = "";

        String nameStr = (name != null) ? name.getText().toString().trim() : "";
        String dateStr = (date != null) ? date.getText().toString().trim() : "";
        String startTimeStr = (startTime != null) ? startTime.getText().toString().trim() : "";
        String endTimeStr = (endTime != null) ? endTime.getText().toString().trim() : "";
        String descriptionStr = (description != null) ? description.getText().toString().trim() : "";
        String addressStr = (address != null) ? address.getText().toString().trim() : "";

        if (nameStr.length() <= 0) {
            errorMsg.setText("Please enter the Event Name");
            return;
        }
        if (descriptionStr.length() <= 0) {
            errorMsg.setText("Please enter the Description");
            return;
        }
        if (addressStr.length() <= 0) {
            errorMsg.setText("Please enter the Event Address");
            return;
        }

        try {
            JSONObject obj = new JSONObject();
            obj.put("name", nameStr);
            obj.put("date", dateStr);
            obj.put("starttime", startTimeStr);
            obj.put("endtime", endTimeStr);
            obj.put("description", descriptionStr);
            obj.put("address", addressStr);
            obj.put("creatorId", userId);
            jsonObjString = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            String encodedString = URLEncoder.encode(jsonObjString, "UTF-8");
            ////////http://10.0.2.2:8080/
            createEventUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/create?creationString=" + encodedString;
            try {
                new JsonAsyncTask().execute();

            } catch (Exception e) {
                e.printStackTrace();
                errorMsg.setText("Failed");
            }
        } catch (UnsupportedEncodingException ex) {
            errorMsg.setText(" Url Exeption ");
        }

    }

    public void createEvent(View view) {
        try {
            generatePostUrlParams();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class JsonAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet request = new HttpGet(createEventUrl);
            InputStream resultStream = null;
            String result = null;
            try {
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                resultStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resultStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "Event Created Successfully", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Event Creation Failed", Toast.LENGTH_SHORT);
                toast.show();
            }
            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), EventsByYou.class);
            resultsIntent.putExtra("UserId", userId);
            startActivity(resultsIntent);
        }
    }

}