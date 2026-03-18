package com.operimus.Marketing.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;




@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("POST")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Post extends CampaignMaterials {

    @Column(name = "scheduled_data")
    private LocalDateTime scheduled_date;
    
    @Column(name = "file_path")
    private String file_path;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("post")
    private List<PostPlatform> platforms = new ArrayList<>();
    
}