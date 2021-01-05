package com.springraft.testexamples.servletstack.services;

import com.springraft.testexamples.servletstack.models.Counter;
import com.springraft.testexamples.servletstack.repositories.CounterRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@AllArgsConstructor
@Service
public class CounterService {

    /*--------------------------------------------------------------------------------*/

    private final CounterRepository counterRepository;

    /*--------------------------------------------------------------------------------*/

    @Transactional
    public void save(Counter counter) {
        counterRepository.save(counter);
    }

    public Long increment() {
        Counter counter = counterRepository.getOne((long)1);
        long newValue = counter.getValue() + 1;
        counter.setValue(newValue);

        try {
            this.save(counter);
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            return increment();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return newValue;
    }

    public Long decrement() {
        Counter counter = counterRepository.getOne((long)1);
        long newValue = counter.getValue() - 1;
        counter.setValue(newValue);

        try {
            this.save(counter);
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            return decrement();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return newValue;
    }

    public Long get() {
        return counterRepository.getOne((long)1).getValue();
    }

}
