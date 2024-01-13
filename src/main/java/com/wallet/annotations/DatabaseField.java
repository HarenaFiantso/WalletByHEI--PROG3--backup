package com.wallet.annotations;

import com.wallet.types.GenerativeValue;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseField {
  String name() default "";

  String fieldType() default FieldType.NONE;

  int size() default 0;

  boolean required() default false;

  boolean identity() default false;

  boolean references() default false;

  GenerativeValue generative() default GenerativeValue.NONE;
}
