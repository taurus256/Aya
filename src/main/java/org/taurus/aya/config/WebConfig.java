package org.taurus.aya.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/app/*.*")
                .addResourceLocations("classpath:/static/");
        registry
                .addResourceHandler("/app/aya/**")
                .addResourceLocations("classpath:/static/aya/");
        registry
                .addResourceHandler("/app/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}

