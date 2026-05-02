package com.testFramework.play_qa_api.models.createUser;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUserReq {

    private String email;
    private String username;
    private String password;
    private ProfileReq profile;
    private ContactsReq contacts;
    private AddressReq address;
    private EmploymentReq employment;
    private SettingsReq settings;
}
