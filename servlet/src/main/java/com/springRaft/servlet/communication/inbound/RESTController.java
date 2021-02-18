package com.springRaft.servlet.communication.inbound;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("raft")
public class RESTController implements InboundCommunication {

    /**
     * TODO
     * */
    @RequestMapping(value = "/appendEntries", method = RequestMethod.POST)
    public Boolean appendEntries() {
        return true;
    }

    /**
     * TODO
     * */
    @RequestMapping(value = "/requestVote", method = RequestMethod.POST)
    public Boolean requestVote() {
        return true;
    }

}
