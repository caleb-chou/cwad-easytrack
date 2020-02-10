package com.cwad.easytracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
<<<<<<< HEAD
import android.content.DialogInterface;
=======
import android.content.Intent;
>>>>>>> f6aaff8b307a5356c10450dc186fab58d61e0dd8
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
<<<<<<< HEAD
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
=======
>>>>>>> f6aaff8b307a5356c10450dc186fab58d61e0dd8
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.SEND_SMS;

public class StartActivity extends AppCompatActivity {

    String phone;
    int SMS_PERMISSION_CODE;
    int GET_CONTACTS_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        SharedPreferences settings
                = PreferenceManager.getDefaultSharedPreferences(StartActivity.this);
        final SharedPreferences.Editor settings_editor = settings.edit();

        final EditText pn = findViewById(R.id.phone_number);
        Button save = findViewById(R.id.save_button);
<<<<<<< HEAD
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = pn.getText().toString();
                settings_editor.putString("pn",phone);
                settings_editor.apply();
                Snackbar.make(findViewById(android.R.id.content), "Phone number saved successfully.", Snackbar.LENGTH_SHORT).show();
            }
=======
        save.setOnClickListener(v -> {
            phone = pn.getText().toString();
            settings_editor.putString("pn",phone);
            settings_editor.apply();
>>>>>>> f6aaff8b307a5356c10450dc186fab58d61e0dd8
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
            startActivity(new Intent(this, DestinationActivity.class));
        });

<<<<<<< HEAD
        final Button contact_picker = findViewById(R.id.pick_contact);
        contact_picker.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(StartActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            GET_CONTACTS_CODE);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Contact permissions granted.", Toast.LENGTH_SHORT).show();
                    Cursor contacts = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null, null);
                    final Map<String, String> contact_info = new HashMap<>();
                    while (contacts.moveToNext()) {
                        String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));
                        String phone_number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contact_info.put(name, phone_number);
                    }
                    for(String k : contact_info.keySet())
                        System.out.println(k + "\t:\t" + contact_info.get(k));
                    contacts.close();

                    // Spinner ps = new Spinner(getApplicationContext(), Spinner.MODE_DIALOG);

                    PopupMenu menu = new PopupMenu(getApplicationContext(), contact_picker);
                    for(String k : contact_info.keySet())
                        menu.getMenu().add(k);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            pn.setText(contact_info.get(item.getTitle().toString()));
                            System.out.println(contact_info.get(item.getTitle().toString()));
                            return true;
                        }
                    });
                    menu.show();
                }
            }
        });

=======
>>>>>>> f6aaff8b307a5356c10450dc186fab58d61e0dd8
    }
}
