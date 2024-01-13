package com.wallet.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public interface CrudOperations<T> {
  T findById(Long toFind);

  List<T> findAll();

  List<T> saveAll(List<T> toSave);

  List<T> updateAll(List<T> toUpdate);

  T save(T toSave);

  T update(T toUpdate);

  void delete(T toDelete);

  void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet);
}
