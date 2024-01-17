package com.wallet.handlers;

import lombok.*;
import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.FieldType;
import com.wallet.annotations.DatabaseTable;

import java.lang.reflect.Field;
import java.util.*;

@ToString
@EqualsAndHashCode
public class TableDefinition<T> {

  @Getter
  private String name;
  @Getter
  private String schema = "public";
  @Getter
  private EntityColumnMapping id;
  @Getter
  private final List<EntityColumnMapping> otherColumns = new ArrayList<>();
  @Getter
  private final Class<T> clazz;

  private final List<String> columnName = new ArrayList<>();
  private final HashMap<String, String> mapColPsqlToJava = new HashMap<>();

  public TableDefinition(Class<T> classy) throws Exception {
    this.clazz = classy;

    DatabaseTable table = classy.getAnnotation(DatabaseTable.class);
    if (table == null) {
      String className = classy.getSimpleName();
      throw new Exception(STR."\{className} in: \{classy.getPackage()} is not an entity");
    }

    doDefinition(classy, table);
    if (id == null) {
      throw new Exception("Table should have an ID column (primary key)");
    }
  }

  public List<String> mapColumns() {
    return columnName;
  }

  public boolean containPsqlColumn(String column) {
    return mapColPsqlToJava.containsKey(column);
  }

  public String getJavaFieldFromPsqlColumn(String psqlColumn) {
    return mapColPsqlToJava.get(psqlColumn);
  }

  private void doDefinition(Class<T> classy, DatabaseTable table) {
    parseSchema(table);
    parseTableName(classy, table);
    readAllField(classy.getDeclaredFields());
  }

  private void parseSchema(DatabaseTable table) {
    String schema = table.schema();
    if (!Objects.equals(schema, "public")) {
      this.schema = schema;
    }
  }

  private void parseTableName(Class<T> classy, DatabaseTable table) {
    String customTableName = table.name().toLowerCase().trim();
    if (!customTableName.isEmpty()) {
      this.name = customTableName;
    } else {
      this.name = classy.getSimpleName().toLowerCase();
    }
  }

  private void readAllField(Field[] fields) {
    for (Field field : fields) {
      DatabaseField column = field.getAnnotation(DatabaseField.class);
      if (column != null) {
        defineColumn(field, column);
      }
    }
  }

  private void defineColumn(Field field, DatabaseField columnAnnotation) {
    EntityColumnMapping definition = new EntityColumnMapping();
    String javaCol = field.getName();
    definition.setJavaColumnName(javaCol);

    String psqlCol = parseColumnName(field, columnAnnotation);
    definition.setPostgresColumnName(psqlCol);
    columnName.add(psqlCol);

    mapColPsqlToJava.put(psqlCol, javaCol);

    definition.setColumnAnnotation(columnAnnotation);
    definition.setJavaType(field.getType().getSimpleName());
    definition.setPostgresType(parsePsqlType(field, columnAnnotation));
    if (columnAnnotation.identity() && this.id == null) {
      this.id = definition;
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
      type = TypeMapper.get(javaReturnType);
    }

    return type;
  }
}
