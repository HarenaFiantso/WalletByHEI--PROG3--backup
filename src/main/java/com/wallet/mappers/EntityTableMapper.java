package com.wallet.mappers;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
import com.wallet.annotations.FieldType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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
  private final String schema = "public";
  @Getter
  private final List<EntityColumnMapper> otherColumns = new ArrayList<>();
  @Getter
  private final Class<T> entityClass;
  @Getter
  private final List<String> columnNames = new ArrayList<>();
  private final HashMap<String, String> columnMapping = new HashMap<>();

  @Getter
  private String name;
  @Getter
  private EntityColumnMapper primaryKeyColumn;

  /**
   * Constructs an EntityTableMapper for the given entity class.
   *
   * @param entityClass the class of the entity to map
   */
  public EntityTableMapper(Class<T> entityClass) throws Exception {
    this.entityClass = entityClass;
    initializeTableMapping();
  }

  private void initializeTableMapping() throws Exception {
    DatabaseTable tableAnnotation = entityClass.getAnnotation(DatabaseTable.class);
    if (tableAnnotation == null) {
      throw new Exception(STR."The class \{entityClass.getSimpleName()} in package \{entityClass.getPackage()} is not annotated as an entity.");
    }

    parseSchema(tableAnnotation);
    parseTableName(tableAnnotation);
    processFields(entityClass.getDeclaredFields());

    if (primaryKeyColumn == null) {
      throw new Exception("The table must have an ID column (primary key).");
    }
  }

  private void parseSchema(DatabaseTable tableAnnotation) {
    String schema = tableAnnotation.schema();
    if (!Objects.equals(schema, "public")) {
      this.name = schema;
    }
  }

  private void parseTableName(DatabaseTable tableAnnotation) {
    String customTableName = tableAnnotation.name().toLowerCase().trim();
    this.name = customTableName.isEmpty() ? entityClass.getSimpleName().toLowerCase() : customTableName;
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
    return customColumnName.isEmpty() ? field.getName().toLowerCase() : customColumnName;
  }

  private String parsePsqlType(Field field, DatabaseField column) {
    String definedOnAnnotation = column.fieldType();
    return !definedOnAnnotation.equals(FieldType.NONE) ? definedOnAnnotation : JavaToDatabaseTypeMapper.get(field.getType().getSimpleName());
  }
}
