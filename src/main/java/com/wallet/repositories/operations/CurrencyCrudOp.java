package com.wallet.repositories.operations;

import com.wallet.entities.Currency;
import com.wallet.repositories.implementations.CrudMakerParams;
import com.wallet.repositories.implementations.CrudOperationsImpl;

public class CurrencyCrudOp extends CrudOperationsImpl<Currency> {
  private static final String CURRENCY_ID = "currency_id";
  private static final String CURRENCY_NAME = "currency_name";
  private static final String CURRENCY_CODE = "currency_code";

  public CurrencyCrudOp() {
    super(
        CrudMakerParams
            .builder()
            .entityClass(Currency.class)
            .createColumnSet(new String[]{CURRENCY_NAME, CURRENCY_CODE})
            .updateByColumn(CURRENCY_ID)
            .deleteByAColumn(CURRENCY_ID)
            .build()
    );
  }
}
