package com.wallet.repository.implementations;

import com.wallet.database.ConnectionToDb;
import com.wallet.entities.TransferHistory;
import com.wallet.repository.CrudOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransferHistoryCrudOperations implements CrudOperations<TransferHistory> {
  private static final String TRANSFER_HISTORY_ID_COLUMN = "transfer_history_id";
  private static final String TRANSFER_DATE_COLUMN = "transfer_date";
  private static final String DEBIT_TRANSACTION_ID_COLUMN = "debit_transaction_id";
  private static final String CREDIT_TRANSACTION_ID_COLUMN = "credit_transaction_id";

  private static final String SELECT_BY_ID_QUERY =
      "SELECT * FROM transfer_history WHERE transfer_history_id = ?";
  private static final String SELECT_ALL_QUERY = "SELECT * FROM transfer_history";
  private static final String INSERT_QUERY =
      "INSERT INTO transfer_history (transfer_date, debit_transaction_id, credit_transaction_id)"
          + " VALUES (?, ?, ?) RETURNING *";
  private static final String UPDATE_QUERY =
      "UPDATE transfer_history SET transfer_date = ?, debit_transaction_id = ?,"
          + " credit_transaction_id = ? WHERE transfer_history_id = ? RETURNING *";
  private static final String DELETE_QUERY =
      "DELETE FROM transfer_history WHERE transfer_history_id = ?";

  @Override
  public TransferHistory findById(Long toFind) {
    TransferHistory transferHistory = null;
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(SELECT_BY_ID_QUERY);
      statement.setLong(1, toFind);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        transferHistory = new TransferHistory();
        transferHistory.setTransferHistoryId(resultSet.getLong(TRANSFER_HISTORY_ID_COLUMN));
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to retrieve account : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return transferHistory;
  }

  @Override
  public List<TransferHistory> findAll() {
    List<TransferHistory> transferHistories = new ArrayList<>();
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(SELECT_ALL_QUERY);
      resultSet = statement.executeQuery();

      while (resultSet.next()) {
        TransferHistory transferHistory = new TransferHistory();
        transferHistory.setTransferHistoryId(resultSet.getLong(TRANSFER_HISTORY_ID_COLUMN));
        transferHistory.setTransferDate(resultSet.getTimestamp(TRANSFER_DATE_COLUMN));
        transferHistory.setDebitTransactionId(resultSet.getInt(DEBIT_TRANSACTION_ID_COLUMN));
        transferHistory.setCreditTransactionId(resultSet.getInt(CREDIT_TRANSACTION_ID_COLUMN));

        transferHistories.add(transferHistory);
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to retrieve transfers histories : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return transferHistories;
  }

  @Override
  public List<TransferHistory> saveAll(List<TransferHistory> toSave) {
    List<TransferHistory> savedTransferHistories = new ArrayList<>();

    for (TransferHistory transferHistory : toSave) {
      TransferHistory savedTransferHistory = this.save(transferHistory);
      savedTransferHistories.add(savedTransferHistory);
    }

    return savedTransferHistories;
  }

  @Override
  public List<TransferHistory> updateAll(List<TransferHistory> toUpdate) {
    List<TransferHistory> updatedTransferHistories = new ArrayList<>();

    for (TransferHistory transferHistory : toUpdate) {
      TransferHistory updatedTransferHistory = this.save(transferHistory);
      updatedTransferHistories.add(updatedTransferHistory);
    }

    return updatedTransferHistories;
  }

  @Override
  public TransferHistory save(TransferHistory toSave) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

      statement.setTimestamp(1, toSave.getTransferDate());
      statement.setInt(2, toSave.getDebitTransactionId());
      statement.setInt(3, toSave.getCreditTransactionId());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected > 0) {
        resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
          toSave.setTransferHistoryId(resultSet.getLong(1));
          return toSave;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to save transfer history : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }

    return null;
  }

  @Override
  public TransferHistory update(TransferHistory toUpdate) {
    Connection connection = null;
    PreparedStatement statement = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(UPDATE_QUERY);

      statement.setTimestamp(1, toUpdate.getTransferDate());
      statement.setInt(2, toUpdate.getDebitTransactionId());
      statement.setInt(3, toUpdate.getCreditTransactionId());
      statement.setLong(4, toUpdate.getTransferHistoryId());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected > 0) {
        return toUpdate;
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to update transfer history : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, null);
    }

    return null;
  }

  @Override
  public void delete(TransferHistory toDelete) {
    Connection connection = null;
    PreparedStatement statement = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(DELETE_QUERY);
      statement.setLong(1, toDelete.getTransferHistoryId());

    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to delete transfer history :\{e.getMessage()}");
    } finally {
      closeResources(connection, statement, null);
    }
  }

  @Override
  public void closeResources(
      Connection connection, PreparedStatement statement, ResultSet resultSet) {
    try {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
