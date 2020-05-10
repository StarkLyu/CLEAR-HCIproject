package com.example.clear;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class PostInfo {
    String url;
    String json;

    PostInfo(String url, String json){
        this.url=url;
        this.json=json;
    }

    String getUrl(){
        return this.url;
    }

    void setUrl(String url){
        this.url=url;
    }

    String getJson(){
        return this.json;
    }

    void setJson(String json){
        this.json=json;
    }

    public String postMethod(){
        try {
            Log.i("json",this.json);

//            String content = String.valueOf(object);
            URL url=new URL(this.url);
            HttpURLConnection connect=(HttpURLConnection)url.openConnection();
            connect.setDoInput(true);
            connect.setDoOutput(true);
            connect.setRequestMethod("POST");
            connect.setUseCaches(false);
            final String tokenStr ="Bearer LZPxZSDMEk7s6fZFduU-ZBqf8sTDyT8x";
            connect.setRequestProperty("Authorization", tokenStr);
            connect.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

            OutputStream outputStream = connect.getOutputStream();
            outputStream.write(json.getBytes());

            int response = connect.getResponseCode();
            System.out.println(connect);
            if (response== HttpURLConnection.HTTP_OK)
            {
                System.out.println(response);
                InputStream input=connect.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(input));
                String line = null;
                System.out.println(connect.getResponseCode());
                StringBuffer sb = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
            else {
                System.out.println(response);
                return "not exsits";
            }
        } catch (Exception e) {
            Log.e("e:", String.valueOf(e));
            return "internet errar";
        }
    }

}
