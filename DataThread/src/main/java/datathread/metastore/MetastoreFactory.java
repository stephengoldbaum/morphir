package datathread.metastore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory class for creating Metastore instances.
 *
 * This class exists to provide a centralized way to create and configure Metastore instances based on input arguments or paths.
 * It simplifies the process of setting up Metastore instances by handling the necessary configuration and validation internally.
 *
 * Developers would use this class to:
 * 1. Create Metastore instances from command-line arguments.
 * 2. Create Metastore instances from specified paths.
 * 3. Retrieve input and output paths from command-line arguments.
 */
public class MetastoreFactory {

    /**
     * Creates an input Metastore instance based on command-line arguments.
     *
     * @param args the command-line arguments
     * @return an Optional containing the created Metastore instance, or an empty Optional if no valid Metastore could be created
     */
    public static Optional<Metastore> getInputMetastore(String[] args) {
        List<Metastore> metastores = getInputPaths(args)
                .flatMap(path -> Arrays.asList(path.resolve("automated"), path.resolve("edited")).stream())
                .filter(Files::exists)
                .map(path -> new FileMetastore(path))
                .collect(Collectors.toList());

        return metastores.isEmpty() ? Optional.empty() : Optional.of(new FederatedMetastore(metastores));
    }

    /**
     * Creates an input Metastore instance based on a specified path.
     *
     * @param path the base path
     * @return an Optional containing the created Metastore instance, or an empty Optional if no valid Metastore could be created
     */
    public static Optional<Metastore> getInputMetastores(Path path) {
        List<Metastore> metastores = Arrays.asList(path.resolve("automated"), path.resolve("edited")).stream()
                .filter(Files::exists)
                .map(p -> new FileMetastore(p))
                .collect(Collectors.toList());

        return metastores.isEmpty() ? Optional.empty() : Optional.of(new FederatedMetastore(metastores));
    }

    /**
     * Creates an output Metastore instance based on command-line arguments.
     *
     * @param args the command-line arguments
     * @return an Optional containing the created Metastore instance, or an empty Optional if no valid Metastore could be created
     */
    public static Optional<Metastore> getOutputMetastore(String[] args) {
        Optional<Metastore> result = getOutputArgument(args)
                .map(folder -> Paths.get(folder.trim()))
                .map(path -> new FileMetastore(path));

        return result;
    }

    /**
     * Retrieves the output path from command-line arguments.
     *
     * @param args the command-line arguments
     * @return an Optional containing the output path, or an empty Optional if no valid path could be found
     */
    public static Optional<Path> getOutputPath(String[] args) {
        Optional<Path> result = getOutputArgument(args)
                .map(folder -> Paths.get(folder.trim()));

        return result;
    }

    /**
     * Retrieves the input argument from command-line arguments.
     *
     * @param args the command-line arguments
     * @return a Stream of input arguments
     */
    public static Stream<String> getInputArgument(String[] args) {
        return getArgument("--input", args).stream()
                .flatMap(MetastoreFactory::split);
    }

    /**
     * Retrieves the input paths from command-line arguments.
     *
     * @param args the command-line arguments
     * @return a Stream of input paths
     */
    public static Stream<Path> getInputPaths(String[] args) {
        return getInputArgument(args)
                .flatMap(MetastoreFactory::split)
                .map(folder -> Paths.get(folder))
                .filter(Files::exists);
    }

    /**
     * Retrieves the output argument from command-line arguments.
     *
     * @param args the command-line arguments
     * @return an Optional containing the output argument, or an empty Optional if no valid argument could be found
     */
    public static Optional<String> getOutputArgument(String[] args) {
        return getArgument("--output", args);
    }

    /**
     * Retrieves the argument for a specified flag from command-line arguments.
     *
     * @param flag the flag to search for
     * @param args the command-line arguments
     * @return an Optional containing the argument for the specified flag, or an empty Optional if no valid argument could be found
     */
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

    /**
     * Splits a comma-separated argument into a Stream of strings.
     *
     * @param arg the argument to be split
     * @return a Stream of split strings
     */
    public static Stream<String> split(String arg) {
        return Arrays.asList(arg.split(",")).stream()
                .map(s -> s.trim());
    }
}