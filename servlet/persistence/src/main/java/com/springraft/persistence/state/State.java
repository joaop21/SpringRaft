package com.springraft.persistence.state;

public interface State {

    State State(long id, long currentTerm, String votedFor);

    Long getId();
    Long getCurrentTerm();
    String getVotedFor();

}