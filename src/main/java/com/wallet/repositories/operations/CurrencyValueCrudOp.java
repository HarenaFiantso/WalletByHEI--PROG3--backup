package com.wallet.repositories.operations;

import com.wallet.config.ConnectionToDb;
import com.wallet.entities.Currency;
import com.wallet.entities.CurrencyValue;
import com.wallet.repositories.implementations.CrudOperationsParams;
import com.wallet.repositories.implementations.CrudOperationsImpl;

import java.sql.*;

public class CurrencyValueCrudOp extends CrudOperationsImpl<CurrencyValue> {
  private static final String CURRENCY_VALUE_ID_COLUMN = "currency_value_id";
  private static final String CURRENCY_VALUE_DATE_COLUMN = "currency_value_date";
  private static final String EXCHANGE_RATE_COLUMN = "exchange_rate";
  private static final String SOURCE_CURRENCY_ID_COLUMN = "source_currency_id";
  private static final String DESTINATION_CURRENCY_ID_COLUMN = "destination_currency_id";

  private static final String SELECT_BY_CURRENCIES =
      "SELECT * FROM currency_value WHERE source_currency_id = ? AND destination_currency_id = ?";
  private static final String SELECT_FOR_DATE =
      "SELECT * FROM currency_value WHERE currency_value_date = ?";

  public CurrencyValueCrudOp() {
    super(
        CrudOperationsParams
            .builder()
            .entityClass(Currency.class)
            .createColumnSet(new String[]{CURRENCY_VALUE_DATE_COLUMN, EXCHANGE_RATE_COLUMN, SOURCE_CURRENCY_ID_COLUMN, DESTINATION_CURRENCY_ID_COLUMN})
            .updateByColumn(CURRENCY_VALUE_ID_COLUMN)
            .deleteByAColumn(CURRENCY_VALUE_ID_COLUMN)
            .build()
    );
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
