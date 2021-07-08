# Key-Value Store

It contains examples that mimic a Key-Value Store to be able to compare our implementations of the Raft algorithm against etcd's implementation. It also includes the different deployments in docker containers and benchmarks that test them.

## Compatible API

All implementations of Raft implement a subset of the etcd API that is compatible, so that it is possible to compare both services equally. In this case the compatible API endpoints are:

* Get the value of a key: 
    ```http
    GET /v2/keys/{key} HTTP/1.1
    Accept: */*
    ```
* Insert or update the value of a key:
    ```http
    PUT /v2/keys/{key} HTTP/1.1
    Accept: */*

    value={value}
    ```
* Delete a key:
    ```http
    DELETE /v2/keys/message HTTP/1.1
    Accept: */*
    ```

## Versions

Throughout the development and testing phase, new examples were created in order to experiment with new configurations. Each different configuration is in a different version.

### V1

It includes all configurations using the two stacks. The **servlet stack** is accompanied by **Hibernate** and the **H2 in-memory database**. While the **reactive stack** comes with **R2DBC** and the **H2 in-memory database**.

### V2

It includes a key-value-store that does not make use of any persistence mechanism. Instead, it keeps a HashMap in memory, speeding up data access and update.

### V3

Includes a key-value-store developed in the reactive stack where access to the **H2 in-memory database** is managed by **Hibernate** (JDBC -> blocking).

## Deployment

The various infrastructures ready to be deployed are in the `/deployment` folder on each version folder. Just choose which infrastructure to use, move to the correspondent folder and deploy it with:
`docker-compose -f __infrastructure__.yaml up -d --build`

    * If an error occurs at infrastructure startup, it is most likely due to the .jar not being created. When this happens you need to go to the infrastructure code folder and run the `mvn --batch-mode --update-snapshots verify` command to generate the necessary .jar files.

To teardown the infrastructure just: 
`docker-compose -f __infrastructure__.yaml down -v`

## Benckmarking

Essentially, there are two load tests that target different data:

1. Same data. All operations are performed against a single key.
2. Random data. Operations are done on different keys.

The results of these two tests could show differences in the treatment of operations on the same data.

Furthermore, these two tests are coded using different tools, because the first tool chosen (**locust**) reached 90% of CPU usage, and for a larger number of users, it did not create all the users requested. Thus it was necessary to add a secondary tool (**wrk**) to validate the tests.

## Results

The results collected for the different versions are under the `/stats` folder.
