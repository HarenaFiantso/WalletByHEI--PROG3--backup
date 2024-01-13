package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
import com.wallet.annotations.type.ColumnType;
import com.wallet.annotations.type.GenerativeValue;
import com.wallet.model.type.CurrencyCodeType;
import com.wallet.model.type.CurrencyNameType;
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
@DatabaseTable(name = "currency")
public class Currency {

  @DatabaseField(name = "currency_id", identity = true, generative = GenerativeValue.SEQUENCE)
  private Long currencyId;

  @DatabaseField(name = "currency_name", columnType = ColumnType.TEXT, required = true)
  private CurrencyNameType currencyName;

  @DatabaseField(name = "currency_code", columnType = ColumnType.TEXT, required = true)
  private CurrencyCodeType currencyCode;
}
