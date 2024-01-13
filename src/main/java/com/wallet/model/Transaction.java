package com.wallet.model;

import com.wallet.annotation.DatabaseField;
import com.wallet.annotation.Table;
import com.wallet.model.type.TransactionType;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "transaction")
public class Transaction {
  @DatabaseField(name = "transaction_id")
  private Long transactionId;

  @DatabaseField(name = "amount")
  private Double amount;

  @DatabaseField(name = "label")
  private String label;

  @DatabaseField(name = "transaction_date")
  private Timestamp transactionDate;

  @DatabaseField(name = "transaction_type")
  private TransactionType transactionType;

  @DatabaseField(name = "account_id")
  private int accountId;

  @DatabaseField(name = "category_id")
  private int categoryId;
}
