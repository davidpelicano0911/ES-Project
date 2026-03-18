package com.operimus.Marketing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * PublicLandingPageDTO - Data Transfer Object for public landing page display
 * Contains only the minimal information needed to display a landing page publicly
 * This DTO restricts sensitive information and metadata not needed for public viewing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicLandingPageDTO {
    
    private Long id;
    private String body;
    private String design;
}
