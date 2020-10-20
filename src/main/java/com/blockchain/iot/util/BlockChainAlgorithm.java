package com.blockchain.iot.util;

import com.blockchain.iot.model.Block;
import com.blockchain.iot.model.BlockType;
import com.blockchain.iot.model.Trust;

import java.text.DecimalFormat;
import java.util.*;

public class BlockChainAlgorithm {

    public static Integer trustConsensusAlgorithm(HashMap<Integer, Double> mapTrustScore) {
        Object[] crunchifyKeys = mapTrustScore.keySet().toArray();

        System.out.println("map size " + crunchifyKeys.length);
        if (crunchifyKeys.length > 0) {
            Object key = crunchifyKeys[new Random().nextInt(crunchifyKeys.length)];
            System.out.println("************ Random Value ************ \n" + key + " :: " + mapTrustScore.get(key));
            return (Integer) key;
        }
        return 0;
    }

    public static boolean exists(Block block, List<Block> blockChain) {
        for (int i = 0; i < blockChain.size(); i++) {
            if (block.getHash().equals(blockChain.get(i).getHash())) {
                return true;
            }
        }
        return false;
    }

    public static boolean validate(Block block, List<Block> blockChain) {
        for (int i = 0; i < blockChain.size(); i++) {
            if (block.getPreviousHash().equals(blockChain.get(i).getHash())) {
                return true;
            }
        }
        return false;
    }

    public static boolean validateTrustBlock(Block block, List<Block> blockChain) {
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        HashMap<Integer, Integer> mapCount = new HashMap<Integer, Integer>();
        HashMap<Integer, Double> mapTrustScore = new HashMap<Integer, Double>();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        Integer selectedNode = 0;
        List<Block> blockChainCopy = blockChain;
        for (int i = 0; i < blockChainCopy.size(); i++) {
            if (blockChainCopy.get(i).getBlockType().equals(BlockType.RATING)) {
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
            }
        }
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Double trustScore = 0.0;
            if (mapCount.get(pair.getKey()) != null) {
                trustScore = (Double) pair.getValue() / mapCount.get(pair.getKey());
                trustScore = Double.parseDouble(decimalFormat.format(trustScore));
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
            if ((block.getNode() + "").equals(pair.getKey() + "") && (block.getTrustScore() + "").equals(pair.getValue() + "")) {
                System.out.println("Validated Trust score block is created by high scoring node");
                return true;
            }
        }
        System.out.println("Trust score block is created by not the high scoring node");
        return false;
    }

}
