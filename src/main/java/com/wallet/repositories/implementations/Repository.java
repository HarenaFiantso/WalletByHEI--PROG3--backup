package com.wallet.repositories.implementations;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
import com.wallet.handlers.EntityTableMapper;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic repository providing CRUD operations for a given entity type.
 *
 * @param <T> the type of the entity this repository manages
 */
public class Repository<T> {

  private EntityTableMapper<T> tableDefinition;

  private String tableName;
  private String schema;

  protected String saveQuery;
  protected String findByIdQuery;
  protected String findAllQuery;
  protected String updateQuery;
  protected String deleteQuery;

  private final List<String> mappedReturnColumns;
  private final CrudOperationsParams crudParams;

  /**
   * Constructs a new Repository with the specified CRUD operation parameters.
   *
   * @param crudParams the parameters defining CRUD operations for this repository
   */
  public Repository(CrudOperationsParams crudParams) {
    this.crudParams = crudParams;
    initializeClass(crudParams);
    initializeQueries();
    this.mappedReturnColumns = mapCleanReturnColumn(crudParams.getReadReturnColumns(), this.tableDefinition.mapColumns());
  }

  /**
   * Initializes SQL queries based on the table definition and CRUD parameters.
   */
  private void initializeQueries() {
    String columnsToInsert = joinColumns(crudParams.getCreateColumnSet());
    String insertPlaceholders = createPlaceholderString(crudParams.getCreateColumnSet().length);
    String readableColumns = getReadableColumns(crudParams.getReadReturnColumns());

    String columnConditionFindWhere = getDefaultOrCustom(crudParams.getReadIdentityColumn(), tableDefinition.getId().getPostgresColumnName());
    String columnConditionDeleteWhere = getDefaultOrCustom(crudParams.getDeleteByAColumn(), tableDefinition.getId().getPostgresColumnName());

    saveQuery = constructSaveQuery(columnsToInsert, insertPlaceholders, readableColumns);
    findByIdQuery = constructFindByIdQuery(readableColumns, columnConditionFindWhere);
    findAllQuery = constructFindAllQuery(readableColumns);
    updateQuery = constructUpdateQuery(crudParams.getUpdatableColumns(), columnConditionFindWhere, readableColumns);
    deleteQuery = constructDeleteQuery(columnConditionDeleteWhere, readableColumns);
  }

  // ... (Utility methods like joinColumns, createPlaceholderString, getReadableColumns, etc.)
  private String joinColumns(String[] columns) {
    List<String> cleanInserts = mapCleanReturnColumn(columns, tableDefinition.mapColumns());
    return String.join(", ", cleanInserts);
  }

  private String createPlaceholderString(int count) {
    return String.join(", ", Collections.nCopies(count, "?"));
  }

  private String getReadableColumns(String[] columns) {
    return checkAndCleanGivenColumns(columns, String.join(", ", tableDefinition.mapColumns()));
  }

  private String getDefaultOrCustom(String customValue, String defaultValue) {
    return (customValue != null) ? customValue : defaultValue;
  }

  // ... (Methods for constructing SQL queries like constructSaveQuery, constructFindByIdQuery, etc.)
  private String constructSaveQuery(String columnsToInsert, String insertPlaceholders, String readableColumns) {
    return String.format("INSERT INTO %s (%s) VALUES (%s) RETURNING %s",
        getSchemaTable(), columnsToInsert, insertPlaceholders, readableColumns);
  }

  private String constructFindByIdQuery(String readableColumns, String columnConditionFindWhere) {
    return String.format("SELECT %s FROM %s WHERE %s = ?", readableColumns, getSchemaTable(), columnConditionFindWhere);
  }

  private String constructFindAllQuery(String readableColumns) {
    return String.format("SELECT %s FROM %s", readableColumns, getSchemaTable());
  }

  private String constructUpdateQuery(String[] updatableColumns, String columnConditionFindWhere, String readableColumns) {
    String columnsSet = joinColumns(updatableColumns);
    return String.format("UPDATE %s SET %s WHERE %s = ? RETURNING %s",
        getSchemaTable(), columnsSet, columnConditionFindWhere, readableColumns);
  }

  private String constructDeleteQuery(String columnConditionDeleteWhere, String readableColumns) {
    return String.format("DELETE FROM %s WHERE %s = ? RETURNING %s",
        getSchemaTable(), columnConditionDeleteWhere, readableColumns);
  }

  private String getSchemaTable() {
    return String.format("%s.%s", schema, tableName);
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

  // ... (Methods for handling reflection operations like getMethodGetterValue, getFieldValue, etc.)
  private Object getMethodGetterValue(Object value, String fieldName) throws Exception {
    return value.getClass()
        .getMethod(STR."get\{Character.toUpperCase(fieldName.charAt(0))}\{fieldName.substring(1)}").invoke(value);
  }

  private Object getFieldValue(T value, String fieldName) throws Exception {
    Object returnedValue = getMethodGetterValue(value, fieldName);

    Field privateField = value.getClass().getDeclaredField(fieldName);
    DatabaseField fieldColumn = privateField.getAnnotation(DatabaseField.class);
    if (fieldColumn.references()) {
      EntityTableMapper<?> getReferenceTable = new EntityTableMapper<>(returnedValue.getClass());
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
    EntityTableMapper<?> tableDefinition = new EntityTableMapper<>(returnedClassType);
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

    EntityTableMapper<?> tableDefinition = new EntityTableMapper<>(type);
    Class<?> idType = tableDefinition.getId().getJavaType().getClass();
    String idTypeName = idType.getSimpleName();
    return STR."get\{Character.toUpperCase(idTypeName.charAt(0))}\{idTypeName.substring(1)}";
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

  private void initializeClass(CrudOperationsParams params) {
    Class<?> capture = params.getEntityClass();
    Class<T> classy = (Class<T>) capture;
    try {
      this.tableDefinition = new EntityTableMapper<>(classy);
      this.tableName = tableDefinition.getName();
      this.schema = tableDefinition.getSchema();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getUpdateColumnConditioner() {
    String customColumnConditioner = this.crudParams.getUpdateByColumn();
    if (customColumnConditioner == null) {
      customColumnConditioner = this.tableDefinition.getId().getPostgresColumnName();
    }
    return customColumnConditioner;
  }

}
