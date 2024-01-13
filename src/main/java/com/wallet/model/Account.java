package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
import com.wallet.annotations.type.ColumnType;
import com.wallet.annotations.type.GenerativeValue;
import com.wallet.model.type.AccountType;

import java.time.LocalDateTime;
import java.util.List;

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
@DatabaseTable(name = "account")
public class Account {

  @DatabaseField(name = "account_id", identity = true, generative = GenerativeValue.SEQUENCE)
  private Long accountId;

  @DatabaseField(name = "account_name", columnType = ColumnType.VARCHAR, required = true)
  private String accountName;

  @DatabaseField(name = "account_type", columnType = ColumnType.TEXT, required = true)
  private AccountType accountType;

  @DatabaseField(name = "currency_id", columnType = ColumnType.INT, references = true, required = true)
  private int currencyId;

  private Double balance;
  private LocalDateTime lastTransactionDate;
  private List<Transaction> transactionList;
}
