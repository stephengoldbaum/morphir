package datathread.metastore;

public interface MetastoreConnector {
    <T> void transfer(Metastore source, Metastore target, Class<T> ofType);

    <T> void publish(Metastore source, T target);

    <T> void consume(T target, Metastore source);
}