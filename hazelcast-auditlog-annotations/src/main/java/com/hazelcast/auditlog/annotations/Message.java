package com.hazelcast.auditlog.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(SOURCE)
public @interface Message {
    int code();
    String value();
    
}
