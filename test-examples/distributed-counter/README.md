# Distributed Counter

This example represents a counter implemented in both stacks, which can be incremented, decremented and accessed.
This counter is an entity in the database, and to ensure no lost-updates occur, it is used Optimistic Locking. In case of rollback, the operation of increment/decrement is repeated, until a commit is reached. This is possible because both operations are commutative.

When there are more than one server, one of them has to be the leader, so it can handles the client requests and broadcast them to the other servers. No protocol is assumed, no timeout is treated, no lost communication is retransmitted.

It is just an example that explores important parts of the Raft algorithm (communication and persistence), and collects preliminary results to compare stacks.

## Deployment

To deploy one server only, you just need to be in the root folder of one stack, then:

* `mvn spring-boot:run`


To deploy, for example, 3 servers, you need to run this commands in separate tabs:

* `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8001 --group.leader=true --group.members=8001,8002,8003"`

* `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8002 --group.members=8001,8002,8003"`

* `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8003 --group.members=8001,8002,8003"`

In the end you'll have 3 servers (8001, 8002, 8003), where 8001 is the leader.

## Benchamark

To run tests against the cluster of servers, it is used [wrk](https://github.com/wg/wrk) benchmark.
If you are in the benchmark folder you can run, for example:

* `wrk -c 64 -d 1m -t 64 -s CounterIncrementPost.lua http://localhost:8001/counter/increment`

  * `-c 64`: Number of concurrent clients making requests
  * `-t 64`: Number of threads that handle the existing clientes
  * `-d 1m`: Benchmark runs for 1 minute
  * `-s CounterIncrementPost.lua`: File that contains the POST to the API
  * `http://localhost:8001/counter/increment`: Route to the leader API
