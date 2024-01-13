package com.wallet.repositories.implementations;

import com.wallet.repositories.CrudOperations;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class CrudMaker<T> extends Repository<T> implements CrudOperations<T> {
  public CrudMaker(CrudMakerParams params) throws Exception {
    super(params);
  }

  @Override
  public T findById(Long toFind) {
    return null;
  }

  @Override
  public List<T> findAll() {
    return null;
  }

  @Override
  public List<T> saveAll(List<T> toSave) {
    return null;
  }

  @Override
  public List<T> updateAll(List<T> toUpdate) {
    return null;
  }

  @Override
  public T save(T toSave) {
    return null;
  }

  @Override
  public T update(T toUpdate) {
    return null;
  }

  @Override
  public void delete(T toDelete) {}

  @Override
  public void closeResources(
      Connection connection, PreparedStatement statement, ResultSet resultSet) {}
}
