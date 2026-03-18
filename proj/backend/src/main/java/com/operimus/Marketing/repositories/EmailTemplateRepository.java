package com.operimus.Marketing.repositories;

import com.operimus.Marketing.entities.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
	boolean existsByName(String name);
}