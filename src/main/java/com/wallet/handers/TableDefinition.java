package com.wallet.handers;

import lombok.*;
import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.type.ColumnType;
import com.wallet.annotations.Table;

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
  private ColumnDefinition id;
  @Getter
  private final List<ColumnDefinition> otherColumns = new ArrayList<>();
  @Getter
  private final Class<T> clazz;

  private final List<String> columnName = new ArrayList<>();
  private final HashMap<String, String> mapColPsqlToJava = new HashMap<>();

  public TableDefinition(Class<T> classy) throws Exception {
    this.clazz = classy;

    Table table = classy.getAnnotation(Table.class);
    if(table == null){
      String className = classy.getSimpleName();
      throw new Exception(STR."\{className} in: \{classy.getPackage()} is not an entity");
    }

    doDefinition(classy, table);
    if(id == null) throw new Exception("table should have an identity column (primary key)");
  }

  public List<String> mapColumns(){
    return columnName;
  }

  public boolean containPsqlColumn(String column){
    return mapColPsqlToJava.containsKey(column);
  }

  public String getJavaFieldFromPsqlColumn(String psqlColumn){
    return mapColPsqlToJava.get(psqlColumn);
  }

  private void doDefinition(Class<T> classy, Table table){
    parseSchema(table);
    parseTableName(classy, table);
    readAllField(classy.getDeclaredFields());
  }

  private void parseSchema(Table table){
    String schema = table.schema();
    if(!Objects.equals(schema, "public")){
      this.schema = schema;
    }
  }

  private void parseTableName(Class<T> classy, Table table){
    String customTableName = table.name().toLowerCase().trim();
    if(!customTableName.isEmpty()){
      this.name = customTableName;
    }else {
      this.name = classy.getSimpleName().toLowerCase();
    }
  }

  private void readAllField(Field[] fields){
    for (Field field : fields) {
      DatabaseField column = field.getAnnotation(DatabaseField.class);
      if(column != null){
        defineColumn(field, column);
      }
    }
  }

  private void defineColumn(Field field, DatabaseField columnAnnotation){
    ColumnDefinition definition = new ColumnDefinition();
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
    }else {
      otherColumns.add(definition);
    }
  }

  private String parseColumnName(Field field, DatabaseField column){
    String customColumnName = column.name().trim();
    if(!customColumnName.isEmpty()){
      return customColumnName;
    }
    return field.getName().toLowerCase();
  }

  private String parsePsqlType(Field field, DatabaseField column){
    String type;

    String definedOnAnnotation = column.columnType();
    if(!definedOnAnnotation.equals(ColumnType.NONE)){
      type = definedOnAnnotation;
    }else {
      String javaReturnType = field.getType().getSimpleName();
      type = TypeMapper.get(javaReturnType);
    }

    return type;
  }
}
