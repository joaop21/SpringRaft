package com.springraft.raft.stateMachine;

import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("singleton")
public class WaitingRequests {

    /* Application Context for getting beans */
    private final ApplicationContext applicationContext;

    /* Map that holds indexes and waiting rooms */
    private final Map<Long,WaitingRoom> clientRequests;

    /* --------------------------------------------------- */

    @Autowired
    public WaitingRequests (ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.clientRequests = new HashMap<>();
    }

    /* --------------------------------------------------- */

    /**
     * Method that inserts a new client request and its waiting room in the clientRequests Map.
     *
     * @param index Key to put in the Map.
     *
     * @return WaitingRoom which is the room for this request.
     * */
    @Synchronized
    public WaitingRoom insertWaitingRequest(Long index) {

        // getting a room
        WaitingRoom room = this.applicationContext.getBean(WaitingRoom.class);

        WaitingRoom previousRoom = this.clientRequests.putIfAbsent(index, room);

        if (previousRoom != null) {

            // send a null response to the previous
            previousRoom.putResponse();

            // put the new room
            this.clientRequests.put(index, room);

        }

        return room;

    }

    /**
     * Method that puts a response in a waiting room, and removes it from the map,
     * because the client request is already responded.
     *
     * @param index Entry's index that is the key in the map.
     * @param response Object that represents the response to the client request.
     * */
    @Synchronized
    public void putResponse(Long index, Object response) {

        WaitingRoom room = this.clientRequests.remove(index);

        if (room != null)
            room.putResponse(response);

    }

}

