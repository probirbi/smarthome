package com.blockchain.iot;

import com.blockchain.iot.data.TestData;
import com.blockchain.iot.model.Block;
import com.blockchain.iot.model.SmartHome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class SmartHomeController {

    List<SmartHome> smartHomes = new ArrayList<SmartHome>();

    List<Block> blockChain = new ArrayList<Block>();

    int prefix = 1;

    String prefixString = new String(new char[prefix]).replace('\0', '0');

    @Autowired
    RestTemplate restTemplate;

 /*   @GetMapping("/get-data")
    public String getData() {

        String responseFromNode2 = restTemplate.getForObject("http://localhost:8082/get-respose-from-2", String.class);

        return "response from node 1  sum \n" + responseFromNode2;
    }*/

    @GetMapping("/smarthomes")
    public List<SmartHome> getSmartHome() {

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
        return smartHomes;
    }

    @PostMapping("/smarthomes")
    public String saveSmartHome(@RequestBody SmartHome smartHome) {
        smartHomes.add(smartHome);

        if (blockChain.size() == 0) {
            Block smartHomeBlock = new Block("This is iotblockchain1 block", smartHome, "0", new Date().getTime());
            smartHomeBlock.mineBlock(prefix);
            blockChain.add(smartHomeBlock);
        } else {
            Block smartHomeBlock = new Block("This is iotblockchain1 block", smartHome, blockChain.get(blockChain.size() - 1).getHash(), new Date().getTime());
            smartHomeBlock.mineBlock(prefix);
            blockChain.add(smartHomeBlock);
        }
        System.out.println("Block No: "+blockChain.size());
        return "success";

    }

    @GetMapping("/seeddata")
    public void insertData() {
        TestData.callPost();
    }
}
