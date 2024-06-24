package org.cloud.ai.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private int n;
    private Double temperature;

}
