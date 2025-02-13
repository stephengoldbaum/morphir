//package datathread.metastore;
//
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Optional;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class MetastoreFactoryTest {
//
//    @BeforeEach
//    public void setUp() {
//        // Mock static methods in Files class
//        mockStatic(Files.class);
//    }
//
////    @Test
////    public void testGetInputMetastore() {
////        String[] args = {"--input", "path1,path2"};
////        Path path1 = Paths.get("path1");
////        Path path2 = Paths.get("path2");
////
////        when(Files.exists(path1.resolve("automated"))).thenReturn(true);
////        when(Files.exists(path1.resolve("edited"))).thenReturn(false);
////        when(Files.exists(path2.resolve("automated"))).thenReturn(false);
////        when(Files.exists(path2.resolve("edited"))).thenReturn(true);
////
////        Optional<Metastore> result = MetastoreFactory.getInputMetastore(args);
////
////        assertTrue(result.isPresent());
////    }
//
//    @Test
//    public void testGetInputMetastores() {
//        Path path = Paths.get("path");
//
//        when(Files.exists(path.resolve("automated"))).thenReturn(true);
//        when(Files.exists(path.resolve("edited"))).thenReturn(false);
//
//        Optional<Metastore> result = MetastoreFactory.getInputMetastores(path);
//
//        assertTrue(result.isPresent());
//    }
//
////    @Test
////    public void testGetOutputMetastore() {
////        String[] args = {"--output", "outputPath"};
////        Path outputPath = Paths.get("outputPath");
////
////        Optional<Metastore> result = MetastoreFactory.getOutputMetastore(args);
////
////        assertTrue(result.isPresent());
////    }
//
//    @Test
//    public void testGetOutputPath() {
//        String[] args = {"--output", "outputPath"};
//        Path outputPath = Paths.get("outputPath");
//
//        Optional<Path> result = MetastoreFactory.getOutputPath(args);
//
//        assertTrue(result.isPresent());
//        assertEquals(outputPath, result.get());
//    }
//
//    @Test
//    public void testGetInputArgument() {
//        String[] args = {"--input", "path1,path2"};
//
//        Stream<String> result = MetastoreFactory.getInputArgument(args);
//
//        assertArrayEquals(new String[]{"path1", "path2"}, result.toArray());
//    }
//
//    @Test
//    public void testGetInputPaths() {
//        String[] args = {"--input", "path1,path2"};
//        Path path1 = Paths.get("path1");
//        Path path2 = Paths.get("path2");
//
//        when(Files.exists(path1)).thenReturn(true);
//        when(Files.exists(path2)).thenReturn(true);
//
//        Stream<Path> result = MetastoreFactory.getInputPaths(args);
//
//        assertArrayEquals(new Path[]{path1, path2}, result.toArray());
//    }
//
//    @Test
//    public void testGetOutputArgument() {
//        String[] args = {"--output", "outputPath"};
//
//        Optional<String> result = MetastoreFactory.getOutputArgument(args);
//
//        assertTrue(result.isPresent());
//        assertEquals("outputPath", result.get());
//    }
//
//    @Test
//    public void testGetArgument() {
//        String[] args = {"--input", "inputPath"};
//
//        Optional<String> result = MetastoreFactory.getArgument("--input", args);
//
//        assertTrue(result.isPresent());
//        assertEquals("inputPath", result.get());
//    }
//}