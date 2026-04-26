package com.studygrind.dto.response;

public class SubscriptionStatusResponse {
    private boolean hasActiveTrial;
    private long trialRemainingDays;
    private boolean hasActiveSubscription;
    private long subscriptionRemainingDays;
    private boolean canAccessContent;
    private String statusMessage;
    
    public SubscriptionStatusResponse() {}
    
    // Getters and Setters
    public boolean isHasActiveTrial() { return hasActiveTrial; }
    public void setHasActiveTrial(boolean hasActiveTrial) { this.hasActiveTrial = hasActiveTrial; }
    public long getTrialRemainingDays() { return trialRemainingDays; }
    public void setTrialRemainingDays(long trialRemainingDays) { this.trialRemainingDays = trialRemainingDays; }
    public boolean isHasActiveSubscription() { return hasActiveSubscription; }
    public void setHasActiveSubscription(boolean hasActiveSubscription) { this.hasActiveSubscription = hasActiveSubscription; }
    public long getSubscriptionRemainingDays() { return subscriptionRemainingDays; }
    public void setSubscriptionRemainingDays(long subscriptionRemainingDays) { this.subscriptionRemainingDays = subscriptionRemainingDays; }
    public boolean isCanAccessContent() { return canAccessContent; }
    public void setCanAccessContent(boolean canAccessContent) { this.canAccessContent = canAccessContent; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}