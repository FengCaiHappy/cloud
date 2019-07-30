package com.feng.service.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

public class QueryHZW {

    public static void main(String[] args){
        queryHzw();
    }

    private static void queryHzw(){
        while (true){
            String res = get("https://prod-api.ishuhui.com/ver/a5379534/anime/detail?id=1&type=comics&.json", new HashMap<>());
            Gson gson = new GsonBuilder().setDateFormat(DateFormat.DEFAULT)
                    .create();
            Map<String, Object> resMap = gson.fromJson(res, Map.class);
            Map<String, Object> data = (Map)resMap.get("data");
            Map<String, Object> comicsIndexes = (Map)data.get("comicsIndexes");
            Map<String, Object> map = (Map)comicsIndexes.get("1");
            Float maxNum = Float.parseFloat(map.get("maxNum").toString());
            if(maxNum > 949){
                JOptionPane.showMessageDialog(null, "【提示】", "海贼王已更新", JOptionPane.INFORMATION_MESSAGE);
                return;
            }else{
                System.out.println("还没有更新");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get请求
     *
     * @param url
     * @param param
     * @return
     */
    public static String get(String url,Map<String, Object> param) {
        StringBuilder builder=new StringBuilder();
        try {
            StringBuilder params=new StringBuilder();
            for(Map.Entry<String, Object> entry:param.entrySet()){
                params.append(entry.getKey());
                params.append("=");
                params.append(entry.getValue().toString());
                params.append("&");
            }
            if(params.length()>0){
                params.deleteCharAt(params.lastIndexOf("&"));
            }
            URL restServiceURL = new URL(url+(params.length()>0 ? "?"+params.toString() : ""));
            HttpURLConnection httpConnection = (HttpURLConnection) restServiceURL.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setRequestProperty("Accept", "application/json");
            if (httpConnection.getResponseCode() != 200) {
                throw new RuntimeException("HTTP GET Request Failed with Error code : "
                        + httpConnection.getResponseCode());
            }
            InputStream inStrm = httpConnection.getInputStream();
            byte []b=new byte[1024];
            int length=-1;
            while((length=inStrm.read(b))!=-1){
                builder.append(new String(b,0,length));
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
