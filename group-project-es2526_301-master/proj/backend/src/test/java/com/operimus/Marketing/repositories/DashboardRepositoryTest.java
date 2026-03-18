package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.Dashboard;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class DashboardRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DashboardRepository dashboardRepository;

    @BeforeEach
    void cleanUp() {
        dashboardRepository.deleteAll();
    }

    @Test
    void whenSave_thenDashboardIsPersisted() {
        Dashboard dashboard = new Dashboard("Sales Dashboard");

        Dashboard saved = dashboardRepository.save(dashboard);

        assertNotNull(saved.getId());
        assertEquals("Sales Dashboard", saved.getTitle());
    }

    @Test
    void whenFindById_thenReturnDashboard() {
        Dashboard dashboard = new Dashboard("Marketing Dashboard");
        entityManager.persist(dashboard);
        entityManager.flush();

        Dashboard found = dashboardRepository.findById(dashboard.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("Marketing Dashboard", found.getTitle());
    }

    @Test
    void whenFindById_withInvalidId_thenReturnNull() {
        Dashboard found = dashboardRepository.findById(999L).orElse(null);
        assertNull(found);
    }

    @Test
    void whenDelete_thenDashboardIsRemoved() {
        Dashboard dashboard = new Dashboard("To Be Deleted");
        entityManager.persist(dashboard);
        entityManager.flush();

        dashboardRepository.delete(dashboard);

        Dashboard deleted = dashboardRepository.findById(dashboard.getId()).orElse(null);
        assertNull(deleted);
    }

    @Test
    void whenFindAll_thenReturnAllDashboards() {
        Dashboard dashboard1 = new Dashboard("Dashboard One");
        Dashboard dashboard2 = new Dashboard("Dashboard Two");

        entityManager.persist(dashboard1);
        entityManager.persist(dashboard2);
        entityManager.flush();

        var dashboards = dashboardRepository.findAll();

        assertEquals(2, dashboards.size());
        assertTrue(dashboards.stream().anyMatch(d -> "Dashboard One".equals(d.getTitle())));
        assertTrue(dashboards.stream().anyMatch(d -> "Dashboard Two".equals(d.getTitle())));
    }

    @Test
    void whenSaveWithNullTitle_thenPersistedWithNull() {
        Dashboard dashboard = new Dashboard(null);

        Dashboard saved = dashboardRepository.save(dashboard);

        assertNotNull(saved.getId());
        assertNull(saved.getTitle());
    }
}