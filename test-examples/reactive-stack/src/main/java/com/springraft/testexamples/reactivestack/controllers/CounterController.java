package com.springraft.testexamples.reactivestack.controllers;

import com.springraft.testexamples.reactivestack.models.Counter;
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
    // @Autowired
    //private CommunicationService communicationService;

    /*--------------------------------------------------------------------------------*/

    /**
     * TODO
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> get() {

        return new ResponseEntity<>(this.counter.get(), HttpStatus.OK);

    }

    /**
     * TODO
     * @return
     */
    @RequestMapping(value = "/increment", method = RequestMethod.POST)
    public ResponseEntity<?> increment() {

        //communicationService.increment();

        return new ResponseEntity<>(this.counter.increment().block(), HttpStatus.OK);

    }

    /**
     * TODO
     */
    @RequestMapping(value = "/decrement", method = RequestMethod.POST)
    public ResponseEntity<?> decrement() {

        //communicationService.increment();

        return new ResponseEntity<>(this.counter.decrement().block(), HttpStatus.OK);

    }

}
