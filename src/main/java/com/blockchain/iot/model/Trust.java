package com.blockchain.iot.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class Trust {
    private String node;
    private String serviceName;
    private String serviceProvider;
    private String ratingCriteria;
    private double score;
    private Double currentTrustScore;
    private Double latestTrustScore;
    private Double currentRatingScore;
    private Double latestRatingScore;
    private String randomSelected;

    public Trust(String node, String serviceName, String serviceProvider, String ratingCriteria, double score) {
        this.node = node;
        this.serviceName = serviceName;
        this.serviceProvider = serviceProvider;
        this.ratingCriteria = ratingCriteria;
        this.score = score;
    }

    public Trust() {

    }
}
