package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.LandingPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class LandingPageRepositoryTest {

    @Autowired
    private LandingPageRepository landingPageRepository;

    @BeforeEach
    void cleanUp() {
        landingPageRepository.deleteAll(); 
    }

    @Test
    void whenSave_thenLandingPageIsPersisted() {
        LandingPage landingPage = new LandingPage();
        landingPage.setName("New Landing Page");
        landingPage.setDescription("This is a new landing page.");
        landingPage.setBody("<p>New landing page content</p>");
        landingPage.setDesign("{}");

        LandingPage savedLandingPage = landingPageRepository.save(landingPage);

        assertNotNull(savedLandingPage.getId());
        assertEquals("New Landing Page", savedLandingPage.getName());
    }

    @Test
    void whenExistsByName_thenReturnTrue() {
        LandingPage landingPage = new LandingPage();
        landingPage.setName("Existing Landing Page");
        landingPage.setDescription("This landing page exists.");
        landingPage.setBody("<h1>Hello World</h1>");
        landingPage.setDesign("{\"version\":1}");

        landingPageRepository.save(landingPage);

        boolean exists = landingPageRepository.existsByName("Existing Landing Page");
        assertTrue(exists);
    }

    @Test
    void whenNotExistsByName_thenReturnFalse() {
        boolean exists = landingPageRepository.existsByName("Nonexistent Landing Page");
        assertFalse(exists);
    }

    @Test
    void whenDelete_thenLandingPageIsRemoved() {
        LandingPage landingPage = new LandingPage();
        landingPage.setName("Landing Page to Delete");
        landingPage.setDescription("This landing page will be deleted.");
        landingPage.setBody("<h1>Hello World</h1>");
        landingPage.setDesign("{}");

        landingPageRepository.save(landingPage);
        landingPageRepository.delete(landingPage);

        LandingPage found = landingPageRepository.findById(landingPage.getId()).orElse(null);
        assertNull(found);
    }
}
