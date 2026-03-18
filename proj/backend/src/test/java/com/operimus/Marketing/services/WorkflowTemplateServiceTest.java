package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.WorkflowTemplate;
import com.operimus.Marketing.repositories.WorkflowTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowTemplateServiceTest {

    @Mock
    private WorkflowTemplateRepository templateRepository;

    @InjectMocks
    private WorkflowTemplateService service;

    private WorkflowTemplate template1;
    private WorkflowTemplate template2;

    @BeforeEach
    void setUp() {
        template1 = new WorkflowTemplate();
        template1.setId(1L);
        template1.setName("Template 1");
        template1.setDescription("Description 1");
        template1.setTemplateData("{}");

        template2 = new WorkflowTemplate();
        template2.setId(2L);
        template2.setName("Template 2");
        template2.setDescription("Description 2");
        template2.setTemplateData("{}");

        List<WorkflowTemplate> all = Arrays.asList(template1, template2);

        when(templateRepository.findAll()).thenReturn(all);
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template1));
        when(templateRepository.findById(2L)).thenReturn(Optional.of(template2));
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());
        when(templateRepository.existsById(1L)).thenReturn(true);
        when(templateRepository.existsById(99L)).thenReturn(false);

        when(templateRepository.save(any(WorkflowTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void whenCreateTemplate_thenReturnSavedTemplate() {
        WorkflowTemplate newTemplate = new WorkflowTemplate();
        newTemplate.setName("Template");
        newTemplate.setDescription("A new template");
        newTemplate.setTemplateData("{}");

        when(templateRepository.save(any(WorkflowTemplate.class))).thenReturn(newTemplate);

        WorkflowTemplate saved = service.createWorkflowTemplate(newTemplate);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Template");
        assertThat(saved.getTemplateData()).isNotNull();
        verify(templateRepository, times(1)).save(newTemplate);
    }

    @Test
    void whenGetAllTemplates_thenReturnList() {
        List<WorkflowTemplate> result = service.getAllWorkflowTemplates();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(WorkflowTemplate::getName)
                .containsExactlyInAnyOrder("Template 1", "Template 2");

        verify(templateRepository, times(1)).findAll();
    }

    @Test
    void whenGetByValidId_thenReturnTemplate() {
        WorkflowTemplate found = service.getWorkflowTemplateById(1L);

        assertThat(found).isEqualTo(template1);
        assertThat(found.getName()).isEqualTo("Template 1");
        verify(templateRepository, times(1)).findById(1L);
    }

    @Test
    void whenGetByInvalidId_thenThrowEntityNotFound() {
        assertThatThrownBy(() -> service.getWorkflowTemplateById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Template not found");

        verify(templateRepository, times(1)).findById(99L);
    }

    @Test
    void whenUpdateTemplate_thenFieldsAreUpdatedAndSaved() {
        WorkflowTemplate update = new WorkflowTemplate();
        update.setName("Updated Name");
        update.setDescription("New description");
        update.setTemplateData("{}");

        WorkflowTemplate result = service.updateWorkflowTemplate(1L, update);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("New description");

        verify(templateRepository, times(1)).findById(1L);
        verify(templateRepository, times(1)).save(argThat(t ->
                t.getId().equals(1L) &&
                "Updated Name".equals(t.getName())
        ));
    }

    @Test
    void whenUpdateWithNullFields_thenOnlyNonNullAreUpdated() {
        WorkflowTemplate update = new WorkflowTemplate();
        update.setName("Only Name Changes");

        WorkflowTemplate result = service.updateWorkflowTemplate(1L, update);

        assertThat(result.getName()).isEqualTo("Only Name Changes");
        assertThat(result.getDescription()).isEqualTo(template1.getDescription());
        assertThat(result.getTemplateData()).isEqualTo(template1.getTemplateData());

        verify(templateRepository, times(1)).save(result);
    }

    @Test
    void whenUpdateInvalidId_thenThrowEntityNotFound() {
        WorkflowTemplate update = new WorkflowTemplate();
        update.setName("Template");

        assertThatThrownBy(() -> service.updateWorkflowTemplate(99L, update))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Template not found");

        verify(templateRepository, times(1)).findById(99L);
        verify(templateRepository, never()).save(any());
    }

    @Test
    void whenDeleteValidId_thenDeleteIsCalled() {
        service.deleteWorkflowTemplate(1L);

        verify(templateRepository, times(1)).existsById(1L);
        verify(templateRepository, times(1)).deleteById(1L);
    }

    @Test
    void whenDeleteInvalidId_thenThrowEntityNotFound() {
        assertThatThrownBy(() -> service.deleteWorkflowTemplate(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Template not found");

        verify(templateRepository, times(1)).existsById(99L);
        verify(templateRepository, never()).deleteById(any());
    }
}