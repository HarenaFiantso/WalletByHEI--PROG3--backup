package com.wallet.mappers;

import java.util.HashMap;
import java.util.Map;

public class JavaToDatabaseTypeMapper {
  private static final String NO_TYPE_MAPPED = "<mapped-type>";
  private static final Map<String, String> typeMappings = new HashMap<>();

  static {
    typeMappings.put("boolean", "boolean");
    typeMappings.put("int", "integer");
    typeMappings.put("integer", "integer");
    typeMappings.put("long", "bigint");
    typeMappings.put("bigintger", "bigint");
    typeMappings.put("bigint", "bigint");
    typeMappings.put("float", "float");
    typeMappings.put("double", "double precision");
    typeMappings.put("char", "char");
    typeMappings.put("character", "char");
    typeMappings.put("string", "varchar");
    typeMappings.put("charsequence", "varchar");
    typeMappings.put("date", "date");
    typeMappings.put("localdate", "date");
    typeMappings.put("time", "time");
    typeMappings.put("localtime", "time");
    typeMappings.put("timestamp", "timestamp");
    typeMappings.put("localdatetime", "timestamp");
    typeMappings.put("instant", "timestamp");
  }

  public static String get(String type) {
    if (type == null || type.trim().isEmpty()) {
      return NO_TYPE_MAPPED;
    }
    return typeMappings.getOrDefault(type.toLowerCase(), NO_TYPE_MAPPED);
  }
}
