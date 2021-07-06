package com.springraft.persistence.state;

import reactor.core.publisher.Mono;

public interface StateService {

    /**
     * Method for getting the current state from persistence mechanism.
     *
     * @return Mono<State> A mono with state.
     * */
    Mono<? extends State> getState();

    /**
     * Method for inserting or updating the current persisted state.
     *
     * @param state New state to insert/update.
     * @return Mono<State> New persisted state.
     * */
    Mono<? extends State> saveState(State state);


    /**
     * Method for persist the new state after transit to candidate state.
     *
     * @return Mono<State> New state after transit to candidate state.
     * */
    Mono<State> newCandidateState();

    /**
     * Method for getting the current state's term from persistence mechanism.
     *
     * @return Mono<Long> A mono with state's current term.
     * */
    Mono<Long> getCurrentTerm();

    /**
     * Method for getting the current state's voted for from persistence mechanism.
     *
     * @return Mono<String> A mono with state's current votedFor.
     * */
    Mono<String> getVotedFor();

    /**
     * Method that sets and saves the new voted for.
     *
     * @param votedFor is the voted server name in the new term.
     *
     * @return Mono<State> A mono with the new persisted state.
     * */
    Mono<State> setVotedFor(String votedFor);

    /**
     * Method that sets and saves the new state.
     *
     * @param term is the new state's term.
     * @param votedFor is the voted server name in the new term.
     *
     * @return Mono<State> A mono with the new persisted state.
     * */
    Mono<State> setState(Long term, String votedFor);

}

