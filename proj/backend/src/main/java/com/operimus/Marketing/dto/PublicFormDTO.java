package com.operimus.Marketing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * PublicFormDTO - Data Transfer Object for public form display
 * Contains only the minimal information needed to display a form publicly
 * This DTO restricts sensitive information and metadata not needed for public viewing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicFormDTO {
    
    private Long id;
    private String formJson;
}
