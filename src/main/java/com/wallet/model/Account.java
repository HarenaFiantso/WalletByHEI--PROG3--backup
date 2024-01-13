package com.wallet.model;

import com.wallet.annotation.DatabaseField;
import com.wallet.annotation.Table;
import com.wallet.model.type.AccountType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "account")
public class Account {
  @DatabaseField(name = "account_id")
  private Long accountId;

  @DatabaseField(name = "account_name")
  private String accountName;

  @DatabaseField(name = "account_type")
  private AccountType accountType;

  @DatabaseField(name = "currency_id")
  private int currencyId;

  @DatabaseField
  private Double balance;

  @DatabaseField
  private LocalDateTime lastTransactionDate;

  @DatabaseField
  private List<Transaction> transactionList;
}
