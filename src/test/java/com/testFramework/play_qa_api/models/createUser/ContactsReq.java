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
public class ContactsReq {

    private String phone;

    @JsonProperty("phone_verified")
    private Boolean phoneVerified;

    private String telegram;
    private String whatsapp;
    private String linkedin;
    private String github;
    private String website;

    @JsonProperty("emergency_contact")
    private String emergencyContact;
}
