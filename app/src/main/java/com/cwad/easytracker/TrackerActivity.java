package com.cwad.easytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

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

    private static final int BACKGROUND_LOCATION_REQUEST_CODE = 400;
    private String destinationName, destinationID, phoneNumber;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String apiKey;
    private boolean reached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        destinationName = getIntent().getStringExtra("DESTINATION_NAME");
        destinationID = getIntent().getStringExtra("DESTINATION_ID");
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");
        apiKey = getString(R.string.api_key);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_REQUEST_CODE);
        }


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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == BACKGROUND_LOCATION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i("Permission", "ACCESS_BACKGROUND_LOCATION permission granted");
            } else {
                Toast.makeText(this, "App needs to access background location services to function",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, StartActivity.class));
            }
        }
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
                        // String duration = legs.getJSONObject("duration").getString("text");
                        int distance = legs.getJSONObject("distance").getInt("value");
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

    protected String sendSMS(int distance){
        // int distance = Integer.valueOf(distanceString);
        // write logic for what to do with certain distances and send SMS when
        if( distance < 100 ) {
            SharedPreferences settings = getSharedPreferences("user_settings", Context.MODE_PRIVATE);
            String pn = settings.getString("pn","");
            SmsManager sms_manager = SmsManager.getDefault();
            sms_manager.sendTextMessage(
                    pn,
                    null,
                    "Arrived at destination.",
                    null,
                    null
            );
            Toast.makeText(
                    getApplicationContext(),
                    "Sent SMS message.",
                    Toast.LENGTH_SHORT
            ).show();
            return "Sent SMS";
        }

        return "Nothing";  // return something to display SMS sent on GUI
    }
}
