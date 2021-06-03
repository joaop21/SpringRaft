package com.springRaft.testexamples.reactivekeyvaluestore.node;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/v2/keys")
@AllArgsConstructor
public class NodeController {

    // private final ServiceUnsafe service;
    // private final ServiceLock service;
    private final ServicePublisher service;

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.GET
    )
    public Mono<ResponseEntity<Map<String,Object>>> get(@PathVariable String key) {

        AtomicReference<ResponseEntity<Map<String,Object>>> responseEntity = new AtomicReference<>();

        return this.service.get(key)
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
    public Mono<ResponseEntity<Map<String,Object>>> upsert(@PathVariable String key, @RequestBody String text, ServerHttpRequest request) {

        return this.service.upsert(key,text)
                .flatMap(list -> {

                    Map<String,Object> response = new HashMap<>();

                    if (list.size() == 1) {

                        return Mono.create(createSink -> {
                            response.put("action", "set");
                            response.put("node", list.get(0));
                            createSink.success(ResponseEntity.created(request.getURI()).body(response));
                        });

                    } else {

                        return Mono.create(createSink -> {
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
    public Mono<ResponseEntity<Map<String,Object>>> delete(@PathVariable String key) {

        AtomicReference<ResponseEntity<Map<String,Object>>> responseEntity = new AtomicReference<>();

        return this.service.delete(key)
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
