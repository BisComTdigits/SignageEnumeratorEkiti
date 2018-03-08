package com.biscom.signageenumeratorekiti;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.biscom.signageenumeratorekiti.list.ListBasic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MenuActivity extends AppCompatActivity {
    private final String NAMESPACE = "http://tempuri.org/";
    private final String URL = "http://eksaa.biscomtdigits.com/MobileAppWSTypeTeamA.asmx";
    private final String SOAP_ACTION = "http://tempuri.org/";
    private String TAG = "OS-MENU";
    private static String responseJSON;
    private static String IsExec;
    ListView lstmenupage;
    String getmyuserFK;
    String getmyuserTOKEN;
    String shareduser;
    private static String neededDesc;
    private static String neededDescription;
    Integer sItemPosition;
    TextView txtactivitytitle;
    private JSONArray arrayresultformenuitems;
    private ArrayList<String> menuitemsarraylist;
    private ArrayList<HashMap<String, String>> list;

    ProgressBar pg;

    public static final String FIRST_COLUMN="First";
    public static final String SECOND_COLUMN="Second";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        lstmenupage =(ListView)findViewById(R.id.lstmenupage);

        menuitemsarraylist = new ArrayList<String>();
        final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        getmyuserFK = sharedPref.getString("User_Fk","0");
        getmyuserTOKEN = sharedPref.getString("User_Fk","0");
        IsExec = sharedPref.getString("IsExec","0");
        shareduser = sharedPref.getString("PREUSERNAME","");
        pg = (ProgressBar) findViewById(R.id.progressBar1);
        txtactivitytitle = (TextView) findViewById(R.id.textView2);
        JSON_Get_ProjectKeyFacts task = new JSON_Get_ProjectKeyFacts();
        task.execute();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        txtactivitytitle.setText("\n\nSIGNAGE ACTIVITIES   [" +String.valueOf(year)+"]");
        lstmenupage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                sItemPosition=arg2;
                getneededvalues(sItemPosition);
                //pg.setVisibility(View.GONE);
                pg.setVisibility(View.INVISIBLE);
                LayoutInflater inflater= LayoutInflater.from(MenuActivity.this);
                View view=inflater.inflate(R.layout.alertview, null);
                TextView textview=(TextView)view.findViewById(R.id.textmsg);
                textview.setText(neededDesc);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);
                alertDialog.setTitle("Description");
                alertDialog.setIcon(R.mipmap.ic_launcher);
//alertDialog.setMessage("Here is a really long message.");
                alertDialog.setView(view);
                alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {

                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();


            }
        });

        NavigationView navigationViewx = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationViewx.getHeaderView(0);
        TextView myusertextview = (TextView) headerView.findViewById(R.id.textView);
        Menu nav_Menu = navigationViewx.getMenu();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.enumeration) {
                    // Handle the preference  action
                    Intent myintent = new Intent(MenuActivity.this, AfterLoginActivity.class);
                    //startActivity(myintent);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());
                } else if (id == R.id.nav_about) {
                    // Handle the About action

                }else if (id == R.id.mnu_dashboard) {

                    Intent myintent = new Intent(MenuActivity.this, DashBoardActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());
                }else if (id == R.id.mnu_monthlyincome) {
                    Intent myintent = new Intent(MenuActivity.this, MonthlyIncomeActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());
                }
                else if (id == R.id.mnu_weeklyreport) {
                    Intent myintent = new Intent(MenuActivity.this, WeeklyRegistrationReportActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());
                }

                else if (id == R.id.mnu_Structuresbylga) {
                    Intent myintent = new Intent(MenuActivity.this, StructureByLGActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());
                }
                else if (id == R.id.mnu_close) {
                    Intent myintent = new Intent(MenuActivity.this, FirstActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());
                    finish();
                }
                else if (id == R.id.mnu_suggest) {
                    Intent myintent = new Intent(MenuActivity.this, SuggestionActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());

                }
                else if (id == R.id.mnu_billdistribution) {
                    Intent myintent = new Intent(MenuActivity.this, BillDistribution.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());
                }
                else if (id == R.id.mnu_summary) {
                    Intent myintent = new Intent(MenuActivity.this, SummaryReportActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());

                }
                else if (id == R.id.mnu_MapReport) {
                    Intent myintent = new Intent(MenuActivity.this, MapRecordSelectActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());

                }

                else if (id == R.id.mnu_MapStructuresNearMe) {
                    Intent myintent = new Intent(MenuActivity.this, StructuresNearMe.class);
                    startActivity(myintent);

                }
                else if (id == R.id.mnu_manifestdownload) {
                    Intent myintent = new Intent(MenuActivity.this, ManifestDownloadActivity.class);
                    startActivity(myintent);

                }
                else if (id == R.id.mnu_fieldwork) {
                    Intent myintent = new Intent(MenuActivity.this, FieldReportingAreaListActivity.class);
                    startActivity(myintent);

                }
                else if (id == R.id.mnu_fieldmonitor) {
                    Intent myintent = new Intent(MenuActivity.this, EnforcementByAreaListActivity.class);
                    startActivity(myintent);

                }

                else if (id == R.id.mnu_signout) {
                    final SharedPreferences sharedPref = getBaseContext().getSharedPreferences("com.biscom.signageenumeratorekiti.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("PREUSERNAME", "");
                    editor.putString("PREPASSWORD", "");
                    editor.commit();
                    Intent myintent = new Intent(MenuActivity.this, FirstActivity.class);
                    startActivity(myintent, ActivityOptions.makeSceneTransitionAnimation(MenuActivity.this).toBundle());
                    finish();
                }


                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
       // DrawerLayout drawerstart = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawerstart.openDrawer(GravityCompat.START);
        myusertextview.setText(shareduser.toString());
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private class JSON_Get_ProjectKeyFacts extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            Log.i(TAG, "doInBackground");
            invokeJSONWS(getmyuserTOKEN,"JSON_Get_ProjectKeyFacts");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(TAG, "onPostExecute");
            pg.setVisibility(View.INVISIBLE);
            JSONObject j = null;
            try {
                //Parsing the fetched Json String to JSON Object
                j = new JSONObject(responseJSON);
                //Storing the Array of JSON String to our JSON Array
                arrayresultformenuitems = j.getJSONArray("myJresult");
                //Calling method getStudents to get the students from the JSON Array
                //Toast.makeText(getBaseContext(), String.valueOf(arrayresultformenuitems.length()), Toast.LENGTH_LONG).show();
                getMonthlyIncome(arrayresultformenuitems);
                //pg.setVisibility(View.GONE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute");
            //Display progress bar
            //pg.setVisibility(View.VISIBLE);
            pg.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(TAG, "onProgressUpdate");
        }

    }
    public void invokeJSONWS(String usertoken, String methName) {
        // Create request
        SoapObject request = new SoapObject(NAMESPACE, methName);
        // Property which holds input parameters
        PropertyInfo paramPI = new PropertyInfo();
        // Set Name
        paramPI.setName("Token");
        // Set Value
        paramPI.setValue(getmyuserFK);
        // Set dataType
        paramPI.setType(String.class);
        // Add the property to request object
        request.addProperty(paramPI);

        PropertyInfo paramPI2 = new PropertyInfo();
        // Set Name
        paramPI2.setName("islandingPageItem");
        // Set Value
        paramPI2.setValue(1);
        // Set dataType
        paramPI2.setType(Integer.class);
        // Add the property to request object
        request.addProperty(paramPI2);

        PropertyInfo paramPI3 = new PropertyInfo();
        // Set Name
        paramPI3.setName("IsExec");
        // Set Value
        paramPI3.setValue(Integer.valueOf(IsExec));
        // Set dataType
        paramPI3.setType(Integer.class);
        // Add the property to request object
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
    private void getMonthlyIncome(JSONArray j){
        //Traversing through all the items in the json array
        //menuitemsarraylist.clear();
        list=new ArrayList<HashMap<String,String>>();
        for(int i=0;i<j.length();i++){
            try {
                //Getting json object
                JSONObject json = j.getJSONObject(i);
                menuitemsarraylist.add(json.getString("Name"));
                HashMap<String,String> temp=new HashMap<String, String>();
                temp.put(FIRST_COLUMN, "\n"+json.getString("Name").toString()+"\n");
                Locale locale = new Locale("en", "NG");
                NumberFormat formatter = NumberFormat.getInstance();
                if (android.text.TextUtils.isDigitsOnly(json.getString("Value"))){
                    temp.put(SECOND_COLUMN, "\n"+(formatter.format((Double.valueOf(json.getString("Value")))).replace(".00",""))+"\n");

                }else{
                    temp.put(SECOND_COLUMN, "\n"+formatter.format((Double.valueOf(json.getString("Value"))))+"\n");
                }
                list.add(temp);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ListViewAdapters arrayAdapter=new ListViewAdapters(this, list)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the current item from ListView
                View view = super.getView(position,convertView,parent);
                if(position %2 == 1)
                {
                    // Set a background color for ListView regular row/item
                    //view.setBackgroundColor(Color.parseColor("#EAEDED"));

                }
                else
                {
                    // Set the background color for alternate row/item
                    //view.setBackgroundColor(Color.parseColor("#D5DBDB"));
                }
                return view;
            }
        };
        //ListViewAdapters adapter=new ListViewAdapters(this, list);
        lstmenupage.setAdapter(arrayAdapter);


        // );
    }

    public void getneededvalues(int position){
        try {
            //Getting object of given index
            JSONObject json =arrayresultformenuitems.getJSONObject(position);
            //Fetching name from that object
            neededDesc = json.getString("Description");
            neededDescription = json.getString("Description");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Returning the name

    }
}
