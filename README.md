# Swagger Validator Badge <img src="https://raw.githubusercontent.com/swagger-api/swagger.io/wordpress/images/assets/SW-logo-clr.png" height="50" align="right">

[![Build Status](https://img.shields.io/jenkins/s/https/jenkins.swagger.io/view/OSS%20-%20Java/job/oss-swagger-validator-badge-v1.svg)](https://jenkins.swagger.io/view/OSS%20-%20Java/job/oss-swagger-validator-badge-v1)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.swagger/swagger-validator/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.swagger/swagger-validator)
[![Build Status](https://jenkins.swagger.io/view/OSS%20-%20Java/job/oss-swagger-validator-badge-v1/badge/icon?subject=jenkins%20build)](https://jenkins.swagger.io/view/OSS%20-%20Java/job/oss-swagger-validator-badge-v1/)

----

**NOTE:** If you're looking for `validator-badge` 2.X and OpenApi 3.0, please refer to [master branch](https://github.com/swagger-api/validator-badge)

----

This project shows a "valid swagger" badge on your site.  There is an online version hosted on http://swagger.io.  You can also pull a docker image of the validator directly from [DockerHub](https://hub.docker.com/r/swaggerapi/swagger-validator/).

You can validate any OpenAPI specification against the [OpenAPI 2.0 Schema](https://github.com/OAI/OpenAPI-Specification/blob/master/schemas/v2.0/schema.json) as follows:

```
<img src="http://online.swagger.io/validator?url={YOUR_URL}">
```

Of course the `YOUR_URL` needs to be addressable by the validator (i.e. won't find anything on localhost).  If it validates, you'll get a nice green VALID logo.  Failures will give an INVALID logo, and if there are errors parsing the specification or reaching it, an ugly red ERROR logo.

For example, using [https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json](https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json) as a source, we get ...

![](https://online.swagger.io/validator?url=https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v2.0/json/petstore-expanded.json)

If your specification fails to validate for some reason, or if there is an error, you can get more information on why by visiting ```http://online.swagger.io/validator/debug?url={YOUR_URL}```.

Since the validator uses a browserless back-end to fetch the contents and schema, it's not subject to the terrible world of CORS.

You can also post a spec up to the service with CURL:

```
curl -X POST -d @swagger.json -H 'Content-Type:application/json' http://online.swagger.io/validator/debug
```

In this example, `swagger.json` is the swagger definition in JSON format, in the CWD.

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

## Security contact

Please disclose any security-related issues or vulnerabilities by emailing [security@swagger.io](mailto:security@swagger.io), instead of using the public issue tracker.

## License

```
Copyright 2019 SmartBear Software

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```