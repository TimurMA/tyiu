package com.tyiu.corn.model;

import com.tyiu.corn.model.enums.Feasibility;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Idea {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String type;
    private String problem;
    private String solution;
    private String result;
    private String customer;
    private String description;
    private Long budget;
    private Feasibility feasibility;
    private String suitability;

    @ManyToMany
    private List<Profile> profiles;

    @OneToMany
    private List<Comment> comments;
    //Поля для фронтенда
    private String idea;
    private String status;
    private String date;
    private double rating;
    private double risk;

}
