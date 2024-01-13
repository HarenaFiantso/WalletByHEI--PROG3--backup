package com.wallet.repositories.implementations;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
import com.wallet.handers.TableDefinition;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Repository<T> {
  
  private String COLUMN_CONDITION_DELETE_WHERE = "<column-condition-delete-where>";
  private String READABLE_COLUMN = "<readable-column>";

  protected String CSV_INSERT_VALUES = "<csv-insert-value>";
  private String COLUMN_TO_INSERT = "<column-to-insert>";

  protected String COLUMN_SET_UPDATABLE = "<column-set-updatable>";
  private String COLUMN_CONDITION_FIND_WHERE = "<column-condition-where>";

  private TableDefinition<T> tableDefinition;

  private String tableName;
  private String schema;

  protected String SAVE_QUERY;
  protected String FIND_BY_ID_QUERY;
  protected String FIND_ALL_QUERY;
  protected String UPDATE_QUERY;
  protected String DELETE_QUERY;

  private final List<String> mappedReturnColumns;
  private final CrudMakerParams crudParams;

  public Repository(CrudMakerParams crudParams) {
    this.crudParams = crudParams;
    initializeClass(crudParams);
    initializeQueries();
    this.mappedReturnColumns = mapCleanReturnColumn(crudParams.getReadReturnColumns(), this.tableDefinition.mapColumns());
  }

  private void initializeQueries() {
    List<String> cleanInserts = mapCleanReturnColumn(crudParams.getCreateColumnSet(), this.tableDefinition.mapColumns());
    this.COLUMN_TO_INSERT = String.join(", ", cleanInserts);
    this.CSV_INSERT_VALUES = String.join(", ", Collections.nCopies(cleanInserts.size(), "?"));
    this.READABLE_COLUMN = checkAndCleanGivenColumns(crudParams.getReadReturnColumns(), String.join(", ", this.tableDefinition.mapColumns()));

    String customFindBy = crudParams.getReadIdentityColumn();
    this.COLUMN_CONDITION_FIND_WHERE = (customFindBy != null) ? customFindBy : this.tableDefinition.getId().getPostgresColumnName();

    String customDeleteColumn = crudParams.getDeleteByAColumn();
    COLUMN_CONDITION_DELETE_WHERE = (customDeleteColumn != null) ? customDeleteColumn : COLUMN_CONDITION_DELETE_WHERE;

    doSaveQuery();
    doFindByIdQuery();
    doFindAllQuery();
    doUpdateQuery();
    doDeleteQuery();
  }

  private List<String> mapCleanReturnColumn(String[] columns, List<String> defaultColumns) {
    if (columns == null || columns.length == 0) {
      return defaultColumns;
    }
    List<String> cleanColumns = new ArrayList<>();
    for (String column : columns) {
      if (this.tableDefinition.containPsqlColumn(column)) {
        cleanColumns.add(column);
      }
    }
    return cleanColumns;
  }

  private Object getMethodGetterValue(Object value, String fieldName) throws Exception {
    return value.getClass()
        .getMethod(STR."get\{Character.toUpperCase(fieldName.charAt(0))}\{fieldName.substring(1)}").invoke(value);
  }

  private Object getFieldValue(T value, String fieldName) throws Exception {
    Object returnedValue = getMethodGetterValue(value, fieldName);

    Field privateField = value.getClass().getDeclaredField(fieldName);
    DatabaseField fieldColumn = privateField.getAnnotation(DatabaseField.class);
    if (fieldColumn.references()) {
      TableDefinition<?> getReferenceTable = new TableDefinition<>(returnedValue.getClass());
      String fieldRefName = getReferenceTable.getId().getJavaColumnName();
      return getMethodGetterValue(returnedValue, fieldRefName);
    }

    return returnedValue;
  }

  protected void wrapObjectToStatement(T value, PreparedStatement statement) throws Exception {
    wrapObjectToStatement(value, statement, false);
  }

  protected void wrapObjectToStatement(T value, PreparedStatement statement, boolean update) throws Exception {
    List<String> cleanColumnInserts = mapCleanReturnColumn(update ? this.crudParams.getUpdatableColumns() : this.crudParams.getCreateColumnSet(), this.tableDefinition.mapColumns());
    if (update) {
      cleanColumnInserts.add(getUpdateColumnConditioner());
    }

    for (int i = 0; i < cleanColumnInserts.size(); i++) {
      String insertColumn = cleanColumnInserts.get(i);
      String javaFieldName = this.tableDefinition.getJavaFieldFromPsqlColumn(insertColumn);
      statement.setObject((i + 1), getFieldValue(value, javaFieldName));
    }
  }

  private String fieldNameToMethodSetter(String fieldName) {
    return STR."SET\{Character.toUpperCase(fieldName.charAt(0))}\{fieldName.substring(1)}";
  }

  private void setValueToField(T instance, String fieldName, Field field, Object value) throws Exception {
    DatabaseField annotatedColumn = field.getAnnotation(DatabaseField.class);
    if (annotatedColumn == null) return;

    if (!annotatedColumn.references()) {
      this.tableDefinition.getClazz().getMethod(fieldNameToMethodSetter(fieldName), field.getType()).invoke(instance, value);
      return;
    }
    Class<?> returnedClassType = field.getType();
    TableDefinition<?> tableDefinition = new TableDefinition<>(returnedClassType);
    Object refInstance = returnedClassType.getDeclaredConstructor().newInstance();
    String javaFieldName = tableDefinition.getId().getJavaColumnName();
    String setterMethod = fieldNameToMethodSetter(javaFieldName);
    Class<?> fieldType = returnedClassType.getDeclaredField(javaFieldName).getType();

    returnedClassType.getMethod(setterMethod, fieldType).invoke(refInstance, value);
    this.tableDefinition.getClazz().getMethod(fieldNameToMethodSetter(fieldName), field.getType()).invoke(instance, refInstance);
  }

  private String resultSetGetterName(Class<?> type) throws Exception {
    DatabaseTable checkTable = type.getAnnotation(DatabaseTable.class);
    if (checkTable == null) {
      String name = type.getSimpleName();
      return STR."get\{Character.toUpperCase(name.charAt(0))}\{name.substring(1)}";
    }

    TableDefinition<?> tableDefinition = new TableDefinition<>(type);
    return STR."get\{tableDefinition.getId().getJavaType()}";
  }

  protected T mapResultSetToInstance(ResultSet result) throws Exception {
    T instance = this.tableDefinition.getClazz().getDeclaredConstructor().newInstance();
    for (String returnColumn : mappedReturnColumns) {
      final String fieldName = this.tableDefinition.getJavaFieldFromPsqlColumn(returnColumn);
      final Field field = this.tableDefinition.getClazz().getDeclaredField(fieldName);
      final Class<?> type = field.getType();
      final Object value = result.getClass().getMethod(resultSetGetterName(type), String.class).invoke(result, returnColumn);
      setValueToField(instance, fieldName, field, value);
    }
    return instance;
  }


  private String checkAndCleanGivenColumns(String[] columns) {
    List<String> cleanColumns = new ArrayList<>();
    for (String column : columns) {
      if (this.tableDefinition.containPsqlColumn(column)) {
        cleanColumns.add(column);
      }
    }
    return String.join(", ", cleanColumns);
  }

  private String checkAndCleanGivenColumns(String[] column, String defaultValue) {
    if (column == null || column.length == 0) return defaultValue;
    return checkAndCleanGivenColumns(column);
  }

  private void initializeClass(CrudMakerParams params) {
    Class<?> capture = params.getEntityClass();
    Class<T> classy = (Class<T>) capture;
    try {
      this.tableDefinition = new TableDefinition<>(classy);
      this.tableName = tableDefinition.getName();
      this.schema = tableDefinition.getSchema();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getSchemaTable() {
    return STR."\{schema}.\"\{tableName}\"";
  }

  private void doSaveQuery() {
    this.SAVE_QUERY = String.format("INSERT INTO %s (%s) VALUES (%s) RETURNING %s",
        getSchemaTable(), COLUMN_TO_INSERT, CSV_INSERT_VALUES, READABLE_COLUMN);
  }

  private void doFindByIdQuery() {
    this.FIND_BY_ID_QUERY = String.format("SELECT %s FROM %s WHERE %s = ?", READABLE_COLUMN, getSchemaTable(), COLUMN_CONDITION_FIND_WHERE);
  }

  private void doFindAllQuery() {
    this.FIND_ALL_QUERY = String.format("SELECT %s FROM %s", READABLE_COLUMN, getSchemaTable());
  }

  private void doUpdateQuery() {
    List<String> updates = mapCleanReturnColumn(crudParams.getUpdatableColumns(), this.tableDefinition.mapColumns());
    this.COLUMN_SET_UPDATABLE = String.join(", ", updates.stream().map(s -> STR."\{s} = ?").toList());
    this.UPDATE_QUERY = String.format("UPDATE %s SET %s WHERE %s = ? RETURNING %s",
        getSchemaTable(), COLUMN_SET_UPDATABLE, getUpdateColumnConditioner(), READABLE_COLUMN);
  }

  private void doDeleteQuery() {
    this.DELETE_QUERY = String.format("DELETE FROM %s WHERE %s = ? RETURNING %s",
        getSchemaTable(), COLUMN_CONDITION_DELETE_WHERE, READABLE_COLUMN);
  }

  private String getUpdateColumnConditioner() {
    String customColumnConditioner = this.crudParams.getUpdateByColumn();
    if (customColumnConditioner == null) {
      customColumnConditioner = this.tableDefinition.getId().getPostgresColumnName();
    }
    return customColumnConditioner;
  }

}
