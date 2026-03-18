package com.operimus.Marketing.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.operimus.Marketing.entities.Segments;

@Repository
public interface SegmentsRepository extends JpaRepository<Segments, Long> {

}
