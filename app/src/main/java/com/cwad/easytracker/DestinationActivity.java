package com.cwad.easytracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class DestinationActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int INTERNET_REQUEST_CODE = 200;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 300;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private String destinationName, destinationID, phoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");

        if (phoneNumber == null) {
            // handle empty phone number

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    INTERNET_REQUEST_CODE);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = location;
                Log.i("Location", currentLocation.toString() + "");
            }
        });

        Places.initialize(this, getString(R.string.api_key));
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        Button destinationBtn = findViewById(R.id.destination_btn);
        destinationBtn.setOnClickListener( (v -> {
            Intent autoCompleteIntent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields).build(this);
            startActivityForResult(autoCompleteIntent, AUTOCOMPLETE_REQUEST_CODE);
        }));

        Button startTrackerBtn = findViewById(R.id.start_tracker_btn);
        startTrackerBtn.setOnClickListener(v -> {
            if (currentLocation != null && destinationID != null) {
                Intent startTrackerIntent = new Intent(this, TrackerActivity.class);
                startTrackerIntent
                        .putExtra("DESTINATION_NAME", destinationName)
                        .putExtra("DESTINATION_ID", destinationID)
                        .putExtra("PHONE_NUMBER", phoneNumber);
                startActivity(startTrackerIntent);
            } else {
                // handle null destination or location
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i("Permission", "ACESS_FINE_LOCATION permission granted");
                } else {
                    Toast.makeText(this, "App needs to access location services to function", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, StartActivity.class));
                }
                break;
            case INTERNET_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i("Permission", "INTERNET permission granted");
                } else {
                    Toast.makeText(this, "App needs to access the internet to function", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, StartActivity.class));
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                destinationID = place.getId();
                destinationName = place.getName();
                TextView destinationText = findViewById(R.id.destination_text);
                destinationText.setText("Destination: " + destinationName);
                if (currentLocation != null){
                    getInformation(destinationID);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("Autocomplete", "An error occured: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("Autocomplete", "Activity cancelled");
            }
        }
    }

    protected void getInformation(String placeID){
        String url = getURL(placeID).toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject legs = response.getJSONArray("routes")
                                .getJSONObject(0)
                                .getJSONArray("legs")
                                .getJSONObject(0);
                        String duration = legs.getJSONObject("duration").getString("text");
                        TextView durationText = findViewById(R.id.duration_text);
                        durationText.setText("Duration: " + duration);
                        String distance = legs.getJSONObject("distance").getString("text");
                        TextView distanceText = findViewById(R.id.distance_text);
                        distanceText.setText("Distance: " + distance);
                        Log.i("JSON", "Distance: " + distance + ", Duration: " + duration);
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
                .appendQueryParameter("key", getString(R.string.api_key))
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


}
