# Swagger Validator Badge <img src="https://raw.githubusercontent.com/swagger-api/swagger.io/wordpress/images/assets/SW-logo-clr.png" height="50" align="right">

[![Build Status](https://img.shields.io/jenkins/build.svg?jobUrl=https://jenkins.swagger.io/job/oss-swagger-validator-badge-master)](https://jenkins.swagger.io/view/OSS%20-%20Java/job/oss-swagger-validator-badge-master)

This project shows a "valid swagger" badge on your site, supporting Swagger/OpenAPI 2.0 and OpenAPI 3.x specifications.  

There is an online version hosted on http://validator.swagger.io.  

### Using Docker

You can also pull a docker image of the validator directly from [DockerHub](https://hub.docker.com/r/swaggerapi/swagger-validator-v2/), e.g.:

```
docker pull swaggerapi/swagger-validator-v2:v2.1.7
docker run -it -p 8080:8080 --name swagger-validator-v2 swaggerapi/swagger-validator-v2:v2.1.7
```

Since version `2.0.2` local and non http/https urls are rejected by default, along with redirects; this is controllable with docker env variables / java system properties:

```
docker run -it -p 8080:8080 -e "REJECT_LOCAL=false" -e "REJECT_REDIRECT=false" --name swagger-validator-v2 swaggerapi/swagger-validator-v2:v2.1.7
```

In non docker environments, system properties `rejectLocal` and `rejectRedirect` can be used.



Web UI is reachable at http://localhost:8080/index.html and OpenAPI spec at http://localhost:8080/validator/openapi.json



You can validate OpenAPI specifications version 2.0 (Swagger), 3.0 and 3.1. [Swagger Parser](https://github.com/swagger-api/swagger-parser/blob/master/README.md) is used for semantic validation.
Depending on `jsonSchemaValidation` query parameter value also JSON Schema validation can be executed (default to `true`) 

Additional parameters allow to customize parsing and validation mode.

```
<img src="https://validator.swagger.io/validator?url={YOUR_URL}">
```

Of course the `YOUR_URL` needs to be addressable by the validator (i.e. won't find anything on localhost).  If it validates, you'll get a nice green VALID logo.  Failures will give an INVALID logo, and if there are errors parsing the specification or reaching it, an ugly red ERROR logo.

For example, using [https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json](https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json) as a source, we get ...

![](https://validator.swagger.io/validator?url=https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json)

If your specification fails to validate for some reason, or if there is an error, you can get more information on why by visiting ```https://validator.swagger.io/validator/debug?url={YOUR_URL}```.

Since the validator uses a browserless back-end to fetch the contents and schema, it's not subject to the terrible world of CORS.

### Using cURL

You can also post a spec up to the service with cURL:

```bash
curl -X POST -d @swagger.json -H 'Content-Type:application/json' https://validator.swagger.io/validator/debug
```

In this example, `swagger.json` is the swagger definition in JSON format, in the CWD.

If your swagger definition file is in YAML format, the command needs to be adapted like so:

```bash
curl --data-binary @swagger.yaml -H 'Content-Type:application/yaml' https://validator.swagger.io/validator/debug
```

Note the use of `--data-binary` to avoid stripping newlines, along with a different `Content-Type` header.

### Note

All of the above is also applicable to OpenAPI 3.x specifications; for example, using [https://petstore3.swagger.io/api/v3/openapi.json](https://petstore3.swagger.io/api/v3/openapi.json) as a source, we get ...

![](https://validator.swagger.io/validator?url=https://petstore3.swagger.io/api/v3/openapi.json)

Since version 2.1.0 a `/parseByUrl` and `/parseByContent` are available, returning a serialized parsed specification, with parsing and result configurable by
parameters, e.g. passing `resolve`, etc. See [Swagger Parser](https://github.com/swagger-api/swagger-parser/blob/master/README.md#options).

### Running locally

You can build and run the validator locally:

```bash
mvn package jetty:run
```

And access the validator like such:

```
http://localhost:8080/validator?url={URL}
```

or

```
http://localhost:8080/validator?url=http://petstore.swagger.io/v2/swagger.json
```
```
http://localhost:8080/validator?url=https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v3.0/petstore.yaml
```

## Security contact

Please disclose any security-related issues or vulnerabilities by emailing [security@swagger.io](mailto:security@swagger.io), instead of using the public issue tracker.
