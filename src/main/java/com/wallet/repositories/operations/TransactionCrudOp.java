package com.wallet.repositories.operations;

import com.wallet.config.ConnectionToDb;
import com.wallet.entities.Account;
import com.wallet.entities.Transaction;
import com.wallet.repositories.implementations.CrudOperationsParams;
import com.wallet.repositories.implementations.CrudOperationsImpl;
import com.wallet.types.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionCrudOp extends CrudOperationsImpl<Transaction> {
  private static final String TRANSACTION_ID = "transaction_id";
  private static final String AMOUNT = "amount";
  private static final String LABEL = "label";
  private static final String TRANSACTION_DATE = "transaction_date";
  private static final String TRANSACTION_TYPE = "transaction_type";
  private static final String ACCOUNT_ID = "account_id";
  private static final String CATEGORY_ID = "category_id";

  private static final String SELECT_TRANSFERS_BETWEEN_ACCOUNTS =
      "SELECT * FROM transaction WHERE account_id = ? OR account_id = ?";

  public TransactionCrudOp() {
    super(
        CrudOperationsParams
            .builder()
            .entityClass(Transaction.class)
            .createColumnSet(new String[]{AMOUNT, LABEL, TRANSACTION_DATE, TRANSACTION_TYPE, ACCOUNT_ID, CATEGORY_ID})
            .updateByColumn(TRANSACTION_ID)
            .deleteByAColumn(TRANSACTION_ID)
            .build()
    );
  }

  public List<Transaction> findTransfersBetweenAccounts(Account euroAccount, Account ariaryAccount) {
    List<Transaction> transactions = new ArrayList<>();
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      Long euroAccountId = euroAccount.getAccountId();
      Long ariaryAccountId = ariaryAccount.getAccountId();

      if (euroAccountId != null && ariaryAccountId != null) {
        connection = ConnectionToDb.getConnection();
        statement = connection.prepareStatement(SELECT_TRANSFERS_BETWEEN_ACCOUNTS);
        statement.setInt(1, Math.toIntExact(euroAccount.getAccountId()));
        statement.setInt(2, Math.toIntExact(ariaryAccount.getAccountId()));

        resultSet = statement.executeQuery();

        while (resultSet.next()) {
          Transaction transaction = new Transaction();
          transaction.setTransactionId(resultSet.getLong(TRANSACTION_ID));
          transaction.setTransactionDate(Timestamp.valueOf(resultSet.getTimestamp(TRANSACTION_DATE).toLocalDateTime()));
          transaction.setTransactionType(TransactionType.valueOf(resultSet.getString(TRANSACTION_TYPE)));
          transaction.setAmount(resultSet.getDouble(AMOUNT));
          transaction.setLabel(resultSet.getString(LABEL));
          transaction.setAccountId(resultSet.getInt(ACCOUNT_ID));
          transaction.setCategoryId(resultSet.getInt(CATEGORY_ID));

          transactions.add(transaction);
        }
      } else {
        throw new IllegalArgumentException("The account ID should not be null");
      }
    } catch (Exception e) {
      throw new RuntimeException(STR."Failed the find transfer between account : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }

    return transactions;
  }
}
