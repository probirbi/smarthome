package com.blockchain.iot.data;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TestData {

    public static void callPost() {
        for (int i = 0 ; i < 10 ; i++) {
           Random random = new Random();
            int rangeMin = 0;
            int rangeMax = 20;

            int smokeDetectors = random.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            int doorLocks = random.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            int windows = random.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            int homeAppliances = random.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            int lightBulbs = random.nextInt((rangeMax - rangeMin) + 1) + rangeMin;
            try {
                String url = "http://localhost:8081/smarthomes";

                String result = "";
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-type", "application/json");
                String json = "{" +
                        "\"timeStamp\":" + "\"" + sdf.format(new Date()) + "\"," +
                        "\"smokeDetectors\":" + "" +  smokeDetectors +","+
                        "\"doorLocks\":" + "" +  doorLocks +","+
                        "\"windows\":" + "" + windows + "," +
                        "\"homeAppliances\":" + "" + homeAppliances + "," +
                        "\"lightBulbs\":" + "" + lightBulbs +
                        "}";

                System.out.println(json);
                httpPost.setEntity(new StringEntity(json));

                CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
                CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
                HttpEntity entity = closeableHttpResponse.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity);
                    System.out.println(result);
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
