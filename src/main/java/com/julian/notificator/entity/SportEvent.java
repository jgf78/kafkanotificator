package com.julian.notificator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_time", "match_name"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_time")
    private String eventTime;

    private String sport;

    private String competition;

    @Column(name = "match_name")
    private String matchName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(
        mappedBy = "event",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<SportEventLink> links = new ArrayList<>();
}