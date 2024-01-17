package com.wallet.handlers;

import com.wallet.annotations.DatabaseField;
import lombok.*;

@Data
public class EntityColumnMapping {
  private String javaFieldName;
  private String databaseColumnName;
  private String fieldValue;
  private String javaDataType;
  private String databaseDataType;
  private DatabaseField databaseFieldAnnotation;
}
