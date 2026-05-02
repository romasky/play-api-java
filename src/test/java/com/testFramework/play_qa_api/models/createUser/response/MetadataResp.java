package com.testFramework.play_qa_api.models.createUser.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetadataResp {

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("last_login_at")
    private String lastLoginAt;

    @JsonProperty("last_login_ip")
    private String lastLoginIp;

    @JsonProperty("login_count")
    private Integer loginCount;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_verified")
    private Boolean isVerified;

    @JsonProperty("is_premium")
    private Boolean isPremium;

    private String role;
    private List<String> tags;
    private String source;

    @JsonProperty("user_agent")
    private String userAgent;
}
