package com.operimus.Marketing.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class FacebookApiService {

    @Value("${facebook.page.id}")
    private String pageId;

    @Value("${facebook.access.token}")
    private String pageAccessToken;

    @Value("${facebook.public.page.id}")
    private String publicPageId;

    private static final String GRAPH_URL = "https://graph.facebook.com/v24.0/";

    private final RestTemplate restTemplate = new RestTemplate();


    public String uploadPhoto(byte[] imageBytes, String filename) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("access_token", pageAccessToken);
        body.add("source", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        String url = GRAPH_URL + pageId + "/photos?published=false";

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Map.class
        );

        return response.getBody().get("id").toString();
    }


    public String publishPost(String message, String link) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("access_token", pageAccessToken);
        body.add("message", message);

        if (link != null) {
            body.add("link", link);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.exchange(
                GRAPH_URL + pageId + "/feed",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        return response.getBody().get("id").toString();
    }

    public String publishPostWithImage(String message, String mediaFbid) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("access_token", pageAccessToken);
        body.add("message", message);
        body.add("attached_media[0]", "{\"media_fbid\":\"" + mediaFbid + "\"}");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<Map> response = restTemplate.exchange(
                GRAPH_URL + pageId + "/feed",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        return response.getBody().get("id").toString();
    }

  
    public Map<String, Integer> getPostStatistics(String postId) {

        String url = GRAPH_URL + postId
                + "?fields=likes.summary(true),shares.summary(true),comments.summary(true)"
                + "&access_token=" + pageAccessToken;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> data = response.getBody();

        Map<String, Integer> stats = new HashMap<>();
        int likes = 0;
        int shares = 0;
        int comments = 0;


        if (data.containsKey("likes")) {
            Map likesObj = (Map) data.get("likes");
            Map summary = (Map) likesObj.get("summary");
            likes = (int) summary.get("total_count");
        }

        if (data.containsKey("shares")) {
            Map sharesObj = (Map) data.get("shares");
            shares = (int) sharesObj.get("count");
        }

        if (data.containsKey("comments")) {
            Map commentsObj = (Map) data.get("comments");
            Map summary = (Map) commentsObj.get("summary");
            comments = (int) summary.get("total_count");
        }


        stats.put("likes", likes);
        stats.put("shares", shares);
        stats.put("comments", comments);

        return stats;
    }


    public Integer getPostReach(String postId) {

        String url = GRAPH_URL + postId + "/insights"
                + "?metric=post_impressions_unique"
                + "&access_token=" + pageAccessToken;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            var data = (List<Map<String, Object>>) body.get("data");
            if (data.isEmpty()) return 0;

            var item = data.get(0);
            var values = (List<Map<String, Object>>) item.get("values");
            return (Integer) values.get(0).get("value");

        } catch (Exception e) {
            return -1; 
        }
    }


    public String getPublicPostUrl(String platformPostId) {
        if (platformPostId == null) return null;

        String[] parts = platformPostId.split("_");
        if (parts.length < 2) {
            return null;
        }

        String postId = parts[1];

        return "https://www.facebook.com/" + publicPageId + "/posts/" + postId;
    }




}