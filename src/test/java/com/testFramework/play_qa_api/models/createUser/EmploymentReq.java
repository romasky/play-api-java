package com.testFramework.play_qa_api.models.createUser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmploymentReq {

    private String status;
    private String company;
    private String position;
    private String department;

    @JsonProperty("start_date")
    private String startDate;

    private SalaryReq salary;
}
