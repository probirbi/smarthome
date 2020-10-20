package com.blockchain.iot.controller;

import com.blockchain.iot.model.*;
import com.blockchain.iot.util.BlockChainAlgorithm;
import com.blockchain.iot.util.DateUtil;
import com.blockchain.iot.util.NodeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.*;

import static java.lang.Double.parseDouble;

@Controller
public class HomeController {

    List<Services> services = new ArrayList<Services>();

    @Value("${broadcast.ports}")
    String broadcastPorts;

    @GetMapping("/home")
    public String getData(HttpServletRequest request, Model model) {
        init();
        model.addAttribute("service", services);
        return "home";
    }

    private void init() {
        if (services.size() == 0) {
            Services service = new Services();

            service.setNode("Temperature Node");
            service.setServiceName("temperatures");
            service.setServiceProvider("Temperature Node");
            service.setRatingCriteria("serviceSatisfaction");
            service.setAutoGenerate("autogeneratetemperatures");
            services.add(service);

            /*service = new Services();
            service.setNode("Smart Home Node");
            service.setServiceName("smartHome");
            service.setServiceProvider("Smart Home Node");
            service.setRatingCriteria("evaluationLogicTwo");
            services.add(service);*/

            service = new Services();
            service.setNode("Parking Space Node");
            service.setServiceName("parkingspaces");
            service.setServiceProvider("Parking Space Node");
            service.setRatingCriteria("evaluationLogicThree");
            services.add(service);

            service = new Services();
            service.setNode("EHealth Node");
            service.setServiceName("ehealths");
            service.setServiceProvider("EHealth Node");
            service.setRatingCriteria("evaluationLogicThree");
            services.add(service);

            service = new Services();
            service.setNode("Energy Consumption");
            service.setServiceName("energyconsumptions");
            service.setServiceProvider("Energy Consumption Node");
            service.setRatingCriteria("evaluationLogicThree");
            services.add(service);

        }
    }

    @GetMapping("/temperatures")
    public String temperature(HttpServletRequest request, Model model) {

        init();
        //for (int i=0; i< 100; i++) {
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
                block = addInLocalBlockChain(block);
                broadcast(block);

                System.out.println("block broadcast");
                Block newRatingBlock = block;
                newRatingBlock.setHash("");
                newRatingBlock.setPreviousHash("");
                newRatingBlock.setTrustScore(null);
                Double rating = doRatingForTemperature(block);
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
                    String json = mapper.writeValueAsString(newRatingBlock);
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
        //}
        return "redirect:/home";
    }

    private Double doRatingForTemperature(Block block) {
        try {

            String json = block.getData().toString();
            if (json.indexOf("temperatureCelsius=") > 0) {
                json = json.substring(json.indexOf("temperatureCelsius=") + 19, json.length());
                json = json.substring(0, json.indexOf(","));
                Double temperatureCelsius = parseDouble(json);

                System.out.println(temperatureCelsius);
                if (temperatureCelsius > 10 && temperatureCelsius <= 15) {
                    return 1.0;
                } else if (temperatureCelsius > 5 && temperatureCelsius <= 10) {
                    return 0.9;
                } else if (temperatureCelsius > 15 && temperatureCelsius <= 20) {
                    return 0.8;
                } else if (temperatureCelsius > 0 && temperatureCelsius <= 5) {
                    return 0.7;
                } else if (temperatureCelsius > 20 && temperatureCelsius <= 30) {
                    return 0.6;
                } else {
                    return 0.5;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //return 0.0;
        }
        return 0.0;
    }

    @GetMapping("/parkingspaces")
    public String parkingSpace(HttpServletRequest request, Model model) {

        init();
        try {
            String url = "http://localhost:8083/parkingspace?requestedBy=SmartHomeNode";
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity responseEntity = closeableHttpResponse.getEntity();

            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity);
                System.out.println(result);

                Gson gson = new Gson();
                Type type = new TypeToken<Block>() {
                }.getType();

                Block block = gson.fromJson(result, type);
                block = addInLocalBlockChain(block);
                broadcast(block);

                Block newRatingBlock = block;
                newRatingBlock.setHash("");
                newRatingBlock.setPreviousHash("");
                newRatingBlock.setTrustScore(null);
                Double rating = doRatingForParkingSpace(block);
                newRatingBlock.setBlockType(BlockType.RATING);
                newRatingBlock.setBlockCreatedBy("SmartHomeNode");
                newRatingBlock.setRating(rating);
                newRatingBlock.setRatingDoneBy("SmartHomeNode");

                System.out.println("Rating for parking space: ");

                try {
                    url = "http://localhost:8082/blockchain?create=true";
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.setHeader("Content-type", "application/json");
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(newRatingBlock);
                    httpPost.setEntity(new StringEntity(json));

                    closeableHttpClient = HttpClients.createDefault();
                    closeableHttpResponse = closeableHttpClient.execute(httpPost);
                    responseEntity = closeableHttpResponse.getEntity();

                    if (responseEntity != null) {
                        result = EntityUtils.toString(responseEntity);
                        if (result != null && !result.equals("") && !result.equals("{}")) {
                            gson = new Gson();
                            type = new TypeToken<Block>() {
                            }.getType();
                            newRatingBlock = gson.fromJson(result, type);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                broadcast(newRatingBlock);
                System.out.println("Rating for broadcasting: ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/home";
    }

    private double doRatingForParkingSpace(Block block) {
        try {
            String json = block.getData().toString();
            if (json.indexOf("parkedSpace=") > 0) {
                json = json.substring(json.indexOf("parkedSpace=") + 12, json.length());
                //json = json.substring(json.indexOf("temperatureCelsius=") + 19, json.length());
                json = json.substring(0, json.indexOf(","));
                int parkedSpace = (int)Double.parseDouble(json);

                if (parkedSpace > 450 && parkedSpace <= 500) {
                    return 1.0;
                } else if (parkedSpace > 400 && parkedSpace <= 450) {
                    return 0.9;
                } else if (parkedSpace > 350 && parkedSpace <= 400) {
                    return 0.8;
                } else if (parkedSpace > 300 && parkedSpace <= 350) {
                    return 0.7;
                } else if (parkedSpace > 250 && parkedSpace <= 300) {
                    return 0.6;
                } else {
                    return 0.5;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @GetMapping("/ehealths")
    public String eHealth(HttpServletRequest request, Model model) {

        init();
        // System.out.println("get data");
        try {
            String url = "http://localhost:8085/ehealth?requestedBy=SmartHomeNode";
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
                block = addInLocalBlockChain(block);
                broadcast(block);

                System.out.println("block broadcast");
                Block newRatingBlock = block;
                newRatingBlock.setHash("");
                newRatingBlock.setPreviousHash("");
                newRatingBlock.setTrustScore(null);
                Double rating = doRatingForEHealth(block);
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
                    String json = mapper.writeValueAsString(newRatingBlock);
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

    private Double doRatingForEHealth(Block block) {
        try {
            String json = block.getData().toString();
            System.out.println("rating json");
            System.out.println(json);
            if (json.indexOf("bloodPressure=") > 0) {
                json = json.substring(json.indexOf("bloodPressure=") + 14, json.length());
                json = json.substring(0, json.indexOf("}"));
                System.out.println(json);
                int bloodPressure = (int)Double.parseDouble(json);
                //int bloodPressure = Integer.parseInt(json);
                System.out.println(bloodPressure);

                if (bloodPressure > 120 && bloodPressure <= 130) {
                    return 1.0;
                } else if (bloodPressure > 115 && bloodPressure <= 120) {
                    return 1.9;
                } else if (bloodPressure > 110 && bloodPressure <= 115) {
                    return 1.8;
                } else if (bloodPressure > 105 && bloodPressure <= 110) {
                    return 1.7;
                } else if (bloodPressure > 130 && bloodPressure <= 140) {
                    return 1.6;
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

    @GetMapping("/energyconsumptions")
    public String energyConsumption(HttpServletRequest request) {
        init();

        try {
            String url = "http://localhost:8088/energyconsumption?requestedBy=SmartHomeNode";
            HttpGet httpGet = new HttpGet(url);

            System.out.println();
            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity responseEntity = closeableHttpResponse.getEntity();

            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity);
                System.out.println(result);
                Gson gson = new Gson();
                Type type = new TypeToken<Block>() {
                }.getType();
                Block block = gson.fromJson(result, type);
                block = addInLocalBlockChain(block);
                broadcast(block);

                System.out.println("Rating block");
                Block newRatingBlock = block;
                newRatingBlock.setPreviousHash("");
                newRatingBlock.setHash("");
                newRatingBlock.setTrustScore(null);
                Double rating = doRatingForEnergyConsumption(block);
                newRatingBlock.setRating(rating);
                newRatingBlock.setBlockType(BlockType.RATING);
                newRatingBlock.setBlockCreatedBy("SmartHomeNode");
                newRatingBlock.setRatingDoneBy("SmartHomeNode");

                try {
                    url = "http://localhost:8082/blockchain?create=true";
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.setHeader("Content-type", "application/json");

                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(block);
                    httpPost.setEntity(new StringEntity(json));
                    closeableHttpClient = HttpClients.createDefault();
                    closeableHttpResponse = closeableHttpClient.execute(httpPost);
                    responseEntity = closeableHttpResponse.getEntity();

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

    private Double doRatingForEnergyConsumption(Block block) {
        try {
            String json = block.getData().toString();
            if (json.indexOf("consumedElectricity=") > 0) {
                json = json.substring(json.indexOf("consumedElectricity=") + 20, json.length());
                json = json.substring(0, json.indexOf(","));
                Double consumedElectricity = Double.parseDouble(json);

                System.out.println(consumedElectricity);
                if (consumedElectricity > 30000 && consumedElectricity <= 40000) {
                    return 1.0;
                } else if (consumedElectricity > 25000 && consumedElectricity <= 30000) {
                    return 0.9;
                } else if (consumedElectricity > 20000 && consumedElectricity <= 25000) {
                    return 0.8;
                } else if (consumedElectricity > 15000 && consumedElectricity <= 20000) {
                    return 0.7;
                } else if (consumedElectricity > 13000 && consumedElectricity <= 15000) {
                    return 0.6;
                } else {
                    return 0.5;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Block addInLocalBlockChain(Block block) {

        try {
            String url = "http://localhost:8082/addInLocalBlockChain";

            System.out.println(url);
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
                if (result != null && !result.equals("") && !result.equals("{}")) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<Block>() {
                    }.getType();
                    block = gson.fromJson(result, type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return block;
    }

    private void broadcast(Block block) {
        String[] broadCastUrls = broadcastPorts.split(",");
        for (int i = 0; i < broadCastUrls.length; i++) {
            try {
                String url = "http://localhost:" + broadCastUrls[i] + "/broadcast";

                System.out.println(url);
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
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/getcurrenttrustscore")
    public String getTrustScore(@RequestParam(required = false) Integer node, Model model, HttpServletRequest
            request) throws JsonProcessingException {
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        HashMap<Integer, Integer> mapCount = new HashMap<Integer, Integer>();
        HashMap<Integer, Double> mapTrustScore = new HashMap<Integer, Double>();
        HashMap<Integer, List<String>> mapBlock = new HashMap<Integer, List<String>>();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        Integer selectedNode = 0;
        List<Block> blockChainCopy = new ArrayList<Block>();

        try {
            String url = "http://localhost:8082/blockchain";
            String result = "";
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                System.out.println(result);
                Gson gson = new Gson();
                Type type = new TypeToken<List<Block>>() {
                }.getType();
                blockChainCopy = gson.fromJson(result, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < blockChainCopy.size(); i++) {
            if (blockChainCopy.get(i).getBlockType().equals(BlockType.RATING)) {
                if (blockChainCopy.get(i).getRatingDoneBy() != null && !blockChainCopy.get(i).getRatingDoneBy().equals("SmartHomeNode")) {
                    continue;
                }
                if (DateUtil.lessThanOneHour(blockChainCopy.get(i).getTimeStamp())) {
                    continue;
                }
                if (map.get(blockChainCopy.get(i).getNode()) != null) {
                    map.put(blockChainCopy.get(i).getNode(), map.get(blockChainCopy.get(i).getNode()) + blockChainCopy.get(i).getRating());
                } else {
                    map.put(blockChainCopy.get(i).getNode(), blockChainCopy.get(i).getRating());
                }
                if (mapCount.get(blockChainCopy.get(i).getNode()) != null) {
                    mapCount.put(blockChainCopy.get(i).getNode(), mapCount.get(blockChainCopy.get(i).getNode()) + 1);
                } else {
                    mapCount.put(blockChainCopy.get(i).getNode(), 1);
                }
                if (mapBlock.get(blockChainCopy.get(i).getNode()) != null) {
                    List<String> blocks = (List<String>) mapBlock.get(blockChainCopy.get(i).getNode());
                    blocks.add(blockChainCopy.get(i).getBlockNumber() + "");
                    mapBlock.put(blockChainCopy.get(i).getNode(), blocks);
                    System.out.println(" block " + mapBlock.get(blockChainCopy.get(i).getNode()));
                } else {
                    List<String> blocks = new ArrayList<String>();
                    blocks.add(blockChainCopy.get(i).getBlockNumber() + "");
                    mapBlock.put(blockChainCopy.get(i).getNode(), blocks);
                    System.out.println(" block one " + mapBlock.get(blockChainCopy.get(i).getNode()));
                }
            }
        }
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Double trustScore = 0.0;
            if (mapCount.get(pair.getKey()) != null) {
                trustScore = (Double) pair.getValue() / mapCount.get(pair.getKey());
                trustScore = parseDouble(decimalFormat.format(trustScore));
            }
            if (trustScore > 0.6) {
                mapTrustScore.put((Integer) pair.getKey(), trustScore);
            }
        }

        it = mapTrustScore.entrySet().iterator();
        List<Trust> trusts = new ArrayList<Trust>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println("Node : " + pair.getKey() + "    Trust Score : " + pair.getValue());
            Trust trust = new Trust();
            trust.setNode(pair.getKey() + "");
            trust.setCurrentTrustScore((Double) pair.getValue());
            trusts.add(trust);
        }
        model.addAttribute(mapTrustScore);
        selectedNode = BlockChainAlgorithm.trustConsensusAlgorithm(mapTrustScore);
        for (Trust trust : trusts) {
            if (trust.getNode().equals(selectedNode + "")) {
                trust.setRandomSelected("Yes");
            }
        }
        //  if (request.getSession().getAttribute("trusts") != null) {
        //      List<Trust> sessionTrusts = new ArrayList<Trust>();
        //      for (Trust current : sessionTrusts) {
        //          for (Trust newTrust : trusts) {
        //              if (newTrust.getNode().equals(current.getNode())) {
        //                  newTrust.setCurrentTrustScore(current.getLatestTrustScore());
        //              }
        //          }
        //      }
        //  }

        request.getSession().setAttribute("trusts", trusts);
        System.out.println("block");
        System.out.println(mapBlock.get(selectedNode));
        if (selectedNode > 0) {
            Block trustScoreBlock = new Block();
            trustScoreBlock.setBlockCreatedBy(NodeName.getNodeName(selectedNode));
            trustScoreBlock.setRatingDoneBy("SmartHomeNode");
            trustScoreBlock.setServiceRequestedBy("SmartHomeNode");
            trustScoreBlock.setBlockType(BlockType.TRUST);
            trustScoreBlock.setNode(selectedNode);
            trustScoreBlock.setRequestTimeStamp(new Date().getTime());
            trustScoreBlock.setData("Rating Block Numbers " + mapBlock.get(selectedNode) + "");
            trustScoreBlock.setPreviousHash(blockChainCopy.get(blockChainCopy.size() - 1).getHash());
            trustScoreBlock.setTimeStamp(new Date().getTime());
            trustScoreBlock.setTrustScore((Double) mapTrustScore.get(selectedNode));
            try {
                String url = "http://localhost:8082/blockchain?create=true";
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-type", "application/json");
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(trustScoreBlock);
                System.out.println(json);
                httpPost.setEntity(new StringEntity(json));

                CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
                CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost);
                HttpEntity responseEntity = closeableHttpResponse.getEntity();

                if (responseEntity != null) {
                    String result = EntityUtils.toString(responseEntity);
                    if (result != null && !result.equals("") && !result.equals("{}")) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<Block>() {
                        }.getType();
                        trustScoreBlock = gson.fromJson(result, type);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            broadcast(trustScoreBlock);
        }

        model.addAttribute("trusts", trusts);
        model.addAttribute("selectedNode", selectedNode);
        System.out.println("Random selected node is : " + selectedNode);
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(mapTrustScore);
        model.addAttribute("json", json);

        init();
        model.addAttribute("service", services);
        return "home";
    }

    @GetMapping("/getlatesttrustscore")
    public String getLatestTrustScore(@RequestParam(required = false) Integer node, Model model, HttpServletRequest
            request) throws JsonProcessingException {
        List<Block> blockChainCopy = new ArrayList<Block>();
        HashMap<Integer, Double> mapTrustScore = new HashMap<Integer, Double>();

        try {
            String url = "http://localhost:8082/blockchain";
            String result = "";
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                System.out.println(result);
                Gson gson = new Gson();
                Type type = new TypeToken<List<Block>>() {
                }.getType();
                blockChainCopy = gson.fromJson(result, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Trust> trusts = new ArrayList<Trust>();
        for (int i = 0; i < blockChainCopy.size(); i++) {
            if (blockChainCopy.get(i).getBlockType().equals(BlockType.TRUST)) {
                mapTrustScore.put(blockChainCopy.get(i).getNode(), blockChainCopy.get(i).getTrustScore());
            }
        }
        Iterator it = mapTrustScore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairLatest = (Map.Entry) it.next();
            System.out.println("Node : " + pairLatest.getKey() + "    Trust Score : " + pairLatest.getValue());
            Trust trust = new Trust();
            trust.setNode(pairLatest.getKey() + "");
            trust.setLatestTrustScore((Double) pairLatest.getValue());
            trusts.add(trust);
        }
        request.getSession().setAttribute("trusts", trusts);
        model.addAttribute("latest", trusts);
        init();
        model.addAttribute("service", services);
        return "home";
    }

    @GetMapping("/getcurrentratingscore")
    public String getCurrentRatingScore(@RequestParam(required = false) Integer node, Model model, HttpServletRequest
            request) throws JsonProcessingException {
        List<Block> blockChainCopy = new ArrayList<Block>();
        HashMap<Integer, Double> mapCurrentRatingScore = new HashMap<Integer, Double>();

        try {
            String url = "http://localhost:8082/blockchain";
            String result = "";
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                System.out.println(result);
                Gson gson = new Gson();
                Type type = new TypeToken<List<Block>>() {
                }.getType();
                blockChainCopy = gson.fromJson(result, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Trust> currentRatings = new ArrayList<Trust>();
        for (int i = 0; i < blockChainCopy.size(); i++) {
            if (blockChainCopy.get(i).getBlockType().equals(BlockType.RATING)) {
                mapCurrentRatingScore.put(1, blockChainCopy.get(i).getRating());
            }
        }
        Iterator it = mapCurrentRatingScore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairCurrentRating = (Map.Entry) it.next();
            System.out.println("Node : " + pairCurrentRating.getKey() + "    Rating Score : " + pairCurrentRating.getValue());
            Trust trust = new Trust();
            //trust.setNode(pairLatestRating.getKey() + "");
            trust.setCurrentRatingScore((Double) pairCurrentRating.getValue());
            currentRatings.add(trust);
        }
        //request.getSession().setAttribute("latestRating", currentRatings);
        model.addAttribute("currentRating", currentRatings);
        init();
        model.addAttribute("service", services);
        return "home";
    }

    @GetMapping("/getlatestratingscore")
    public String getLatestRatingScore(@RequestParam(required = false) String blockCreatedBy, Integer node, Model model, HttpServletRequest
            request) throws JsonProcessingException {
        List<Block> blockChainCopy = new ArrayList<Block>();
        HashMap<Integer, Double> mapLatestRatingScore = new HashMap<Integer, Double>();

        try {
            String url = "http://localhost:8082/blockchain";
            String result = "";
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
            HttpEntity entity = closeableHttpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                System.out.println(result);
                Gson gson = new Gson();
                Type type = new TypeToken<List<Block>>() {
                }.getType();
                blockChainCopy = gson.fromJson(result, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Trust> latestRatings = new ArrayList<Trust>();
        for (int i = 0; i < blockChainCopy.size(); i++) {
            if (!DateUtil.lessThanOneHour(blockChainCopy.get(i).getTimeStamp())) {
                continue;
            }
            if (blockChainCopy.get(i).getBlockType().equals(BlockType.RATING)) {
                mapLatestRatingScore.put(1, blockChainCopy.get(i).getRating());
            }
        }
        Iterator it = mapLatestRatingScore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairLatestRating = (Map.Entry) it.next();
            System.out.println("Node : " + pairLatestRating.getKey() + "    Rating Score : " + pairLatestRating.getValue());
            Trust trust = new Trust();
            //trust.setNode(pairLatestRating.getKey() + "");
            trust.setLatestRatingScore((Double) pairLatestRating.getValue());
            latestRatings.add(trust);
        }
        //request.getSession().setAttribute("latestRating", latestRatings);
        model.addAttribute("latestRating", latestRatings);
        init();
        model.addAttribute("service", services);
        return "home";
    }


/*************************************Generate automatically 1000 Blocks ************/

@GetMapping("/autogeneratetemperatures")
public String autoGenerateTemperature(HttpServletRequest request, Model model) {

    init();
    for (int i=0; i< 1000; i++) {
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
            block = addInLocalBlockChain(block);
            broadcast(block);

            System.out.println("block broadcast");
            Block newRatingBlock = block;
            newRatingBlock.setHash("");
            newRatingBlock.setPreviousHash("");
            newRatingBlock.setTrustScore(null);
            Double rating = doRatingForTemperature(block);
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
                String json = mapper.writeValueAsString(newRatingBlock);
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
    }
    return "redirect:/home";
}

/**********************************************************/

    @GetMapping("/evaluate")
    public String evaluate(@RequestParam String node, Model model) {
        Integer nodeInt = 0;
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
            String url = "http://localhost:8082/evaluatenode?node=" + nodeInt + "&nodeFrom=2";
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
                Type type = new TypeToken<List<Trust>>() {
                }.getType();
                List<Trust> trusts = gson.fromJson(result, type);
                model.addAttribute("trusts", trusts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String url = "http://localhost:8081/evaluatenode?node=" + nodeInt + "&nodeFrom=2";
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
                Type type = new TypeToken<List<Trust>>() {
                }.getType();
                List<Trust> trusts = gson.fromJson(result, type);
                model.addAttribute("trusts", trusts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "home";
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
