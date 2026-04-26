package com.studygrind.util;

public final class Constants {
    
    private Constants() {}
    
    // Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_STUDENT = "student";
    
    // Security
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCK_MINUTES = 30;
    public static final int REFRESH_TOKEN_EXPIRY_DAYS = 7;
    
    // Business
    public static final int TRIAL_DAYS = 14;
    public static final double DEFAULT_COURSE_PRICE = 29.99;
    
    // File
    public static final long MAX_FILE_SIZE_MB = 50;
}