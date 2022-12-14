package de.unituebingen.metadata.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class MetadataApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MetadataApplication.class, args);
	}

	@Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(1000000);
        return multipartResolver;
    }

	@Configuration
	@EnableWebMvc
	public class WebConfig implements WebMvcConfigurer {

		@Override
		public void addCorsMappings(CorsRegistry corsRegistry) {
			corsRegistry.addMapping("/**")
					.allowedOrigins("http://localhost:4200", "http://localhost", "https://localhost", "http://193.196.29.32/", "https://193.196.29.32/")
					.allowedMethods("*")
					.maxAge(3600L)
					.allowedHeaders("*")
					.exposedHeaders("Authorization")
					.allowCredentials(true);
		}
	}

}
