package com.springRaft.servlet.communication.inbound;

import com.springRaft.servlet.communication.message.RequestReply;

public interface ClientInboundCommunication {

    /**
     * Abstract method for the inbound strategies to implement it, so they can handle the reception
     * of general requests.
     *
     * @param command String that contains the command to execute in the FSM.
     *
     * @return RequestReply Reply to send to the client who made the request.
     * */
    RequestReply clientRequest(String command);

}
