package com.operimus.Marketing.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import com.operimus.Marketing.services.FacebookApiService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Entity
@DiscriminatorValue("FACEBOOK")
public class FacebookPlatform extends PostPlatform {


    public FacebookPlatform() {}


    @Override
    public void publish(FacebookApiService facebookApiService) {
        try {
            String message = getPost().getDescription();
            String filename = getPost().getFile_path();

            String mediaFbid = null;

            if (filename != null) {
                Path path = Paths.get("uploads/").resolve(filename);
                byte[] bytes = Files.readAllBytes(path);

                mediaFbid = facebookApiService.uploadPhoto(bytes, filename);
            }

            // Sem imagem → post puro
            if (mediaFbid == null) {
                String postId = facebookApiService.publishPost(message, null);
                setPlatformPostId(postId);
                setStatus(PostStatus.PUBLISHED);
                return;
            }

            // Com imagem → post + attached_media
            String postId = facebookApiService.publishPostWithImage(message, mediaFbid);
            setPlatformPostId(postId);
            setStatus(PostStatus.PUBLISHED);

        } catch (Exception e) {
            e.printStackTrace();
            setStatus(PostStatus.FAILED);
        }
    }


    

}