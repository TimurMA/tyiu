package com.tyiu.corn.model.entities;

import com.tyiu.corn.model.enums.IdeaMarketStatusType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
public class IdeaMarket {
    @Id
    private Long id;
    private Long ideaId;
    private Long position;
    private String name;
    private String initiator;
    private String description;
    private LocalDate createdAt;
    private Long maxTeamSize;
    private IdeaMarketStatusType status;
    private Long requests;
    private Long acceptedRequests;
}