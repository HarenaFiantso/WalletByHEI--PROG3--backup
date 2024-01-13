package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.Table;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "transfer_history")
public class TransferHistory {
  @DatabaseField(name = "transfer_history_id")
  private Long transferHistoryId;

  @DatabaseField(name = "transfer_date")
  private Timestamp transferDate;

  @DatabaseField(name = "debit_transaction_id")
  private int debitTransactionId;

  @DatabaseField(name = "credit_transaction_id")
  private int creditTransactionId;

  @DatabaseField
  private Double amount;
}
