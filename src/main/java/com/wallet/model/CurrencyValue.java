package com.wallet.model;

import com.wallet.annotation.DatabaseField;
import com.wallet.annotation.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "currency_value")
public class CurrencyValue {
  @DatabaseField(name = "currency_value_id")
  private Long currencyValueId;

  @DatabaseField(name = "currency_value_date")
  private LocalDateTime currencyValueDate;

  @DatabaseField(name = "exchange_rate")
  private Double exchangeRate;

  @DatabaseField(name = "destination_currency_id")
  private int destinationCurrencyId;

  @DatabaseField(name = "source_currency_id")
  private int sourceCurrencyId;
}
