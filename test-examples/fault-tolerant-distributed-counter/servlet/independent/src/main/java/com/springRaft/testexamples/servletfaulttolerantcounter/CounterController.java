package com.springRaft.testexamples.servletfaulttolerantcounter;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("counter")
@AllArgsConstructor
public class CounterController {

    private final CounterService service;

    @RequestMapping(
            value = "{id}",
            method = RequestMethod.GET
    )
    public ResponseEntity<?> get(@PathVariable Long id) {

        Counter counter = this.service.get(id);
        return counter == null
                ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                : new ResponseEntity<>(counter, HttpStatus.OK);

    }

    @RequestMapping(
            value = "/increment/{id}",
            method = RequestMethod.POST
    )
    public ResponseEntity<?> increment(@PathVariable Long id) {

        Counter counter = this.service.get(id);

        if (counter == null) {

            counter = this.service.create(new Counter(id, (long) 0));
            counter = this.service.increment(counter);
            return new ResponseEntity<>(counter, HttpStatus.CREATED);

        } else {

            counter = this.service.increment(counter);
            return new ResponseEntity<>(counter, HttpStatus.OK);

        }

    }

    @RequestMapping(
            value = "/decrement/{id}",
            method = RequestMethod.POST
    )
    public ResponseEntity<?> decrement(@PathVariable Long id) {

        Counter counter = this.service.get(id);

        if (counter == null) {

            counter = this.service.create(new Counter(id, (long) 0));
            counter = this.service.decrement(counter);
            return new ResponseEntity<>(counter, HttpStatus.CREATED);

        } else {

            counter = this.service.decrement(counter);
            return new ResponseEntity<>(counter, HttpStatus.OK);

        }

    }

}
