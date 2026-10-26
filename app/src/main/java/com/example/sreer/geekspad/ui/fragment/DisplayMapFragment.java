package com.example.sreer.geekspad.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.sreer.geekspad.R;
import com.example.sreer.geekspad.db.FireBaseHelper;
import com.example.sreer.geekspad.model.User;
import com.example.sreer.geekspad.ui.activity.ProfileActivity;
import com.example.sreer.geekspad.ui.adapter.CustomInfoWindowAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Long.parseLong;

/**
 * Created by kalirajkalimuthu on 3/18/17.
 */

public class DisplayMapFragment extends Fragment implements OnMapReadyCallback,FireBaseHelper.GetAllUsersInterface {

    private List<User> usersList = new ArrayList<User>();
    private FireBaseHelper fireBaseHelper;
    private LayoutInflater inflater;

    private GoogleMap mMap;
    private MapView mapView;
    private boolean configured;

    public GoogleMap getMap() {
        return mMap;
    }

    public void setMap(GoogleMap mMap) {
        this.mMap = mMap;
    }


    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
       fireBaseHelper = new FireBaseHelper(DisplayMapFragment.this);
        if (!configured) return;
        MapView map = (MapView) view.findViewById(R.id.map);
        mapView = map;
        map.onCreate(bundle);
        map.onResume();
        map.getMapAsync(this);

        getActivity().setTitle("Users Map View");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        configured = !getString(R.string.google_maps_key).isEmpty();
        if (!configured) { android.widget.TextView notice = new android.widget.TextView(requireContext()); notice.setPadding(24,24,24,24); notice.setText("Google Maps needs your own restricted Android API key. Profiles and location filters work in List. See README for configuration; this is not a simulated map."); return notice; }
        View view = inflater.inflate(R.layout.display_map_fragment, container, false);
        this.inflater = inflater;
        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        fireBaseHelper.getAllUsers();
    }

    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause() { if (mapView != null) mapView.onPause(); super.onPause(); }
    @Override public void onDestroyView() { if (mapView != null) { mapView.onDestroy(); mapView = null; } super.onDestroyView(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }

    public void addMarker(User user){
        if (user.getLatitude() == null || user.getLongitude() == null) return;
        LatLng point;
        try { point = new LatLng(parseDouble(user.getLatitude()),parseDouble(user.getLongitude())); } catch (NumberFormatException invalid) { return; }
        MarkerOptions marker = new MarkerOptions() .position(point);
        mMap.addMarker(marker.title(user.getEmail()));
        CameraUpdate newLocation = CameraUpdateFactory.newLatLngZoom(point, 6);
        mMap.moveCamera(newLocation);

    }

    public void markUsers(List<User> users){
        mMap.clear();
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(inflater,users));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                User user = new User();
                user.setEmail(marker.getTitle());
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("email",user.getEmail());
                startActivity(intent);
            }
        });
        for(User user: users){
            addMarker(user);
        }
    }

    public void applyFilter(String country, String state, String skill, String skill_level){
         if (configured && fireBaseHelper != null) fireBaseHelper.getFilteredUsers(country,state,skill,skill_level);
    }

    @Override
    public void onSuccessGetAllUsers(List<User> users) {
       usersList = users;
        markUsers(usersList);
    }

    @Override
    public void onFailureGetAllUsers() {

    }

    @Override
    public void onSuccessGetFilteredUsers(List<User> users) {
        usersList = users;
        markUsers(usersList);
    }

    @Override
    public void onFailureGetFilteredUsers() {

    }
}
