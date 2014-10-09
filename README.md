# Swagger Validator Badge

This project shows a "valid swagger" badge on your site.  Temporarily, there is an online version hosted on the Reverb petstore sample.

You can validate any swagger specification against the [Swagger 2.0 Schema]() as follows:

```
<img src="http://online.swagger.io/validator?url={YOUR_URL}">
```

Of course the `YOUR_URL` needs to be addressable by the validator (i.e. won't find anything on localhost).  If it validates, you'll get a nice green VALID logo.  Failures will give an INVALID logo, and if there are errors parsing the specification or reaching it, an ugly red ERROR logo.

For example, using [https://raw.githubusercontent.com/reverb/swagger-spec/master/examples/v2.0/json/petstore-expanded.json](https://raw.githubusercontent.com/reverb/swagger-spec/master/examples/v2.0/json/petstore-expanded.json) as a source, we get ...

![](http://online.swagger.io/validator?url=https://raw.githubusercontent.com/reverb/swagger-spec/master/examples/v2.0/json/petstore-expanded.json)

If your specification fails to validate for some reason, or if there is an error, you can get more information on why by visiting ```http://online.swagger.io/validator/debug?url={YOUR_URL}```.

Since the validator uses a browserless back-end to fetch the contents and schema, it's not subject to the terrible world of CORS.
