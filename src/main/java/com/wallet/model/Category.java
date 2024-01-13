package com.wallet.model;

import com.wallet.annotations.DatabaseField;
import com.wallet.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "category")
public class Category {
  @DatabaseField(name = "category_id")
  private Long categoryId;

  @DatabaseField(name = "category_name")
  private String categoryName;
}
