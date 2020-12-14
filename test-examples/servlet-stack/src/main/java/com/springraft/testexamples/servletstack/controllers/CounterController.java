package com.springraft.testexamples.servletstack.controllers;

import com.springraft.testexamples.servletstack.models.Counter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("counter")
public class CounterController {

    /*--------------------------------------------------------------------------------*/

    /* Constant Counter, which doesn't change its reference */
    private final Counter counter = new Counter();

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    @RequestMapping(value = "/increment", method = RequestMethod.POST)
    public ResponseEntity<?> increment() {

        return new ResponseEntity<>(this.counter.increment(), HttpStatus.OK);

    }

    /**
     * TODO
     * */
    @RequestMapping(value = "/decrement", method = RequestMethod.POST)
    public ResponseEntity<?> decrement() {

        return new ResponseEntity<>(this.counter.decrement(), HttpStatus.OK);

    }

}
