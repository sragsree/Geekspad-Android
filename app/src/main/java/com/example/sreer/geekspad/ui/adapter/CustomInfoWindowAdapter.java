package com.example.sreer.geekspad.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.model.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by sreer on 05-05-2017.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    LayoutInflater inflater = null;
    private TextView mUserName, mUserLocation;
    private List<User> users;

    public CustomInfoWindowAdapter(LayoutInflater inflater, List<User> users) {
        this.inflater = inflater;
        this.users = users;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View v = inflater.inflate(R.layout.user_detail_row, null);
        mUserName = (TextView) v.findViewById(R.id.fullname);
        mUserLocation = (TextView) v.findViewById(R.id.locationdetail);
        if (marker != null) {
            User user = User.findUser(users,marker.getTitle());
            if(user!=null) {
                mUserName.setText(user.getFirstname() + " " + user.getLastname());
                mUserLocation.setText(user.getCountry() + " | " + user.getState() + " | " + user.getCity());
            }
            else{
                mUserName.setText("Details Unavailable");
                mUserLocation.setText("Details Unavailable");
            }
        }
        return (v);
    }
}
