package com.blockchain.iot.controller;

import com.blockchain.iot.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class BlockChainController {

    List<Block> blockChain = new ArrayList<Block>();

    List<Trust> trusts = new ArrayList<Trust>();

    int prefix = 1;

    String prefixString = new String(new char[prefix]).replace('\0', '0');

    @GetMapping("/blockchain")
    public List<Block> getBlockchain() {

        for (int i = 0; i < blockChain.size(); i++) {
            String previousHash = i == 0 ? "0"
                    : blockChain.get(i - 1)
                    .getHash();
            boolean flag = blockChain.get(i)
                    .getHash()
                    .equals(blockChain.get(i)
                            .calculateBlockHash())
                    && previousHash.equals(blockChain.get(i)
                    .getPreviousHash())
                    && blockChain.get(i)
                    .getHash()
                    .substring(0, prefix)
                    .equals(prefixString);
            if (flag) {
                System.out.println("Blocks in the block chain is validated");
            }
        }
        return blockChain;
    }

    @PostMapping("/broadcast")
    public Block broadcast(@RequestBody Block block) {
      //  if (block.getNode() >= 1 && block.getNode() <= 3) {
            if (blockChain.size() > 0) {
                if (validate(block)) {
                    blockChain.add(block);
                }
            } else {
                blockChain.add(block);
            }
      //  }
        return block;
    }

    private boolean validate(Block block) {
        for (int i = 0; i < blockChain.size(); i++) {
            if (block.getPreviousHash().equals(blockChain.get(i).getHash())) {
                return true;
            }
        }
        return false;
    }

    @PostMapping("/blockchain")
    public Block saveBlockchain(@RequestBody Block block, HttpServletRequest request) {

        String create = request.getParameter("create");
        if (blockChain.size() == 0) {
            //     Block blockNew = new Block(block.getDescription(), block.getData(), "0", new Date().getTime(), block.getNode());
            Block blockNew = block;
            blockNew.setPreviousHash("0");
            blockNew.setBlockNumber(1);
            blockNew.setResponseTimeStamp(new Date().getTime());
            blockNew.mineBlock(prefix);
            if (create.equals("true")) {
                blockChain.add(blockNew);
            }
            return blockNew;
        } else {
            //        Block blockNew = new Block(block.getDescription(), block.getData(), blockChain.get(blockChain.size() - 1).getHash(), new Date().getTime(), block.getNode());
            Block blockNew = block;
            blockNew.setPreviousHash(blockChain.get(blockChain.size() - 1).getHash());
            blockNew.setBlockNumber(blockChain.size() + 1);
            blockNew.setResponseTimeStamp(new Date().getTime());
            blockNew.mineBlock(prefix);
            if (create.equals("true")) {
                blockChain.add(blockNew);
            }
            return blockNew;
        }
    }

    @PostMapping("/blockchain/updaterating")
    public String updateBlockchain(@RequestBody Block block) {

      //  System.out.println("updaterating");
      //  System.out.println(block.getHash());
       // System.out.println(block.getRating());
        for (int i = 0; i < blockChain.size(); i++) {
            if (block.getHash().equals(blockChain.get(i).getHash())) {
                blockChain.get(i).setRating(block.getRating());
                break;
            }
        }
        return "rating updated in blockchain";
    }

    @PostMapping("/blockchainevaluatenode")
    public String saveBlockchain(@RequestParam int node) {
        for (int i = 0; i < blockChain.size(); i++) {
            if (node == blockChain.get(i).getNode()) {
                switch (node) {
                    case 1 :
                        evaluationLogicOne(blockChain.get(i));
                        break;
                    case 2 :
                        evaluationLogicTwo(blockChain.get(i));
                        break;
                    case 3 :
                        evaluationLogicThree(blockChain.get(i));
                        break;
                }
            }
        }
        return "Node " + node + " evaluated";
    }

    @GetMapping("/evaluatenode")
    public List<Trust> evaluatenode(@RequestParam int node, @RequestParam int nodeFrom) {

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HHmmss");

        double trustScore = 0.0;
        double totalRating = 0.0;
        int ratingCount = 0;
     //   System.out.println(node);

        for (int i = 0; i < blockChain.size(); i++) {
            if (node == blockChain.get(i).getNode()) {
                ratingCount = ratingCount + 1;
                totalRating = totalRating + blockChain.get(i).getRating();
            }
        }
      //  System.out.println(ratingCount);
       // System.out.println(totalRating);
        trustScore = totalRating / ratingCount;

      //  System.out.println("trustscore");
     //   System.out.println(trustScore);
        for (int i = 0; i < blockChain.size(); i++) {
            if (node == blockChain.get(i).getNode()) {
                blockChain.get(i).setTrustScore(new Double(decimalFormat.format(trustScore)));
      //          System.out.println("updated");
            }
        }



        double temperatureScore = 0.0;
        double smartHomeScore = 0.0;
        double parkingSpaceScore = 0.0;

        for (int i = 0; i < blockChain.size(); i++) {
            String previousHash = i == 0 ? "0"
                    : blockChain.get(i - 1)
                    .getHash();
            boolean flag = blockChain.get(i)
                    .getHash()
                    .equals(blockChain.get(i)
                            .calculateBlockHash())
                    && previousHash.equals(blockChain.get(i)
                    .getPreviousHash())
                    && blockChain.get(i)
                    .getHash()
                    .substring(0, prefix)
                    .equals(prefixString);

            if (blockChain.get(i).getNode() == 2) {
                smartHomeScore = blockChain.get(i).getTrustScore();
            } else  if (blockChain.get(i).getNode() == 1) {
                temperatureScore = blockChain.get(i).getTrustScore();
            } else  if (blockChain.get(i).getNode() == 3) {
                parkingSpaceScore = blockChain.get(i).getTrustScore();
            }

            if (flag) {
                System.out.println("Blocks in the block chain is validated");
            }
        }

        trusts.clear();
        if (nodeFrom != 1) {
            Trust trust = new Trust();
            trust.setNode("Temperature Node");
            trust.setServiceName("temperatures");
            trust.setServiceProvider("Temperature Node");
            trust.setRatingCriteria("evaluationLogicOne");
            trust.setScore(temperatureScore);
            trusts.add(trust);
        }

        if (nodeFrom != 2){
            Trust trust = new Trust();
            trust.setNode("Smart Home Node");
            trust.setServiceName("smartHomes");
            trust.setServiceProvider("Smart Home Node");
            trust.setRatingCriteria("evaluationLogicTwo");
            trust.setScore(smartHomeScore);
            trusts.add(trust);
        }

        if (nodeFrom != 3) {
            Trust trust = new Trust();
            trust.setNode("Parking Space Node");
            trust.setServiceName("parkingSpaces");
            trust.setServiceProvider("Parking Space Node");
            trust.setRatingCriteria("evaluationLogicThree");
            trust.setScore(parkingSpaceScore);
            trusts.add(trust);
        }

        if (blockChain.size() > 1000) {
            ObjectMapper mapper = new ObjectMapper();

            try {
                mapper.writeValue(new File("G:\\Master Thesis\\blockchain-iot\\blockchain\\src\\main\\resources\\blockchain" + sdf.format(new Date())+ ".json"), blockChain);
                blockChain = new ArrayList<Block>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        return trusts;
    }

    private void evaluationLogicOne(Block block) {

     //   Sensor sensor = (Sensor) block.getSensor();
     //   if (sensor.getTemperatureCelsius() >= 10 && sensor.getTemperatureCelsius() <= 20) {
     //       block.setTrustScore(1.0);
     //   } else {
     //       block.setTrustScore(0.5);
     //   }
    }

    private void evaluationLogicTwo(Block block) {
     //   SmartHome smartHome = (SmartHome) block.getSmartHome();
     //   if (smartHome.getSmokeDetectors() >= 2 && smartHome.getDoorLocks() <= 2) {
     //       block.setTrustScore(1.0);
     //   } else {
     //       block.setTrustScore(0.4);
     //   }
    }

    private void evaluationLogicThree(Block block) {
     //   ParkingSpace parkingSpace = (ParkingSpace) block.getData();
     //   if (parkingSpace.getParkedSpace() > 250 && parkingSpace.getFreeSpace() < 100) {
     //       block.setTrustScore(1.0);
      //  } else {
      //      block.setTrustScore(0.2);
       // }
    }

    @GetMapping("/trusts")
    public List<Trust> trust() {

        double temperatureScore = 0.0;
        double smartHomeScore = 0.0;
        double parkingSpaceScore = 0.0;

        for (int i = 0; i < blockChain.size(); i++) {
            String previousHash = i == 0 ? "0"
                    : blockChain.get(i - 1)
                    .getHash();
            boolean flag = blockChain.get(i)
                    .getHash()
                    .equals(blockChain.get(i)
                            .calculateBlockHash())
                    && previousHash.equals(blockChain.get(i)
                    .getPreviousHash())
                    && blockChain.get(i)
                    .getHash()
                    .substring(0, prefix)
                    .equals(prefixString);
            if (blockChain.get(i).getNode() == 1) {
                smartHomeScore = blockChain.get(i).getTrustScore();
            } else  if (blockChain.get(i).getNode() == 2) {
                temperatureScore = blockChain.get(i).getTrustScore();
            } else  if (blockChain.get(i).getNode() == 3) {
                parkingSpaceScore = blockChain.get(i).getTrustScore();
            }

            if (flag) {
                System.out.println("Blocks in the block chain is validated");
            }
        }

        trusts.clear();
        Trust trust = new Trust();
        trust.setNode("Temperature Node");
        trust.setServiceName("temperatures");
        trust.setServiceProvider("Temperature Node");
        trust.setRatingCriteria("evaluationLogicOne");
        trust.setScore(temperatureScore);
        trusts.add(trust);
        trust = new Trust();
        trust.setNode("Smart Home Node");
        trust.setServiceName("smartHomes");
        trust.setServiceProvider("Smart Home Node");
        trust.setRatingCriteria("evaluationLogicTwo");
        trust.setScore(smartHomeScore);
        trusts.add(trust);
        trust = new Trust();
        trust.setNode("Parking Space Node");
        trust.setServiceName("parkingSpaces");
        trust.setServiceProvider("Parking Space Node");
        trust.setRatingCriteria("evaluationLogicThree");
        trust.setScore(parkingSpaceScore);
        trusts.add(trust);
        return trusts;
    }
}
