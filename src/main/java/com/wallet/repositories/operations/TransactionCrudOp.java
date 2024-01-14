package com.wallet.repositories.operations;

import com.wallet.entities.Transaction;
import com.wallet.repositories.implementations.CrudOperationsParams;
import com.wallet.repositories.implementations.CrudOperationsImpl;

public class TransactionCrudOp extends CrudOperationsImpl<Transaction> {
  private static final String TRANSACTION_ID = "transaction_id";
  private static final String AMOUNT = "amount";
  private static final String LABEL = "label";
  private static final String TRANSACTION_DATE = "transaction_date";
  private static final String TRANSACTION_TYPE = "transaction_type";
  private static final String ACCOUNT_ID = "account_id";

  public TransactionCrudOp() {
    super(
        CrudOperationsParams
            .builder()
            .entityClass(Transaction.class)
            .createColumnSet(new String[]{AMOUNT, LABEL, TRANSACTION_DATE, TRANSACTION_TYPE, ACCOUNT_ID})
            .updateByColumn(TRANSACTION_ID)
            .deleteByAColumn(TRANSACTION_ID)
            .build()
    );
  }
}
