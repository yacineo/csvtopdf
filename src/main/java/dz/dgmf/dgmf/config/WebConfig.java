package dz.dgmf.dgmf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/pdf/generate")
                .allowedOrigins("http://localhost:4202") // Match your Angular app's port
                .allowedMethods("GET", "POST"); // Add other methods if needed (PUT, DELETE, etc.)
    }
}