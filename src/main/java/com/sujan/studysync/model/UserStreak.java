package com.sujan.studysync.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_streaks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStreak extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    private Integer currentStreak = 0;

    @Builder.Default
    private Integer longestStreak = 0;

    private LocalDate lastStudyDate;

    @Builder.Default
    private Integer totalDays = 0;

    @Builder.Default
    private Integer totalMinutes = 0;

    @Builder.Default
    private Integer roomsJoined = 0;

    @Builder.Default
    private Integer resourcesShared = 0;
}
