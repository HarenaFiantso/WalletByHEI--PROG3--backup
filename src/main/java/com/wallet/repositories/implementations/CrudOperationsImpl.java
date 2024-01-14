package com.wallet.repositories.implementations;

import com.wallet.database.config.ConnectionToDb;
import com.wallet.repositories.CrudOperations;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrudOperationsImpl<T> extends Repository<T> implements CrudOperations<T> {

  public CrudOperationsImpl(CrudOperationsParams params) {
    super(params);
  }

  @Override
  public T findById(T toFind) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(this.FIND_BY_ID_QUERY);
      this.wrapObjectToStatement(toFind, statement);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return this.mapResultSetToInstance(resultSet);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to find this entity by Id");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return null;
  }

  @Override
  public List<T> findAll() {
    List<T> list = new ArrayList<>();
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(this.FIND_ALL_QUERY);

      resultSet = statement.executeQuery();

      while (resultSet.next()) {
        list.add(this.mapResultSetToInstance(resultSet));
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to retrieve all data from this entity");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return list;
  }

  @Override
  public List<T> saveAll(List<T> toSaves) {
    List<T> list = new ArrayList<>();

    if (toSaves == null || toSaves.isEmpty()) {
      return list;
    }

    for (T toSave : toSaves) {
      T saved = this.save(toSave);

      if (saved != null) {
        list.add(saved);
      }
    }

    return list;
  }

  @Override
  public T save(T toSave) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(this.SAVE_QUERY);
      this.wrapObjectToStatement(toSave, statement);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return this.mapResultSetToInstance(resultSet);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to save from this entity");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return null;
  }

  @Override
  public T update(T toUpdate) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(this.UPDATE_QUERY);
      this.wrapObjectToStatement(toUpdate, statement);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return this.mapResultSetToInstance(resultSet);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to update from this entity");
    } finally {
      closeResources(connection, statement, resultSet);
    }
    return null;
  }

  @Override
  public T delete(T toDelete) {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionToDb.getConnection();

      statement = connection.prepareStatement(this.DELETE_QUERY);
      this.wrapObjectToStatement(toDelete, statement);

      resultSet = statement.executeQuery();

      if (resultSet.next()) {
        return this.mapResultSetToInstance(resultSet);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete from this entity");
    } finally {
      closeResources(connection, statement, resultSet);
    }

    return null;
  }

  @Override
  public void closeResources(
      Connection connection, PreparedStatement statement, ResultSet resultSet) {
    try {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
