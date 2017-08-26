package com.example.sreer.geekspad.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.ui.fragment.ChatUsersListFragment;
import com.example.sreer.geekspad.ui.fragment.DisplayMapFragment;
import com.example.sreer.geekspad.ui.fragment.ProfileViewFragment;
import com.example.sreer.geekspad.ui.fragment.UsersListViewFragment;
import com.example.sreer.geekspad.utils.AppUtil;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class HomePageActivity extends AppCompatActivity{

    private BottomNavigationView mBottomNav;
    private ProfileViewFragment userProfile;
    private UsersListViewFragment usersList;
    private DisplayMapFragment usersMap;
    private ChatUsersListFragment chatView;
    private MenuItem mFilterMenuItem;


    private  View popupView;
    private  Display display;
    private  Spinner mState;
    private  Spinner mCountry;
    private Spinner mSkills;
    private String skill_level;
    private List<String> states = new ArrayList<String>(){
        {
            add("Select State(None)");
        }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.detailFragment);
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        usersList = new UsersListViewFragment();
        fragmentTransaction.replace(R.id.detailFragment, usersList);
        fragmentTransaction.commit();

        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);
        mBottomNav.getMenu().getItem(1).setChecked(true);
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected( MenuItem item) {
               handleMenuSelection(item);
                return true;
            }
        });
    }



    public void handleMenuSelection(MenuItem item){

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.detailFragment);
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        switch (item.getItemId()) {
            case R.id.menu_home:
                if(!(fragment instanceof ProfileViewFragment)) {
                    mFilterMenuItem.setVisible(false);
                    ProfileViewFragment userProfile = new ProfileViewFragment();
                    fragmentTransaction.replace(R.id.detailFragment, userProfile);
                }
                    break;
            case R.id.menu_list:
                if(!(fragment instanceof UsersListViewFragment)) {
                    mFilterMenuItem.setVisible(true);
                    usersList = new UsersListViewFragment();
                    fragmentTransaction.replace(R.id.detailFragment, usersList);
                }
                     break;
            case R.id.menu_map:
                if(!(fragment instanceof DisplayMapFragment)) {
                    mFilterMenuItem.setVisible(true);
                    usersMap = new DisplayMapFragment();
                    fragmentTransaction.replace(R.id.detailFragment, usersMap);
                }
                break;
            case R.id.menu_chat:
                if(!(fragment instanceof ChatUsersListFragment)) {
                    mFilterMenuItem.setVisible(false);
                    chatView = new ChatUsersListFragment();
                    fragmentTransaction.replace(R.id.detailFragment, chatView);
                }
                break;
        }

        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        mFilterMenuItem = menu.findItem(R.id.filter);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.log_out:
                logout();
                break;
            case R.id.filter:
                showPopup();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        logout();

    }

    public void logout(){
        startActivity(new Intent(this, LoginActivity.class)); //Go back to home page
        finish();
        Toast.makeText(this, "Logged Out Successfully" , Toast.LENGTH_LONG).show();
    }

    public void showPopup() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.filter_menu_popup, null);

        display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        final PopupWindow popupWindow = new PopupWindow(popupView, width - 40,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // display the popup in the center
        popupWindow.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.CENTER, 0, 0);
        final Button doneButton = (Button) popupView.findViewById(R.id.add_skills_button);
        final TextView seekbarText = (TextView) popupView.findViewById(R.id.proficiency);
        final SeekBar seekBar = (SeekBar) popupView.findViewById(R.id.input_proficiency);

        doneButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                applyFilter();

                popupWindow.dismiss();
            }
        });


        ImageButton closeButton = (ImageButton) popupView.findViewById(R.id.popup_close);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                skill_level = String.valueOf(progress) + "%";
                seekbarText.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });


        // initialize skills in the spinner
        mSkills = (Spinner) popupView.findViewById(R.id.input_skills);
        ArrayAdapter skillsAdapter = ArrayAdapter.createFromResource(this,
                R.array.skills, R.layout.spinner_item);
        skillsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSkills.setAdapter(skillsAdapter);

        mSkills.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                seekBar.setProgress(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mCountry = (Spinner) popupView.findViewById(R.id.input_country);
        mState = (Spinner) popupView.findViewById(R.id.input_state);

        mCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                populateStateData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        populateCountryData();
        populateStateData();
    }


    public void populateStateData(){
        String country = mCountry.getSelectedItem().toString();
        states.clear();
        states.add("Select State(None)");
        if(!country.contains("Select"))
            states.addAll(AppUtil.getStates(this,country));
        ArrayAdapter<String> statesAdapter=new ArrayAdapter<String>(this, R.layout.spinner_item, states);
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mState.setAdapter(statesAdapter);

    }

    public void populateCountryData(){
        ArrayAdapter countryAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, R.layout.spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCountry.setAdapter(countryAdapter);

    }

    public void applyFilter(){
        String country = mCountry.getSelectedItem().toString();
        String state = mState.getSelectedItem().toString();
        String  skill = mSkills.getSelectedItem().toString();
        country = (country != null && !country.toLowerCase().contains("select")) ? country : null;
        state = (state != null && !state.toLowerCase().contains("select")) ? state :null;
        skill = (skill != null && !skill.toLowerCase().contains("select")) ? skill : null;

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.detailFragment);

        if(fragment instanceof UsersListViewFragment)
            ((UsersListViewFragment)fragment).applyFilter(country,state,skill,skill_level);
        else if (fragment instanceof DisplayMapFragment)
            ((DisplayMapFragment)fragment).applyFilter(country,state,skill,skill_level);
    }
}
