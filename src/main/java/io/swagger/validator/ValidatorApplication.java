package io.swagger.validator;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.validator.resources.ValidatorResource;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class ValidatorApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register root resource
        classes.add(ValidatorResource.class);
        return classes;
    }
}