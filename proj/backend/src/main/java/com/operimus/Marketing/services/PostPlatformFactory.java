package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.Post;
import com.operimus.Marketing.entities.PostPlatform;
import com.operimus.Marketing.entities.FacebookPlatform;
import com.operimus.Marketing.entities.PostStatus;
import com.operimus.Marketing.entities.TwitterPlatform;
import com.operimus.Marketing.entities.InstagramPlatform;

public class PostPlatformFactory {

    // Factory method
    public static PostPlatform create(String type, Post post) {
        PostPlatform platform = null;
        switch (type.toUpperCase()) {
            case "FACEBOOK":
                FacebookPlatform facebook = new FacebookPlatform();
                facebook.setPost(post);
                facebook.setStatus(PostStatus.SCHEDULED);
                return facebook;


            case "TWITTER":
                platform = new TwitterPlatform();
                break;

            case "INSTAGRAM":
                platform = new InstagramPlatform();
                break;

            default:
                throw new IllegalArgumentException("Unknown platform: " + type);
        }
        platform.setPost(post);
        platform.setStatus(PostStatus.SCHEDULED);
        return platform;
    }

    
}