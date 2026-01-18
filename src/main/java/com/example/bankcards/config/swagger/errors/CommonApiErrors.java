package com.example.bankcards.config.swagger.errors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommonApiErrors {

    boolean badRequest() default false;

    boolean unauthorized() default false;

    boolean forbidden() default false;

    boolean cardNotFound() default false;
    boolean userNotFound() default false;

    boolean cardConflict() default false;
    boolean userConflict() default false;

    boolean methodNotAllowed() default false;

    boolean internalServerError() default false;
}

