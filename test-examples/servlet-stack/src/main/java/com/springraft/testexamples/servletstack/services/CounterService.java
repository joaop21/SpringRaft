package com.springraft.testexamples.servletstack.services;

import com.springraft.testexamples.servletstack.models.Counter;
import com.springraft.testexamples.servletstack.repositories.CounterRepository;
import lombok.AllArgsConstructor;
import lombok.Synchronized;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CounterService {

    /*--------------------------------------------------------------------------------*/

    private final CounterRepository counterRepository;

    /*--------------------------------------------------------------------------------*/

    public void save(Counter counter) {
        counterRepository.save(counter);
    }

    @Synchronized
    public Long increment() {
        Counter counter = counterRepository.getOne((long)1);
        long newValue = counter.getValue() + 1;
        counter.setValue(newValue);
        counterRepository.save(counter);
        return newValue;
    }

    @Synchronized
    public Long decrement() {
        Counter counter = counterRepository.getOne((long)1);
        long newValue = counter.getValue() - 1;
        counter.setValue(newValue);
        counterRepository.save(counter);
        return newValue;
    }

    public Long get() {
        return counterRepository.getOne((long)1).getValue();
    }


}
