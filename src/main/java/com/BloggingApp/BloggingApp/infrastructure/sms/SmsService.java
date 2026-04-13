package com.BloggingApp.BloggingApp.infrastructure.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsService {

    @Value("${fast2sms.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public SmsService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    @Async
    public void sendOtpSms(String phoneNumber, String otp) {
        // Fast2SMS BulkV2 logic with proper '&' separators
        String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey +
                "&route=q" +
                "&message=" + "Your Blogging App OTP is: " + otp +
                "&language=english" +
                "&flash=0" +
                "&numbers=" + phoneNumber;

        try {
            // RestTemplate isko GET request ki tarah hit karega
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("SMS Response: " + response);
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
        }
    }
}
