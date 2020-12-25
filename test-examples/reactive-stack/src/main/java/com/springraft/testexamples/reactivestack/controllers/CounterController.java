package com.springraft.testexamples.reactivestack.controllers;

import com.springraft.testexamples.reactivestack.models.Counter;
import com.springraft.testexamples.reactivestack.services.CommunicationService;
import com.springraft.testexamples.reactivestack.services.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("counter")
public class CounterController {

    /*--------------------------------------------------------------------------------*/

    /* Constant Counter, which doesn't change its reference */
    private final Counter counter = new Counter();

    /* Inject Communication Service */
    @Autowired
    private CounterService counterService;

    /* Inject Communication Service */
    @Autowired
    private CommunicationService communicationService;

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> get() {

        return new ResponseEntity<>(counterService.get(), HttpStatus.OK);

    }

    /**
     * TODO
     * @return
     */
    @RequestMapping(value = "/increment", method = RequestMethod.POST)
    public ResponseEntity<?> increment() {

        communicationService.increment();

        return new ResponseEntity<>(counterService.increment(), HttpStatus.OK);

    }

    /**
     * TODO
     */
    @RequestMapping(value = "/decrement", method = RequestMethod.POST)
    public ResponseEntity<?> decrement() {

        communicationService.decrement();

        return new ResponseEntity<>(counterService.decrement(), HttpStatus.OK);

    }

}
