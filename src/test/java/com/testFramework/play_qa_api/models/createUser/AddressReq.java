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
public class AddressReq {

    private String country;
    private String state;
    private String city;
    private String street;
    private String building;
    private String apartment;

    @JsonProperty("zip_code")
    private String zipCode;

    private CoordinatesReq coordinates;
}
