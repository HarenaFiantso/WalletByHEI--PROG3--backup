package com.wallet.handers;

public class TypeMapper {
  public static final String NO_TYPE_MAPPED = "<mapped-type>";

  public static String get(String type){
    return switch (type.toLowerCase()){
      case "boolean" -> "boolean";
      case "int", "integer" -> "integer";
      case "long", "bigintger", "bigint" -> "bigint";
      case "float" -> "float";
      case "double" -> "double precision";
      case "char", "character" -> "char";
      case "string", "charsequence" -> "varchar";
      case "date", "localdate" -> "date";
      case "time", "localtime" -> "time";
      case "timestamp", "localdatetime", "instant" -> "timestamp";
      default -> NO_TYPE_MAPPED;
    };
  }

  public static String typeToSequence(String type){
    return switch (type){
      case "smallint" -> "smallserial";
      case "bigint", "long" -> "bigserial";
      default -> "serial";
    };
  }
}
