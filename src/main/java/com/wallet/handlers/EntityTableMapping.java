package com.wallet.handlers;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@ToString
@EqualsAndHashCode
public class EntityTableMapping<T> {

  @Getter
  private String tableName;
  @Getter
  private String schemaName = "public";
  @Getter
  private EntityColumnMapping primaryKeyColumn;
  @Getter
  private final List<EntityColumnMapping> columnMappings = new ArrayList<>();
  @Getter
  private final Class<T> entityClass;

  private final List<String> columnNames = new ArrayList<>();
  private final HashMap<String, String> fieldToColumnMap = new HashMap<>();

  public EntityTableMapping(Class<T> entityClass) throws Exception {
    this.entityClass = entityClass;

    DatabaseTable tableAnnotation = entityClass.getAnnotation(DatabaseTable.class);
    if (tableAnnotation == null) {
      String className = entityClass.getSimpleName();
      throw new Exception(STR."Class \{className} in package \{entityClass.getPackage()} is not an entity");
    }

    initializeMapping(entityClass, tableAnnotation);
    if (primaryKeyColumn == null) {
      throw new Exception("Entity must have a primary key column");
    }
  }

  private void initializeMapping(Class<T> entityClass, DatabaseTable tableAnnotation) {
    parseSchema(tableAnnotation);
    parseTableName(tableAnnotation);
    mapFieldsToColumns(entityClass.getDeclaredFields());
  }

  private void parseSchema(DatabaseTable tableAnnotation) {
    String schema = tableAnnotation.schema();
    if (!Objects.equals(schema, "public")) {
      this.schemaName = schema;
    }
  }

  private void parseTableName(DatabaseTable tableAnnotation) {
    String specifiedTableName = tableAnnotation.name().toLowerCase().trim();
    this.tableName = !specifiedTableName.isEmpty() ? specifiedTableName : entityClass.getSimpleName().toLowerCase();
  }

  private void mapFieldsToColumns(Field[] fields) {
    for (Field field : fields) {
      DatabaseField columnAnnotation = field.getAnnotation(DatabaseField.class);
      if (columnAnnotation != null) {
        EntityColumnMapping columnMapping = createColumnMapping(field, columnAnnotation);
        columnNames.add(columnMapping.getDatabaseColumnName());
        fieldToColumnMap.put(columnMapping.getDatabaseColumnName(), columnMapping.getJavaFieldName());

        if (columnAnnotation.identity() && primaryKeyColumn == null) {
          primaryKeyColumn = columnMapping;
        } else {
          columnMappings.add(columnMapping);
        }
      }
    }
  }

  private EntityColumnMapping createColumnMapping(Field field, DatabaseField columnAnnotation) {
    EntityColumnMapping columnMapping = new EntityColumnMapping();
    columnMapping.setJavaFieldName(field.getName());
    columnMapping.setDatabaseColumnName(getDatabaseColumnName(field, columnAnnotation));
    columnMapping.setJavaDataType(field.getType().getSimpleName());
    columnMapping.setDatabaseDataType(getDatabaseDataType(field, columnAnnotation));
    columnMapping.setDatabaseFieldAnnotation(columnAnnotation);
    return columnMapping;
  }

  private String getDatabaseColumnName(Field field, DatabaseField columnAnnotation) {
    String specifiedColumnName = columnAnnotation.name().trim();
    return !specifiedColumnName.isEmpty() ? specifiedColumnName : field.getName().toLowerCase();
  }

  private String getDatabaseDataType(Field field, DatabaseField columnAnnotation) {
    String specifiedType = columnAnnotation.fieldType();
    return !specifiedType.isEmpty() ? specifiedType : TypeMapper.get(field.getType().getSimpleName());
  }
}
