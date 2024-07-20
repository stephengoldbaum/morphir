package datathread;

import java.util.Optional;

public class Identifier {
    private String scheme;
    private String[] domain;
    private String name;

    public Identifier(String scheme, String[] domain, String name) {
        this.scheme = scheme;
        this.domain = domain;
        this.name = name;
    }

    public String scheme() { return this.scheme; }
    public String[] domain() { return this.domain; }
    public String name() { return this.name; }

    public static Optional<Identifier> from(String urn) {
        try {
            String[] items = urn.split("\\:");
            String scheme = items[0];
            String[] domain = items[1].split("\\/");
            String name = items[2];

            return Optional.of(new Identifier(scheme, domain, name));
        }
        catch(Exception x) {
            x.printStackTrace();
            return Optional.empty();
        }
    }

    public static String toString(Identifier id) {
        String scheme = id.scheme();
        String domain = String.join("/", id.domain());
        String name = id.name();

        return scheme + ":" + domain + ":" + name;
    }
}
