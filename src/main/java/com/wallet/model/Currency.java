package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.Table;
import com.wallet.model.type.CurrencyCodeType;
import com.wallet.model.type.CurrencyNameType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "currency")
public class Currency {
  @DatabaseField(name = "currency_id")
  private Long currencyId;

  @DatabaseField(name = "currency_name")
  private CurrencyNameType currencyName;

  @DatabaseField(name = "currency_code")
  private CurrencyCodeType currencyCode;
}
