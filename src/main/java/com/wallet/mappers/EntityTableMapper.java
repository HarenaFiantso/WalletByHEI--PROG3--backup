package com.wallet.mappers;

import lombok.*;
import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.FieldType;
import com.wallet.annotations.DatabaseTable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Maps an entity class to its corresponding database table representation.
 * It extracts the table name, schema, columns, and their respective types
 * based on the provided annotations in the entity class.
 *
 * @param <T> the type of the entity class being mapped
 */
@ToString
@EqualsAndHashCode
public class EntityTableMapper<T> {

  @Getter
  private String name;
  @Getter
  private String schema = "public";
  @Getter
  private EntityColumnMapper primaryKeyColumn;
  @Getter
  private final List<EntityColumnMapper> otherColumns = new ArrayList<>();
  @Getter
  private final Class<T> entityClass;

  /**
   * -- GETTER --
   *  Returns a list of column names for the mapped table.
   *
   */
  @Getter
  private final List<String> columnNames = new ArrayList<>();
  private final HashMap<String, String> columnMapping = new HashMap<>();

  /**
   * Constructs an EntityTableMapper for the given entity class.
   *
   * @param entityClass the class of the entity to map
   */
  public EntityTableMapper(Class<T> entityClass) throws Exception {
    this.entityClass = entityClass;

    DatabaseTable tableAnnotation = entityClass.getAnnotation(DatabaseTable.class);
    if (tableAnnotation == null) {
      throw new Exception(STR."The class \{entityClass.getSimpleName()} in package \{entityClass.getPackage()} is not annotated as an entity.");
    }

    defineTableMapping(entityClass, tableAnnotation);
    if (primaryKeyColumn.isEmpty()) {
      throw new Exception("The table must have an ID column (primary key).");
    }
  }

  /**
   * Checks if the given PostgresSQL column name is mapped to a Java field.
   *
   * @param columnName the name of the PostgresSQL column
   * @return true if the column is mapped, false otherwise
   */
  public boolean containsColumn(String columnName) {
    return columnMapping.containsKey(columnName);
  }

  /**
   * Retrieves the Java field name corresponding to the given PostgreSQL column name.
   *
   * @param columnName the name of the PostgresSQL column
   * @return the Java field name, or null if not mapped
   */
  public String getJavaFieldFromPsqlColumn(String columnName) {
    return columnMapping.get(columnName);
  }

  private void defineTableMapping(Class<T> entityClass, DatabaseTable tableAnnotation) {
    parseSchema(tableAnnotation);
    parseTableName(entityClass, tableAnnotation);
    processFields(entityClass.getDeclaredFields());
  }

  private void parseSchema(DatabaseTable tableAnnotation) {
    String schema = tableAnnotation.schema();
    if (!Objects.equals(schema, "public")) {
      this.schema = schema;
    }
  }

  private void parseTableName(Class<T> entityClass, DatabaseTable tableAnnotation) {
    String customTableName = tableAnnotation.name().toLowerCase().trim();
    if (!customTableName.isEmpty()) {
      this.name = customTableName;
    } else {
      this.name = entityClass.getSimpleName().toLowerCase();
    }
  }

  private void processFields(Field[] fields) {
    for (Field field : fields) {
      DatabaseField column = field.getAnnotation(DatabaseField.class);
      if (column != null) {
        defineColumnMapping(field, column);
      }
    }
  }

  private void defineColumnMapping(Field field, DatabaseField columnAnnotation) {
    EntityColumnMapper definition = new EntityColumnMapper();
    String javaCol = field.getName();
    definition.setJavaColumnName(javaCol);

    String psqlCol = parseColumnName(field, columnAnnotation);
    definition.setPostgresColumnName(psqlCol);
    columnNames.add(psqlCol);

    columnMapping.put(psqlCol, javaCol);

    definition.setColumnAnnotation(columnAnnotation);
    definition.setJavaType(field.getType().getSimpleName());
    definition.setPostgresType(parsePsqlType(field, columnAnnotation));
    if (columnAnnotation.identity() && this.primaryKeyColumn == null) {
      this.primaryKeyColumn = definition;
    } else {
      otherColumns.add(definition);
    }
  }

  private String parseColumnName(Field field, DatabaseField column) {
    String customColumnName = column.name().trim();
    if (!customColumnName.isEmpty()) {
      return customColumnName;
    }
    return field.getName().toLowerCase();
  }

  private String parsePsqlType(Field field, DatabaseField column) {
    String type;

    String definedOnAnnotation = column.fieldType();
    if (!definedOnAnnotation.equals(FieldType.NONE)) {
      type = definedOnAnnotation;
    } else {
      String javaReturnType = field.getType().getSimpleName();
      type = JavaToDatabaseTypeMapper.get(javaReturnType);
    }

    return type;
  }
}
