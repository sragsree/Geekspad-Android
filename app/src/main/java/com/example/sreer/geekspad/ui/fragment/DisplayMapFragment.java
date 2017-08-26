package com.example.sreer.geekspad.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
        MapView map = (MapView) view.findViewById(R.id.map);
        map.onCreate(bundle);
        map.onResume();
        map.getMapAsync(this);

        getActivity().setTitle("Users Map View");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.display_map_fragment, container, false);
        this.inflater = inflater;
        return view;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        fireBaseHelper.getAllUsers();

        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
           }
        }

    public void addMarker(User user){
        LatLng point = new LatLng(parseDouble(user.getLatitude()),parseDouble(user.getLongitude()));
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
                intent.putExtra("email",user.cleanEmailAddress());
                startActivity(intent);
            }
        });
        for(User user: users){
            addMarker(user);
        }
    }

    public void applyFilter(String country, String state, String skill, String skill_level){
         fireBaseHelper.getFilteredUsers(country,state,skill,skill_level);
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
