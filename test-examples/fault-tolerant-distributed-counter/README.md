# Fault-Tolerant Distributed Counter

This example already uses the Raft implementations present in the root of the project. It represents a program that manages multiple counters that can be accessed, incremented and decremented. This example is implemented in both the servlet stack and the reactive stack, and in two different configurations for each of the stacks, which are independent and embedded respectively.

## Deployment

Infrastructure deployment varies depending on the configurations to be used.

### Independent

Using application servers apart from the Raft cluster requires deploying twice as many servers, as will be demonstrated below. If we use the counter application that uses the servlet stack, with a Raft cluster that also uses the servlet stack, a possible deployment could be:

1. Go to the application folder `/test-examples/fault-tolerant-distributed-counter/servlet/independent`;
1. Deploy 3 application server using different ports in different terminal tabs:
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=10001"`
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=10002"`
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=10003"`
1. Go to the raft application folder `/servlet`
1. Deploy the Raft cluster using different terminal tabs:
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.address=localhost --server.port=8001 --raft.hostname=localhost:8001 --raft.cluster=localhost:8001,localhost:8002,localhost:8003 --raft.election-timeout-min=2000 --raft.election-timeout-max=3000 --raft.heartbeat=1000 --raft.application-server=localhost:10001"`
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.address=localhost --server.port=8002 --raft.hostname=localhost:8001 --raft.cluster=localhost:8001,localhost:8002,localhost:8003 --raft.election-timeout-min=2000 --raft.election-timeout-max=3000 --raft.heartbeat=1000 --raft.application-server=localhost:10002"`
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.address=localhost --server.port=8003 --raft.hostname=localhost:8001 --raft.cluster=localhost:8001,localhost:8002,localhost:8003 --raft.election-timeout-min=2000 --raft.election-timeout-max=3000 --raft.heartbeat=1000 --raft.application-server=localhost:10003"`

After this, simply make requests to any server in the Raft cluster as if the request were directed to the application server. Find more about this configuration [here](https://github.com/joaop21/SpringRaft/wiki/How-To-Use#ChooseConfiguration-Independent).

### Embedded

If we use the counter application that uses the servlet stack, with a Raft cluster that also uses the servlet stack, a possible deployment could be:

1. Go to the application folder `/test-examples/fault-tolerant-distributed-counter/servlet/embedded`;
1. Deploy 3 application server using different ports in different terminal tabs:
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8001 --raft.hostname=localhost:8001 --raft.cluster=localhost:8001,localhost:8002,localhost:8003 --raft.election-timeout-min=2000 --raft.election-timeout-max=3000 --raft.heartbeat=1000 --raft.application-server=localhost:8001 -raft.state-machine-strategy=EMBEDDED"`
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8002 --raft.hostname=localhost:8002 --raft.cluster=localhost:8001,localhost:8002,localhost:8003 --raft.election-timeout-min=2000 --raft.election-timeout-max=3000 --raft.heartbeat=1000 --raft.application-server=localhost:8002 -raft.state-machine-strategy=EMBEDDED"`
    1. `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8003 --raft.hostname=localhost:8003 --raft.cluster=localhost:8001,localhost:8002,localhost:8003 --raft.election-timeout-min=2000 --raft.election-timeout-max=3000 --raft.heartbeat=1000 --raft.application-server=localhost:8003 -raft.state-machine-strategy=EMBEDDED"`

After this, simply make requests to any server as if the request were directed to the application server. Find more about this configuration [here](https://github.com/joaop21/SpringRaft/wiki/How-To-Use#ChooseConfiguration-Embedded).

## Client Requests

* Get the value of a counter with the ID 1:
    ```http
    GET /raft/counter/1 HTTP/1.1
    Host: localhost:8002
    Accept: */*
    ```
    * The response is 404 (Not Found) if the counter with that ID doesn't exist;
    * The response is 200 (OK) if the counter exists with a body like:
        ```json
        {
        "id": 1,
        "value": 16
        }
        ```
* Increment the value of the counter with ID 1:
    ```http
    POST /raft/counter/increment/1 HTTP/1.1
    Host: localhost:8002
    Accept: */*
    ```
    * The response is 201 (Created) if the counter with that ID does not exist, and the response body is:
    ```json
    {
    "id": 1,
    "value": 1
    }
    ```
    * The response is 200 (OK) if the counter already exists, and the response body is, for example:
    ```json
    {
    "id": 1,
    "value": 15
    }
    ```
