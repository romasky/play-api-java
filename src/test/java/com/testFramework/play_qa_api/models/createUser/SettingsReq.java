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
public class SettingsReq {

    private String language;
    private String timezone;
    private String theme;

    @JsonProperty("notifications_enabled")
    private Boolean notificationsEnabled;

    @JsonProperty("email_notifications")
    private Boolean emailNotifications;

    @JsonProperty("sms_notifications")
    private Boolean smsNotifications;

    @JsonProperty("two_factor_enabled")
    private Boolean twoFactorEnabled;

    @JsonProperty("private_profile")
    private Boolean privateProfile;
}
