package com.springRaft.servlet.config;

import com.springRaft.servlet.persistence.state.State;
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

    /**
     * Bean for creating State Objects.
     *
     * @return State created with initialization arguments.
     * */
    @Bean(name = "InitialState")
    public State newState() {
        return new State((long) 1,(long) 1,null);
    }

}
