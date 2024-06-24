package org.cloud.ai.controller;

import org.cloud.ai.pojo.ChatRequest;
import org.cloud.ai.pojo.ChatResponse;
import org.cloud.ai.pojo.Message;
import org.cloud.common.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/openai")
public class ChatBotController {
    @Autowired
    private RestTemplate restTemplate;



    @Value("${openai.chatGpt.model}")
    private String model;

    @Value("${openai.chatGpt.max-completions}")
    private int maxCompletions;

    @Value("${openai.chatGpt.temperature}")
    private double temperature;


    @Value("${openai.chatGpt.api.url}")
    private String apiUrl;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(String prompt) {

        ChatRequest request = new ChatRequest(model,
                List.of(new Message("user", prompt)),
                maxCompletions,
                temperature);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChatRequest> requestEntity = new HttpEntity<>(request,headers);



        ChatResponse chatResponse = restTemplate.postForObject(apiUrl, requestEntity, ChatResponse.class);
        return Result.success(chatResponse);
    }
}
