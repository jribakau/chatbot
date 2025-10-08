package jr.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadPath = new File(uploadDir).getAbsoluteFile();
        String fileUrl = uploadPath.toURI().toString();

        registry.addResourceHandler("/uploads/characters/**").addResourceLocations(fileUrl).setCachePeriod(3600);
    }
}
