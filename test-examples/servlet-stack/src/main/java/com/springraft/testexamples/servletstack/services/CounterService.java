package com.springraft.testexamples.servletstack.services;

import com.springraft.testexamples.servletstack.models.Counter;
import com.springraft.testexamples.servletstack.repositories.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    /*--------------------------------------------------------------------------------*/

    @Autowired
    private CounterRepository counterRepository;

    /*--------------------------------------------------------------------------------*/

    public Counter save(Counter counter) {
        return counterRepository.save(counter);
    }

    public synchronized Long increment(long id) {
        Counter counter = counterRepository.getOne(id);
        long newValue = counter.getValue() + 1;
        counter.setValue(newValue);
        counterRepository.save(counter);
        return newValue;
    }

    public synchronized Long decrement(long id) {
        Counter counter = counterRepository.getOne(id);
        long newValue = counter.getValue() - 1;
        counter.setValue(newValue);
        counterRepository.save(counter);
        return newValue;
    }

    public Long get(long id) {
        return counterRepository.getOne(id).getValue();
    }


}
