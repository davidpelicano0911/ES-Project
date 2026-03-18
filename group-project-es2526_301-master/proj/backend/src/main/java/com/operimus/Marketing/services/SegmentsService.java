package com.operimus.Marketing.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.operimus.Marketing.entities.Segments;
import com.operimus.Marketing.repositories.SegmentsRepository;

@Service
public class SegmentsService {
    @Autowired
    private SegmentsRepository segmentsRepository;

    public List<Segments> getAllSegments() {
        return segmentsRepository.findAll();
    }   

    public Segments createSegment(Segments segment) {
        if (segment == null) {
            throw new IllegalArgumentException("Segment cannot be null");
        }
        if (segment.getName() == null || segment.getName().isEmpty()) {
            throw new IllegalArgumentException("Segment name cannot be null or empty");
        }
        return segmentsRepository.save(segment);
    }
}
