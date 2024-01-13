package com.wallet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseField {
  String name() default "";
  String defaultValue() default "";
  String columnType() default ColumnType.NONE;


  int size() default 0;
  int precision() default 0;
  int scale() default 0;


  boolean required() default false;
  boolean identity() default false;
  boolean references() default false;
  boolean unique() default false;

  GenerativeValue generative() default GenerativeValue.NONE;
}
