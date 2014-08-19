# Swagger Validator Badge

This project shows a "valid swagger" badge on your site.  Temporarily, there is an online version hosted on the Reverb petstore sample.

You can validate any swagger specification against the [Swagger 2.0 Schema]() as follows:

```
<img src="http://petstore.swagger.wordnik.com/validator?url={YOUR_URL}">
```

Of course the `YOUR_URL` needs to be addressable by the validator (i.e. won't find anything on localhost).  If it validates, you'll get a nice green VALID logo.  Failures will give an INVALID logo, and if there are errors parsing the specification or reaching it, an ugly red ERROR logo.

For example, using [this](https://raw.githubusercontent.com/reverb/swagger-spec/master/examples/v2.0/json/petstore-expanded.json) as a source:

![](http://petstore.swagger.wordnik.com/validator?url=https://raw.githubusercontent.com/reverb/swagger-spec/master/examples/v2.0/json/petstore-expanded.json)

Since the validator uses a browserless back-end to fetch the contents and schema, it's not subject to the terrible world of CORS.