package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.LandingPage;
import com.operimus.Marketing.repositories.LandingPageRepository;
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LandingPageServiceTest {

    @Mock
    private LandingPageRepository landingPageRepository;

    @InjectMocks
    private LandingPageService landingPageService;

    private LandingPage landingPage1;
    private LandingPage landingPage2;

    @BeforeEach
    void setUp() {
        landingPage1 = new LandingPage();
        landingPage1.setId(1L);
        landingPage1.setName("Landing Page 1");
        landingPage1.setDescription("Description 1");
        landingPage1.setBody("<p>Body content 1</p>");
        landingPage1.setDesign("<html>...</html>");

        landingPage2 = new LandingPage();
        landingPage2.setId(2L);
        landingPage2.setName("Landing Page 2");
        landingPage2.setDescription("Description 2");
        landingPage2.setBody("<p>Body content 2</p>");
        landingPage2.setDesign("<html>...</html>");

        when(landingPageRepository.findById(1L)).thenReturn(Optional.of(landingPage1));
        when(landingPageRepository.findById(2L)).thenReturn(Optional.of(landingPage2));
        when(landingPageRepository.existsByName("Landing Page 1")).thenReturn(true);
        when(landingPageRepository.existsByName("New Landing Page")).thenReturn(false);
        when(landingPageRepository.findAll()).thenReturn(Arrays.asList(landingPage1, landingPage2));
        when(landingPageRepository.save(any(LandingPage.class))).thenAnswer(invocation -> invocation.getArgument(0));  
    }

    @Test
    void whenCreatePageWithUniqueName_thenReturnSavedPage() {
        LandingPage newPage = new LandingPage();
        newPage.setName("New Landing Page");
        newPage.setDescription("Description for new page");
        newPage.setBody("<p>Body content</p>");
        newPage.setDesign("<html>...</html>");

        LandingPage savedPage = landingPageService.createPage(newPage);

        assertNotNull(savedPage);  
        assertEquals("New Landing Page", savedPage.getName());
        verify(landingPageRepository).save(newPage); 
    }

    @Test
    void whenCreatePageWithDuplicateName_thenThrowException() {
        LandingPage duplicatePage = new LandingPage();
        duplicatePage.setName("Landing Page 1");

        assertThrows(IllegalArgumentException.class, () -> {
            landingPageService.createPage(duplicatePage);
        });

        verify(landingPageRepository).existsByName("Landing Page 1");
        verify(landingPageRepository, never()).save(duplicatePage);  
    }

    @Test
    void whenGetPageByValidId_thenReturnOptionalWithPage() {
        Optional<LandingPage> found = landingPageService.getPageById(1L);

        assertTrue(found.isPresent());  
        assertEquals("Landing Page 1", found.get().getName());
        verify(landingPageRepository).findById(1L);  
    }

    @Test
    void whenGetPageByInvalidId_thenReturnEmptyOptional() {
        Optional<LandingPage> found = landingPageService.getPageById(99L);  

        assertFalse(found.isPresent());  
        verify(landingPageRepository).findById(99L); 
    }

    @Test
    void whenGetAllPages_thenReturnListOfPages() {
        List<LandingPage> pages = landingPageService.getAllPages();

        assertEquals(2, pages.size());  
        assertEquals("Landing Page 1", pages.get(0).getName());
        assertEquals("Landing Page 2", pages.get(1).getName());
        verify(landingPageRepository).findAll(); 
    }

    @Test
    void whenUpdatePageWithValidId_thenReturnUpdatedPage() {
        LandingPage updatedPage = new LandingPage();
        updatedPage.setName("Updated Landing Page");
        updatedPage.setDescription("Updated description");

        LandingPage updated = landingPageService.updatePage(1L, updatedPage);

        assertEquals("Updated Landing Page", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        verify(landingPageRepository).findById(1L);  
        verify(landingPageRepository).save(landingPage1);  
    }

    @Test
    void whenUpdatePageWithInvalidId_thenThrowException() {
        LandingPage updatedPage = new LandingPage();
        updatedPage.setName("Should Fail");

        assertThrows(RuntimeException.class, () -> {
            landingPageService.updatePage(99L, updatedPage);
        });

        verify(landingPageRepository).findById(99L);  
        verify(landingPageRepository, never()).save(any());  
    }

    @Test
    void whenDeletePage_thenRepositoryDeleteByIdIsCalled() {
        landingPageService.deletePage(1L);

        verify(landingPageRepository).deleteById(1L);  
    }
}
