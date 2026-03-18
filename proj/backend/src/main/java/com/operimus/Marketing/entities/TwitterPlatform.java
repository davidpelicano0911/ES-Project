package com.operimus.Marketing.entities;

import com.operimus.Marketing.services.FacebookApiService;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;  

@Entity
@DiscriminatorValue("TWITTER")
public class TwitterPlatform extends PostPlatform {



    @Override
    public void publish(FacebookApiService facebookApiService) {
        try {
         
            
            setStatus(PostStatus.PUBLISHED);

        } catch (Exception e) {
            setStatus(PostStatus.FAILED);
        }
    }

}