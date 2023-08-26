package com.tyiu.corn.model.dto;

import com.tyiu.corn.model.enums.StatusIdea;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingDTO {
    private StatusIdea status;
    private double rating;
    private String marketValue;
    private String originality;
    private String technicalFeasibility;
    private String understanding;
}