package com.example.socket.geosharing;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.R.id.message;

public class MainActivity extends Activity  {
    
    PendingIntent mGeofencePendingIntent;
    private List<Geofence> mGeofenceList;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = "Activity";
    LocationRequest mLocationRequest;
    double Latitude, Longitude;
    TextView latLongTV;
    EditText editLocation, editMobile, editPerimeter;
    Button location, sms, email;
    ImageView locationDevice;
    GPSTracker gps;
    float perimeter = 10;
    String phone, messege, url = "http://maps.google.com/?q=";
    protected boolean checkIfAlreadyhavePermission(){
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)&&(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)&&(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }
         return false;
        }
    protected void requestForSpecificPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS}, 101);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latLongTV = (TextView) findViewById(R.id.textLatLong);
        location = (Button) findViewById(R.id.buttonLocation);
        sms = (Button) findViewById(R.id.buttonSMS);
        email = (Button) findViewById(R.id.buttonEmail);
        //editEmail = (EditText) findViewById(R.id.editEmail);
        editLocation = (EditText) findViewById(R.id.editLocation);
        editMobile = (EditText) findViewById(R.id.editSMS);
        editPerimeter = (EditText) findViewById(R.id.editPerimeter);
        final SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("phoneNo", null, "sms message", null, null);
        Log.d("perimeterr", editPerimeter.getText().toString());
        //perimeter = Float.parseFloat(editPerimeter.getText().toString());

        locationDevice = (ImageView) findViewById(R.id.myLocation);

        // show location button click event
        locationDevice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new GPSTracker(MainActivity.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                     Latitude = gps.getLatitude();
                     Longitude = gps.getLongitude();

                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + Latitude + "\nLong: " + Longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        });


        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission()) {
                //GOOGLE API CONNECT
            } else {
                requestForSpecificPermission();

            }
        } else {
            //GOOGLE API CONNECT
        }
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = editMobile.getText().toString();
                messege = url + latLongTV.getText().toString();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                //Get the SmsManager instance and call the sendTextMessage method to send message
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phone, null, messege, pi, null);

                Toast.makeText(getApplicationContext(), "Message Sent successfully!",
                        Toast.LENGTH_LONG).show();
                //sendSMSMessage();
            }
        });
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });


        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = editLocation.getText().toString();
                GeocodingLocation locationAddress = new GeocodingLocation();
                locationAddress.getAddressFromLocation(address,
                        getApplicationContext(), new GeocoderHandler());
                Log.d("perimeterr", editPerimeter.getText().toString());
                perimeter = Float.parseFloat(editPerimeter.getText().toString());
                //createGeofences(Latitude, Longitude);
                mGeofenceList = new ArrayList<>();
                int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainActivity.this);
                if (resp == ConnectionResult.SUCCESS) {

                    initGoogleAPIClient();

                    createGeofences(Latitude, Longitude);

                } else {
                    Log.e(TAG, "Your Device doesn't support Google Play Services.");
                }
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(1 * 1000)        // 10 seconds, in milliseconds
                        .setFastestInterval(1 * 1000);
            }
        });
    }



    public void initGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionAddListener)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        mGoogleApiClient.connect();
    }
    private GoogleApiClient.ConnectionCallbacks connectionAddListener =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Log.i(TAG, "onConnected");


                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if (location == null) {
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MainActivity.this);

                    } else {
                        //If everything went fine lets get latitude and longitude
                        Latitude = location.getLatitude();
                        Longitude = location.getLongitude();

                        Log.i(TAG, Latitude + " WORKS " + Longitude);

                        //createGeofences(currentLatitude, currentLongitude);
                        //registerGeofences(mGeofenceList);
                    }

                    try{
                        LocationServices.GeofencingApi.addGeofences(
                                mGoogleApiClient,
                                getGeofencingRequest(),
                                getGeofencePendingIntent()
                        ).setResultCallback(new ResultCallback<Status>() {

                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    Log.i(TAG, "Saving Geofence");

                                } else {
                                    Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                                            " : " + status.getStatusCode());
                                }
                            }
                        });

                    } catch (SecurityException securityException) {
                        // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                        Log.e(TAG, "Error");
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {

                    Log.e(TAG, "onConnectionSuspended");

                }
            };

    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Log.e(TAG, "onConnectionFailed");
                }
            };

    /**
     * Create a Geofence list
     */
    public void createGeofences(double latitude, double longitude) {
        String id = UUID.randomUUID().toString();
        //perimeter = Float.valueOf(editPerimeter.getText().toString());
        Log.d("perimeterr",editPerimeter.getText().toString());
        Geofence fence = new Geofence.Builder()
                    .setRequestId(id)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setCircularRegion(latitude, longitude,Float.parseFloat(editPerimeter.getText().toString()))
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build();
        mGeofenceList.add(fence);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onLocationChanged(Location location) {
        Latitude = location.getLatitude();
        Longitude = location.getLongitude();
        Log.i(TAG, "onLocationChanged");
    }
    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            latLongTV.setText(locationAddress);
            Log.d("Address",latLongTV.getText().toString());
            //String names = "prappo,prince,ahsan";
            String[] namesList = latLongTV.getText().toString().split(",");
            String name1 = namesList[0];
            String name2 = namesList[1];
            Latitude = Double.parseDouble(name1);
            Longitude = Double.parseDouble(name2);
            Toast.makeText(MainActivity.this,latLongTV.getText().toString(),Toast.LENGTH_SHORT).show();
        }
    }
    protected void sendEmail() {
        Log.i("Send email", "");
        String[] TO = {""};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, url+latLongTV.getText().toString());

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //granted
                } else {
                    //not granted
                    return;
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

}
