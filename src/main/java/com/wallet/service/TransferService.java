package com.wallet.service;

import com.wallet.entities.*;
import com.wallet.repositories.operations.CurrencyCrudOperations;
import com.wallet.repositories.operations.CurrencyValueCrudOperations;
import com.wallet.repositories.operations.TransactionCrudOperations;
import com.wallet.repositories.operations.TransferHistoryCrudOperations;
import com.wallet.types.TransactionType;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TransferService {
  private final CurrencyCrudOperations currencyCrudOperations = new CurrencyCrudOperations();
  private final CurrencyValueCrudOperations currencyValueCrudOperations =
      new CurrencyValueCrudOperations();
  TransactionCrudOperations transactionCrudOperations = new TransactionCrudOperations();
  private final TransferHistoryCrudOperations transferHistoryCrudOperations =
      new TransferHistoryCrudOperations();

  /* TODO: Create a function to transfer money between two accounts (fourth question a)
   *   - Should write a test for this method */
  public void transferMoneyFirstCase(Account debitAccount, Account creditAccount, Double amount)
      throws IllegalAccessException {
    if (debitAccount.getAccountId().equals(creditAccount.getAccountId())) {
      throw new IllegalAccessException("An account couldn't done transfer for himself");
    }

    Transaction debitTransaction = createTransaction(debitAccount, amount, "DEBIT");
    Transaction creditTransaction = createTransaction(creditAccount, amount, "CREDIT");

    /* Save transactions in the database */
    assert debitTransaction != null;
    assert creditTransaction != null;
    transactionCrudOperations.save(debitTransaction);
    transactionCrudOperations.save(creditTransaction);

    TransferHistory transferHistory = new TransferHistory();
    transferHistory.setTransferDate(Timestamp.valueOf(LocalDateTime.now()));
    transferHistory.setDebitTransactionId(Math.toIntExact(debitTransaction.getTransactionId()));
    transferHistory.setCreditTransactionId(Math.toIntExact(creditTransaction.getTransactionId()));

    /* Save transferHistory in the database */
    transferHistoryCrudOperations.save(transferHistory);
  }

  private Transaction createTransaction(Account account, Double amount, String transactionType) {
    Transaction transaction = new Transaction();
    transaction.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));
    transaction.setTransactionType(TransactionType.valueOf(transactionType));
    transaction.setAmount(amount);
    transaction.setAccountId(Math.toIntExact(account.getAccountId()));

    return transactionCrudOperations.save(transaction);
  }

  /* TODO: Create a function to transfer money between two accounts (fourth question b, in the case where accounts have
   *   - different currencies)
   *   - Should write a test for this method */
  public void transferMoneySecondCaseFirstPart(
      Account debitAccount, Account creditAccount, Double amount) {
    if (!isDifferentCurrency(debitAccount, creditAccount)) {
      throw new IllegalArgumentException("Accounts should be different (EUR -> MGA) ");
    }

    Currency sourceCurrency = getCurrencyById((long) debitAccount.getCurrencyId());
    Currency destinationCurrency = getCurrencyById((long) creditAccount.getCurrencyId());

    if (!canConvertCurrencies()) {
      throw new IllegalArgumentException("Couldn't do currency conversion (EUR -> MGA)");
    }

    Double exchangeRate = getExchangeRate(sourceCurrency, destinationCurrency);
    Double convertedAmount = amount * exchangeRate;
    Transaction debitTransaction = createTransaction(debitAccount, convertedAmount, "DEBIT");
    Transaction creditTransaction = createTransaction(creditAccount, convertedAmount, "CREDIT");

    /* Save transactions in the database */
    transactionCrudOperations.save(debitTransaction);
    transactionCrudOperations.save(creditTransaction);

    /* Save transfer history in the database */
    TransferHistory transferHistory = new TransferHistory();
    transferHistory.setTransferDate(Timestamp.valueOf(LocalDateTime.now()));
    transferHistory.setDebitTransactionId(Math.toIntExact(debitTransaction.getTransactionId()));
    transferHistory.setCreditTransactionId(Math.toIntExact(creditTransaction.getTransactionId()));

    transferHistoryCrudOperations.save(transferHistory);
  }

  private boolean isDifferentCurrency(Account account1, Account account2) {
    return account1.getCurrencyId() != account2.getCurrencyId();
  }

  private Boolean canConvertCurrencies() {
    /* TODO: If we want to allow conversion between EUR and MGA currencies, this method must be adjusted accordingly to
    return true when conversion is possible, takes sourceCurrencyId and destinationCurrencyId as parameters */
    return false;
  }

  private Currency getCurrencyById(Long currencyId) {
    return currencyCrudOperations.findById(currencyId);
  }

  private Double getExchangeRate(Currency sourceCurrency, Currency destinationCurrency) {
    CurrencyValue currencyValue =
        currencyValueCrudOperations.findByCurrencies(
            Math.toIntExact(sourceCurrency.getCurrencyId()),
            Math.toIntExact(destinationCurrency.getCurrencyId()));
    return currencyValue.getExchangeRate();
  }
}
