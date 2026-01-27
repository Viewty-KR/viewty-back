package com.viewty.viewtyback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "https://devso.koyeb.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

//        registry.addMapping("/uploads/**")
//                .allowedOriginPatterns("http://localhost:*", "https://devso.koyeb.app")
//                .allowedMethods("GET")
//                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/uploads/**")
//                .addResourceLocations("file:" + uploadDir + "/");

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        // 모든 경로(API 제외)를 index.html로 포워딩
//        registry.addViewController("/{path:[^\\.]*}")
//                .setViewName("forward:/index.html");
//        registry.addViewController("/**/{path:[^\\.]*}")
//                .setViewName("forward:/index.html");
//    }
}
