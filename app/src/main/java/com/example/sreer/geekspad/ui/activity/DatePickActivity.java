package com.example.sreer.geekspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.example.sreer.geekspad.R;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DatePickActivity extends AppCompatActivity {

    private Button setButton;

    private DatePicker date;
    private int year, month, day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_pick);
        date = (DatePicker)findViewById(R.id.datePicker1);
        setButton =  (Button) findViewById(R.id.date_set_button);
        day = date.getDayOfMonth();
        month = date.getMonth()+1;
        year = date.getYear();

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate();
            }
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    public void setDate(){
        day = date.getDayOfMonth();
        month = date.getMonth();
        year = date.getYear()-1900;
        Log.i("rew", "Back");
        Intent toPassBack = getIntent();
        toPassBack.putExtra("date", getDate(day,month,year));
        setResult(RESULT_OK, toPassBack);
        finish();
    }


    public String getDate(int day, int month, int year){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
        Date d = new Date(year, month, day);
        String strDate = dateFormatter.format(d);
        return strDate;
    }

}

