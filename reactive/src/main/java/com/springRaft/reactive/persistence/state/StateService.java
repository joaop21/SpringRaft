package com.springRaft.reactive.persistence.state;

import reactor.core.publisher.Mono;

public interface StateService {

    Mono<State> getState();
    Mono<State> saveState(State state);

    Mono<State> newCandidateState();
    Mono<Long> getCurrentTerm();
    Mono<String> getVotedFor();
    Mono<State> setVotedFor(String votedFor);
    Mono<State> setState(Long term, String votedFor);

}
