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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient _googleClient;

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
        // Build a new GoogleApiClient
        _googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        _googleClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    protected void onStop() {
        if (null != _googleClient && _googleClient.isConnected()) {
            _googleClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void update(long location) {

        Log.i("Location", String.valueOf(location));
        new CheckInLocation().execute("0", String.valueOf(location));


        new SendToDataLayerThread("/message_path", String.valueOf(location)).start();
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
            Log.i("ServerHelper-UserValue", user);
            String location = params.get(1);
            Log.i("ServerHelper-LocationValue", user);
            // params comes from the execute() call: params[0] is the url.
            ServerHelper s = new ServerHelper();
            s.checkIn(user,location);
            Log.i("ServerHelper", "after checkIn call");
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

    class SendToDataLayerThread extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(_googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(_googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                }
                else {
                    // Log an error
                    Log.v("myTag", "ERROR: failed to send Message");
                }
            }
        }
    }
}
