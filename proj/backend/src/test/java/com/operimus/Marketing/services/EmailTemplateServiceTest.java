package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.EmailTemplate;
import com.operimus.Marketing.repositories.EmailTemplateRepository;
import com.operimus.Marketing.repositories.LeadRepository;
import com.operimus.Marketing.entities.Lead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailTemplateServiceTest {

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private MailJetService mailJetService;

    @InjectMocks
    private EmailTemplateService emailTemplateService;

    private EmailTemplate template1;
    private EmailTemplate template2;
    private Lead lead1;
    private Lead lead2;


    @BeforeEach
    void setUp() {
        // Setup Template 1
        template1 = new EmailTemplate();
        template1.setId(1L);
        template1.setName("Welcome Template");
        template1.setDescription("Welcome email");
        template1.setSubject("Welcome {{firstName}}!");
        template1.setBody("Hello {{firstName}}, welcome to {{company}}!");
        template1.setDesign("<html>...</html>");

        // Setup Template 2
        template2 = new EmailTemplate();
        template2.setId(2L);
        template2.setName("Promo Template");
        template2.setDescription("Promotion email");
        template2.setSubject("Special Offer for {{firstName}}");
        template2.setBody("Hi {{firstName}}, get {{discount}} off at {{company}}!");

        // Setup Leads
        lead1 = new Lead();
        lead1.setId(1L);
        lead1.setEmail("john@example.com");
        lead1.setFirstName("John");
        lead1.setLastName("Doe");

        lead2 = new Lead();
        lead2.setId(2L);
        lead2.setEmail("jane@example.com");
        lead2.setFirstName("Jane");
        lead2.setLastName("Smith");

        // Mock repository behaviors
        when(emailTemplateRepository.findAll()).thenReturn(Arrays.asList(template1, template2));
        when(emailTemplateRepository.findById(1L)).thenReturn(Optional.of(template1));
        when(emailTemplateRepository.findById(2L)).thenReturn(Optional.of(template2));
        when(emailTemplateRepository.findById(99L)).thenReturn(Optional.empty());
        when(emailTemplateRepository.existsByName("Welcome Template")).thenReturn(true);
        when(emailTemplateRepository.existsByName("New Template")).thenReturn(false);

        when(leadRepository.findAll()).thenReturn(Arrays.asList(lead1, lead2));

        // save() returns the argument (identity)
        when(emailTemplateRepository.save(any(EmailTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void whenCreateTemplateWithUniqueName_thenReturnSavedTemplate() {
        EmailTemplate newTemplate = new EmailTemplate();
        newTemplate.setName("New Template");
        newTemplate.setSubject("Test Subject");
        newTemplate.setBody("Test Body");

        EmailTemplate saved = emailTemplateService.createTemplate(newTemplate);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Template");
        verify(emailTemplateRepository).existsByName("New Template");
        verify(emailTemplateRepository).save(newTemplate);
    }

    @Test
    void whenCreateTemplateWithDuplicateName_thenThrowException() {
        EmailTemplate duplicate = new EmailTemplate();
        duplicate.setName("Welcome Template");

        assertThatThrownBy(() -> emailTemplateService.createTemplate(duplicate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Template name already exists");

        verify(emailTemplateRepository).existsByName("Welcome Template");
        verify(emailTemplateRepository, never()).save(any());
    }

    @Test
    void whenGetTemplateByValidId_thenReturnOptionalWithTemplate() {
        Optional<EmailTemplate> found = emailTemplateService.getTemplateById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Welcome Template");
        verify(emailTemplateRepository).findById(1L);
    }

    @Test
    void whenGetTemplateByInvalidId_thenReturnEmptyOptional() {
        Optional<EmailTemplate> found = emailTemplateService.getTemplateById(99L);

        assertThat(found).isEmpty();
        verify(emailTemplateRepository).findById(99L);
    }

    @Test
    void whenGetAllTemplates_thenReturnListOfTemplates() {
        List<EmailTemplate> result = emailTemplateService.getAllTemplates();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(EmailTemplate::getName)
                .containsExactlyInAnyOrder("Welcome Template", "Promo Template");
        verify(emailTemplateRepository).findAll();
    }

    @Test
    void whenUpdateTemplateWithValidId_thenReturnUpdatedTemplate() {
        EmailTemplate updateData = new EmailTemplate();
        updateData.setName("Updated Welcome");
        updateData.setSubject("New Subject: {{firstName}}");
        updateData.setBody(null); // should not overwrite

        EmailTemplate updated = emailTemplateService.updateTemplate(1L, updateData);

        assertThat(updated.getId()).isEqualTo(1L);
        assertThat(updated.getName()).isEqualTo("Updated Welcome");
        assertThat(updated.getSubject()).isEqualTo("New Subject: {{firstName}}");
        assertThat(updated.getBody()).isEqualTo("Hello {{firstName}}, welcome to {{company}}!"); // unchanged
        verify(emailTemplateRepository).findById(1L);
        verify(emailTemplateRepository).save(template1);
    }

    @Test
    void whenUpdateTemplateWithInvalidId_thenThrowException() {
        EmailTemplate updateData = new EmailTemplate();
        updateData.setName("Should Fail");

        assertThatThrownBy(() -> emailTemplateService.updateTemplate(99L, updateData))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Template not found");

        verify(emailTemplateRepository).findById(99L);
        verify(emailTemplateRepository, never()).save(any());
    }

    @Test
    void whenDeleteTemplate_thenRepositoryDeleteByIdIsCalled() {
        emailTemplateService.deleteTemplate(1L);

        verify(emailTemplateRepository).deleteById(1L);
    }

    @Test
    void whenTestEmailTemplate_thenSendEmailsToAllSubscribersWithConvertedPlaceholders() {
        String testEmail = "test@example.com";
        emailTemplateService.test_emailTemplate(1L, testEmail);

        verify(emailTemplateRepository).findById(1L);
        verify(mailJetService).sendEmail(
                any(),
                eq("ferreira.manuel.henrique04@gmail.com"),
                eq("Welcome {{var:firstName}}!"),
                eq("Hello {{var:firstName}}, welcome to {{var:company}}!")
        );
    }

    @Test
    void whenTestEmailTemplateWithInvalidId_thenThrowException() {
        assertThatThrownBy(() -> emailTemplateService.test_emailTemplate(99L, "test@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Template not found");

        verify(emailTemplateRepository).findById(99L);
        verify(mailJetService, never()).sendEmail(any(), any(), any(), any());
    }
}