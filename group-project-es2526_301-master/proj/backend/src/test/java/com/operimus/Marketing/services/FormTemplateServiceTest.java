package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.FormTemplate;
import com.operimus.Marketing.repositories.FormTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FormTemplateServiceTest {

    @Mock
    private FormTemplateRepository formTemplateRepository;

    @InjectMocks
    private FormTemplateService formTemplateService;

    private FormTemplate template1;
    private FormTemplate template2;

    @BeforeEach
    void setUp() {
        template1 = new FormTemplate("Lead Form", "Basic lead capture", 101L, "{\"fields\":[]}");
        template1.setId(1L);
        template1.setCreatedAt(new Date());
        template1.setUpdatedAt(new Date());
        template1.setIsPublished(true);

        template2 = new FormTemplate("Survey Form", "Customer feedback", 102L, "{\"type\":\"survey\"}");
        template2.setId(2L);
        template2.setCreatedAt(new Date());
        template2.setUpdatedAt(new Date());
        template2.setIsPublished(false);

        List<FormTemplate> templates = Arrays.asList(template1, template2);

        when(formTemplateRepository.findAll()).thenReturn(templates);
        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template1));
        when(formTemplateRepository.findById(2L)).thenReturn(Optional.of(template2));
        when(formTemplateRepository.findById(99L)).thenReturn(Optional.empty());
        when(formTemplateRepository.save(any(FormTemplate.class)))
                .thenAnswer(invocation -> {
                    FormTemplate saved = invocation.getArgument(0);
                    if (saved.getId() == null) {
                        saved.setId(100L);
                    }
                    saved.setCreatedAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : new Date());
                    saved.setUpdatedAt(new Date());
                    return saved;
                });
    }

    @Test
    void whenCreateFormTemplate_thenReturnSavedTemplate() {
        FormTemplate newTemplate = new FormTemplate("Contact Form", "Contact us form", 103L, "{\"fields\":[\"name\",\"email\"]}");

        FormTemplate saved = formTemplateService.createFormTemplate(newTemplate);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Contact Form");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getIsPublished()).isFalse();

        verify(formTemplateRepository, times(1)).save(newTemplate);
    }

    @Test
    void whenGetAllFormTemplates_thenReturnListOfTemplates() {
        List<FormTemplate> result = formTemplateService.getAllFormTemplates();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(FormTemplate::getName)
                .containsExactlyInAnyOrder("Lead Form", "Survey Form");

        verify(formTemplateRepository, times(1)).findAll();
    }

    @Test
    void whenGetFormTemplateByValidId_thenReturnTemplate() {
        FormTemplate found = formTemplateService.getFormTemplate(1L);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Lead Form");
        assertThat(found.getIsPublished()).isTrue();

        verify(formTemplateRepository, times(1)).findById(1L);
    }

    @Test
    void whenGetFormTemplateByInvalidId_thenReturnNull() {
        FormTemplate found = formTemplateService.getFormTemplate(99L);

        assertThat(found).isNull();

        verify(formTemplateRepository, times(1)).findById(99L);
    }

    @Test
    void whenUpdateFormTemplate_thenReturnUpdatedTemplate() {
        FormTemplate updateData = new FormTemplate();
        updateData.setName("Updated Lead Form");
        updateData.setDescription("Updated description");
        updateData.setFormJson("{\"fields\":[\"name\",\"phone\"]}");
        updateData.setIsPublished(false);

        FormTemplate result = formTemplateService.updateFormTemplate(1L, updateData);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Updated Lead Form");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getFormJson()).isEqualTo("{\"fields\":[\"name\",\"phone\"]}");
        assertThat(result.getIsPublished()).isFalse();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(formTemplateRepository, times(1)).findById(1L);
        verify(formTemplateRepository, times(1)).save(argThat(template ->
                template.getId().equals(1L) &&
                "Updated Lead Form".equals(template.getName()) &&
                template.getIsPublished().equals(false)
        ));
    }

    @Test
    void whenUpdateFormTemplate_withInvalidId_thenThrowNotFoundException() {
        FormTemplate updateData = new FormTemplate();
        updateData.setName("Should not be saved");

        assertThatThrownBy(() -> formTemplateService.updateFormTemplate(99L, updateData))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
                .hasMessageContaining("Form template not found");

        verify(formTemplateRepository, times(1)).findById(99L);
        verify(formTemplateRepository, never()).save(any());
    }

    @Test
    void whenCreateFormTemplate_thenTimestampsAreSet() {
        FormTemplate newTemplate = new FormTemplate("Test Form", null, 105L, null);

        FormTemplate saved = formTemplateService.createFormTemplate(newTemplate);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    
    @Test
    void whenUpdateFormTemplate_thenUpdatedAtIsRefreshed() throws InterruptedException {
        Date oldUpdatedAt = template1.getUpdatedAt();

        Thread.sleep(50);
        FormTemplate updateData = new FormTemplate();
        updateData.setName("New Name");

        FormTemplate updated = formTemplateService.updateFormTemplate(1L, updateData);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotEqualTo(oldUpdatedAt);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }
         
}