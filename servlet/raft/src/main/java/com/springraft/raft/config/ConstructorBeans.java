package com.springraft.raft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConstructorBeans {

    /**
     * Bean for creating RestTemplate Objects.
     *
     * @return RestTemplate created with no args constructor.
     * */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
