package com.springraft.persistence.state;

public interface StateService {

    /**
     * Method for getting the current state from persistence mechanism.
     *
     * @return State A mono with state.
     * */
    State getState();

    /**
     * Method for inserting or updating the current persisted state.
     *
     * @param state New state to insert/update.
     * @return State New persisted state.
     * */
    State saveState(State state);

    /* --------------------------------------------------- */

    /**
     * Method for persist the new state after transit to candidate state.
     *
     * @return State New state after transit to candidate state.
     * */
    State newCandidateState();

    /**
     * Method for getting the current state's term from persistence mechanism.
     *
     * @return Long A mono with state's current term.
     * */
    Long getCurrentTerm();

    /**
     * Method for getting the current state's voted for from persistence mechanism.
     *
     * @return String A mono with state's current votedFor.
     * */
    String getVotedFor();

    /**
     * Method that sets and saves the new voted for.
     *
     * @param votedFor is the voted server name in the new term.
     *
     * @return State A mono with the new persisted state.
     * */
    State setVotedFor(String votedFor);

    /**
     * Method that sets and saves the new state.
     *
     * @param term is the new state's term.
     * @param votedFor is the voted server name in the new term.
     *
     * @return State A mono with the new persisted state.
     * */
    void setState(Long term, String votedFor);

}
