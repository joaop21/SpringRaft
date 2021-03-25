
[![Build status](https://github.com/joaop21/SpringRaft/workflows/Test-Examples/badge.svg?branch=main)](https://github.com/joaop21/SpringRaft/actions?query=workflow%3ATest-Examples)

# Test Examples

This folder contains examples that were used to test both Servlet and Reactive stacks, and the Raft implementation too.

* [distributed-counter](https://github.com/joaop21/SpringRaft/tree/main/test-examples/distributed-counter): Contains communication and persistence tests in both stacks, using a counter which can be incremented, decremented and accessed.

* [fault-tolerant-distributed-counter](https://github.com/joaop21/SpringRaft/tree/main/test-examples/fault-tolerant-distributed-counter): It contains an example of an application that manages multiple counters, using both the servlet stack and the reactive stack, and also two different configurations for each of the stacks, which are respectively, independent and embedded.
