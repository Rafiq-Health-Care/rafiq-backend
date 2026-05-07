package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class TimeSlot {

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

}
