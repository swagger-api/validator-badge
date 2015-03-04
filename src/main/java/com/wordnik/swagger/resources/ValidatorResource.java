package com.wordnik.swagger.resources;

import com.wordnik.swagger.services.*;
import com.wordnik.swagger.annotations.*;
import com.wordnik.swagger.models.SchemaValidationError;

import com.github.fge.jsonschema.core.report.ProcessingReport;

import javax.ws.rs.core.*;
import javax.ws.rs.*;
import javax.servlet.http.*;

import java.util.*;

@Path("/")
@Api(value = "/validator", description = "Validator for Swagger Specs")
public class ValidatorResource {
  ValidatorService service = new ValidatorService();

  @GET
  @ApiOperation(value = "Validates a spec based on a URL")
  @ApiResponses(value = {  })
  @Produces({"image/png"})
  public Response validateByUrl(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @ApiParam(value = "url of spec to validate" ) @QueryParam("url")String url) throws WebApplicationException {
    try {
      service.validateByUrl(request, response, url);
      response.addHeader("Cache-Control", "no-cache");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return Response.ok().build();
  }

  @GET
  @Path("/debug")
  @Produces({"application/json"})
  @ApiOperation(value = "Validates a spec based on a URL",
    response = SchemaValidationError.class,
    responseContainer = "List")
  @ApiResponses(value = {  })
  public Response debugByUrl(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @ApiParam(value = "url of spec to validate" ) @QueryParam("url")String url) throws WebApplicationException {
    try {
      return Response.ok().entity(service.debugByUrl(request, response, url)).build();
    }
    catch (Exception e) {
      return Response.status(500).build();
    }
  }

  @POST
  @Path("/debug")
  @Produces({"application/json"})
  @ApiOperation(value = "Validates a spec based on a URL",
    response = SchemaValidationError.class,
    responseContainer = "List")
  @ApiResponses(value = {  })
  public Response debugByContent(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @ApiParam(value = "spec contents" )String spec) throws WebApplicationException {
    try {
      return Response.ok().entity(service.debugByContent(request, response, spec)).build();
    }
    catch (Exception e) {
      return Response.status(500).build();
    }
  }
}