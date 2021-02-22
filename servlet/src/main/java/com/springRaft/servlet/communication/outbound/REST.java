package com.springRaft.servlet.communication.outbound;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class REST implements OutboundCommunication {

    /* Object that helps creating REST requests */
    private final RestTemplate restTemplate;

    /* --------------------------------------------------- */

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
