package com.testFramework.play_qa_api.models.mail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagesListResp {
    private List<MessageResp> messages;
    private Integer count;
}
