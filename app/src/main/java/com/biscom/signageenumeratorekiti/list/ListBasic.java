package com.biscom.signageenumeratorekiti.list;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.biscom.signageenumeratorekiti.EnforcementByAreaListActivity;
import com.biscom.signageenumeratorekiti.MyCustomProgressDialog;
import com.biscom.signageenumeratorekiti.R;
import com.biscom.signageenumeratorekiti.adapter.AdapterListBasic;
//import com.material.components.data.DataGenerator;
import com.biscom.signageenumeratorekiti.model.EnforcementReport;
import com.biscom.signageenumeratorekiti.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ListBasic extends AppCompatActivity {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/WebServiceKWASAA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "MAP-RL";
    private static String responseJSON;
    private static String neededArea_FK;
    private static String neededArea;
    private View parent_view;
    private RecyclerView recyclerView;
    private ImageView imglist;
    private AdapterListBasic mAdapter;
    ProgressDialog progressDialog;
    private JSONArray resultSet= new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic);
        parent_view = findViewById(android.R.id.content);


        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        neededArea_FK = sharedPref.getString("neededArea_FK","0");
        neededArea = sharedPref.getString("neededArea","0");
        initToolbar();
        JSON_MNST_GetEnforcementReportPerAreaBy_Area_FK task = new JSON_MNST_GetEnforcementReportPerAreaBy_Area_FK();
        task.execute();
//        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(neededArea);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(JSONArray j) {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        List<EnforcementReport> items = getData(this,j);
        items.addAll(getData(this,j));
        items.addAll(getData(this,j));

        //set data and list adapter
        mAdapter = new AdapterListBasic(this, items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListBasic.OnItemClickListener() {
            @Override
            public void onItemClick(View view, EnforcementReport obj, int position) {
                Snackbar.make(parent_view, obj.ManifestStatus, Snackbar.LENGTH_SHORT).show();
                showCustomDialog(obj.image,obj.BusinessName);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public static List<EnforcementReport> getData(Context ctx, JSONArray j) {
        List<EnforcementReport> items = new ArrayList<>();
        for(int i=0;i<j.length();i++){
            try {
                EnforcementReport obj = new EnforcementReport();
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                obj.image = json.getString("ImageName");
                obj.BusinessName = json.getString("BusinessName");
                obj.ManifestStatus = json.getString("ManifestStatus")+"\n"+json.getString("Comment")+"\n"+json.getString("DisplayName")+"\n"+json.getString("ValueDate")+" "+json.getString("ValueTime");
                items.add(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        Collections.shuffle(items);
        return items;
    }


    private class JSON_MNST_GetEnforcementReportPerAreaBy_Area_FK extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS2("JSON_MNST_GetEnforcementReportPerAreaBy_Area_FK");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.cancel();
            Log.i(TAG, "onPostExecute");
            JSONObject j = null;
            try {
                if (responseJSON.contains("ERROR")){
                }else{
                    //Get list here
                    try {
                        //Parsing the fetched Json String to JSON Object
                        j = new JSONObject(responseJSON);
                        resultSet = j.getJSONArray("myJresult");

                        if (resultSet.length()<=0){
                            AlertDialog.Builder dialog = new AlertDialog.Builder(ListBasic.this);
                            dialog.setTitle( "Report" )
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setMessage("No Record to Display")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialoginterface, int i) {
                                        }
                                    }).show();
                        }else{
                            initComponent(resultSet);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

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
            progressDialog = MyCustomProgressDialog.ctor(ListBasic.this);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS2(String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = df.format(c.getTime());

        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        paramPI.setName("StartDate");
        paramPI.setValue(String.valueOf(formattedDate));
        paramPI.setType(String.class);
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        paramPI2.setName("EndDate");
        paramPI2.setValue(String.valueOf(formattedDate));
        paramPI2.setType(String.class);
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        paramPI3.setName("Area_FK");
        paramPI3.setValue(Integer.valueOf(neededArea_FK));
        paramPI3.setType(Integer.class);
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

    private void showCustomDialog(String imgRUL,String BusinessName) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_enforcement_image);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final ImageView imgpreview = (ImageView) dialog.findViewById(R.id.imgpreview);
        final TextView txt_hdtitle = (TextView) dialog.findViewById(R.id.txt_hdtitle);
        displayImageOriginal(this,imgpreview,imgRUL);
        txt_hdtitle.setText(BusinessName);

        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public static void displayImageOriginal(Context ctx, ImageView img, String url) {
        try {
            Glide.with(ctx).load(url)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.android_loading) // any placeholder to load at start
                    .error(R.mipmap.ic_launcher)  // any image in case of error
                    .into(img);
        } catch (Exception e) {
        }
    }
}