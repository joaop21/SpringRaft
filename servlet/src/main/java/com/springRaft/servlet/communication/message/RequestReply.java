package com.springRaft.servlet.communication.message;

import lombok.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Getter
@Setter
@ToString
public class RequestReply implements Message {

    /* Success of the request */
    private Boolean success;

    /* Result of applying a command to the state machine */
    private Object response;

    /* If this server is not the leader we have to redirect the request */
    private Boolean redirect;

    /* Where to redirect the request */
    private String redirectTo;

    /* --------------------------------------------------- */

    public RequestReply() {
        this.success = false;
        this.response = null;
        this.redirect = false;
        this.redirectTo = null;
    }

    public RequestReply(String redirectTo) {
        this.success = false;
        this.response = null;
        this.redirect = true;
        this.redirectTo = redirectTo;
    }

    public RequestReply(Object response) {
        this.success = true;
        this.response = response;
        this.redirect = false;
        this.redirectTo = null;
    }

}
