package com.blockchain.iot.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Services {
    private String node;
    private String serviceName;
    private String autoGenerate;
    private String serviceProvider;
    private String ratingCriteria;
    private double score;

    public Services(String node, String serviceName, String serviceProvider, String ratingCriteria, double score) {
        this.node = node;
        this.serviceName = serviceName;
        this.serviceProvider = serviceProvider;
        this.ratingCriteria = ratingCriteria;
        this.score = score;
    }

    public Services() {

    }
}
