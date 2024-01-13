package com.wallet.repositories.implementations;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CrudMakerParams {
  private Class<?> entityClass;
  private String[] createColumnSet;
  private String[] readReturnColumns;
  private String readIdentityColumn;
  private String[] updatableColumns;
  private String updateByColumn;
  private String deleteByAColumn;
}
