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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FieldManifestListActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private static String responseJSON;
    ListView lstStructures;
    ListViewAdapters arrayAdapter;
    TextView txttowntitle;
    TextView txtvalueonlist;
    // Search EditText
    EditText inputSearch;
    TextView title;
    Button BtnCancel;
    ImageView imgDelete;
    String getmyuserFK;
    String getmyuserTOKEN;
    Integer TotalValue;
    Integer MAP_StructureZoneFK, MAP_ManifestStatusFK;
    Integer HasStructures = 0;
    String MAP_StructureZone;
    Integer sItemPosition;
    private JSONArray resultSet;
    private ArrayList<HashMap<String, String>> list;
    private View parent_view;
    ProgressDialog progressDialog;
    public static final String FIRST_COLUMN = "First";
    public static final String SECOND_COLUMN = "Second";

    // Location updates intervals in sec
    private static String neededAreaName;
    private static String neededJsonString;
    private static String neededBusinessName;
    private static String neededManifestStatus_FK;
    private static String neededManifestStatusComment;

    // LogCat tag
    private static final String TAG = "LoggedIn";
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

    private static String neededStructureID;


    Button btntakephoto, btnsave;
    Button btnback;
    //ImageView ivdisplayphoto;
    SeekBar sbSeekBar;
    TextView photoname;

    private ColorMatrix colorMatrix;
    private ColorMatrixColorFilter filter;
    private Paint paint;
    private Canvas cv;

    int n = 10000;
    String nn="";
    String User_Fk="0";

    private File photofile;
    private int TAKENPHOTO = 0;
    Bitmap photo, canvasBitmap;
    double latitude;
    double longitude;
    SQLiteDatabase db;
    String fotoname;

    String expStructureID;
    String expManifestStatus_FK;
    String expManifestComment;
    String expUser_FK;
    String expBusinessName;
    String expPhotoname;
    String[] reflistc1;
    String[] reflistc2;
    String[] reflistc3;
    String[] reflistc4;
    String[] reflistc5;
    String[] reflistc6;
    int reflistcounter=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_manifest_list);
        parent_view = findViewById(android.R.id.content);
        lstStructures = (ListView) findViewById(R.id.lstStructures);
        title = (TextView) findViewById(R.id.title);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        BtnCancel = (Button) findViewById(R.id.btnCancel);
        imgDelete = (ImageView) findViewById(R.id.imgdelete);
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        getmyuserFK = sharedPref.getString("User_Fk", "0");
        getmyuserTOKEN = sharedPref.getString("User_Fk", "0");
        User_Fk = sharedPref.getString("User_Fk", "0");
        MAP_StructureZoneFK = sharedPref.getInt("MAP_StructureZoneFK", 0);
        MAP_StructureZone = sharedPref.getString("MAP_StructureZone", "0");
        MAP_ManifestStatusFK = sharedPref.getInt("MAP_ManifestStatusFK", 0);
        neededAreaName = sharedPref.getString("SelectedManifestArea", "");
        title.setText(neededAreaName.toString() + " Area");
        //ivdisplayphoto = (ImageView)findViewById(R.id.iv_displayphotonew);
       // ivdisplayphoto.setVisibility(View.GONE);

        neededJsonString = sharedPref.getString("CurrentManifestJSON", "");
        colorMatrix = new ColorMatrix();
        filter = new ColorMatrixColorFilter(this.colorMatrix);
        paint = new Paint();
        paint.setColorFilter(filter);
        // First we need to check availability of play services
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        //Populate list from Db Here
        JSONObject j = null;
        try {
            //Parsing the fetched Json String to JSON Object
            j = new JSONObject(neededJsonString);
            //Storing the Array of JSON String to our JSON Array
            resultSet = j.getJSONArray("myJresult");
            SetArrayToListView(resultSet);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Populate list from Db Here

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                arrayAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
        lstStructures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                //Gettint the position from the view because the list can be filetered
                txtvalueonlist =(TextView) arg1.findViewById(R.id.value);
                sItemPosition=Integer.valueOf(txtvalueonlist.getText().toString());
                getneededvalues(sItemPosition-1);
                if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //What todo if there is no permission
                    Toast.makeText(FieldManifestListActivity.this, "Cannot Capture: No Complete permission on the device)", Toast.LENGTH_LONG).show();
                } else {
//                    try{
//                        ivdisplayphoto.destroyDrawingCache();
//                    }catch (Exception e) {
//
//                    e.printStackTrace();
//                    }
                    togglePeriodicLocationUpdates();
                    Random gen = new Random();
                    n = 9999;
                    n = gen.nextInt(n);
                    String pattern="0000";
                    DecimalFormat myFormatter = new DecimalFormat(pattern);
                    nn=myFormatter.format(n);

                    File photostorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    Calendar c = Calendar.getInstance();
                    System.out.println("Current time => " + c.getTime());
                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                    String formattedDate = df.format(c.getTime());
                    String root = Environment.getExternalStorageDirectory().toString();
                    File newDir = new File(photostorage + "/ENFORCEMENT_Images");
                    newDir.mkdirs();
                    fotoname =neededStructureID.replace("/","-")+"_"+User_Fk +""+ nn+"_"+(System.currentTimeMillis())+"_EK.jpg";
                    File file = new File (newDir, fotoname);
                    //photofile = new File(photostorage, "/FIELD_Images/"+fotoname.toString() + ".jpg");
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //intent to start camera
                    i.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".provider", file));
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(i, TAKENPHOTO);
                }

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

    private void SetArrayToListView(JSONArray j) {
        //Traversing through all the items in the json array
        list = new ArrayList<HashMap<String, String>>();
        TotalValue = 0;
        for (int i = 0; i < j.length(); i++) {
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                HashMap<String, String> temp = new HashMap<String, String>();
                temp.put(FIRST_COLUMN, json.getString("BusinessName") + "\n[" + json.getString("StructureID") + "]");
                temp.put(SECOND_COLUMN, String.valueOf(i + 1));
                list.add(temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(getBaseContext(), String.valueOf(list.size()) + " Structures(s)", Toast.LENGTH_SHORT).show();
        arrayAdapter = new ListViewAdapters(this, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the current item from ListView
                View view = super.getView(position, convertView, parent);
                if (position % 2 == 1) {
                    // Set a background color for ListView regular row/item
                    view.setBackgroundColor(Color.parseColor("#EAEDED"));


                } else {
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

    public void getneededvalues(int position) {
        try {
            //Getting object of given index
            JSONObject json = resultSet.getJSONObject(position);
            //Fetching name from that object
            neededStructureID = json.getString("StructureID");
            neededBusinessName = json.getString("BusinessName");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        } catch (Exception e) {

        }

    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        try {
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
        } catch (Exception e) {
            return true;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        } catch (Exception e) {

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
        try {
            mGoogleApiClient.connect();
        } catch (Exception e) {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Resuming the periodic location updates
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        } catch (Exception e) {

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            //stopLocationUpdates();
        } catch (Exception e) {

        }

    }

    /**
     * Method to toggle periodic location updates
     */
    private void togglePeriodicLocationUpdates() {
        try {
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
        } catch (Exception e) {

        }

    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        try {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
        } catch (Exception e) {

        }

    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                //Toast.makeText(LoggedInTakeAShotActivity.this, "(No permission on the device)", Toast.LENGTH_LONG).show();
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (Exception e) {

        }

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        } catch (Exception e) {

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
        } catch (Exception e) {

        }

    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        try {
            mLastLocation = location;

            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //What todo if there is no permission
                //Toast.makeText(LoggedInTakeAShotActivity.this, "(Cannot Save: No permission on the device)", Toast.LENGTH_LONG).show();
            } else {
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
                    Toast.makeText(FieldManifestListActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
                    longitude = 0.00;
                    latitude = 0.00;
                }


            }

            //Toast.makeText(getApplicationContext(), "Location changed!",
            //Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

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

                if (responseJSON.contains("-FAILED-")) {
                    //progressDialog.cancel();
                } else if (responseJSON.contains("-SUCCESSFUL-")) {
                    //progressDialog.cancel();
                    //db.execSQL("UPDATE reftable SET istreated='YES' WHERE myrefno='"+exprefno+"'");
                } else {
                    //progressDialog.cancel();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
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
            androidHttpTransport.call(SOAP_ACTION + methName, envelope);
            // Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            // Assign it to static variable
            responseJSON = response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            responseJSON = "Nothing Returned";
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if (requestCode == TAKENPHOTO) {
                if (resultCode == Activity.RESULT_OK) {
                    //Log.d(TAG + ".PICK_CAMERA_IMAGE", "Selected image uri path :" + Uri.fromFile(photofile));

                    //ivdisplayphoto.setImageURI(Uri.fromFile(photofile));
                    //photoname.setText("Click 'Save Image' button");
                    //Start Saving
                    //ivdisplayphoto.setDrawingCacheEnabled(true);
                    //Bitmap bitmap = ivdisplayphoto.getDrawingCache();
//                    try
//                    {
//                        ivdisplayphoto.setImageBitmap(bitmap);
//                    }catch (Exception e) {
//
//                    }


                    if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //What todo if there is no permission
                        Toast.makeText(FieldManifestListActivity.this, "(Cannot Save: No permission on the device)", Toast.LENGTH_LONG).show();
                    }else{
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (mLastLocation != null) {
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();
                            //lblLocation.setText(latitude + ", " + longitude);
                            //Toast.makeText(EnforcementTakeAShotActivity.this, latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                            //must include userFk in front to make it unique
                            //String fotoname =neededStructureID.replace("/","-")+"_"+User_Fk +""+ nn+"_"+String.valueOf(latitude)+"_"+String.valueOf(longitude)+"_EK.jpg";
                            //photoname.setText("ID: "+User_Fk+""+nn+"\nPlease Note the ID");
                            //File file = new File (newDir, fotoname);
                            //if (file.exists ()) file.delete ();
                            try {
                                Toast.makeText(getApplicationContext(), "Saving Image.....", Toast.LENGTH_SHORT ).show();
                                Toast.makeText(getApplicationContext(), "Image Record Saved!", Toast.LENGTH_SHORT ).show();
//                                FileOutputStream out = new FileOutputStream(file);
//                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//                                out.flush();
//                                out.close();

                                showSingleChoiceDialog();


                                //Save RefNo to SQLITE Here

//                                try {
//                                    db.execSQL("INSERT INTO BillManifestUpdate VALUES(StructureID VARCHAR,BusinessName VARCHAR,User_FK VARCHAR,ManifestStatus_FK VARCHAR,Comment VARCHAR,IsPushed VARCHAR);");
//                                } catch (Exception e) {
//
//                                }
                                //END Save RefNo to SQLITE Here

                                //btnsave.setEnabled(false);
                                //btntakephoto.setText("Open Camera");
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT ).show();
                            }

                        } else {
                            //lblLocation.setText("(Couldn't get the location. Make sure location is enabled on the device)");
                            Toast.makeText(FieldManifestListActivity.this, "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
                            longitude=0.00;
                            latitude=0.00;
                        }


                    }

                    //End saving
                }else{
                    //No Picture Taken
                    Toast.makeText(this, "No Picture Taken", Toast.LENGTH_LONG).show();
                }
            }else {
                //No Photo was taken
                Toast.makeText(this, "No Picture Taken", Toast.LENGTH_LONG).show();
            }
        }catch(Exception e){
            Toast.makeText(this, "Oops,can't get the photo from your gallery, Try Again", Toast.LENGTH_LONG).show();
        }
    }
    private String single_choice_selected;
    private String single_choice_selected2;
    private static final String[] MSTATUS = new String[]{
            "Bill Accepted", "Bill Rejected", "Structure not Available", "Bill Already Paid", "Reminder was Accepted" , "I Enforced Sticker", "I Did Final Enforcement"
    };
    private static final String[] MComments_Accepted = new String[]{
            "Everything Went Smooth", "Special Comment on Paper"
    };
    private static final String[] MComments_Rejected = new String[]{
            "Bill rejected without reason", "Client say they already have the bill", "Client Name Spelt Wrongly", "Client want Consolidated Bill" , "Structure Belongs to Government" , "Special Comment on Paper"
    };
    private static final String[] MComments_NotAvalaible = new String[]{
            "Structure Removed (I take pics)", "Structure Closed Down(I take pics)", "Owner not Seen(White Sticker pasted)", "Structure Relocated" , "Special Comment on Paper"
    };
    private static final String[] MComments_AlreadyPaid = new String[]{
            "Paid to State with evidence (Pics)", "Paid to US with evidence", "Just a claim No Evidence", "Special Comment on Paper"
    };
    private static final String[] MComments_Reminder = new String[]{
            "Everything Went Smooth", "Special Comment on Paper"
    };
    private static final String[] MComments_EnforceSticker = new String[]{
            "Sticker was pasted", "Soft Enforcement Because of Plead", "Special Comment on Paper"
    };
    private static final String[] MComments_FinalEnforce = new String[]{
            "We removed the structure", "Client Pleaded and we did Soft Enforcement", "Special Comment on Paper"
    };
    private void showSingleChoiceDialog() {
        single_choice_selected = MSTATUS[0];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(neededBusinessName.toString()+"\nWhat Happened here?");
        builder.setSingleChoiceItems(MSTATUS, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                single_choice_selected = MSTATUS[i];

            }
        });
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Snackbar.make(parent_view, "selected : " + single_choice_selected, Snackbar.LENGTH_SHORT).show();
                if (single_choice_selected=="Bill Accepted"){
                    neededManifestStatus_FK="1";
                    showSingleChoiceDialog2(MComments_Accepted,single_choice_selected);
                }else if (single_choice_selected=="Bill Rejected"){
                    neededManifestStatus_FK="2";
                    showSingleChoiceDialog2(MComments_Rejected,single_choice_selected);
                }
                else if (single_choice_selected=="Structure not Available"){
                    neededManifestStatus_FK="3";
                    showSingleChoiceDialog2(MComments_NotAvalaible,single_choice_selected);
                }
                else if (single_choice_selected=="Bill Already Paid"){
                    neededManifestStatus_FK="4";
                    showSingleChoiceDialog2(MComments_AlreadyPaid,single_choice_selected);
                }
                else if (single_choice_selected=="Reminder was Accepted"){
                    neededManifestStatus_FK="5";
                    showSingleChoiceDialog2(MComments_Reminder,single_choice_selected);
                }
                else if (single_choice_selected=="I Enforced Sticker"){
                    neededManifestStatus_FK="6";
                    showSingleChoiceDialog2(MComments_EnforceSticker,single_choice_selected);
                }
                else if (single_choice_selected=="I Did Final Enforcement"){
                    neededManifestStatus_FK="7";
                    showSingleChoiceDialog2(MComments_FinalEnforce,single_choice_selected);
                }


            }
        });
        //builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

public void showalert(){
    AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
    builder2.setCancelable(false);
    builder2.setTitle("Submitted!");
    builder2.setMessage("Update for\n"+neededBusinessName+ "\nSubmited");
    builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

        }
    });
    builder2.show();
}
    private void showSingleChoiceDialog2(final String[] CommentSelector, String laststatus) {
        single_choice_selected2 = CommentSelector[0];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Any Comments?");
        builder.setSingleChoiceItems(CommentSelector, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                single_choice_selected2 = CommentSelector[i];
                neededManifestStatusComment=single_choice_selected2.toString();
            }
        });
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Save Comments to Local DBsa
                //single_choice_selected2 = CommentSelector[i];
                neededManifestStatusComment=single_choice_selected2.toString();
                Log.e("StatusComment", neededManifestStatusComment);
                try {
                    db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
                    db.execSQL("INSERT INTO BillManifestUpdate VALUES('"+neededStructureID+"','"+neededBusinessName+"','"+User_Fk.toString()+"','"+neededManifestStatus_FK+"','"+neededManifestStatusComment+"','"+fotoname.toString()+"','0');");
                    Log.e("New Record Inserted", fotoname);
                } catch (Exception e) {
                    Log.e("Insertion Error", fotoname+e.toString());
                }
                //Attempt to contact server in background here
                //pushRecordOnlineBackground();
                //The Background service will push online for us

                //Snackbar.make(parent_view, "Submitted!", Snackbar.LENGTH_SHORT).show();
                //ivdisplayphoto.setVisibility(View.INVISIBLE);
                showalert();
            }
        });
        //builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    private void pushRecordOnlineBackground() {
        Log.e(TAG, "Attempting to Push record in background");
        try {
            db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
            Cursor c=db.rawQuery("SELECT * FROM BillManifestUpdate WHERE IsPushed='0'", null);
            Log.e("BillManifestUpdateCount", String.valueOf(c.getCount()));
            if(c.getCount()==0)
            {
                return;
            }
            reflistc1 = new String[c.getCount()];
            reflistc2 = new String[c.getCount()];
            reflistc3 = new String[c.getCount()];
            reflistc4 = new String[c.getCount()];
            reflistc5 = new String[c.getCount()];
            reflistc6 = new String[c.getCount()];
            reflistcounter=0;
            while(c.moveToNext())
            {
                expStructureID=c.getString(0);
                expBusinessName=c.getString(1);
                expUser_FK=c.getString(2);
                expManifestStatus_FK=c.getString(3);
                expManifestComment=c.getString(4);
                expPhotoname=c.getString(5);
                reflistc1[reflistcounter]=expStructureID;
                reflistc2[reflistcounter]=expBusinessName;
                reflistc3[reflistcounter]=expUser_FK;
                reflistc4[reflistcounter]=expManifestStatus_FK;
                reflistc5[reflistcounter]=expManifestComment;
                reflistc6[reflistcounter]=expPhotoname;
                reflistcounter = reflistcounter+1;
            }
            JSON_ReceiveManifestUpdateFromMobile task = new JSON_ReceiveManifestUpdateFromMobile();
            task.execute();
            Log.e(TAG, "Running Push record in background");
        } catch (Exception e) {
//            Toast.makeText(AfterLoginActivity.this, "You Have to Capture First",
//                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error while Attempting to Push record in background");
        }
    }

private class JSON_ReceiveManifestUpdateFromMobile extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        expStructureID=reflistc1[reflistcounter-1];
        expBusinessName=reflistc2[reflistcounter-1];
        expUser_FK=reflistc3[reflistcounter-1];
        expManifestStatus_FK=reflistc4[reflistcounter-1];
        expManifestComment=reflistc5[reflistcounter-1];
        expPhotoname=reflistc6[reflistcounter-1];
        invokeJSONWS4("JSON_ReceiveManifestUpdateFromMobile");
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
                db.execSQL("UPDATE BillManifestUpdate SET IsPushed='1' WHERE StructureID='"+expStructureID+"' AND Fotoname='"+expPhotoname+"'");
            }else{
                //progressDialog.cancel();
            }
            } catch (Exception e) {
                e.printStackTrace();
            }
        reflistcounter=reflistcounter-1;
        if (reflistcounter>0){
            JSON_ReceiveManifestUpdateFromMobile task = new JSON_ReceiveManifestUpdateFromMobile();
            task.execute();
        }
        if (reflistcounter<=0){

//            AlertDialog.Builder dialog = new AlertDialog.Builder(AfterLoginActivity.this);
//            dialog.setTitle( "Export Finished" )
//                    .setIcon(R.mipmap.ic_launcher)
//                    .setMessage("Done with the Export")
//                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialoginterface, int i) {
//                        }
//                    }).show();
//            progressDialog.cancel();
        }


    }

    @Override
    protected void onPreExecute () {
        Log.i(TAG, "onPreExecute");
        //Display progress bar
        //pg.setVisibility(View.VISIBLE);
//        progressDialog = MyCustomProgressDialog.ctor(AfterLoginActivity.this);
//        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        Log.i(TAG, "onProgressUpdate");
    }

}
    public void invokeJSONWS4(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("StructureID");
        paramPI.setValue(expStructureID.toString());
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("ManifestStatus_FK");
        paramPI2.setValue(Integer.valueOf(expManifestStatus_FK.toString()));
        paramPI2.setType(Integer.class);
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("Comment");
        paramPI3.setValue(expManifestComment.toString());
        paramPI3.setType(String.class);
        request.addProperty(paramPI3);

        PropertyInfo paramPI4 = new PropertyInfo();
        paramPI4.setName("BusinessName");
        paramPI4.setValue(expBusinessName.toString());
        paramPI4.setType(String.class);
        request.addProperty(paramPI4);

        PropertyInfo paramPI5 = new PropertyInfo();
        paramPI5.setName("User_FK");
        paramPI5.setValue(Integer.valueOf(expUser_FK));
        paramPI5.setType(Integer.class);
        request.addProperty(paramPI5);

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File newDir = new File(root + "/ENFORCEMENT_Images");
        File file = new File (newDir, expPhotoname.toString());
        String encodedImage="";
        encodedImage=convertToBase64(file.toString());
        PropertyInfo paramPI6 = new PropertyInfo();
        paramPI6.setName("ImageBase64");
        paramPI6.setValue(encodedImage.toString());
        paramPI6.setType(String.class);
        request.addProperty(paramPI6);

        PropertyInfo paramPI7 = new PropertyInfo();
        paramPI7.setName("PictureRef");
        paramPI7.setValue(expPhotoname);
        paramPI7.setType(String.class);
        request.addProperty(paramPI7);

        PropertyInfo paramPI8 = new PropertyInfo();
        paramPI8.setName("Latitude");
        paramPI8.setValue(String.valueOf(latitude));
        paramPI8.setType(String.class);
        request.addProperty(paramPI8);

        PropertyInfo paramPI9 = new PropertyInfo();
        paramPI9.setName("Longitude");
        paramPI9.setValue(String.valueOf(longitude));
        paramPI9.setType(String.class);
        request.addProperty(paramPI9);

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

    private String convertToBase64(String imagePath)

    {
        Log.e("Image",imagePath);
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArrayImage = baos.toByteArray();
        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        return encodedImage;
    }

    public void showDeleteConfirm(){
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setCancelable(false);
        builder2.setTitle("Confirmation");
        builder2.setMessage("This action will delete "+neededAreaName+" manifest area");
        builder2.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    db=openOrCreateDatabase("MobileStructureDB", Context.MODE_PRIVATE, null);
                    db.execSQL("DELETE FROM BillManifestStorage WHERE BillArea='"+neededAreaName+"';");
                    Toast.makeText(FieldManifestListActivity.this,neededAreaName+" Manifest Area Deleted", Toast.LENGTH_SHORT).show();
                    Intent myintent = new Intent(FieldManifestListActivity.this, MenuActivity.class);
                    startActivity(myintent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(FieldManifestListActivity.this,"Local Database Error", Toast.LENGTH_SHORT).show();
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
