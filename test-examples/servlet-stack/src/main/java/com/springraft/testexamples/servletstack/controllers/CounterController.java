package com.springraft.testexamples.servletstack.controllers;

import com.springraft.testexamples.servletstack.services.CommunicationService;
import com.springraft.testexamples.servletstack.services.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("counter")
public class CounterController {

    /*--------------------------------------------------------------------------------*/

    /* Inject Counter Service */
    @Autowired
    private CounterService counterService;

    /* Inject Communication Service */
    @Autowired
    private CommunicationService communicationService;

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     * */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> get() {

        return new ResponseEntity<>(this.counterService.get(1), HttpStatus.OK);

    }

    /**
     * TODO
     * */
    @RequestMapping(value = "/increment", method = RequestMethod.POST)
    public ResponseEntity<?> increment() {

        communicationService.increment();

        return new ResponseEntity<>(this.counterService.increment(1), HttpStatus.OK);

    }

    /**
     * TODO
     * */
    @RequestMapping(value = "/decrement", method = RequestMethod.POST)
    public ResponseEntity<?> decrement() {

        communicationService.decrement();

        return new ResponseEntity<>(this.counterService.decrement(1), HttpStatus.OK);

    }

}
