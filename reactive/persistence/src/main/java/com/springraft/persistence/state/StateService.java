package com.springraft.persistence.state;

import reactor.core.publisher.Mono;

public interface StateService {

    /**
     * Method for getting the current state from persistence mechanism.
     *
     * @return Mono<State> A mono with state.
     * */
    Mono<? extends StateModel> getState();

    /**
     * Method for inserting or updating the current persisted state.
     *
     * @param state New state to insert/update.
     * @return Mono<State> New persisted state.
     * */
    Mono<? extends StateModel> saveState(Object state);


    /**
     * Method for persist the new state after transit to candidate state.
     *
     * @return Mono<State> New state after transit to candidate state.
     * */
    Mono<? extends StateModel> newCandidateState();

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
    Mono<? extends StateModel> setVotedFor(String votedFor);

    /**
     * Method that sets and saves the new state.
     *
     * @param term is the new state's term.
     * @param votedFor is the voted server name in the new term.
     *
     * @return Mono<State> A mono with the new persisted state.
     * */
    Mono<? extends StateModel> setState(Long term, String votedFor);

}

