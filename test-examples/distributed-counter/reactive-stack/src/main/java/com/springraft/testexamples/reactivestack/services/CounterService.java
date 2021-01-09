package com.springraft.testexamples.reactivestack.services;

import com.springraft.testexamples.reactivestack.models.Counter;
import com.springraft.testexamples.reactivestack.repositories.CounterRepository;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class CounterService {

    /*--------------------------------------------------------------------------------*/

    @Autowired
    private CounterRepository counterRepository;

    /*--------------------------------------------------------------------------------*/

    public Mono<Counter> save(Counter counter) {
        return counterRepository.save(counter);
    }

    @Transactional
    public Mono<?> increment() {

        return safeIncrement();

    }

    /**
     * TODO
     * */
    private Mono<Long> safeIncrement () {

        return counterRepository.findById((long)0)
                .flatMap(counter -> {

                    long newValue = counter.getValue()+1;
                    counter.setValue(newValue);

                    return counterRepository.save(counter)
                            .map(Counter::getValue)
                            .onErrorResume(e -> safeIncrement());

                });

    }

    @Transactional
    public Mono<?> decrement() {

        return safeDecrement();

    }

    /**
     * TODO
     * */
    private Mono<Long> safeDecrement () {

        return counterRepository.findById((long)0)
                .flatMap(counter -> {

                    long newValue = counter.getValue()-1;
                    counter.setValue(newValue);

                    return counterRepository.save(counter)
                            .map(Counter::getValue)
                            .onErrorResume(e -> safeDecrement());

                });

    }

    public Mono<Long> get() {
        return counterRepository.findById((long)0).map(Counter::getValue);
    }

}
