package com.wallet.service;

import com.wallet.model.Account;
import com.wallet.model.Transaction;
import com.wallet.model.type.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class TransactionService {

  /* TODO: Create a function that allows you to make a transaction in an account (debit or credit)
   *   - Should write a test for this method */
  public Account performTransaction(Account account, String transactionType, Double amount)
      throws IllegalAccessException {
    LocalDateTime transactionDate = LocalDateTime.now();

    if (Objects.equals(transactionType, "DEBIT")) {
      debitAccount(account, amount);
    } else if (Objects.equals(transactionType, "CREDIT")) {
      creditAccount(account, amount);
    }

    account.setLastTransactionDate(transactionDate);
    Double updatedBalance = calculateUpdatedBalance(account);
    account.setBalance(updatedBalance);

    return account;
  }

  private Double calculateUpdatedBalance(Account account) {
    List<Transaction> transactions = account.getTransactionList();
    Double updatedBalance = 0.0;

    for (Transaction transaction : transactions) {
      if (transaction.getTransactionType() == TransactionType.DEBIT) {
        updatedBalance -= transaction.getAmount();
      } else if (transaction.getTransactionType() == TransactionType.CREDIT) {
        updatedBalance += transaction.getAmount();
      }
    }

    return updatedBalance;
  }

  private void creditAccount(Account account, Double amount) {
    Double currentBalance = account.getBalance();
    Double updatedBalance = currentBalance + amount;
    account.setBalance(updatedBalance);
  }

  private void debitAccount(Account account, Double amount) throws IllegalAccessException {
    Double currentBalance = account.getBalance();

    if (currentBalance >= amount) {
      Double updatedBalance = currentBalance - amount;
      account.setBalance(updatedBalance);
    } else {
      throw new IllegalAccessException("Insufficient funds for this operation");
    }
  }
}
