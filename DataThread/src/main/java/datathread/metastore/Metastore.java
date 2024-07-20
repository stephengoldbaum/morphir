package datathread.metastore;

import datathread.Identifier;

import java.util.List;
import java.util.Optional;


public interface Metastore {
  public <T> Optional<T> read(Identifier id, Class<T> tipe);

  public <T> List<T> readAll(Class<T> tipe);

  public <T> Optional<String> write(Identifier id, Class<T> tipe, T data);
}
