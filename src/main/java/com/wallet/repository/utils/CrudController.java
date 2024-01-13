package com.wallet.repository.utils;

import com.wallet.persistence.handler.EntityTableInfo;

import java.util.List;

public class CrudController<T> {
  private static final String DELETE_CONDITION = "<delete-condition>";
  private static final String DISPLAY_COLUMN = "<display-column>";
  private static final String INSERT_COLUMN = "<insert-columns>";
  private static final String INSERT_VALUES = "<insert-values>";
  private static final String UPDATE_COLUMNS = "<update-columns>";
  private static final String CONDITION_COLUMN = "<condition-column>";
  private static final String CONDITION_VALUE = "<condition-value>";

  private final EntityTableInfo<T> entityTableInfo;
  private final String tableName;
  private final String schema;

  private final CrudMaker parameters;
  private final List<String> returnColumns;
}
