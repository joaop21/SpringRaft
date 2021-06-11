package com.springRaft.reactive.communication.inbound;

import com.springRaft.reactive.communication.message.RequestReply;
import reactor.core.publisher.Mono;

public interface ClientInboundCommunication {

    /**
     * Abstract method for the inbound strategies to implement it, so they can handle the reception
     * of general requests.
     *
     * @param command String that contains the command to execute in the FSM.
     *
     * @return Mono<RequestReply> Reply ti send to the client who made the request.
     * */
    Mono<RequestReply> clientRequest(String command);

}
