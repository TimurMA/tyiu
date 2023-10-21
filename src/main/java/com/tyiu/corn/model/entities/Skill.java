package com.tyiu.corn.model.entities;

import com.tyiu.corn.model.enums.SkillType;

import java.time.LocalDateTime;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table
public class Skill {
    @Id
    private Long id;
    private LocalDateTime createdAt;
    private String name;
    private SkillType type;
    private Boolean confirmed;
    private Long creatorId;
    private Long updaterId;
    private Long deleterId;
}
