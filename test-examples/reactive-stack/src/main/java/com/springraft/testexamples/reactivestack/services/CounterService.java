package com.springraft.testexamples.reactivestack.services;

import com.springraft.testexamples.reactivestack.models.Counter;
import com.springraft.testexamples.reactivestack.repositories.CounterRepository;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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
    public Mono<Long> increment(long id) {

        return counterRepository.findById(id)
                .flatMap(counter -> {
                    long newValue = counter.getValue()+1;
                    counter.setValue(newValue);
                    return counterRepository.save(counter).map(Counter::getValue);
                });

    }

    @Transactional
    public Mono<Long> decrement(long id) {

        return counterRepository.findById(id)
                .flatMap(counter -> {
                    long newValue = counter.getValue()-1;
                    counter.setValue(newValue);
                    return counterRepository.save(counter).map(Counter::getValue);
                });

    }

    public Mono<Long> get(long id) {
        return counterRepository.findById(id).map(Counter::getValue);
    }

}
