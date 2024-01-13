package com.wallet.repositories.operations;

import com.wallet.entities.Account;
import com.wallet.repositories.implementations.CrudMakerParams;
import com.wallet.repositories.implementations.CrudOperationsImpl;

public class AccountCrudOp extends CrudOperationsImpl<Account> {
  private static final String ACCOUNT_NAME = "account_name";
  private static final String ACCOUNT_TYPE = "account_type";
  private static final String CURRENCY_ID = "currency_id";
  private static final String ACCOUNT_ID = "account_id";

  public AccountCrudOp() {
    super(
        CrudMakerParams
            .builder()
            .entityClass(Account.class)
            .createColumnSet(new String[]{ACCOUNT_NAME, ACCOUNT_TYPE, CURRENCY_ID})
            .updateByColumn(ACCOUNT_ID)
            .deleteByAColumn(ACCOUNT_ID)
            .build()
    );
  }
}