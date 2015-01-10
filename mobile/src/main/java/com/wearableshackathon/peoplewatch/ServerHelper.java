package com.wearableshackathon.peoplewatch;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by ryan on 1/10/15.
 */
public class ServerHelper {

    private String checkinURL = "http://10.0.2.2:5000/update";
    private String updateURL = "http://10.0.2.2:5000/locations";
    private String tempURL = "localhost:5000/update?user=0&location=home";

    public void checkIn(String user, String location) {
        String checkInURLParams = checkinURL + "user=" + user + "&location=" + location;
        try {
            makeRequest(checkInURLParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getUserLocations() {
        String[] results = new String[2];
        try {
            String result = makeRequest(updateURL);
            String[] users = result.substring(1,result.length()-2).split(",");
            results = new String[4];
            for (int i = 0; i < users.length; i++){
                String temp = users[i];
                String[] splitInfo = temp.split(":");
                results[i] = splitInfo[1];
                Log.i("user info", splitInfo[0] + ":" + splitInfo[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }


    public String makeRequest(String input) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(new HttpGet(input));
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            String responseString = out.toString();
            Log.i("Message From Server", responseString);
            return responseString;
        } else {
            //Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }

    }
}
