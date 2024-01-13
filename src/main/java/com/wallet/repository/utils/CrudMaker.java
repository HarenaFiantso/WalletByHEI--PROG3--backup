package com.wallet.repository.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CrudMaker {
  private Class<?> entityClass;
  private String[] createColumnSet;
  private String[] readReturnColumns;
  private String readIdentityColumn;
  private String[] updatableColumns;
  private String updateByColumn;
  private String deleteByAColumn;
}
