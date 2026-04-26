package com.studygrind.util;

import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;

@Component
public class PayFastUtil {
    
    public String generateSignature(Map<String, String> data, String passphrase) {
        TreeMap<String, String> sortedMap = new TreeMap<>(data);
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        
        String queryString = sb.toString();
        if (queryString.endsWith("&")) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        
        if (passphrase != null && !passphrase.isEmpty()) {
            queryString = queryString + "&passphrase=" + passphrase;
        }
        
        return getMD5Hash(queryString);
    }
    
    public boolean verifySignature(Map<String, String> data, String signature, String passphrase) {
        String generatedSignature = generateSignature(data, passphrase);
        return generatedSignature.equals(signature);
    }
    
    private String getMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            Formatter formatter = new Formatter();
            for (byte b : digest) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getPayFastUrl() {
        return "https://sandbox.payfast.co.za/eng/process";
    }
}