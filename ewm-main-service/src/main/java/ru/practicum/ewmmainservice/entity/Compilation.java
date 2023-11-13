package ru.practicum.ewmmainservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name = "event_compilation",
    joinColumns = { @JoinColumn(name = "compilation_id")},
    inverseJoinColumns = { @JoinColumn(name = "event_id")})
    List<Event> events;

    @Column(nullable = false)
    private Boolean pinned;

    @Column(length = 512, nullable = false)
    private String title;
}
