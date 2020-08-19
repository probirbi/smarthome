package com.blockchain.iot.model;

import lombok.Getter;
import lombok.Setter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Setter
@Getter
public class Block {
    private String hash;
    private String previousHash;
    private String description;
    private SmartHome data;
    private long timeStamp;
    private int nonce;

    public Block(String description, SmartHome data, String previousHash, long timeStamp) {
        this.description = description;
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = timeStamp;
        this.hash = calculateBlockHash();
    }

    public String mineBlock(int prefix) {
        String prefixString = new String(new char[prefix]).replace('\0', '0');
        while (!hash.substring(0, prefix)
                .equals(prefixString)) {
            nonce++;
            hash = calculateBlockHash();
        }
        return hash;
    }

    public String calculateBlockHash() {
        String dataToHash = previousHash
                + Long.toString(timeStamp)
                + Integer.toString(nonce)
                + data;
        MessageDigest digest = null;
        byte[] bytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        StringBuffer buffer = new StringBuffer();
        for (byte b : bytes) {
            buffer.append(String.format("%02x", b));
        }
        //System.out.println(buffer.toString());
        return buffer.toString();
    }

}
