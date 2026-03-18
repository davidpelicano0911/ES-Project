package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.Segments;
import com.operimus.Marketing.repositories.SegmentsRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SegmentsServiceTest {

    @Mock
    private SegmentsRepository segmentsRepository;

    @InjectMocks
    private SegmentsService segmentsService;

    private Segments segment1;
    private Segments segment2;

    @BeforeEach
    void setUp() {
        segment1 = new Segments();
        segment1.setId(1L);
        segment1.setName("Segment 1");

        segment2 = new Segments();
        segment2.setId(2L);
        segment2.setName("Segment 2");

        List<Segments> segments = Arrays.asList(segment1, segment2);

        when(segmentsRepository.findAll()).thenReturn(segments);
        when(segmentsRepository.save(any(Segments.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void whenGetAllSegments_thenReturnListOfSegments() {
        List<Segments> result = segmentsService.getAllSegments();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting(Segments::getName).containsExactlyInAnyOrder("Segment 1", "Segment 2");

        verify(segmentsRepository, times(1)).findAll();
    }

    @Test
    void whenGetAllSegments_withEmptyRepository_thenReturnEmptyList() {
        when(segmentsRepository.findAll()).thenReturn(List.of());

        List<Segments> result = segmentsService.getAllSegments();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(segmentsRepository, times(1)).findAll();
    }

    @Test
    void whenCreateSegment_thenReturnSavedSegment() {
        Segments newSegment = new Segments();
        newSegment.setName("New Segment");

        when(segmentsRepository.save(any(Segments.class))).thenReturn(newSegment);

        Segments saved = segmentsService.createSegment(newSegment);

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Segment");

        verify(segmentsRepository, times(1)).save(newSegment);
    }

    @Test
    void whenCreateSegment_withNullSegment_thenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> segmentsService.createSegment(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Segment cannot be null");

        verify(segmentsRepository, never()).save(any());
    }

    @Test
    void whenCreateSegment_withNullName_thenThrowIllegalArgumentException() {
        Segments newSegment = new Segments();
        newSegment.setName(null);

        assertThatThrownBy(() -> segmentsService.createSegment(newSegment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Segment name cannot be null or empty");

        verify(segmentsRepository, never()).save(any());
    }

    @Test
    void whenCreateSegment_withEmptyName_thenThrowIllegalArgumentException() {
        Segments newSegment = new Segments();
        newSegment.setName("");

        assertThatThrownBy(() -> segmentsService.createSegment(newSegment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Segment name cannot be null or empty");

        verify(segmentsRepository, never()).save(any());
    }
}