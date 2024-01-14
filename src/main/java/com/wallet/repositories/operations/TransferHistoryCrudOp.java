package com.wallet.repositories.operations;

import com.wallet.entities.Currency;
import com.wallet.entities.TransferHistory;
import com.wallet.repositories.implementations.CrudOperationsParams;
import com.wallet.repositories.implementations.CrudOperationsImpl;

public class TransferHistoryCrudOp extends CrudOperationsImpl<TransferHistory> {
  private static final String TRANSFER_HISTORY_ID = "transfer_history_id";
  private static final String TRANSFER_DATE = "transfer_date";
  private static final String DEBIT_TRANSACTION_ID = "debit_transaction_id";
  private static final String CREDIT_TRANSACTION_ID = "credit_transaction_id";

  public TransferHistoryCrudOp() {
    super(
        CrudOperationsParams
            .builder()
            .entityClass(Currency.class)
            .createColumnSet(new String[]{TRANSFER_DATE, DEBIT_TRANSACTION_ID, CREDIT_TRANSACTION_ID})
            .updateByColumn(TRANSFER_HISTORY_ID)
            .deleteByAColumn(TRANSFER_HISTORY_ID)
            .build()
    );
  }
}
