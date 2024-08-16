package datathread.metastore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetastoreFactory {
    public static Optional<Metastore> getInputMetastore(String[] args) {
        List<Metastore> metastores = getInputPaths(args)
                .flatMap(path -> Arrays.asList(path.resolve("automated"), path.resolve("edited")).stream())
                .filter(Files::exists)
                .map(path -> new FileMetastore(path))
                .collect(Collectors.toList());

        return metastores.isEmpty() ? Optional.empty() : Optional.of(new FederatedMetastore(metastores));
    }

    public static Optional<Metastore> getInputMetastores(Path path) {
        List<Metastore> metastores = Arrays.asList(path.resolve("automated"), path.resolve("edited")).stream()
                .filter(Files::exists)
                .map(p -> new FileMetastore(p))
                .collect(Collectors.toList());

        return metastores.isEmpty() ? Optional.empty() : Optional.of(new FederatedMetastore(metastores));
    }

    public static Optional<Metastore> getOutputMetastore(String[] args) {
        Optional<Metastore> result = getOutputArgument(args)
                .map(folder -> Paths.get(folder.trim()))
                .map(path -> new FileMetastore(path));

        return result;
    }

    public static Optional<Path> getOutputPath(String[] args) {
        Optional<Path> result = getOutputArgument(args)
                .map(folder -> Paths.get(folder.trim()));

        return result;
    }

    public static Stream<String> getInputArgument(String[] args) {
        return getArgument("--input", args).stream()
                .flatMap(MetastoreFactory::split);
    }

    public static Stream<Path> getInputPaths(String[] args) {
        return getInputArgument(args)
                .flatMap(MetastoreFactory::split)
                .map(folder -> Paths.get(folder))
                .filter(Files::exists)
                ;
    }

    public static Optional<String> getOutputArgument(String[] args) {
        return getArgument("--output", args);
    }

    public static Optional<String> getArgument(String flag, String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();

            if (flag.equals(arg)) {
                if (i + 1 < args.length) {
                    return Optional.ofNullable(args[i + 1]);
                } else {
                    System.out.println("Missing argument for " + flag);
                    break;
                }
            }
        }

        return Optional.empty();
    }

    public static Stream<String> split(String arg) {
        return Arrays.asList(arg.split(",")).stream()
                .map(s -> s.trim())
                ;
    }

}
