# Swagger Validator Badge

You can validate any OpenAPI specification against the [OpenAPI 2.0 Schema] and [OpenAPI 3.0 Schema] as follows:

### Running locally

You can build and run the validator locally:

```
mvn package jetty:run
```

And access the validator like such:

```
http://localhost:8080/api/validate?url={URL}
```

or

```
http://localhost:8080/api/validate?url=http://petstore.swagger.io/v2/swagger.json
```
```
http://localhost:8080/api/validate?url=https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v3.0/petstore.yaml
```

---
<img src="http://swagger.io/wp-content/uploads/2016/02/logo.jpg"/>

