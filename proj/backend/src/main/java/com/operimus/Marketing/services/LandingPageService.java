package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.LandingPage;
import com.operimus.Marketing.repositories.LandingPageRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class LandingPageService {

    @Autowired
    private LandingPageRepository landingPageRepository;

    public LandingPage createPage(LandingPage page) {
        if (landingPageRepository.existsByName(page.getName())) {
            throw new IllegalArgumentException("Page name already exists");
        }
        return landingPageRepository.save(page);
    }

    public Optional<LandingPage> getPageById(Long id) {
        return landingPageRepository.findById(id);
    }

    public List<LandingPage> getAllPages() {
        return landingPageRepository.findAll();
    }

    public LandingPage updatePage(Long id, LandingPage updated) {
        LandingPage existing = landingPageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Page not found"));

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getBody() != null) existing.setBody(updated.getBody());
        if (updated.getDesign() != null) existing.setDesign(updated.getDesign());

        return landingPageRepository.save(existing);
    }

    public void deletePage(Long id) {
        landingPageRepository.deleteById(id);
    }
}
