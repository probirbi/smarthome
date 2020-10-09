package com.blockchain.iot.controller;

import com.blockchain.iot.data.TestData;
import com.blockchain.iot.model.SmartHome;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class SmartHomeController {

    List<SmartHome> smartHomes = new ArrayList<SmartHome>();

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/smarthomes")
    public List<SmartHome> getSmartHome() {
           return smartHomes;
    }

    @PostMapping("/smarthomes")
    public String saveSmartHome(@RequestBody SmartHome smartHome) {
        smartHomes.add(smartHome);
        return "success";

    }

    @PostMapping("/evaluatetemperature")
    public String evaluatetemperature() {
        try {
            String url = "http://localhost:8082/evaluate";
            String result = "";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json");

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    @PostMapping("/evaluateparkingspace")
    public String evaluateparkingspace() {
        try {
            String url = "http://localhost:8083/evaluate";
            String result = "";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json");

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "node evaluated";
    }

    @PostMapping("/evaluate")
    public String evaluate() {
        String result = "";
        for (SmartHome smartHome : smartHomes) {
                if (smartHome.getDoorLocks() > 0) {

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String url = "http://localhost:8082/blockchain";
                        HttpPost httpPost = new HttpPost(url);
                        httpPost.setHeader("Content-type", "application/json");
                        String json = "{" +
                                "\"hash\":" + "\"" + "" + "\"," +
                                "\"previousHash\":" + "\"" + "" + "\"," +
                                //"\"description\":" + "\"" + "SmartHome Block" + "\"," +
                                "\"data\":" + "{" +
                                "\"timestamp\":" + "\"" + smartHome.getTimestamp() + "\"," +
                                "\"smokeDetectors\":" + "" + smartHome.getSmokeDetectors() + "," +
                                "\"doorLocks\":" + "" + smartHome.getDoorLocks() + "," +
                                "\"windows\":" + "" + smartHome.getWindows() + "," +
                                "\"homeAppliances\":" + "" + smartHome.getHomeAppliances() + "," +
                                "\"lightBulbs\":" + "" + smartHome.getLightBulbs() +
                                "} ," +
                                //"\"timeStamp\":" + new Date().getTime() + "," +
                                //"\"nonce\":" + 0 + "," +
                                //"\"node\":" + 1 +
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println(smartHome.getDoorLocks() + " Smart home data is verified and found invalid");
                }
        }
        return "node evaluated";
    }

    @GetMapping("/seeddata")
    public void insertData() {
        TestData.callPost();
    }
}
