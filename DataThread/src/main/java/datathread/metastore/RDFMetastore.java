package datathread.metastore;

import datathread.Identifier;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RDFMetastore implements Metastore {
    private final Repository repository;
    private final ObjectMapper mapper;

    public RDFMetastore() {
        this(null, null);
    }

    public RDFMetastore(Repository repository, ObjectMapper mapper) {
        this.repository = repository == null ? new SailRepository(new MemoryStore()) : repository;
        this.mapper = mapper == null ? new ObjectMapper() : mapper;
        this.repository.init();
    }

    @Override
    public <T> Optional<T> read(Identifier id, Class<T> type) {
        try (RepositoryConnection conn = repository.getConnection()) {
            String query = buildReadQuery(id);
            var result = conn.prepareTupleQuery(query).evaluate();

            if (result.hasNext()) {
                String jsonValue = result.next().getValue("value").stringValue();
                return Optional.of(mapper.readValue(jsonValue, type));
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize", e);
        }
    }

    @Override
    public <T> List<T> readAll(Class<T> type) {
        try (RepositoryConnection conn = repository.getConnection()) {
            String query = buildReadAllQuery(type);
            var result = conn.prepareTupleQuery(query).evaluate();

            return StreamSupport.stream(result.spliterator(), false)
                    .map(bs -> {
                        try {
                            return mapper.readValue(
                                    bs.getValue("value").stringValue(),
                                    type
                            );
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to readAll", e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    @Override
    public <T> Optional<String> write(Identifier id, T data) {
        try (RepositoryConnection conn = repository.getConnection()) {
            String jsonValue = mapper.writeValueAsString(data);
            String statement = buildInsertStatement(id, jsonValue);

            conn.prepareUpdate(statement).execute();
            return Optional.of(id.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to RDF store", e);
        }
    }

    @Override
    public Optional<String> delete(Identifier id) {
        try (RepositoryConnection conn = repository.getConnection()) {
            String statement = buildDeleteStatement(id);
            conn.prepareUpdate(statement).execute();
            return Optional.of(id.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from RDF store", e);
        }
    }

    protected String buildReadQuery(Identifier id) {
        return String.format(
                "SELECT ?value WHERE { <%s> <urn:datathread:value> ?value }",
                id.toString()
        );
    }

    protected String buildReadAllQuery(Class tipe) {
        // TODO: How is the type of the data stored in the RDF store?
        return String.format(
                "SELECT ?value WHERE { <%s> ?type ?value }",
                tipe.toString()
        );
    }

    protected String buildInsertStatement(Identifier id, String value) {
        return String.format(
                "INSERT DATA { <%s> <urn:datathread:value> %s }",
                id.toString(),
                quoted(value)
        );
    }

    protected String buildDeleteStatement(Identifier id) {
        return String.format(
                "DELETE WHERE { <%s> ?p ?o }",
                id.toString()
        );
    }

    protected String quoted(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
