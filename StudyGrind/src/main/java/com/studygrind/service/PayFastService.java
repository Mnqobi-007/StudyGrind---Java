package com.studygrind.service;

import com.studygrind.dto.request.PayFastNotifyRequest;
import com.studygrind.model.Subscription;
import com.studygrind.model.User;
import com.studygrind.repository.SubscriptionRepository;
import com.studygrind.repository.TrialPeriodRepository;
import com.studygrind.util.PayFastUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PayFastService {
    
    @Autowired
    private PayFastUtil payFastUtil;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private TrialPeriodRepository trialPeriodRepository;
    
    @Autowired
    private UserService userService;
    
    @Value("${payfast.enabled:false}")
    private boolean payFastEnabled;
    
    @Value("${payfast.merchant.id:}")
    private String merchantId;
    
    @Value("${payfast.merchant.key:}")
    private String merchantKey;
    
    @Value("${payfast.passphrase:}")
    private String passphrase;
    
    @Value("${payfast.testing:true}")
    private boolean testing;
    
    @Value("${payfast.return.url:}")
    private String returnUrl;
    
    @Value("${payfast.cancel.url:}")
    private String cancelUrl;
    
    @Value("${app.url:http://localhost:8080}")
    private String appUrl;
    
    @Value("${app.subscription.monthly.price.cents:2999}")
    private String amount;
    
    public boolean isPayFastEnabled() {
        return payFastEnabled && merchantId != null && !merchantId.isEmpty() 
               && merchantKey != null && !merchantKey.isEmpty();
    }
    
    public Map<String, String> createPaymentRequest(Long userId, String userEmail, String userName) {
        Map<String, String> data = new HashMap<>();
        
        if (!isPayFastEnabled()) {
            data.put("mock", "true");
            data.put("message", "PayFast not configured. In production, this would process a real payment.");
            return data;
        }
        
        data.put("merchant_id", merchantId);
        data.put("merchant_key", merchantKey);
        data.put("return_url", returnUrl != null && !returnUrl.isEmpty() ? returnUrl : appUrl + "/payment/success");
        data.put("cancel_url", cancelUrl != null && !cancelUrl.isEmpty() ? cancelUrl : appUrl + "/payment/cancel");
        // FIXED: Use appUrl property instead of hardcoded localhost
        data.put("notify_url", appUrl + "/api/payfast/notify");
        
        data.put("m_payment_id", String.valueOf(userId) + "_" + System.currentTimeMillis());
        data.put("amount", amount);
        data.put("item_name", "StudyGrind Monthly Subscription");
        data.put("item_description", "1 Month Premium Access");
        
        data.put("custom_str1", String.valueOf(userId));
        data.put("custom_str2", userEmail);
        data.put("custom_str3", userName);
        
        String[] nameParts = userName.split(" ", 2);
        data.put("name_first", nameParts[0]);
        data.put("name_last", nameParts.length > 1 ? nameParts[1] : "");
        data.put("email_address", userEmail);
        
        if (testing) {
            data.put("testing", "1");
        }
        
        String signature = payFastUtil.generateSignature(data, passphrase);
        data.put("signature", signature);
        
        return data;
    }
    
    @Transactional
    public boolean processPaymentNotification(PayFastNotifyRequest notifyRequest) {
        if (!isPayFastEnabled()) {
            return false;
        }
        
        Map<String, String> params = convertToMap(notifyRequest);
        if (!payFastUtil.verifySignature(params, notifyRequest.getSignature(), passphrase)) {
            return false;
        }
        
        if (!notifyRequest.isPaymentComplete()) {
            return false;
        }
        
        Long userId;
        try {
            userId = Long.parseLong(notifyRequest.getCustom_str1());
        } catch (NumberFormatException e) {
            return false;
        }
        
        String paymentId = notifyRequest.getPf_payment_id();
        Optional<Subscription> existingSubscription = subscriptionRepository.findByStripePaymentId(paymentId);
        if (existingSubscription.isPresent()) {
            return true;
        }
        
        User student = userService.findById(userId);
        
        trialPeriodRepository.findByStudentAndIsActiveTrue(student).ifPresent(trial -> {
            trial.setIsActive(false);
            trialPeriodRepository.save(trial);
        });
        
        Subscription subscription = new Subscription();
        subscription.setStudent(student);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        subscription.setStatus("active");
        subscription.setAmount(Double.parseDouble(notifyRequest.getAmount_gross()));
        subscription.setPaymentDate(LocalDateTime.now());
        subscription.setPaymentMethod("payfast");
        subscription.setStripePaymentId(paymentId);
        
        subscriptionRepository.save(subscription);
        
        return true;
    }
    
    @Transactional
    public Subscription createMockSubscription(Long userId) {
        User student = userService.findById(userId);
        
        trialPeriodRepository.findByStudentAndIsActiveTrue(student).ifPresent(trial -> {
            trial.setIsActive(false);
            trialPeriodRepository.save(trial);
        });
        
        Subscription subscription = new Subscription();
        subscription.setStudent(student);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        subscription.setStatus("active");
        subscription.setAmount(29.99);
        subscription.setPaymentDate(LocalDateTime.now());
        subscription.setPaymentMethod("mock");
        subscription.setStripePaymentId("mock_" + System.currentTimeMillis());
        
        return subscriptionRepository.save(subscription);
    }
    
    private Map<String, String> convertToMap(PayFastNotifyRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("m_payment_id", request.getM_payment_id());
        map.put("pf_payment_id", request.getPf_payment_id());
        map.put("payment_status", request.getPayment_status());
        map.put("item_name", request.getItem_name());
        map.put("item_description", request.getItem_description());
        map.put("amount_gross", request.getAmount_gross());
        map.put("amount_fee", request.getAmount_fee());
        map.put("amount_net", request.getAmount_net());
        map.put("custom_str1", request.getCustom_str1());
        map.put("custom_str2", request.getCustom_str2());
        map.put("custom_str3", request.getCustom_str3());
        map.put("name_first", request.getName_first());
        map.put("name_last", request.getName_last());
        map.put("email_address", request.getEmail_address());
        map.put("merchant_id", request.getMerchant_id());
        return map;
    }
}