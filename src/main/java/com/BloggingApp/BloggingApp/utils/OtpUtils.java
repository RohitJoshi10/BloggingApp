package com.BloggingApp.BloggingApp.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpUtils {

    public String generateOtp(){
        Random random = new Random();

        int otpValue = 100000 + random.nextInt(900000);
        return String.valueOf(otpValue);
    }
}
