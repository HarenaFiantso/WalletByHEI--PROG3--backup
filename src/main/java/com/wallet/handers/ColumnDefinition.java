package com.wallet.handers;

import com.wallet.annotations.DatabaseField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ColumnDefinition {
  private String javaColumnName;
  private String postgresColumnName;
  private DatabaseField columnAnnotation;
  private String value;

  private String javaType;
  private String postgresType;
}
