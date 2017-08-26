package com.example.sreer.geekspad.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kalirajkalimuthu on 4/30/17.
 */

public class AppUtil {
    public static List<String> getStates(Context context, String country) {
        List<String> states = new ArrayList<>();
        if (country != null)
            try {
                InputStream countriesFile = context.getAssets().open(country);
                BufferedReader in = new BufferedReader(new InputStreamReader(countriesFile));
                String state = null;
                while ((state = in.readLine()) != null)
                    states.add(state);
            } catch (IOException e) {
                Log.e("Error", "Invalid Country", e);
            }

        return states;
    }


    public static  LatLng getUserLocation(Context context, String address, int args) {

        LatLng location = new LatLng(0, 0);
        Double latitude = null, longitude = null;

        Geocoder locator = new Geocoder(context);
        try {
            List<Address> positions =
                    locator.getFromLocationName(address, args);
            if (positions.size() > 0) {
                Address position = positions.get(0);
                if (position.hasLatitude())
                    latitude = position.getLatitude();
                if (position.hasLongitude())
                    longitude = position.getLongitude();
                location = new LatLng(latitude, longitude);
            }

        } catch (Exception error) {
            Log.e("rew", "Address lookup Error", error);
        }
        return location;
    }
}