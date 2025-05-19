package com.fishtripplanner.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로젝트 루트 기준 "uploads" 디렉토리 경로 생성
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toAbsolutePath().toUri().toString();
        // ↑ 반드시 toUri().toString() 사용 → "file:///" 접두어 + 공백, 한글 자동 인코딩 처리

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
        // → file:///C:/경로/uploads/... 로 매핑됨. 반드시 '/'로 끝나야 함
    }
}
