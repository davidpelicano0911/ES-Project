package com.operimus.Marketing.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;


@Data
public class PostDTO {
    private String name;
    private String description;

    @JsonIgnore
    private MultipartFile image;

    @JsonPropertyDescription("The scheduled date. You MUST use ISO-8601 format (e.g., '2023-12-31T23:59:00').")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduled_date;

    @JsonPropertyDescription("List of platforms where the post will be published. Currently supported: FACEBOOK.")
    private List<String> platforms;
}