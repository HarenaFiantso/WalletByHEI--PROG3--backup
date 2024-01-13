package com.wallet.repositories.implementations;

import com.wallet.handers.TableDefinition;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Repository<T> {

  private TableDefinition<T> tableDefinition;
  private String tableName;
  private String schema;

  private String saveQuery;
  private String findByIdQuery;
  private String findAllQuery;
  private String updateQuery;
  private String deleteQuery;

  private final List<String> returnColumns;
  private final CrudMakerParams params;

  public Repository(CrudMakerParams params) throws Exception {
    this.params = params;
    initialize(params);
    this.returnColumns =
        mapCleanReturnColumn(params.getReadReturnColumns(), tableDefinition.mapColumns());
    setupQueries();
  }

  private void initialize(CrudMakerParams params) throws Exception {
    Class<T> entityClass = (Class<T>) params.getEntityClass();
    this.tableDefinition = new TableDefinition<>(entityClass);
    this.tableName = tableDefinition.getName();
    this.schema = tableDefinition.getSchema();
  }

  private List<String> mapCleanReturnColumn(String[] columns, List<String> defaultColumns) {
    if (columns == null || columns.length == 0) {
      return defaultColumns;
    }
    return Arrays.stream(columns)
        .filter(tableDefinition::containPsqlColumn)
        .collect(Collectors.toList());
  }

  private void setupQueries() {
    this.saveQuery =
        String.format(
            "INSERT INTO %s (%s) VALUES (%s) RETURNING %s",
            getSchemaTable(),
            String.join(", ", params.getCreateColumnSet()),
            String.join(", ", Collections.nCopies(params.getCreateColumnSet().length, "?")),
            String.join(", ", returnColumns));

    this.findByIdQuery =
        String.format(
            "SELECT %s FROM %s WHERE %s = ?",
            String.join(", ", returnColumns), getSchemaTable(), getFindCondition());

    this.findAllQuery =
        String.format("SELECT %s FROM %s", String.join(", ", returnColumns), getSchemaTable());

    this.updateQuery =
        String.format(
            "UPDATE %s SET %s WHERE %s = ? RETURNING %s",
            getSchemaTable(),
            getUpdateColumns(),
            getUpdateCondition(),
            String.join(", ", returnColumns));

    this.deleteQuery =
        String.format(
            "DELETE FROM %s WHERE %s = ? RETURNING %s",
            getSchemaTable(), params.getDeleteByAColumn(), String.join(", ", returnColumns));
  }

  private String getSchemaTable() {
    return String.format("%s.\"%s\"", schema, tableName);
  }

  private String getUpdateColumns() {
    return mapCleanReturnColumn(params.getUpdatableColumns(), tableDefinition.mapColumns()).stream()
        .map(column -> String.format("%s = ?", column))
        .collect(Collectors.joining(", "));
  }

  private String getUpdateCondition() {
    return params.getUpdateByColumn() != null
        ? params.getUpdateByColumn()
        : tableDefinition.getId().getPostgresColumnName();
  }

  private String getFindCondition() {
    return params.getReadIdentityColumn() != null
        ? params.getReadIdentityColumn()
        : tableDefinition.getId().getPostgresColumnName();
  }
}
