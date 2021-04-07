# Key-Value Store

It contains examples that mimic a Key-Value Store to be able to compare our implementations of the Raft algorithm against etcd's implementation. It also includes the different deployments in docker containers and benchmarks that test them.

## Deployment

The various infrastructures ready to be deployed are in the `/deployment` folder. Just choose which infrastructure to use, move to the correspondent folder and deploy it with:
`docker-compose up -d`

To teardown the infrastructure just: 
`docker-compose down -v`

## Benckmarking

The benchmark written in python and using locust is in the `/benchmark` folder. Some data collected on the different infrastructures is in the `/benchmark/stats` folder. To run the benchmark just invoke, for example:

`locust -u 192 -r 192`

This command runs the benchmark with 192 users which distributed over a 3 server infrastructure will make each one handle 64 users.

## Compatible API

Both implementations of Raft implement a subset of the etcd API that is compatible, so that it is possible to compare both services equally. In this case the compatible API endpoints are:

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

