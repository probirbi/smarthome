package com.blockchain.iot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Setter
@Getter
public class Block {
    private String hash;
    private String previousHash;
    private BlockType blockType;
    private int blockNumber;
    private Object data;
    //   @JsonInclude(JsonInclude.Include.NON_NULL)
    //   private Sensor sensor;
    //   @JsonInclude(JsonInclude.Include.NON_NULL)
    //   private SmartHome smartHome;

    private long requestTimeStamp;
    private long responseTimeStamp;
    private String serviceRequestedBy;
    //private String serviceResponseBy;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String ratingDoneBy;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    // private String evaluatedBy;
    private String serviceProvidedBy;
    private String blockCreatedBy;
    private long timeStamp;
  //  @JsonIgnore
    private int nonce;
  //  @JsonIgnore
    private Integer node;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double trustScore;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double rating;
    //@JsonInclude(JsonInclude.Include.NON_EMPTY)
    //private String comment;

    public Block() {
    }

    public Block(Object data, BlockType blockType, String previousHash, long timeStamp, int node, String blockCreatedBy) {
        this.blockType = blockType;
        //   this.description = description;
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = timeStamp;
        this.hash = calculateBlockHash();
        this.node = node;
        this.blockCreatedBy = blockCreatedBy;
    }

    public String mineBlock(int prefix) {
        this.hash = calculateBlockHash();
        String prefixString = new String(new char[prefix]).replace('\0', '0');
        while (!hash.substring(0, prefix)
                .equals(prefixString)) {
            nonce++;
            hash = calculateBlockHash();
        }
        return hash;
    }

    public String calculateBlockHash() {
        String dataToHash = "";
        if (trustScore != null && trustScore > 0) {
            dataToHash = previousHash
                    + Long.toString(timeStamp)
                    + Integer.toString(nonce)
                    + data + Double.toString(trustScore);
        } else if (rating != null && rating > 0) {
            dataToHash = previousHash
                    + Long.toString(timeStamp)
                    + Integer.toString(nonce)
                    + data + Double.toString(rating);
        } else {
            dataToHash = previousHash
                    + Long.toString(timeStamp)
                    + Integer.toString(nonce)
                    + data;
        }
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
        //System.out.println("Data for hash value: "+dataToHash);
        //System.out.println(buffer.toString());
        return buffer.toString();
    }

}
