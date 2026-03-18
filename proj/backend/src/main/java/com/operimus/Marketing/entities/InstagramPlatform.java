package com.operimus.Marketing.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import com.operimus.Marketing.services.FacebookApiService;

@Entity
@DiscriminatorValue("INSTAGRAM")
public class InstagramPlatform extends PostPlatform {

    @Override
    public void publish(FacebookApiService facebookApiService) {
        try {
         
            
            setStatus(PostStatus.PUBLISHED);

        } catch (Exception e) {
            setStatus(PostStatus.FAILED);
        }
    }
}