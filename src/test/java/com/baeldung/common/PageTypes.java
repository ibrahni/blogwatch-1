package com.baeldung.common;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.baeldung.site.SitePage;

@Target(METHOD)
@Retention(RUNTIME)
public @interface PageTypes {

    SitePage.Type[] value();

}
