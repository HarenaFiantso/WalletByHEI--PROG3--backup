package com.wallet.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public interface CrudOperations<T> {
  T findById(T toFind);

  List<T> findAll();

  List<T> saveAll(List<T> toSaves);

  T save(T toSave);

  T update(T toUpdate);

  T delete(T toDelete);

  void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet);
}
