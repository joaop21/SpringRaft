package com.springRaft.testexamples.keyvaluestore.node;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v2/keys")
@AllArgsConstructor
public class NodeController {

    private final NodeRepository repository;

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.GET
    )
    public ResponseEntity<?> get(@PathVariable String key) {

        Optional<Node> node = repository.findByKey(key);

        if (node.isEmpty()) {

            Map<String,Object> response = new HashMap<>();
            response.put("message", "Key not found");
            response.put("key", key);

            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        } else {

            Map<String,Object> response = new HashMap<>();
            response.put("action", "get");
            response.put("node", node.get());
            return new ResponseEntity<>(response, HttpStatus.OK);

        }

    }

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.PUT
    )
    public ResponseEntity<?> upsert(@PathVariable String key, @RequestBody String text) {

        String value = text.replaceFirst("value=", "");

        Optional<Node> node = repository.findByKey(key);

        if (node.isEmpty()) {

            Node savedNode = repository.save(new Node(key, value));

            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{key}")
                    .buildAndExpand(savedNode.getKey()).toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(location);

            Map<String,Object> response = new HashMap<>();
            response.put("action", "set");
            response.put("node", savedNode);

            return new ResponseEntity<>(response, headers, HttpStatus.CREATED);

        } else {

            Node savedNode = node.get();
            repository.deleteNodeByKey(key);

            Node newNode = new Node(key, value);
            repository.save(newNode);

            Map<String,Object> response = new HashMap<>();
            response.put("action", "set");
            response.put("node", newNode);
            response.put("prevNode", savedNode);

            return new ResponseEntity<>(response, HttpStatus.OK);

        }

    }

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<?> delete(@PathVariable String key) {

        Optional<Node> node = repository.findByKey(key);

        if (node.isEmpty()) {

            Map<String,Object> response = new HashMap<>();
            response.put("message", "Key not found");
            response.put("key", key);

            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        } else {

            repository.deleteNodeByKey(key);

            Node newNode = new Node(node.get().getCreatedIndex(), node.get().getKey(), null);

            Map<String,Object> response = new HashMap<>();
            response.put("action", "delete");
            response.put("node", newNode);
            response.put("prevNode", node.get());

            return new ResponseEntity<>(response, HttpStatus.OK);

        }

    }

}
