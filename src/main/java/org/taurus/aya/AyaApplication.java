package org.taurus.aya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@ServletComponentScan("org.taurus.aya.servlets")
@EnableWebMvc
@EnableScheduling
public class AyaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AyaApplication.class, args);
    }

}
