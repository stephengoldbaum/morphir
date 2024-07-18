package datathread.metastore;

import java.util.*;

import datathread.Identifier;


public interface Metastore {
  public <T> Optional<T> resolveAndRead(Identifier id, Class<T> tipe);

  public <T> List<T> findAllAndRead(Class<T> tipe);

  public <T> Optional<String> write(Identifier id, Class<T> tipe, T data);
}
