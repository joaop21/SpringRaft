package com.springRaft.servlet.communication.outbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class REST implements OutboundCommunication {

    /* TODO */
    @Autowired
    private RestTemplate restTemplate;

    /* TODO */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public Boolean appendEntries() {
        try {
            return this.post("localhost:8002", "/appendEntries");
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean requestVote() {
        try {
            return this.post("localhost:8003", "/requestVote");
        } catch (ResourceAccessException e) {
            return false;
        }
    }

    /**
     * TODO
     * */
    private Boolean post(String host, String route) throws ResourceAccessException {
        String endpoint = "http://" + host + "/raft" + route;
        return restTemplate.postForObject(endpoint, null, Boolean.class);
    }

}
