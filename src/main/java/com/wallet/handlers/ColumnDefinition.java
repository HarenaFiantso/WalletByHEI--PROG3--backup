package com.wallet.handlers;

import com.wallet.annotations.DatabaseField;
import lombok.*;

@Data
public class ColumnDefinition {
  private String javaColumnName;
  private String postgresColumnName;
  private String value;
  private String javaType;
  private String postgresType;
  private DatabaseField columnAnnotation;
}
