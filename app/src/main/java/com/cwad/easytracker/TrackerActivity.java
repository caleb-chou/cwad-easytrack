package com.cwad.easytracker;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class TrackerActivity extends AppCompatActivity {

    private String destinationName, destinationID, phoneNumber;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        destinationName = getIntent().getStringExtra("DESTINATION_NAME");
        destinationID = getIntent().getStringExtra("DESTINATION_ID");
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");
        apiKey = getString(R.string.api_key);
        locationRequest = createLocationRequest();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null){
                    return;
                }
                for (Location location: locationResult.getLocations()){
                    currentLocation = location;
                    updateGUI();
                }
            }
        };
        startLocationUpdates();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(20_000);
        locationRequest.setFastestInterval(10_000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    protected void updateGUI(){
        String url = getURL(destinationID).toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject legs = response.getJSONArray("routes")
                                .getJSONObject(0)
                                .getJSONArray("legs")
                                .getJSONObject(0);
                        String duration = legs.getJSONObject("duration").getString("text");
                        String distance = legs.getJSONObject("distance").getString("text");
                        sendSMS(distance);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // TODO: Handle error
                    Log.e("Error", "Exception: ", error);
                });
        queue.add(jsonObjectRequest);
    }

    protected URL getURL(String placeID){
        String origin = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("maps.googleapis.com")
                .path("maps/api/directions/json")
                .appendQueryParameter("origin", origin)
                .appendQueryParameter("destination", "place_id:" + placeID)
                .appendQueryParameter("key", apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.i("URL", "getURL: " + url.toString());
        return url;
    }

    protected String sendSMS(String distanceString){
        int distance = Integer.valueOf(distanceString);
        // write logic for what to do with certain distances and send SMS when

        return "";  // return something to display SMS sent on GUI
    }
}
