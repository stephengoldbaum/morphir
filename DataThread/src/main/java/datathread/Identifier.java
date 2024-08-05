package datathread;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            String[] domain = Arrays.stream(
                    items[1].split("\\/"))
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);

            String name = items[2];

            return Optional.of(new Identifier(scheme, domain, name));
        }
        catch(Exception x) {
            x.printStackTrace();
            System.err.println("Invalid URN: " + urn);
            return Optional.empty();
        }
    }

    public String toString() {
        return Identifier.toString(this);
    }

    public static String toString(Identifier id) {
        String scheme = id.scheme();
        String domain = String.join("/", id.domain());
        String name = id.name();

        return scheme + ":/" + domain + ":" + name;
    }

    public static Optional<Identifier> from(URI urn) {
        return Optional.ofNullable(urn)
                .map(URI::toString)
                .map(s -> urlCode(s, URLDecoder::decode))
                .flatMap(s -> from(s));
    }

    public static URI toURI(Identifier id) {
        try {
            String domain = Arrays.stream(id.domain())
                    .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                    .collect(Collectors.joining("/"));
            String name = URLEncoder.encode(id.name(), StandardCharsets.UTF_8);
            String urn = id.scheme + ":/" + domain + ":" + name;

            return new URI(urn);
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    public static String urlCode(String s, Function<String,String> coder) {
        String result =  Optional.ofNullable(s).stream()
                .flatMap(str -> Arrays.stream(str.split("\\:")))
                .map(coder)
                .collect(Collectors.joining(":"));

        return result;
    }
    public static void main(String[] args) {
        System.out.println(Identifier.from("scheme:/domain1/domain2:name"));
        System.out.println(Identifier.from(URI.create(URLEncoder.encode("scheme:/domain1/foo & bah:name"))));
        System.out.println(Identifier.from(URI.create(URLEncoder.encode("scheme:domain1/foo & bah:name"))));
        System.out.println(Identifier.from("scheme:/domain1/foo & bah:name"));
        System.out.println(new Identifier("scheme", new String[] {"domain1", "foo"}, "name"));
        System.out.println(new Identifier("scheme", new String[] {"domain1", "foo & bah"}, "name"));
        System.out.println(new Identifier("scheme", new String[] {"/domain1", "foo & bah"}, "name"));
    }
}
