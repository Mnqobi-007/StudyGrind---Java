package com.studygrind.dto.request;

import java.util.Map;

public class PayFastNotifyRequest {
    private String m_payment_id;
    private String pf_payment_id;
    private String payment_status;
    private String item_name;
    private String item_description;
    private String amount_gross;
    private String amount_fee;
    private String amount_net;
    private String custom_str1;
    private String custom_str2;
    private String custom_str3;
    private String custom_str4;
    private String custom_str5;
    private String name_first;
    private String name_last;
    private String email_address;
    private String merchant_id;
    private String signature;
    
    public PayFastNotifyRequest() {}
    
    public static PayFastNotifyRequest fromMap(Map<String, String> params) {
        PayFastNotifyRequest request = new PayFastNotifyRequest();
        request.m_payment_id = params.get("m_payment_id");
        request.pf_payment_id = params.get("pf_payment_id");
        request.payment_status = params.get("payment_status");
        request.item_name = params.get("item_name");
        request.item_description = params.get("item_description");
        request.amount_gross = params.get("amount_gross");
        request.amount_fee = params.get("amount_fee");
        request.amount_net = params.get("amount_net");
        request.custom_str1 = params.get("custom_str1");
        request.custom_str2 = params.get("custom_str2");
        request.custom_str3 = params.get("custom_str3");
        request.custom_str4 = params.get("custom_str4");
        request.custom_str5 = params.get("custom_str5");
        request.name_first = params.get("name_first");
        request.name_last = params.get("name_last");
        request.email_address = params.get("email_address");
        request.merchant_id = params.get("merchant_id");
        request.signature = params.get("signature");
        return request;
    }
    
    public boolean isPaymentComplete() {
        return "COMPLETE".equals(payment_status);
    }
    
    // Getters and Setters
    public String getM_payment_id() { return m_payment_id; }
    public void setM_payment_id(String m_payment_id) { this.m_payment_id = m_payment_id; }
    public String getPf_payment_id() { return pf_payment_id; }
    public void setPf_payment_id(String pf_payment_id) { this.pf_payment_id = pf_payment_id; }
    public String getPayment_status() { return payment_status; }
    public void setPayment_status(String payment_status) { this.payment_status = payment_status; }
    public String getItem_name() { return item_name; }
    public void setItem_name(String item_name) { this.item_name = item_name; }
    public String getItem_description() { return item_description; }
    public void setItem_description(String item_description) { this.item_description = item_description; }
    public String getAmount_gross() { return amount_gross; }
    public void setAmount_gross(String amount_gross) { this.amount_gross = amount_gross; }
    public String getAmount_fee() { return amount_fee; }
    public void setAmount_fee(String amount_fee) { this.amount_fee = amount_fee; }
    public String getAmount_net() { return amount_net; }
    public void setAmount_net(String amount_net) { this.amount_net = amount_net; }
    public String getCustom_str1() { return custom_str1; }
    public void setCustom_str1(String custom_str1) { this.custom_str1 = custom_str1; }
    public String getCustom_str2() { return custom_str2; }
    public void setCustom_str2(String custom_str2) { this.custom_str2 = custom_str2; }
    public String getCustom_str3() { return custom_str3; }
    public void setCustom_str3(String custom_str3) { this.custom_str3 = custom_str3; }
    public String getCustom_str4() { return custom_str4; }
    public void setCustom_str4(String custom_str4) { this.custom_str4 = custom_str4; }
    public String getCustom_str5() { return custom_str5; }
    public void setCustom_str5(String custom_str5) { this.custom_str5 = custom_str5; }
    public String getName_first() { return name_first; }
    public void setName_first(String name_first) { this.name_first = name_first; }
    public String getName_last() { return name_last; }
    public void setName_last(String name_last) { this.name_last = name_last; }
    public String getEmail_address() { return email_address; }
    public void setEmail_address(String email_address) { this.email_address = email_address; }
    public String getMerchant_id() { return merchant_id; }
    public void setMerchant_id(String merchant_id) { this.merchant_id = merchant_id; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}