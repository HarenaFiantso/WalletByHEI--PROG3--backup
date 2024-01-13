package com.wallet.annotations;

public class ColumnType{
  public static final String NONE = "";

  public static final String BOOLEAN = "boolean";

  public static final String SMALL_INT = "small_int";
  public static final String INT = "int";
  public static final String BIG_INT = "bigint";
  public static final String NUMERIC = "numeric";
  public static final String DOUBLE = "double precision";
  public static final String FLOAT = "float";

  public static final String TEXT = "text";
  public static final String VARCHAR = "varchar";
  public static final String CHAR = "char";


  public static final String SMALL_SERIAL = "smallserial";
  public static final String BIG_SERIAL = "bigserial";
  public static final String SERIAL = "serial";

  public static final String DATE = "date";
  public static final String TIME = "time";
  public static final String TIME_Z = "timez";
  public static final String TIMESTAMP = "timestamp";
  public static final String TIMESTAMP_Z = "timestampz";
  public static final String BYTEA = "bytea";
  public static final String ARRAY = "array";
  private ColumnType(){}
}
