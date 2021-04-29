package com.springRaft.reactive.persistence.log;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Scope("singleton")
@Transactional
@AllArgsConstructor
public class LogService {

    /* Logger */
    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    /* Repository for Entry operations */
    private final EntryRepository entryRepository;

    /* Repository for Entry operations */
    private final LogStateRepository logStateRepository;

    /* --------------------------------------------------- */

    /**
     * Method for getting the current log state from persistence mechanism.
     *
     * @return Mono<LogState> A mono with log state.
     * */
    public Mono<LogState> getState() {
        return this.logStateRepository.findById((long) 1);
    }

    /**
     * Method for inserting or updating the current persisted log state.
     *
     * @param logState New log state to insert/update.
     * @return Mono<LogState> New persisted log state.
     * */
    public Mono<LogState> saveState(LogState logState) {
        return this.logStateRepository.save(logState)
                .doOnError(error -> log.error("\nError on saveState method: \n" + error));
    }

}
