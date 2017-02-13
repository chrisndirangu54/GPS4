package ke.co.alandick.gps4;


import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;


/**
 * Created cisco dennis.
 */

public class GPSTracker extends Service implements LocationListener {

    private final Context mContext;

    //track already visited locations
    ArrayList<Double> latitudesArray = new ArrayList<>();
    ArrayList<Double> longitudesArray = new ArrayList<>();


    MapsActivity mapsActivity = new MapsActivity();


    int count = 0;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    String tmpplat = "";
    String tmpLon = "";


    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GlobalConstants.delay, GlobalConstants.MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        if ( Build.VERSION.SDK_INT >= 23 &&
                                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //// return  ;
                        }
                        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, GlobalConstants.delay, GlobalConstants.MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(location != null)
            Toast.makeText(mContext, location.toString(), Toast.LENGTH_SHORT).show();
        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(GPSTracker.this);
                return  ;
            }
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {


        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        String sendDate = mapsActivity.dateFormater.format(new Date());

       // mapsActivity.deviceId = Settings.Secure.getString(this.getContentResolver(),
             //   Settings.Secure.ANDROID_ID);



        Toast.makeText(mContext, "GPS TRACKER ::: " + location.toString(), Toast.LENGTH_SHORT).show();
        Toast.makeText(mContext, "TRY SEND REQUEST", Toast.LENGTH_SHORT).show();


        Toast.makeText(mContext, "NOW SEND...", Toast.LENGTH_SHORT).show();


        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();

        if(latitudesArray.contains(latitude) && longitudesArray.contains(longitude)){
            Toast.makeText(mContext, ":::DUPLICATE REQUEST:::", Toast.LENGTH_SHORT).show();
            return;
        }

        latitudesArray.add(latitude);
        longitudesArray.add(longitude);

        requestParams.put("latitude", latitude);
        requestParams.put("longitude", longitude);
        requestParams.put("driverCarId", "AF20002A-26CC-44EA-A418-4FBC637AB702");
        requestParams.put("sendDate", sendDate);
        requestParams.put("factoryId", "E265CF3A-1288-4516-9E54-572A8F1E6FC4");
        requestParams.put("deviceId", MapsActivity.deviceId);

        Toast.makeText(mContext, "device id:::" + MapsActivity.deviceId, Toast.LENGTH_SHORT).show();

        Log.i("MAP", "***********************************************onSuccess: " + "DENNIS1");


        Toast.makeText(mContext, "Sending request number ::::" + mapsActivity.sendCount, Toast.LENGTH_SHORT).show();
        mapsActivity.sendCount ++;
        asyncHttpClient.get(GlobalConstants.endpoint, requestParams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("MAP", "***********************************************onSuccess: " + "DENNIS2");
                String resString = new String(responseBody);
                //Toast.makeText(context, "Response:::" + resString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String resString = new String(responseBody);
                //Toast.makeText(context, "Response:::" + resString, Toast.LENGTH_SHORT).show();
                Log.i("MAP", "***********************************************onSuccess: " + "DENNIS3");
            }
        });


    }


    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    public void SendGPSLocation(String latitude, String longitude, String sendDate) {
        //http://303c4d28.ngrok.io/

        if(latitudesArray.contains(latitude) && longitudesArray.contains(longitude)){
            Toast.makeText(mContext, ":::DUPLICATE REQUEST:::", Toast.LENGTH_SHORT).show();
        }
        //latitudesArray.add(latitude);
        //longitudesArray.add(longitude);
        Toast.makeText(mContext, "REQUEST ::: # " + count, Toast.LENGTH_SHORT).show();
        count++;


                }



    }
