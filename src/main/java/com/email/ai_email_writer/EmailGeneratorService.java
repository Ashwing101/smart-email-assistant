package com.email.ai_email_writer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class EmailGeneratorService {

    private final WebClient webclient;



    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webclient = webClientBuilder.build();
    }

    public  String generateEmailReply(EmailRequest emailRequest){
        //Build Prompt
        String prompt = buildPromt(emailRequest);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );


//        // Do request and get response
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            String jsonBody = objectMapper.writeValueAsString(requestBody);
//            System.out.println("Request body JSON:\n" + jsonBody);
//        } catch (Exception e) {
//            System.out.println("Error serializing request body: " + e.getMessage());
//        }
//

//
//        System.out.println("GeminiApiUrl " +  geminiApiUrl + geminiApiKey);



        String response = webclient.post().uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();



        return extractResponseContent(response);

        // Return Response
        //Craft a Request
        //Do Request and Get Response
        //Return Response

    }

    private  String extractResponseContent(String response){


        try {
            //Help in working with Jackson Data
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();

        } catch (JsonProcessingException e) {
           return "Error Processing Message:" + e.getMessage();
        }

    }

    private String buildPromt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate Professional email reply for the following email content. Please" +
                "don't  generate the subject line ");

        if(emailRequest.getTone() != null){
            if(!emailRequest.getTone().isEmpty()){
                prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
            }
        }
    prompt.append("\n Original email: \n").append(emailRequest.getEmail());
    return prompt.toString();
    }


}
