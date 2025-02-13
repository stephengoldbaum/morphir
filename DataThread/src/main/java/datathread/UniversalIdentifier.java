package datathread;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UniversalIdentifier extends Identifier {
    public final String[] distribution;

    public UniversalIdentifier(String[] distribution, String scheme, String[] domain, String name) {
        super(scheme, domain, name);
        this.distribution = distribution;
    }

    public String[] distribution() { return this.domain; }

    public String toString() {
        return UniversalIdentifier.toString(this);
    }

    public static String toString(UniversalIdentifier id) {
        return UniversalIdentifier.toURI(id).toString();
    }

    public static URI toURI(UniversalIdentifier id) {
        try {
            String domain = Arrays.stream(id.domain())
                    .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                    .collect(Collectors.joining("/"));

            String distribution = Arrays.stream(id.distribution())
                    .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                    .collect(Collectors.joining("."));

            String name = URLEncoder.encode(id.name(), StandardCharsets.UTF_8);
            String url = "https://" + distribution + "/" + id.scheme + "/" + domain + "/" + name;

            return new URI(url);
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }

    public static Optional<UniversalIdentifier> from(URL url) {
        try {
            String[] distribution = url.getHost().split("\\.");
            String[] path = url.getPath().split("/");
            String scheme = path[0];
            String name = path[path.length - 1];
            String[] domain = Arrays.copyOfRange(path, 1, path.length - 2);

            return Optional.of(new UniversalIdentifier(distribution, scheme, domain, name));
        } catch (Exception x) {
            x.printStackTrace();
            System.err.println("Invalid URN: " + url);
            return Optional.empty();
        }
    }


    public static String decodeWithUTF8(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    public static String encodeWithUTF8(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
