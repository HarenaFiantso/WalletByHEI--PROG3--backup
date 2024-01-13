package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
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
@DatabaseTable(name = "category")
public class Category {

  @DatabaseField(name = "category_id", identity = true, generative = GenerativeValue.SEQUENCE)
  private Long categoryId;

  @DatabaseField(name = "category_name", columnType = ColumnType.VARCHAR, required = true)
  private String categoryName;
}
