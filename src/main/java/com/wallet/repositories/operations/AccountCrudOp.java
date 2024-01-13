package com.wallet.repositories.operations;

import com.wallet.entities.Account;
import com.wallet.repositories.implementations.CrudMakerParams;
import com.wallet.repositories.implementations.CrudOperationsImpl;

public class AccountCrudOp extends CrudOperationsImpl<Account> {

  public AccountCrudOp() {
    super(
        CrudMakerParams
            .builder()
            .entityClass(Account.class)
            .createColumnSet(new String[]{"account_name", "account_type", "currency_id"})
            .updateByColumn("account_id")
            .updatableColumns(new String[]{"account_name"})
            .deleteByAColumn("account_id")
            .build()
    );
  }
}