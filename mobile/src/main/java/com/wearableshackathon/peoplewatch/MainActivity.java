package com.wearableshackathon.peoplewatch;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.places, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        Button button = (Button) findViewById(R.id.updateButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long location = spinner.getSelectedItemId();
                update(location);
            }
        });
    }

    private void update(long location) {

        Log.i("Location", String.valueOf(location));
        new CheckInLocation().execute("0", String.valueOf(location));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class CheckInLocation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            List<String> params = new ArrayList<String>();
            for (String s : urls){
                params.add(s);
            }

            String user = params.get(0);
            String location = params.get(1);
            // params comes from the execute() call: params[0] is the url.
            ServerHelper s = new ServerHelper();
            s.checkIn(user,location);
            return "";

        }
    }

    private class UpdateUserLocation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            ServerHelper s = new ServerHelper();
            String[] newLocations = s.getUserLocations();
            StringBuffer locationString = new StringBuffer();
            boolean isFirst = true;
            for (int i = 0; i < newLocations.length; i++){
                if (isFirst){
                    isFirst=false;
                } else {
                    locationString.append(":");
                }
                locationString.append(newLocations[i]);
            }
            String resultString = locationString.toString();
            Log.i("Result String", resultString);
            return resultString;

        }
    }
}
