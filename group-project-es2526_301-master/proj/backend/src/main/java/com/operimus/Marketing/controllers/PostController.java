package com.operimus.Marketing.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.operimus.Marketing.services.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.operimus.Marketing.dto.PostDTO;
import com.operimus.Marketing.entities.Post;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import org.springframework.core.io.Resource;
import java.util.Map;

import com.operimus.Marketing.services.MarketingIndexerService;

@RestController
@RequestMapping("/api/${api.version:v1}/posts")
@Tag(name = "Posts", description = "Endpoints for managing posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private MarketingIndexerService marketingIndexerService;

    @Operation(summary = "Get all posts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of posts", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Post.class)
            )
        }),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Create a new post")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post created successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Post.class)
            )
        }),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> createPost(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Post data to create",
            required = true,
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(implementation = PostDTO.class)
            )
        ) @ModelAttribute PostDTO postDTO) {
        try {
            Post created = postService.createPost(postDTO);
            marketingIndexerService.indexPost(created);
            return ResponseEntity.ok(created);
        } catch (MultipartException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Delete a post by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@Parameter(description = "ID of the post to delete", example = "1") @PathVariable Long id) {
        try {
            boolean deleted = postService.deletePost(id);
            if (deleted) {
                marketingIndexerService.deleteFromIndex(id, "POST");
            }
            return deleted
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get a post by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the post", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Post.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@Parameter(description = "ID of the post to retrieve", example = "1") @PathVariable Long id) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "Update an existing post")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post updated successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Post.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
        @Parameter(description = "ID of the post to update", example = "1") @PathVariable Long id, 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated post data",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PostDTO.class)
            )
        ) @RequestBody PostDTO postDTO) {
        try {
            Post updated = postService.updatePost(id, postDTO);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            marketingIndexerService.indexPost(updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get an image by filename")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of the image", content = {
            @Content(
                mediaType = "image/jpeg",
                schema = @Schema(type = "string", format = "binary")
            )
        }),
        @ApiResponse(responseCode = "404", description = "Image not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@Parameter(description = "Filename of the image to retrieve", example = "example.jpg") @PathVariable String filename) {
        try {
            System.out.println("Fetching image: " + filename);
            Resource resource = postService.getImage(filename);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Publish a post")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post published successfully", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(type = "string", example = "Published successfully")
            )
        }),
        @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/{id}/publish")
    public ResponseEntity<String> publishPost(@Parameter(description = "ID of the post to publish", example = "1") @PathVariable Long id) {

        Post post = postService.getPostById(id);
        if (post == null) return ResponseEntity.notFound().build();

        postService.publish(post);

        return ResponseEntity.ok("Published successfully");
    }

    @Operation(summary = "Get post statistics by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful retrieval of post statistics", content = {
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        }),
        @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getPostStats(@Parameter(description = "ID of the post to retrieve statistics for", example = "1") @PathVariable Long id) {
        Map<String, Integer> stats = postService.getPostStatistics(id);
        return ResponseEntity.ok(stats);
    }
}