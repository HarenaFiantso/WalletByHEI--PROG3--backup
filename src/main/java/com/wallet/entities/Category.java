package com.wallet.entities;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.DatabaseTable;
import com.wallet.annotations.FieldType;
import com.wallet.types.GenerativeValue;
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

  @DatabaseField(name = "category_name", fieldType = FieldType.VARCHAR, required = true)
  private String categoryName;
}
