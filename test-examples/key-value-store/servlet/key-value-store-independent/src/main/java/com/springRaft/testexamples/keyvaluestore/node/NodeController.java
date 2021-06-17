package com.springRaft.testexamples.keyvaluestore.node;

import com.springRaft.testexamples.keyvaluestore.node.service.NodeService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

@RestController
@Scope("singleton")
@RequestMapping("/v2/keys")
@AllArgsConstructor
public class NodeController {

    private final NodeService service;

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.GET
    )
    public ResponseEntity<?> get(@PathVariable String key) {

        Optional<Node> node = this.service.get(key);

        Map<String,Object> response = new HashMap<>();
        if (node.isEmpty()) {

            response.put("message", "Key not found");
            response.put("key", key);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        } else {

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

        List<Node> list = this.service.upsert(key, text.replaceFirst("value=", ""));

        if (list.size() == 1) {

            Map<String,Object> response = new HashMap<>();
            response.put("action", "set");
            response.put("node", list.get(0));

            return ResponseEntity
                    .created(ServletUriComponentsBuilder.fromCurrentRequest().build().toUri())
                    .body(response);

        } else {

            Map<String,Object> response = new HashMap<>();
            response.put("action", "set");
            response.put("node", list.get(1));
            response.put("prevNode", list.get(0));

            return ResponseEntity.ok(response);

        }

    }

    /* --------------------------------------------------- */

    @RequestMapping(
            value = "{key}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<?> delete(@PathVariable String key) {

        Optional<Node> node = this.service.delete(key);

        if (node.isEmpty()) {

            Map<String,Object> response = new HashMap<>();
            response.put("message", "Key not found");
            response.put("key", key);

            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        } else {

            Node newNode = new Node(node.get().getCreatedIndex(), node.get().getKey(), null);

            Map<String,Object> response = new HashMap<>();
            response.put("action", "delete");
            response.put("node", newNode);
            response.put("prevNode", node.get());

            return new ResponseEntity<>(response, HttpStatus.OK);

        }

    }

}
