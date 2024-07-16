package datathread.metastore;

import java.util.*;

import datathread.Indentifier;


public interface Metastore {
  public <T> Optional<T> resolveAndRead(Indentifier id, Class<T> tipe);

  public <T> List<T> findAllAndRead(Class<T> tipe);

  public <T> Optional<String> write(Indentifier id, Class<T> tipe, T data);
}
