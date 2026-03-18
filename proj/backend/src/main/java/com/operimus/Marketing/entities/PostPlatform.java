package com.operimus.Marketing.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.operimus.Marketing.services.FacebookApiService;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import jakarta.persistence.Transient;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
public abstract class PostPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonBackReference
    private Post post;

    private String platformPostId;

    private String platformType;

    
    @Enumerated(EnumType.STRING)
    private PostStatus status;
    private Integer numberLikes;
    private Integer numberShares;
    private Integer numberComments;
    private Integer numberReachs;

    @Transient
    private String postUrl;
    
    @Transient
    private Double performanceScore;

    public Double getPerformanceScore() {
        double reachScore = (numberReachs != null ? numberReachs : 0) * 0.50;
        double shareScore = (numberShares != null ? numberShares : 0) * 0.25;
        double commentScore = (numberComments != null ? numberComments : 0) * 0.15;
        double likeScore = (numberLikes != null ? numberLikes : 0) * 0.10;

        return reachScore + shareScore + commentScore + likeScore;
    }
    

    public abstract void publish(FacebookApiService facebookApiService);


    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    



    @PrePersist
    public void prePersist() {
        if (platformType == null) {
            DiscriminatorValue dv = this.getClass().getAnnotation(DiscriminatorValue.class);
            if (dv != null) {
                platformType = dv.value();
            }
        }
    }
}
