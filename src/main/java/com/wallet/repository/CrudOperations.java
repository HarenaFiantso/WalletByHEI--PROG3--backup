package com.wallet.repository;

import com.wallet.annotation.SqlOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public interface CrudOperations<T> {
  @SqlOperation(type = SqlOperation.Type.SELECT)
  T findById(Long toFind);

  @SqlOperation(type = SqlOperation.Type.SELECT)
  List<T> findAll();

  @SqlOperation(type = SqlOperation.Type.INSERT)
  List<T> saveAll(List<T> toSave);

  @SqlOperation(type = SqlOperation.Type.UPDATE)
  List<T> updateAll(List<T> toUpdate);

  @SqlOperation(type = SqlOperation.Type.INSERT)
  T save(T toSave);

  @SqlOperation(type = SqlOperation.Type.UPDATE)
  T update(T toUpdate);

  @SqlOperation(type = SqlOperation.Type.DELETE)
  void delete(T toDelete);

  void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet);
}
