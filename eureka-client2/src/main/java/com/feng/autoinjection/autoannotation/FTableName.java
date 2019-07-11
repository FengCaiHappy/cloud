package com.feng.autoinjection.autoannotation;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FTableName {
    String name() default "";
}
