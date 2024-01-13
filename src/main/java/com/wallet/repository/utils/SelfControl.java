package com.wallet.repository.utils;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.Table;
import com.wallet.handers.TableDefinition;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelfControl<T> {
  private String COLUMN_CONDITION_DELETE_WHERE = "<column-condition-delete-where>";
  private String READABLE_COLUMN = "<readable-column>";

  private String COLUMN_TO_INSERT = "<column-to-insert>";
  protected String CSV_INSERT_VALUES = "<csv-insert-value>";

  protected String COLUMN_SET_UPDATABLE = "<column-set-updatable>";
  private String COLUMN_CONDITION_FIND_WHERE = "<column-condition-where>";
  protected String COLUMN_CONDITION_VALUE = "<column-condition-value>";

  private TableDefinition<T> definition;

  private String tableName;
  private String schema;

  protected String saveQuery;
  protected String findByIdentityQuery;
  protected String findAllQuery;
  protected String updateByColumnQuery;
  protected String deleteByIdentityQuery;
  protected String deleteAllQuery;

  private final List<String> mapReturnColumns;
  private final CrudMakerParams params;

  public SelfControl(CrudMakerParams params){
    this.params = params;
    doClassy(params);
    List<String> cleanInserts = mapCleanReturnColumn(
        params.getCreateColumnSet(),
        this.definition.mapColumns()
    );
    this.COLUMN_TO_INSERT = String.join(", ",cleanInserts);
    this.CSV_INSERT_VALUES = String.join(", ", Collections.nCopies(cleanInserts.size(), "?"));
    this.READABLE_COLUMN = checkAndCleanGivenColumns(
        params.getReadReturnColumns(),
        String.join(", ", this.definition.mapColumns())
    );
    String customFindBy = params.getReadIdentityColumn();
    if(customFindBy != null){
      this.COLUMN_CONDITION_FIND_WHERE = customFindBy;
    }else {
      this.COLUMN_CONDITION_FIND_WHERE = this
          .definition
          .getId()
          .getPostgresColumnName()
      ;
    }
    String customDeleteColumn = params.getDeleteByAColumn();
    if(customDeleteColumn != null){
      COLUMN_CONDITION_DELETE_WHERE = customDeleteColumn;
    }
    doSaveQuery();
    doFindByIdentityQuery();
    doFindAllQuery();
    doUpdateByColumnQuery();
    doDeleteByIdentityQuery();
    doDeleteAllQuery();
    this.mapReturnColumns = mapCleanReturnColumn(
        params.getReadReturnColumns(),
        this.definition.mapColumns()
    );
  }

  private List<String> mapCleanReturnColumn(String[] columns, List<String> defaultColumns){
    if(columns == null || columns.length == 0) {
      return defaultColumns;
    }
    List<String> cleanColumns = new ArrayList<>();
    for (String column : columns) {
      if( this.definition.containPsqlColumn(column) ){
        cleanColumns.add(column);
      }
    }
    return cleanColumns;
  }

  private Object getMethodGetterValue(Object value, String fieldName) throws Exception {
    return value.getClass().getMethod(
        STR."get\{Character.toUpperCase(fieldName.charAt(0))}\{fieldName.substring(1)}"
    ).invoke(value);
  }

  private Object getFieldValue(T value, String fieldName) throws Exception {
    Object returnedValue = getMethodGetterValue(value, fieldName);

    Field privateField = value.getClass().getDeclaredField(fieldName);
    DatabaseField fieldColumn = privateField.getAnnotation(DatabaseField.class);
    if(fieldColumn.references()){
      TableDefinition<?> getReferenceTable = new TableDefinition<>(returnedValue.getClass());
      String fieldRefName = getReferenceTable.getId().getJavaColumnName();
      return getMethodGetterValue(returnedValue, fieldRefName);
    }

    return returnedValue;
  }

  protected <Id> void wrapIdentityToStatement(Id id, PreparedStatement statement) throws Exception {
    statement.setObject(1, id);
  }

  protected void wrapObjectToStatement(T value, PreparedStatement statement) throws Exception {
    wrapObjectToStatement(value, statement, false);
  }

  protected void wrapObjectToStatement(T value, PreparedStatement statement, boolean update) throws Exception {
    List<String> cleanColumnInserts = mapCleanReturnColumn(
        update ? this.params.getUpdatableColumns() : this.params.getCreateColumnSet(),
        this.definition.mapColumns()
    );
    if(update){
      cleanColumnInserts.add(getUpdateColumnConditioner());
    }

    for (int i = 0; i < cleanColumnInserts.size(); i++) {
      String insertColumn = cleanColumnInserts.get(i);
      String javaFieldName = this.definition.getJavaFieldFromPsqlColumn(insertColumn);
      statement.setObject((i + 1),  getFieldValue(value, javaFieldName));
    }
  }


  private String fieldNameToMethodSetter(String fieldName){
    return STR."set\{Character.toUpperCase(fieldName.charAt(0))}\{fieldName.substring(1)}";
  }

  private void setValueToField(T instance, String fieldName, Field field, Object value) throws Exception {
    DatabaseField annotatedColumn = field.getAnnotation(DatabaseField.class);
    if(annotatedColumn == null) return;

    if(!annotatedColumn.references()){
      this.definition
          .getClazz()
          .getMethod(
              fieldNameToMethodSetter(fieldName),
              field.getType()
          )
          .invoke(instance, value);
      return;
    }
    Class<?> returnedClassType = field.getType();
    TableDefinition<?> tableDefinition = new TableDefinition<>(returnedClassType);
    Object refInstance = returnedClassType.getDeclaredConstructor().newInstance();
    String javaFieldName = tableDefinition.getId().getJavaColumnName();
    String setterMethod = fieldNameToMethodSetter(javaFieldName);
    Class<?> fieldType = returnedClassType.getDeclaredField(javaFieldName).getType();

    returnedClassType.getMethod(setterMethod, fieldType).invoke(refInstance, value);
    this.definition
        .getClazz()
        .getMethod(
            fieldNameToMethodSetter(fieldName),
            field.getType()
        )
        .invoke(instance, refInstance);
  }

  private String resultSetGetterName(Class<?> type) throws Exception {
    Table checkTable = type.getAnnotation(Table.class);
    if(checkTable == null){
      String name = type.getSimpleName();
      return STR."get\{Character.toUpperCase(name.charAt(0))}\{name.substring(1)}";
    }

    TableDefinition<?> tableDefinition = new TableDefinition<>(type);
    return STR."get\{tableDefinition.getId().getJavaType()}";
  }

  protected T mapResultSetToInstance(ResultSet result) throws Exception {
    T instance = this.definition.getClazz().getDeclaredConstructor().newInstance();
    for (String returnColumn : mapReturnColumns) {
      final String fieldName = this.definition.getJavaFieldFromPsqlColumn(returnColumn);
      final Field field = this
          .definition
          .getClazz()
          .getDeclaredField(fieldName);
      final Class<?> type = field.getType();
      final Object value = result
          .getClass()
          .getMethod(resultSetGetterName(type), String.class)
          .invoke(result, returnColumn);
      setValueToField(instance, fieldName, field, value);
    }
    return instance;
  }


  private String checkAndCleanGivenColumns(String[] columns){
    List<String> cleanColumns = new ArrayList<>();
    for (String column : columns) {
      if( this.definition.containPsqlColumn(column) ){
        cleanColumns.add(column);
      }
    }
    return String.join(", ", cleanColumns);
  }

  private String checkAndCleanGivenColumns(String[] column, String defaultValue){
    if(column == null || column.length == 0) return defaultValue;
    return checkAndCleanGivenColumns(column);
  }

  private void doClassy(CrudMakerParams params){
    Class<?> capture = params.getEntityClass();
    Class<T> classy = (Class<T>) capture;
    try {
      this.definition = new TableDefinition<>(classy);
      this.tableName = definition.getName();
      this.schema = definition.getSchema();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getSchemaTable(){
    return STR."\{schema}.\"\{tableName}\"";
  }

  private void doSaveQuery(){
    this.saveQuery = STR."insert into \{getSchemaTable()} (\{COLUMN_TO_INSERT}) values (\{CSV_INSERT_VALUES}) returning \{READABLE_COLUMN}";
  }

  private void doFindByIdentityQuery(){
    this.findByIdentityQuery = STR."select \{READABLE_COLUMN} from \{getSchemaTable()} where \{COLUMN_CONDITION_FIND_WHERE} = ?";
  }

  private void doFindAllQuery(){
    this.findAllQuery = STR."select \{READABLE_COLUMN} from \{getSchemaTable()}";
  }

  private String getUpdateColumnConditioner(){
    String customColumnConditioner = this.params.getUpdateByColumn();
    if(customColumnConditioner == null){
      customColumnConditioner = this.definition.getId().getPostgresColumnName();
    }
    return customColumnConditioner;
  }

  private void doUpdateByColumnQuery(){
    List<String> updates = mapCleanReturnColumn(
        this.params.getUpdatableColumns(),
        this.definition.mapColumns()
    );

    this.COLUMN_SET_UPDATABLE = String.join(
        ", ",
        updates
            .stream()
            .map(s -> STR."\{s} = ?")
            .toList()
    );

    this.updateByColumnQuery = STR."update \{getSchemaTable()} set \{COLUMN_SET_UPDATABLE} where \{getUpdateColumnConditioner()} = ?  returning \{READABLE_COLUMN}";
  }

  private void doDeleteByIdentityQuery(){
    this.deleteByIdentityQuery = STR."delete from \{getSchemaTable()} where \{COLUMN_CONDITION_DELETE_WHERE} = \{COLUMN_CONDITION_VALUE} returning \{READABLE_COLUMN}";
  }

  private void doDeleteAllQuery(){
    this.deleteAllQuery = STR."delete from \{getSchemaTable()} returning \{READABLE_COLUMN}";
  }


  protected Object guessIdentity(T value){
    try {
      String customColumnName = this.params.getUpdateByColumn();
      if (customColumnName == null) {
        customColumnName = this.definition.getId().getPostgresColumnName();
      }
      String javaField = this.definition.getJavaFieldFromPsqlColumn(customColumnName);
      return getMethodGetterValue(value, javaField);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
