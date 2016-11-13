package application.eventssc;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class CreateEvent extends AppCompatActivity {

    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;

    private TextView tvDisplayStartTime;
    private TimePicker timePicker1;
    private Button btnChangeTime;

    private int hour;
    private int minute;

    static final int DATE_DIALOG_ID = 999;
    static final int START_TIME_DIALOG_ID = 998;
    static final int END_TIME_DIALOG_ID = 997;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        dateView = (TextView) findViewById(R.id.dateLabel);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month + 1, day);

        setCurrentTimeOnView();
        //addListenerOnButton();

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
        dateView.setText(new StringBuilder().append(month).append("/").append(day).append("/").append(year));
    }

    @SuppressWarnings("deprecation")
    public void setCurrentTimeOnView() {

        tvDisplayStartTime = (TextView) findViewById(R.id.startTimeLabel);
        //timePicker1 = (TimePicker) findViewById(R.id.timePicker1);

        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        // set current time into textview
        tvDisplayStartTime.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));

        // set current time into timepicker
        //timePicker1.setCurrentHour(hour);
        //timePicker1.setCurrentMinute(minute);

    }

    @SuppressWarnings("deprecation")
    public void setStartTime(View view) {
        showDialog(START_TIME_DIALOG_ID);
    }

//    public void addListenerOnButton() {
//
//        btnChangeTime = (Button) findViewById(R.id.setButton);
//        btnChangeTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialog(TIME_DIALOG_ID);
//            }
//        });
//    }

    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        switch (id) {
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

            // set current time into timepicker
            //timePicker1.setCurrentHour(hour);
            //timePicker1.setCurrentMinute(minute);

        }
    };

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }


}
