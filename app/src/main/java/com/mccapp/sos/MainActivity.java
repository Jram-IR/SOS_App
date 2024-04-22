package com.mccapp.sos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> selectedList;
    private ArrayList<String> phoneNumbers = new ArrayList<>();
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private ImageView btnSOS;
    private ListView sosContactsLv;
    private MaterialButton btnAddSOSContacts;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSOS = findViewById(R.id.sosImg);
        btnAddSOSContacts = findViewById(R.id.addSOSContacts);
        sosContactsLv = findViewById(R.id.sosContactsLv);



        btnAddSOSContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddContactsActivity.class);
                startActivity(i);
            }
        });


        selectedList = new ArrayList<>();
        Intent intent = getIntent();
        selectedList = intent.getStringArrayListExtra("SELECTED_LIST");
        Log.d("Selected", selectedList.toString());

        //populate list view
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedList);
        sosContactsLv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        btnSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for location permission
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                } else {
                    // Permission already granted, start getting location
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Permission not granted, request it
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.SEND_SMS},
                                PERMISSION_REQUEST_SEND_SMS);
                    } else {
                        // Permission already granted
                        // You can proceed with sending SMS
                        Toast.makeText(MainActivity.this, "Sending SOS...", Toast.LENGTH_SHORT).show();
                       // getLocation();
                    }

                }
            }
        });




    }

    private void getSelectedPhoneNumbers(String mapUrl){
        for (String entry : selectedList) {
            // Split each entry by '\n' to separate the name and phone number
            String[] parts = entry.split("\n");

            // Extract the phone number (which should be the second part)
            if (parts.length >= 2) {
                String phoneNumber = parts[1];
                // Add the phone number to the new ArrayList
                phoneNumbers.add(phoneNumber);
                sendSOSMessage(mapUrl,phoneNumber);
            } else {
                Log.d("SOS", "some error in phone number retrieval");
            }
        }
        Log.d("SOS Numbers", phoneNumbers.toString());
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a location listener
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // When the location changes, format the latitude and longitude into a Google Maps URL
                String mapUrl = "https://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();

                // Send SMS with SOS alert and map URL
                getSelectedPhoneNumbers(mapUrl);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        };

        // Request location updates
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        }
    }

    private void sendSOSMessage(String mapUrl,String phoneNumber) {

        String message = "SOS Alert! Check my location: " + mapUrl;
        Log.d("SOS", mapUrl);

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            // Send the SMS
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SOS Alert Sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        //sms permission
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // You can proceed with sending SMS
                Log.d("SOS", "permission for sms granted!");
            } else {
                // Permission denied
                // You may inform the user or handle this case accordingly
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



}