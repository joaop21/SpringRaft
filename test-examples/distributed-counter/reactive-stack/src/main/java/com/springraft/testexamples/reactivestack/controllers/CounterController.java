package com.springraft.testexamples.reactivestack.controllers;

import com.springraft.testexamples.reactivestack.services.CommunicationService;
import com.springraft.testexamples.reactivestack.services.CounterService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("counter")
public class CounterController {

    /*--------------------------------------------------------------------------------*/

    /* Inject Communication Service */
    private final CounterService counterService;

    /* Inject Communication Service */
    private final CommunicationService communicationService;

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
     */
    @RequestMapping(value = "/increment", method = RequestMethod.POST)
    public ResponseEntity<?> increment() {

        return new ResponseEntity<>(
                communicationService.increment()
                        .collectList()
                        .then(counterService.increment()),
                HttpStatus.OK
        );

        //return new ResponseEntity<>(counterService.increment(), HttpStatus.OK);

    }

    /**
     * TODO
     */
    @RequestMapping(value = "/decrement", method = RequestMethod.POST)
    public ResponseEntity<?> decrement() {

        return new ResponseEntity<>(
                communicationService.decrement()
                        .collectList()
                        .then(counterService.decrement()),
                HttpStatus.OK);

    }

}
