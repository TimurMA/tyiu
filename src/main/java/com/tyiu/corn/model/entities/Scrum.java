package com.tyiu.corn.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Scrum {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer count;

    @ManyToMany
    private List<Profile> profiles;

    @OneToMany
    private List<Task> tasks;
}
