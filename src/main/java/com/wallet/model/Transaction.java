package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
import com.wallet.annotations.type.ColumnType;
import com.wallet.annotations.type.GenerativeValue;
import com.wallet.model.type.TransactionType;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents a user account in the system.
 *
 * @see DatabaseTable: Used to specify the database table associated with the current entity
 * @see DatabaseField: Used to specify the database field associated with the current table
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@DatabaseTable(name = "transaction")
public class Transaction {

  @DatabaseField(name = "transaction_id", identity = true, generative = GenerativeValue.SEQUENCE)
  private Long transactionId;

  @DatabaseField(name = "amount", columnType = ColumnType.DOUBLE, required = true)
  private Double amount;

  @DatabaseField(name = "label", columnType = ColumnType.VARCHAR, required = true)
  private String label;

  @DatabaseField(name = "transaction_date", columnType = ColumnType.TIMESTAMP)
  private Timestamp transactionDate;

  @DatabaseField(name = "transaction_type", columnType = ColumnType.TEXT, required = true)
  private TransactionType transactionType;

  @DatabaseField(name = "account_id", columnType = ColumnType.INT, references = true, required = true)
  private int accountId;

  @DatabaseField(name = "category_id", columnType = ColumnType.INT, references = true, required = true)
  private int categoryId;
}
