package com.wallet.repositories.implementations;

import com.wallet.database.ConnectionToDb;
import com.wallet.entities.CurrencyValue;
import com.wallet.repositories.CrudOperations;
import java.sql.*;
import java.util.List;

public class CurrencyValueCrudOperations implements CrudOperations<CurrencyValue> {
  private static final String CURRENCY_VALUE_ID_COLUMN = "currency_value_id";
  private static final String CURRENCY_VALUE_DATE_COLUMN = "currency_value_date";
  private static final String EXCHANGE_RATE_COLUMN = "exchange_rate";
  private static final String SOURCE_CURRENCY_ID_COLUMN = "source_currency_id";
  private static final String DESTINATION_CURRENCY_ID_COLUMN = "destination_currency_id";

  private static final String SELECT_BY_CURRENCIES =
      "SELECT * FROM currency_value WHERE source_currency_id = ? AND destination_currency_id = ?";
  private static final String SELECT_FOR_DATE =
      "SELECT * FROM currency_value WHERE currency_value_date = ?";

  @Override
  public CurrencyValue findById(Long toFind) {
    return null;
  }

  @Override
  public List<CurrencyValue> findAll() {
    return null;
  }

  @Override
  public List<CurrencyValue> saveAll(List<CurrencyValue> toSave) {
    return null;
  }

  @Override
  public List<CurrencyValue> updateAll(List<CurrencyValue> toUpdate) {
    return null;
  }

  @Override
  public CurrencyValue save(CurrencyValue toSave) {
    return null;
  }

  @Override
  public CurrencyValue update(CurrencyValue toUpdate) {
    return null;
  }

  @Override
  public void delete(CurrencyValue toDelete) {}

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

  public CurrencyValue findByCurrencies(int sourceCurrencyId, int destinationCurrencyId) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    CurrencyValue currencyValue = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(SELECT_BY_CURRENCIES);
      statement.setInt(1, sourceCurrencyId);
      statement.setInt(2, destinationCurrencyId);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        currencyValue = new CurrencyValue();
        currencyValue.setCurrencyValueId(resultSet.getLong(CURRENCY_VALUE_ID_COLUMN));
        currencyValue.setCurrencyValueDate(
            resultSet.getTimestamp(CURRENCY_VALUE_DATE_COLUMN).toLocalDateTime());
        currencyValue.setExchangeRate(resultSet.getDouble(EXCHANGE_RATE_COLUMN));
        currencyValue.setSourceCurrencyId(resultSet.getInt(SOURCE_CURRENCY_ID_COLUMN));
        currencyValue.setDestinationCurrencyId(resultSet.getInt(DESTINATION_CURRENCY_ID_COLUMN));
      }
    } catch (SQLException e) {
      throw new RuntimeException(
          STR."Failed to retrieve currency value by currencies : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }

    return currencyValue;
  }

  public CurrencyValue findCurrencyValueForDate(Timestamp transactionDate) {
    CurrencyValue currencyValue = null;
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();
      statement = connection.prepareStatement(SELECT_FOR_DATE);
      statement.setTimestamp(1, transactionDate);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        currencyValue = new CurrencyValue();
        currencyValue.setCurrencyValueId(resultSet.getLong(CURRENCY_VALUE_ID_COLUMN));
        currencyValue.setCurrencyValueDate(
            resultSet.getTimestamp(CURRENCY_VALUE_DATE_COLUMN).toLocalDateTime());
        currencyValue.setExchangeRate(resultSet.getDouble(EXCHANGE_RATE_COLUMN));
        currencyValue.setSourceCurrencyId(resultSet.getInt(SOURCE_CURRENCY_ID_COLUMN));
        currencyValue.setDestinationCurrencyId(resultSet.getInt(DESTINATION_CURRENCY_ID_COLUMN));
      }
    } catch (SQLException e) {
      throw new RuntimeException(STR."Failed to retrieve currency value for date : \{e.getMessage()}");
    } finally {
      closeResources(connection, statement, resultSet);
    }

    return currencyValue;
  }
}
