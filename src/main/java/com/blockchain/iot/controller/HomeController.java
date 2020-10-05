package com.blockchain.iot.controller;

import com.blockchain.iot.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class HomeController {

    List<Services> services = new ArrayList<Services>();

    String[] broadCastUrls = {"http://localhost:8081/broadcast", "http://localhost:8082/broadcast"};

    @GetMapping("/home")
    public String getData(HttpServletRequest request, Model model) {
        init();
        model.addAttribute("service",services);
        return "home";
    }

    private void init() {
        if (services.size() == 0) {
            Services service = new Services();

            service.setNode("Temperature Node");
            service.setServiceName("temperatures");
            service.setServiceProvider("Temperature Node");
            service.setRatingCriteria("evaluationLogicOne");
            services.add(service);

            /*trust = new Services();
            trust.setNode("Smart Home Node");
            trust.setServiceName("smartHome");
            trust.setServiceProvider("Smart Home Node");
            trust.setRatingCriteria("evaluationLogicTwo");
            trusts.add(trust);*/

            service = new Services();
            service.setNode("Parking Space Node");
            service.setServiceName("parkingSpace");
            service.setServiceProvider("Parking Space Node");
            service.setRatingCriteria("evaluationLogicThree");
            services.add(service);
        }
    }

    @GetMapping("/temperatures")
    public String temperature(HttpServletRequest request, Model model) {

        init();
        // System.out.println("get data");
        try {
            String url = "http://localhost:8081/temperature?requestedBy=SmartHomeNode";
            String result = "";
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                System.out.println(result);
                Gson gson = new Gson();
                Type type = new TypeToken<Block>() {
                }.getType();
                Block block = gson.fromJson(result, type);
            //  model.addAttribute("sensor", sensor);
                broadcast(block);

                System.out.println("block broadcast");
                Block newRatingBlock = block;
                newRatingBlock.setHash("");
                newRatingBlock.setPreviousHash("");
                newRatingBlock.setTrustScore(null);
                Double rating = doRating(block);
                newRatingBlock.setBlockType(BlockType.RATING);
                newRatingBlock.setBlockCreatedBy("SmartHomeNode");
                newRatingBlock.setRating(rating);
                newRatingBlock.setRatingDoneBy("SmartHomeNode");

                System.out.println("rating block");
                try {
                    url = "http://localhost:8082/blockchain?create=true";
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.setHeader("Content-type", "application/json");
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(block);
                    System.out.println(json);
                    httpPost.setEntity(new StringEntity(json));

                    closeableHttpClient = HttpClients.createDefault();
                    closeableHttpResponse = closeableHttpClient.execute(httpPost);
                    HttpEntity responseEntity = closeableHttpResponse.getEntity();

                    if (responseEntity != null) {
                        result = EntityUtils.toString(responseEntity);
                        if (result != null && !result.equals("") && !result.equals("{}")) {
                            gson = new Gson();
                            type = new TypeToken<Block>() {
                            }.getType();
                            newRatingBlock = gson.fromJson(result, type);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                broadcast(newRatingBlock);

                System.out.println("broadcast rating block");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/home";
    }

    private void broadcast(Block block) {
        for (int i = 0; i < broadCastUrls.length; i++) {
            try {
                String url = broadCastUrls[i];
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(block);
                httpPost.setEntity(new StringEntity(json));

                CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
                CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
                HttpEntity responseEntity = closeableHttpResponse.getEntity();

                if (responseEntity != null) {
                    String result = EntityUtils.toString(responseEntity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/evaluate")
    public String evaluate(@RequestParam String node, Model model) {
        int nodeInt = 0;
        if (node.equals("Temperature Node")) {
            nodeInt = 1;
        }
        if (node.equals("Smart Home Node")) {
            nodeInt = 2;
        }
        if (node.equals("Parking Space Node")) {
            nodeInt = 3;
        }

        try {
            String url = "http://localhost:8082/evaluatenode?node="+nodeInt+"&nodeFrom=2";
            String result = "";
        //    System.out.println(url);
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
       //         System.out.println(result);
                Gson gson = new Gson();
                Type type = new TypeToken<List<Trust>>(){}.getType();
                List<Trust> trusts = gson.fromJson(result, type);
                model.addAttribute("trusts",trusts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String url = "http://localhost:8081/evaluatenode?node="+nodeInt+"&nodeFrom=2";
            String result = "";
        //    System.out.println(url);
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
          //      System.out.println(result);
                Gson gson = new Gson();
                Type type = new TypeToken<List<Trust>>(){}.getType();
                List<Trust> trusts = gson.fromJson(result, type);
                model.addAttribute("trusts",trusts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "home";
    }

    @GetMapping("/parkingSpace")
    public String parkingSpace(HttpServletRequest request, Model model) {

        init();
        // System.out.println("get data");
        try {
            String url = "http://localhost:8083/parkingSpace";
            String result = "";
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity responseEntity = closeableHttpResponse.getEntity();
            if (responseEntity != null) {
                result = EntityUtils.toString(responseEntity);
           //     System.out.println(result);

                Gson gson = new Gson();
                Type type = new TypeToken<ParkingSpace>() {
                }.getType();
                ParkingSpace parkingSpace = gson.fromJson(result, type);
                model.addAttribute("parkingSpace", parkingSpace);
                double rating = doRatingForParkingSpace(parkingSpace);
                updateBlockchain(parkingSpace.getHash(), rating);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/home";
    }

    private Double doRating(Block block) {
        try {

            String json = block.getData().toString();
            if (json.indexOf("temperatureCelsius=") > 0) {
                json = json.substring(json.indexOf("temperatureCelsius=")+19, json.length());
                json = json.substring(0,json.indexOf(","));
                Double temperatureCelsius = Double.parseDouble(json);

                System.out.println(temperatureCelsius);
                if (temperatureCelsius >= 10 && temperatureCelsius <= 20) {
                    return 1.0;
                } else if (temperatureCelsius > 20 && temperatureCelsius <= 30) {
                    return 0.7;
                } else {
                    return 0.5;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
        return 0.0;
    }

    private double doRatingForParkingSpace(ParkingSpace parkingSpace) {
        if (parkingSpace.getParkedSpace() > 400 && parkingSpace.getParkedSpace() <= 500) {
            return 1.0;
        } else  if (parkingSpace.getParkedSpace() > 300 && parkingSpace.getParkedSpace() <= 400) {
            return 0.7;
        } else {
            return 0.5;
        }
    }

    private void updateBlockchain(String hash, double rating) {
        try {
            String url = "http://localhost:8082/blockchain/updaterating";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json");
            String json = "{" +
                    "\"hash\":" + "\"" + hash + "\"," +
                    "\"previousHash\":" + "\"" + "" + "\"," +
                    "\"description\":" + "\"" + "" + "\"," +
                    "\"data\":" + "{" +
                    "} ," +
                    "\"timeStamp\":" + "\"" + "" + "\"," +
                    "\"nonce\":" + 0 + "," +
                    "\"node\":" + 0 + "," +
                    "\"rating\":" + rating +
                    "}";
        //    System.out.println(json);
            httpPost.setEntity(new StringEntity(json));

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity entity = closeableHttpResponse.getEntity();

            if (entity != null) {
                String result = EntityUtils.toString(entity);
          //      System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String url = "http://localhost:8081/blockchain/updaterating";
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json");
            String json = "{" +
                    "\"hash\":" + "\"" + hash + "\"," +
                    "\"previousHash\":" + "\"" + "" + "\"," +
                    "\"description\":" + "\"" + "" + "\"," +
                    "\"data\":" + "{" +
                    "} ," +
                    "\"timeStamp\":" + "\"" + "" + "\"," +
                    "\"nonce\":" + 0 + "," +
                    "\"node\":" + 0 + "," +
                    "\"rating\":" + rating +
                    "}";
        //    System.out.println(json);
            httpPost.setEntity(new StringEntity(json));
            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
        //        System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
