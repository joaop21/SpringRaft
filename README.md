
[![Build status](https://github.com/joaop21/SpringRaft/workflows/Main-Build/badge.svg?branch=main)](https://github.com/joaop21/SpringRaft/actions?query=workflow%3AMain-Build)
[![Build status](https://github.com/joaop21/SpringRaft/workflows/Test-Examples/badge.svg?branch=main)](https://github.com/joaop21/SpringRaft/actions?query=workflow%3ATest-Examples)

# SpringRaft

SpringRaft is a dissertation project, which consists in the Raft's consensus algorithm implementation, both in Servlet and Reactive stacks. This implementations should be modular, so that they can be expanded, and should be generic, so that they can be reused in different use cases.

The final purpose of this project is the comparison of the 2 stacks, when applied in this case.

If you want to know more about this project visit the [wiki](https://github.com/joaop21/SpringRaft/wiki).

## Repo Structure

Raft implementation using Spring **Servlet** Stack is under `servlet/`.

Raft implementation using Spring **Reactive** Stack is under `servlet/`.

Examples and tests built before and after the Raft implementation, are under `test-examples/`.

Deployment of the independent configurations of both implementations is under `deployment/`.

Workflows for testing the Pull Requests are under `.github/workflows/`.
