package com.cwad.easytracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import static android.Manifest.permission.SEND_SMS;

public class StartActivity extends AppCompatActivity {

    String phone;
    int SMS_PERMISSION_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        SharedPreferences settings
                = PreferenceManager.getDefaultSharedPreferences(StartActivity.this);
        final SharedPreferences.Editor settings_editor = settings.edit();

        final EditText pn = findViewById(R.id.phone_number);
        Button save = findViewById(R.id.save_button);
        save.setOnClickListener(v -> {
            phone = pn.getText().toString();
            settings_editor.putString("pn",phone);
            settings_editor.apply();
        });


        if(ContextCompat.checkSelfPermission(
                StartActivity.this,
                SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(StartActivity.this,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_CODE);
        else
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Permission already granted. Awesome!",
                    Snackbar.LENGTH_LONG
            ).show();

        final EditText message = findViewById(R.id.message_field);

        Button send = findViewById(R.id.send_button);
        send.setOnClickListener(view -> {
            SmsManager sms_manager = SmsManager.getDefault();
            sms_manager.sendTextMessage(phone, null, message.getText().toString(),null, null);
            Toast.makeText(getApplicationContext(), "Sent SMS message.", Toast.LENGTH_LONG).show();
        });

        Button mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener((v) -> {
            startActivity(new Intent(this, MapsActivity.class));
        });

    }
}
