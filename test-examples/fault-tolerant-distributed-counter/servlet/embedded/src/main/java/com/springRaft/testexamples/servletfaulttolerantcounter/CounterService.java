package com.springRaft.testexamples.servletfaulttolerantcounter;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CounterService {

    private final CounterRepository repository;

    public Counter get(Long id) {
        return this.repository
                .findById(id)
                .orElse(null);
    }

    public Counter create(Counter counter) {
        return this.repository.save(counter);
    }

    public Counter increment(Counter counter) {
        counter.increment();
        return this.repository.save(counter);
    }

    public Counter decrement(Counter counter) {
        counter.decrement();
        return this.repository.save(counter);
    }

}
