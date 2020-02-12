package com.cwad.easytracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.SEND_SMS;

public class StartActivity extends AppCompatActivity {

    String phone;
    int SMS_PERMISSION_CODE;
    int GET_CONTACTS_CODE;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        SharedPreferences settings
                = getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        final SharedPreferences.Editor settings_editor = settings.edit();

        final EditText pn = findViewById(R.id.phone_number);
        pn.setText(
                settings.getString(
                        "pn",
                        ""
                )
        );

        Button save = findViewById(R.id.save_button);

        save.setOnClickListener(v -> {
            phone = pn.getText().toString();
            settings_editor.putString("pn", phone);
            settings_editor.apply();
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Phone number saved successfully.",
                    Snackbar.LENGTH_SHORT
            ).show();
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
        message.setText(settings.getString("message",""));

        Button message_save = findViewById(R.id.message_save_button);
        message_save.setOnClickListener(view -> {
            settings_editor.putString("message",message.getText().toString());
            settings_editor.apply();
            Toast.makeText(
                    getApplicationContext(),
                    "Saved SMS message.",
                    Toast.LENGTH_LONG
            ).show();
        });

        final Button contact_picker = findViewById(R.id.pick_contact);
        contact_picker.setOnClickListener(v -> {
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StartActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        GET_CONTACTS_CODE);
            }
            else {
                Toast.makeText(
                        getApplicationContext(),
                        "Contact permissions granted.",
                        Toast.LENGTH_SHORT
                ).show();
                Cursor contacts = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                final Map<String, String> contact_info = new HashMap<>();
                while (contacts.moveToNext()) {
                    String name = contacts.getString(
                            contacts.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY
                            )
                    );
                    String phone_number = contacts.getString(
                            contacts.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                    );
                    contact_info.put(name, phone_number);
                }
                for(String k : contact_info.keySet())
                    System.out.println(k + "\t:\t" + contact_info.get(k));
                contacts.close();

                // Spinner ps = new Spinner(getApplicationContext(), Spinner.MODE_DIALOG);


                PopupMenu menu = new PopupMenu(getApplicationContext(), contact_picker);
                for(String k : contact_info.keySet())
                    menu.getMenu().add(k);
                menu.setOnMenuItemClickListener(item -> {
                    pn.setText(contact_info.get(item.getTitle().toString()));
                    System.out.println(contact_info.get(item.getTitle().toString()));
                    return true;
                });
                menu.show();
            }
        });
        Button setTrackerBtn = findViewById(R.id.set_tracker_btn);
        setTrackerBtn.setOnClickListener((v) -> {
            Intent setTracker = new Intent(
                    this,
                    DestinationActivity.class
            );
            setTracker.putExtra("PHONE_NUMBER", phone);
            startActivity(setTracker);
        });
    }
}
