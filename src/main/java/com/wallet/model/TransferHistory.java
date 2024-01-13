package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;

import java.sql.Timestamp;

import com.wallet.annotations.type.ColumnType;
import com.wallet.annotations.type.GenerativeValue;
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
@DatabaseTable(name = "transfer_history")
public class TransferHistory {

  @DatabaseField(name = "transfer_history_id", identity = true, generative = GenerativeValue.SEQUENCE)
  private Long transferHistoryId;

  @DatabaseField(name = "transfer_date", columnType = ColumnType.TIMESTAMP, required = true)
  private Timestamp transferDate;

  @DatabaseField(name = "debit_transaction_id", columnType = ColumnType.INT, references = true, required = true)
  private int debitTransactionId;

  @DatabaseField(name = "credit_transaction_id", columnType = ColumnType.INT, references = true, required = true)
  private int creditTransactionId;

  private Double amount;
}
