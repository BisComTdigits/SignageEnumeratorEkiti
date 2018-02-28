package com.biscom.signageenumeratorekiti;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;

public class FieldReportingAreaListActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "MAP-RL";
    private static String responseJSON;
    ListView lstStructures;
    TextView txttowntitle;

    Button BtnCancel;
    ImageView imgDelete;
    String getmyuserFK;
    String getmyuserTOKEN;
    Integer TotalValue;
    Integer MAP_StructureZoneFK, MAP_ManifestStatusFK;
    Integer HasStructures=0;
    String MAP_StructureZone;
    private static String neededAreaName;
    private static String neededJsonString;
    Integer sItemPosition;
    private JSONArray resultSet= new JSONArray();;
    private ArrayList<HashMap<String, String>> list;

    ProgressDialog progressDialog;

    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 0; // 10 meters

    double latitude;
    double longitude;
    String User_Fk="0";
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_reporting_area_list);
        lstStructures =(ListView)findViewById(R.id.lstStructures);
        txttowntitle =(TextView)findViewById(R.id.txttowntitle);
        BtnCancel = (Button) findViewById(R.id.btnCancel);
        imgDelete = (ImageView) findViewById(R.id.imgdelete);
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        getmyuserFK = sharedPref.getString("User_Fk","0");
        getmyuserTOKEN = sharedPref.getString("User_Fk","0");
        User_Fk = sharedPref.getString("User_Fk","0");
        MAP_StructureZoneFK = sharedPref.getInt("MAP_StructureZoneFK",0);
        MAP_StructureZone = sharedPref.getString("MAP_StructureZone","0");
        MAP_ManifestStatusFK = sharedPref.getInt("MAP_ManifestStatusFK",0);
        // First we need to check availability of play services
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        //Populate list from Db Here
        try {
        db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
        Cursor c=db.rawQuery("SELECT * FROM BillManifestStorage", null);
        JSONObject returnObj = new JSONObject();
        c.moveToFirst();
        while (c.isAfterLast() == false) {
            int totalColumn = c.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (c.getColumnName(i) != null) {
                    try {
                        if (c.getString(i) != null) {
                            Log.d("TAG_NAME", c.getString(i));
                            rowObject.put(c.getColumnName(i), c.getString(i));
                        } else {
                            rowObject.put(c.getColumnName(i), "");
                        }
                    } catch (Exception e) {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            c.moveToNext();
        }
        c.close();
            SetArrayToListView(resultSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Populate list from Db Here


        lstStructures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                sItemPosition = arg2;
                getneededvalues(sItemPosition);
                final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("SelectedManifestArea", neededAreaName);
                editor.putString("CurrentManifestJSON", neededJsonString);
                editor.commit();
                Intent myintent = new Intent(FieldReportingAreaListActivity.this, FieldManifestListActivity.class);
                startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(FieldReportingAreaListActivity.this).toBundle());
            }
        });

        BtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    finish();
            }


        });
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            showDeleteConfirm();
            }


        });
    }

    private void SetArrayToListView(JSONArray j){
        //Traversing through all the items in the json array
        list=new ArrayList<HashMap<String,String>>();
        TotalValue=0;
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                HashMap<String,String> temp=new HashMap<String, String>();
                temp.put(FIRST_COLUMN, json.getString("BillArea"));
                temp.put(SECOND_COLUMN,json.getString("TotalCount"));
                list.add(temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(getBaseContext(), String.valueOf(list.size())+ " Area(s)", Toast.LENGTH_SHORT).show();
        ListViewAdapters arrayAdapter=new ListViewAdapters(this, list){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);
                if(position %2 == 1)
                {
                    // Set a background color for ListView regular row/item
                    view.setBackgroundColor(Color.parseColor("#EAEDED"));


                }
                else
                {
                    // Set the background color for alternate row/item
                    view.setBackgroundColor(Color.parseColor("#D5DBDB"));
                }
                return view;
            }
        };
        //ListViewAdapters adapter=new ListViewAdapters(this, list);
        lstStructures.setAdapter(arrayAdapter);

        // );
    }

    public void getneededvalues(int position){
        try {
            //Getting object of given index
            JSONObject json =resultSet.getJSONObject(position);
            //Fetching name from that object
            neededAreaName = json.getString("BillArea");
            neededJsonString = json.getString("StoredJSON");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        try{
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }catch(Exception e){

        }

    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        try{
            int resultCode = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                            PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "This device is not supported.", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                return false;
            }
            return true;
        }catch(Exception e){
            return true;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        try{
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }catch(Exception e){

        }

    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        try{
            mGoogleApiClient.connect();
        }catch(Exception e){

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try{

            // Resuming the periodic location updates
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }catch(Exception e){

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            //stopLocationUpdates();
        }catch(Exception e){

        }

    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        try{
            if (!mRequestingLocationUpdates) {
                mRequestingLocationUpdates = true;
                // Starting the location updates
                startLocationUpdates();
                Log.d(TAG, "Periodic location updates started!");
            } else {
                // Changing the button text
                //btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

                //mRequestingLocationUpdates = false;

                // Stopping the location updates
                //stopLocationUpdates();

                //Log.d(TAG, "Periodic location updates stopped!");
            }
        }catch(Exception e){

        }

    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        try{
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
        }catch(Exception e){

        }

    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {
        try{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                //Toast.makeText(LoggedInTakeAShotActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }catch(Exception e){

        }

    }
    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }catch(Exception e){

        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        //displayLocation();
        try {
            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }catch (Exception e){

        }

    }
    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        try{
            mLastLocation = location;

            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                //Toast.makeText(LoggedInTakeAShotActivity.this, "(Cannot Save: No permission on the device)", Toast.LENGTH_LONG).show();
            }else{
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                    JSON_PushNewLocationFromMobile task = new JSON_PushNewLocationFromMobile();
                    task.execute();
                    //lblLocation.setText(latitude + ", " + longitude);
                    //Toast.makeText(LoggedInTakeAShotActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                    Toast.makeText(FieldReportingAreaListActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
                    longitude=0.00;
                    latitude=0.00;
                }


            }

            //Toast.makeText(getApplicationContext(), "Location changed!",
            //Toast.LENGTH_SHORT).show();
        }catch(Exception e){

        }

        // Displaying the new location on UI
        //displayLocation();
    }

    private class JSON_PushNewLocationFromMobile extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS2("JSON_PushNewLocationFromMobile");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.i(TAG, "onPostExecute");
            //progressDialog.cancel();
            JSONObject j = null;
            try {

                if (responseJSON.contains("-FAILED-")){
                    //progressDialog.cancel();
                } else if (responseJSON.contains("-SUCCESSFUL-")){
                    //progressDialog.cancel();
                    //db.execSQL("UPDATE reftable SET istreated='YES' WHERE myrefno='"+exprefno+"'");
                }else{
                    //progressDialog.cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute () {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            //progressDialog = MyCustomProgressDialog.ctor(AfterLoginActivity.this);
            //progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            //progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS2(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("latitude");
        paramPI.setValue(String.valueOf(latitude));
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("longitude");
        paramPI2.setValue(String.valueOf(longitude));
        paramPI2.setType(String.class);
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("User_FK");
        paramPI3.setValue(String.valueOf(User_Fk));
        paramPI3.setType(String.class);
        request.addProperty(paramPI3);


        // Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        // Set output SOAP object
        envelope.setOutputSoapObject(request);
        // Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        try {
            // Invole web service
            androidHttpTransport.call(SOAP_ACTION+methName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to static variable
            responseJSON = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            responseJSON="Nothing Returned";
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void showDeleteConfirm(){
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setCancelable(false);
        builder2.setTitle("Confirmation");
        builder2.setMessage("This action will delete all local manifest area");
        builder2.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
                    db.execSQL("DELETE FROM BillManifestStorage;");
                    Toast.makeText(FieldReportingAreaListActivity.this,"Local Storage Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    Toast.makeText(FieldReportingAreaListActivity.this,"Local Database Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder2.setNegativeButton("No Please", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder2.show();
    }


}
