package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;

import java.time.LocalDateTime;

import com.wallet.annotations.type.ColumnType;
import com.wallet.annotations.type.GenerativeValue;
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
@DatabaseTable(name = "currency_value")
public class CurrencyValue {

  @DatabaseField(name = "currency_value_id", identity = true, generative = GenerativeValue.SEQUENCE)
  private Long currencyValueId;

  @DatabaseField(name = "currency_value_date", columnType = ColumnType.DATE, required = true)
  private LocalDateTime currencyValueDate;

  @DatabaseField(name = "exchange_rate", columnType = ColumnType.DOUBLE, required = true)
  private Double exchangeRate;

  @DatabaseField(name = "destination_currency_id", columnType = ColumnType.INT, references = true, required = true)
  private int destinationCurrencyId;

  @DatabaseField(name = "source_currency_id", columnType = ColumnType.INT, references = true, required = true)
  private int sourceCurrencyId;
}
