package com.operimus.Marketing.controllers;


import com.operimus.Marketing.entities.Post;
import com.operimus.Marketing.repositories.PostRepository;
import com.operimus.Marketing.security.TestSecurityConfig;
import com.operimus.Marketing.services.HubSpotLeadService;
import com.operimus.Marketing.services.MarketingIndexerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.operimus.Marketing.services.MarketingIndexerService;
import org.springframework.boot.test.mock.mockito.MockBean; 
import java.time.format.DateTimeFormatter;




import java.util.List;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.operimus.Marketing.services.PostService;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import java.util.Map;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "api.version=v1",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://example.com/issuer"
})
class PostControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private MarketingIndexerService marketingIndexerService;

    @MockBean
    private HubSpotLeadService hubSpotLeadService;

    @Value("${api.version}")
    private String apiVersion;

    @BeforeEach
    void setupDefaults() {
        // Default mocks
        when(postService.getAllPosts()).thenReturn(List.of());
        when(postService.getPostById(anyLong())).thenReturn(null);
        when(postService.getImage(anyString())).thenReturn(null);
        when(postService.getPostStatistics(anyLong())).thenReturn(Map.of());
    }

    @Test
    void givenExistingPosts_whenGetAll_thenReturnList() throws Exception {
        Post post = new Post();
        post.setId(1L);
        post.setName("Post 1");

        when(postService.getAllPosts()).thenReturn(List.of(post));

        mockMvc.perform(get("/api/" + apiVersion + "/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Post 1"));
    }

    @Test
    void givenValidId_whenGetPostById_thenReturnPost() throws Exception {
        Post post = new Post();
        post.setId(10L);
        post.setName("Find Me");

        when(postService.getPostById(10L)).thenReturn(post);

        mockMvc.perform(get("/api/" + apiVersion + "/posts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Find Me"));
    }

    @Test
    void givenInvalidId_whenGetPostById_thenReturn404() throws Exception {
        when(postService.getPostById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/" + apiVersion + "/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenValidData_whenCreatePost_thenReturn200() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "fake".getBytes()
        );

        Post saved = new Post();
        saved.setId(1L);
        saved.setName("New Post");

        when(postService.createPost(any())).thenReturn(saved);

        mockMvc.perform(multipart("/api/" + apiVersion + "/posts")
                        .file(image)
                        .param("name", "New Post")
                        .param("description", "desc")
                        .param("platforms", "FACEBOOK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Post"));
    }

    @Test
    void givenExistingPost_whenDelete_thenReturn200() throws Exception {
        when(postService.deletePost(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/" + apiVersion + "/posts/1"))
                .andExpect(status().isOk());
    }

    @Test
    void givenValidUpdate_whenPut_thenReturnUpdated() throws Exception {
        Post updated = new Post();
        updated.setId(1L);
        updated.setName("Updated");

        when(postService.updatePost(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/" + apiVersion + "/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Updated",
                              "description": "Changed",
                              "platforms": ["INSTAGRAM"],
                              "scheduled_date": "2030-01-01T10:00:00"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }
}
