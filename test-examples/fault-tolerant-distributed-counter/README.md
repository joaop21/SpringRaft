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

After this, simply make requests to any server in the Raft cluster as if the request were directed to the application server.

### Embedded
