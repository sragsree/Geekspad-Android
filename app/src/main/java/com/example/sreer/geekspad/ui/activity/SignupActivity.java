package com.example.sreer.geekspad.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.model.User;
import com.example.sreer.geekspad.utils.AppUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.parseLong;


public class SignupActivity extends AppCompatActivity {

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mEmail;
    private EditText mPhone;
    private EditText mPassword;
    private EditText mBirthDay;
    private Spinner mCountry;
    private Spinner mState;
    private EditText mCity;
    private Button mAddSkillButton;
    private TextView mCalendarLink;
    private User user;


    private String country;
    private  String state;
    public ProgressDialog progressDailog;

    private  List<String> states = new ArrayList<String>(){{
        add("Select State(None)");
    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setTitle("Register Here");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirstName = (EditText) findViewById(R.id.fname);
        mLastName = (EditText) findViewById(R.id.lname);
        mPassword = (EditText) findViewById(R.id.input_password);
        mBirthDay = (EditText) findViewById(R.id.input_year);
        mCountry = (Spinner) findViewById(R.id.input_country);
        mState = (Spinner)  findViewById(R.id.input_state);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPhone = (EditText) findViewById(R.id.input_phone);
        mCity  = (EditText) findViewById(R.id.input_city);
        mCalendarLink = (TextView) findViewById(R.id.link_calendar);
        mAddSkillButton = (Button) findViewById(R.id.btn_add_skills);

        progressDailog = new ProgressDialog(this);
        progressDailog.setMessage("Loading");
        progressDailog.getWindow().
                setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDailog.setCancelable(false);



        mAddSkillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = createUser();
                if(user !=null){
                    goToSkillsView(user);
                }
            }
        });
        mCalendarLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBirthday();
            }
        });

        mCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                country = mCountry.getSelectedItem().toString();
                state = null;
                populateStateDetails();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                state = mState.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        populateCountryData();
        populateStateDetails();

    }



    public boolean isValidEmail(){
        String email = mEmail.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern) && email.length() > 0;

    }


    public void goToSkillsView(User user){
       if(!ValidateFields())
           return;

        Intent skillsView = new Intent(this, SkillsActivity.class);
        skillsView.putExtra("user",user);
        skillsView.putExtra("email",user.getEmail());
        skillsView.putExtra("password",mPassword.getText().toString());
        skillsView.putExtra("ForEdit",false);
        startActivity(skillsView);
    }

    public boolean ValidateFields(){
       boolean status  = true;
        if(isEmptyFirstName()){
            mFirstName.setError("FirstName is required!");
            status = false;
        }
        else if(isEmptyLastName()) {
            mLastName.setError("FirstName is required!");
            status = false;
        }
        else if(!isValidEmail()) {
            mEmail.setError("Must enter a valid email");
            status = false;
        }
        else if(!isValidPassword()) {
            mPassword.setError("Must enter password of minimum 6 character");
            status = false;
        }
        else if(!isValidBirthday()) {
            mBirthDay.setError("Must enter valid date");
            status = false;
        }
        else if(!isValidCountry()) {
            TextView errorText = (TextView)mCountry.getSelectedView();
            errorText.setError("Country is Required");
            errorText.setTextColor(Color.RED);
            status = false;
        }
        else if(!isValidState()){
            TextView errorText = (TextView)mState.getSelectedView();
            errorText.setError("State is Required");
            errorText.setTextColor(Color.RED);
            status = false;
        }
        else if(!isValidCity()){
            mCity.setError("Muster a valid city name");
            status = false;
        }

        return status;
    }



    public boolean isValidCity(){
        return mCity.getText().toString().length() > 0 ;
    }

    public boolean isEmptyFirstName(){
        return this.mFirstName.getText().toString().length()==0;
    }
    public boolean isEmptyLastName(){
        return this.mLastName.getText().toString().length()==0;
    }

    public boolean isUserEmailExist(){
        return true;
    }

    public boolean isValidPassword(){
        return mPassword.getText().toString().length() >=6;
    }

    public boolean isValidBirthday(){
        return true;
    }

    public boolean isValidCountry(){
        return (country != null && !country.toLowerCase().contains("select"));
    }

    public boolean isValidState(){
        return (state != null && !state.toLowerCase().contains("select"));
    }


    public void setBirthday(){
        Intent datePicker = new Intent(this, DatePickActivity.class);
        startActivityForResult(datePicker,1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("rew", "Back to Home - " + requestCode+ " " +resultCode);

        if (requestCode == 1 && resultCode == RESULT_OK){
            String birth_day = data.getStringExtra("date");
            mBirthDay.setText(birth_day);
        }
    }


    public void populateCountryData(){
        ArrayAdapter countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, R.layout.spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountry.setAdapter(countryAdapter);

    }

    public void populateStateDetails(){

        String country = mCountry.getSelectedItem().toString();
        states.clear();
        states.add("Select State(None)");
        if(!country.contains("Select"))
        states.addAll(AppUtil.getStates(this,country));
        ArrayAdapter<String> statesAdapter=new ArrayAdapter<String>(this, R.layout.spinner_item, states);
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mState.setAdapter(statesAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public User createUser(){
        if(isEmptyFirstName())
            mFirstName.setError("FirstName is required!");
        else if(isEmptyLastName())
            mLastName.setError("Last Name is required!");
        else if(!isValidEmail())
            mEmail.setError("Must enter a valid email");
        else if(!isValidPassword())
            mPassword.setError("Must enter password of minimum 6 character");
        else if(!isValidBirthday())
            mBirthDay.setError("Must enter valid date");
        else if(!isValidCountry()) {
            TextView errorText = (TextView)mCountry.getSelectedView();
            errorText.setError("Country is Required");
            errorText.setTextColor(Color.RED);
        }
        else if(!isValidState()){
            TextView errorText = (TextView)mState.getSelectedView();
            errorText.setError("State is Required");
            errorText.setTextColor(Color.RED);
        }
        else if(!isValidCity()){
            mCity.setError("Muster a valid city name");
        }
        else {

            mFirstName.setError(null);
            mEmail.setError(null);
            mPassword.setError(null);
            mBirthDay.setError(null);

            user = new User(mFirstName.getText().toString(), mEmail.getText().toString(),
                    mCountry.getSelectedItem().toString(), mState.getSelectedItem().toString());
            if (mLastName.getText().toString().length() > 0)
                user.setLastname(mLastName.getText().toString());

            if (mPhone.getText().toString().length() > 0)
                user.setPhone(mPhone.getText().toString());

            if (mBirthDay.getText().toString().length() > 0)
                user.setBirthDate(mBirthDay.getText().toString());

            if (mCity.getText().toString().length() > 0)
                user.setCity(mCity.getText().toString());

            LatLng location = AppUtil.getUserLocation(this.getApplicationContext(), user.getCity() + ", " + user.getState() + ", " + user.getCountry(), 3);
            user.setLatitude(String.valueOf(location.latitude));
            user.setLongitude(String.valueOf(location.longitude));
        }
        return user;
    }

}
