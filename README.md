# WAES Technical Assignment

[![Build Status](https://travis-ci.org/ppedemon/waes-ta.svg?branch=master)](https://travis-ci.org/ppedemon/waes-ta) [![Coverage Status](https://coveralls.io/repos/github/ppedemon/waes-ta/badge.svg?branch=master)](https://coveralls.io/github/ppedemon/waes-ta?branch=master) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/f5ac469ff1ac4f3e8c433807280f8f09)](https://www.codacy.com/app/ppedemon/waes-ta?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ppedemon/waes-ta&amp;utm_campaign=Badge_Grade)

## Overview

The assignment is implemented as a set of asynchronous, stateless HTTP endpoints. I've used
[Vert.x](https://vertx.io/) and [Rective Java](https://github.com/ReactiveX/RxJava), with
a design mirroring a typical Spring solution, in the sense that the code features a
controller, service, and repository layers. For data persistence I'm using MongoDB.

## Features

* CI pipeline running tests, static code reports, and building+pushing Docker image to
    [Docker Hub](https://cloud.docker.com/repository/docker/ppedemon/waes-ta).
* 95% test coverage according to Jacoco. Full details available in [Coveralls](https://coveralls.io/github/ppedemon/waes-ta).
* "A code quality" according to [Codacy](https://app.codacy.com/project/ppedemon/waes-ta/dashboard?bid=13401365).
* Current implementation hosted in a [Kubernetes cluster](http://184.172.247.245:30800/swagger).
    Check the [waes-ta-devops](https://github.com/ppedemon/waes-ta-devops) project for the full details.
* API is secured with bearer JWT authorization. User management and JWT token negotiation
    is delegated to [KeyCloak](https://www.keycloak.org/).
* API is fully documented with [Swagger](http://184.172.247.245:30800/swagger).

## Usage

Coming soon!

## Assumptions

* **Payload not restricted to JSON**: the application doesn't assume that the base64 encoded
    data to compare is actually JSON. You can send any kind of data as long as it's base64 encoded.
* **Users provide IDs:** Since the statement requires that endpoints for pushing data to
    compare (`/v1/diff/{id}/left` and `/v1/diff/{id}/right`) get IDs explicitly, I'm assuming
    that the user is responsible for providing them. When users are in control of resource
    IDs, it makes sense to [use the `PUT` method](https://stackoverflow.com/questions/630453/put-vs-post-in-rest)
    with upsert semantics for resource creation. This is exactly what I'm doing in my implementation.
* **Base64 Data must be decoded before comparing:** Due to padding two different data blobs can end up
    having base64 encoding with equal length. It would be very unnatural for the Rest API to report
    equal length and a list of differences when the original sides to compare were actually of different
    length. Therefore the compare endpoint decodes base64 data to byte arrays before comparing.
* **Comparison IDs don't have to be globally unique:** It would be too restrictive to ask users to provide
    globally unique comparison IDs. So IDs have to be unique **only** at the user level. Comparisons are
    uniquely identified by a combination of a unique user ID and the user-provided comparison ID. User
    information (including the user ID) is provided to the API by means of a Bearer JWT token.

## Possible Improvements

* Provide endpoint to page through all user's comparisons. This is easy, but I haven't had the time to code it 😟.
* Add a description to both sides of a comparison, so users can get better insights about comparisons when querying.
* MongoDB limits document size to 16Mb, so using MongoDB for storage might is not suitable large data chunks.
  In order to avoid running into data size issues, this applications limits data size to 5Mb. We could circumvent
  this limitation by using another kind of storage, such as GridFS or HDFS.
