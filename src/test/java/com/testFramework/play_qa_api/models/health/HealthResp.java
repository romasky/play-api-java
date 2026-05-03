package com.testFramework.play_qa_api.models.health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthResp {
    private String status;
    private String time;
}
