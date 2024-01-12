package com.wallet.repository.implementations;

import com.wallet.database.ConnectionToDb;
import com.wallet.model.Currency;
import com.wallet.model.type.CurrencyCodeType;
import com.wallet.model.type.CurrencyNameType;
import com.wallet.repository.CrudOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyCrudOperations implements CrudOperations<Currency> {
  private static final String CURRENCY_ID_COLUMN = "currency_id";
  private static final String CURRENCY_NAME_COLUMN = "currency_name";
  private static final String CURRENCY_CODE_COLUMN = "currency_code";

  private static final String SELECT_BY_ID_QUERY = "SELECT * FROM currency WHERE currency_id = ?";
  private static final String SELECT_ALL_QUERY = "SELECT * FROM currency";
  private static final String INSERT_QUERY =
      "INSERT INTO currency (currency_name, currency_code) VALUES (CAST(? AS currency_name), CAST(?"
          + " AS currency_code)) RETURNING *";
  private static final String UPDATE_QUERY =
      "UPDATE currency SET currency_name = CAST(? AS currency_name), currency_code = CAST(? AS"
          + " currency_code) WHERE currency_id = ? RETURNING *";
  private static final String DELETE_QUERY = "DELETE FROM currency WHERE currency_id = ?";

  @Override
  public Currency findById(Long toFind) {
    Currency currency = null;
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(SELECT_BY_ID_QUERY);
      statement.setLong(1, toFind);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        currency = new Currency();
        currency.setCurrencyId(resultSet.getLong(CURRENCY_ID_COLUMN));
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to retrieve currency : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return currency;
  }

  @Override
  public List<Currency> findAll() {
    List<Currency> currencies = new ArrayList<>();
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(SELECT_ALL_QUERY);
      resultSet = statement.executeQuery();

      while (resultSet.next()) {
        Currency currency = new Currency();
        currency.setCurrencyId(resultSet.getLong(CURRENCY_ID_COLUMN));
        currency.setCurrencyName(
            CurrencyNameType.valueOf(resultSet.getString(CURRENCY_NAME_COLUMN)));
        currency.setCurrencyCode(
            CurrencyCodeType.valueOf(resultSet.getString(CURRENCY_CODE_COLUMN)));

        currencies.add(currency);
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to retrieve currencies : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return currencies;
  }

  @Override
  public List<Currency> saveAll(List<Currency> toSave) {
    List<Currency> savedCurrencies = new ArrayList<>();

    for (Currency currency : toSave) {
      Currency savedCurrency = this.save(currency);
      savedCurrencies.add(savedCurrency);
    }

    return savedCurrencies;
  }

  @Override
  public List<Currency> updateAll(List<Currency> toUpdate) {
    List<Currency> updateCurrencies = new ArrayList<>();

    for (Currency currency : toUpdate) {
      Currency updateCurrency = this.save(currency);
      updateCurrencies.add(updateCurrency);
    }

    return updateCurrencies;
  }

  @Override
  public Currency save(Currency toSave) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

      statement.setString(1, String.valueOf(toSave.getCurrencyName()));
      statement.setString(2, String.valueOf(toSave.getCurrencyCode()));

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected > 0) {
        resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
          toSave.setCurrencyId(resultSet.getLong(1));
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
  public Currency update(Currency toUpdate) {
    Connection connection = null;
    PreparedStatement statement = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(UPDATE_QUERY);

      statement.setString(1, String.valueOf(toUpdate.getCurrencyName()));
      statement.setString(2, String.valueOf(toUpdate.getCurrencyCode()));
      statement.setLong(3, toUpdate.getCurrencyId());

      int rowsAffected = statement.executeUpdate();

      if (rowsAffected > 0) {
        return toUpdate;
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to update currency : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, null);
    }

    return null;
  }

  @Override
  public void delete(Currency toDelete) {
    Connection connection = null;
    PreparedStatement statement = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(DELETE_QUERY);
      statement.setLong(1, toDelete.getCurrencyId());

    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to delete currency :\{e.getMessage()}");
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
