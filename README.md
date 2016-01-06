# Swagger Validator Badge

[![Build Status](https://travis-ci.org/swagger-api/validator-badge.svg?branch=master)](https://travis-ci.org/swagger-api/validator-badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.swagger/swagger-validator/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.swagger/swagger-validator)

This project shows a "valid swagger" badge on your site.  There is an online version hosted on http://swagger.io.  You can also pull a docker image of the validator directly from [DockerHub](https://hub.docker.com/r/swaggerapi/swagger-validator/).

You can validate any OpenAPI specification against the [OpenAPI 2.0 Schema](https://github.com/OAI/OpenAPI-Specification/blob/master/schemas/v2.0/schema.json) as follows:

```
<img src="http://online.swagger.io/validator?url={YOUR_URL}">
```

Of course the `YOUR_URL` needs to be addressable by the validator (i.e. won't find anything on localhost).  If it validates, you'll get a nice green VALID logo.  Failures will give an INVALID logo, and if there are errors parsing the specification or reaching it, an ugly red ERROR logo.

For example, using [https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json](https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json) as a source, we get ...

![](http://online.swagger.io/validator?url=https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json)

If your specification fails to validate for some reason, or if there is an error, you can get more information on why by visiting ```http://online.swagger.io/validator/debug?url={YOUR_URL}```.

Since the validator uses a browserless back-end to fetch the contents and schema, it's not subject to the terrible world of CORS.

### Running locally

You can build and run the validator locally:

```
mvn package jetty:run
```

And access the validator like such:

```
http://localhost:8002/?url={URL}
```

or

```
http://localhost:8002/?url=http://petstore.swagger.io/v2/swagger.json
```
