package com.studygrind.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Resource handling
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadDir + "/");
        
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/");
        
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/");
    }
}