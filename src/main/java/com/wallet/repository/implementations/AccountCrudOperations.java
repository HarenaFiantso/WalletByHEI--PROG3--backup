package com.wallet.repository.implementations;

import com.wallet.database.ConnectionToDb;
import com.wallet.entities.Account;
import com.wallet.types.AccountType;
import com.wallet.repository.CrudOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountCrudOperations implements CrudOperations<Account> {
  private static final String ACCOUNT_ID_COLUMN = "account_id";
  private static final String ACCOUNT_NAME_COLUMN = "account_name";
  private static final String ACCOUNT_TYPE_COLUMN = "account_type";
  private static final String CURRENCY_ID_COLUMN = "currency_id";

  private static final String SELECT_BY_ID_QUERY = "SELECT * FROM account WHERE account_id = ?";
  private static final String SELECT_ALL_QUERY = "SELECT * FROM account";
  private static final String INSERT_QUERY =
      "INSERT INTO account (account_name, account_type, currency_id) VALUES (?, CAST(? AS"
          + " account_type), ?) RETURNING *";
  private static final String UPDATE_QUERY =
      "UPDATE account SET account_name = ?, account_type = CAST(? AS account_type), currency_id = ?"
          + " WHERE account_id = ? RETURNING *";
  private static final String DELETE_QUERY = "DELETE FROM account WHERE account_id = ?";

  @Override
  public Account findById(Long toFind) {
    Account account = null;
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(SELECT_BY_ID_QUERY);
      statement.setLong(1, toFind);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        account = new Account();
        account.setAccountId(resultSet.getLong(ACCOUNT_ID_COLUMN));
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to retrieve account : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return account;
  }

  @Override
  public List<Account> findAll() {
    List<Account> accounts = new ArrayList<>();
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(SELECT_ALL_QUERY);
      resultSet = statement.executeQuery();

      while (resultSet.next()) {
        Account account = new Account();
        account.setAccountId(resultSet.getLong(ACCOUNT_ID_COLUMN));
        account.setAccountName(resultSet.getString(ACCOUNT_NAME_COLUMN));
        account.setAccountType(AccountType.valueOf(resultSet.getString(ACCOUNT_TYPE_COLUMN)));
        account.setCurrencyId(resultSet.getInt(CURRENCY_ID_COLUMN));

        accounts.add(account);
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to retrieve accounts : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return accounts;
  }

  @Override
  public List<Account> saveAll(List<Account> toSave) {
    List<Account> savedAccounts = new ArrayList<>();

    for (Account account : toSave) {
      Account savedAccount = this.save(account);
      savedAccounts.add(savedAccount);
    }

    return savedAccounts;
  }

  @Override
  public List<Account> updateAll(List<Account> toUpdate) {
    List<Account> updatedAccounts = new ArrayList<>();

    for (Account account : toUpdate) {
      Account updatedAccount = this.update(account);
      updatedAccounts.add(updatedAccount);
    }

    return updatedAccounts;
  }

  @Override
  public Account save(Account toSave) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

      statement.setString(1, toSave.getAccountName());
      statement.setString(2, String.valueOf(toSave.getAccountType()));
      statement.setInt(3, toSave.getCurrencyId());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected > 0) {
        resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
          toSave.setAccountId(resultSet.getLong(1));
          return toSave;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to save account : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }

    return null;
  }

  @Override
  public Account update(Account toUpdate) {
    Connection connection = null;
    PreparedStatement statement = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(UPDATE_QUERY);

      statement.setString(1, toUpdate.getAccountName());
      statement.setString(2, String.valueOf(toUpdate.getAccountType()));
      statement.setInt(3, toUpdate.getCurrencyId());
      statement.setLong(4, toUpdate.getAccountId());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected > 0) {
        return toUpdate;
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to update account : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, null);
    }

    return null;
  }

  @Override
  public void delete(Account toDelete) {
    Connection connection = null;
    PreparedStatement statement = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(DELETE_QUERY);
      statement.setLong(1, toDelete.getAccountId());

    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to delete account :\{e.getMessage()}");
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
