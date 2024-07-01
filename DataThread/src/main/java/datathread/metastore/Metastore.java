public interface Metastore {
    public T Optional<T> resolveAndRead(String id, Class<T> tipe);

    public T List<T> findAllAndRead(Class<T> tipe);

    public <T,Error> Optional<Error> write(String id, String tipe, T data);
  }
