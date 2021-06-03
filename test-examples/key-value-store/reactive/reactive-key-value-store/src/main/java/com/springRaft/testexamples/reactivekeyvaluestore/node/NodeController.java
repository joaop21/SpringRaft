package com.springRaft.testexamples.reactivekeyvaluestore.node;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/v2/keys")
@AllArgsConstructor
public class NodeController {

    private final ServiceLock serviceLock;
    private final ServicePublisher servicePublisher;

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.GET
    )
    public Mono<ResponseEntity<?>> get(@PathVariable String key) {

        AtomicReference<ResponseEntity<?>> responseEntity = new AtomicReference<>();

        //return this.serviceLock.get(key)
        return this.servicePublisher.get(key)
                .doOnSuccess(node -> {

                    Map<String,Object> response = new HashMap<>();

                    if (node == null) {

                        response.put("message", "Key not found");
                        response.put("key", key);
                        responseEntity.set(new ResponseEntity<>(response, HttpStatus.NOT_FOUND));

                    } else {

                        response.put("action", "get");
                        response.put("node", node);
                        responseEntity.set(new ResponseEntity<>(response, HttpStatus.OK));

                    }
                })
                .then(Mono.defer(() -> Mono.just(responseEntity.get())));

    }

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.PUT
    )
    public Mono<ResponseEntity<?>> upsert(@PathVariable String key, @RequestBody String text, ServerHttpRequest request) {

        return this.servicePublisher.upsert(key,text)
                .collectList()
                .flatMap(list -> {

                    if (list.size() == 1) {

                        return Mono.create(createSink -> {
                            Map<String,Object> response = new HashMap<>();
                            response.put("action", "set");
                            response.put("node", list.get(0));
                            createSink.success(ResponseEntity.created(request.getURI()).body(response));
                        });

                    } else {

                        return Mono.create(createSink -> {
                            Map<String,Object> response = new HashMap<>();
                            response.put("action", "set");
                            response.put("node", list.get(1));
                            response.put("prevNode", list.get(0));
                            createSink.success(ResponseEntity.ok(response));
                        });

                    }

                });


    }

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.DELETE
    )
    public Mono<ResponseEntity<?>> delete(@PathVariable String key) {

        AtomicReference<ResponseEntity<?>> responseEntity = new AtomicReference<>();

        return this.servicePublisher.delete(key)
                .doOnSuccess(node -> {

                    Map<String,Object> response = new HashMap<>();

                    if (node == null) {

                        response.put("message", "Key not found");
                        response.put("key", key);
                        responseEntity.set(new ResponseEntity<>(response, HttpStatus.NOT_FOUND));

                    } else {

                        response.put("action", "delete");
                        response.put("node", new Node(node.getCreatedIndex(), node.getKey(), null, true));
                        response.put("prevNode", node);
                        responseEntity.set(ResponseEntity.ok(response));

                    }

                })
                .then(Mono.defer(() -> Mono.just(responseEntity.get())));

    }

}
