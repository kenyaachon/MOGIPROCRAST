package com.iruss.mogivisions.procrastimatev1;

/**
 * Created by Moses on 3/30/2018.
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler() {
    }

    public String makeServiceCall(String reqUrl) {
        String response = "";
        BufferedReader reader = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());


            response = convertStreamToString(in);

            Log.d("Test", "Online database retrieved successfully");
        } catch (SocketTimeoutException e){
            Log.e(TAG, "SocketTimeOutException: " + e.getMessage());
            return "SocketTimeoutException";
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());

        } catch ( SocketException e){
            Log.e(TAG, "Exception: " + e.getMessage());
            //return "Exception";
        }
        catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            //return "IOException";
        }
        catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            //return "Exception";

        }

        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}