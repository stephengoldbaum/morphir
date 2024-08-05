package datathread.backends;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.data.casc.manifest.model.*;
import datathread.Identifier;
import datathread.grammar.*;
import datathread.metastore.FileMetastore;
import datathread.metastore.Metastore;
import datathread.metastore.MetastoreCLIProcessor;
import datathread.metastore.Router;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class CasCEmitter {
    private final Map<String, Dataset> datasets;
    private final Map<String, Element> elements;
    private final Optional<String> dgc;

    public CasCEmitter(List<Dataset> datasets, List<Element> elements, Optional<String> dgc) {
        this.datasets = datasets.stream()
                .collect(Collectors.toMap(Dataset::getId, dataset -> dataset));

        this.elements = elements.stream()
                .collect(Collectors.toMap(Element::getId, element -> element));

        this.dgc = dgc;
    }

    public CasCManifest get() {
        List<SystemDataElement> systemDataElements = elements.values().stream()
            .map(element -> handleElement(element))
            .collect(Collectors.toList());

        List<LineageDataset> lineageDatasets = datasets.values().stream()
            .map(dataset -> handleDataset(dataset))
            .collect(Collectors.toList());

        CasCManifest casc = new CasCManifest(
            systemDataElements,
            null, //transformations,
            null, //interfaces,
            null, //dataEntities,
            lineageDatasets,
            null, //logicalServices,
            null  //manifestGeneration
        );

        return casc;
    }


    //// Elements ////
    public SystemDataElement handleElement(Element element) {
        Map<String,Set<String>> associatedDgcDataElements = this.dgc
                .map(key -> Collections.singletonMap(key, Collections.singleton(element.getName())))
                .orElse(null);

        SystemDataElement sde = newSystemDataElement(
            element.getId()
            , element.getName()
            , Elements.getElementType(element).orElse(null)
            , associatedDgcDataElements
        );

        return sde;
    }

    public static SystemDataElement newSystemDataElement(String id, String name, ElementType elementType, Map<String,Set<String>> associatedDgcDataElements) {
        SystemDataElement sde = new SystemDataElement(
            name,
            null, //description = description;
            handleElementType(elementType), //logicalDataType = logicalDataType;
            null, //sampleValues = sampleValues;
            null, //persisted = persisted;
            null, //treatment = treatment;
            null, //requestor = requestor;
            null, //intendedImplementationDate = intendedImplementationDate;
            null, //boundaries = boundaries;
            null, //piiElements = piiElements;
            null, //dataSubjectCategories = dataSubjectCategories;
            associatedDgcDataElements, //associatedDgcDataElements = associatedDgcDataElements;
            null, //logicalServices = logicalServices;
            id.toString(), //systemKey = systemKey;
            null, //customAttributes = customAttributes;
            null  //dataType = dataType;
        );

        return sde;
    }

    public static String handleElementType(ElementType elementType) {
        if(elementType instanceof Elements.Boolean) {
            return "True/False";
        } else if(elementType instanceof Elements.Date) {
            return "Date";
        } else if(elementType instanceof Elements.DateTime) {
            return "DateTime";
        } else if(elementType instanceof Elements.Enum) {
            return "Enum";
        } else if(elementType instanceof Elements.Number) {
            Elements.Number number = (Elements.Number) elementType;
            return Elements.isDecimal(number) ? "Decimal Number" : "Whole Number";
        } else if(elementType instanceof Elements.Record) {
            return "Record";
        } else if(elementType instanceof Elements.Reference) {
            // TODO: get the base type
            return "Reference";
        } else if(elementType instanceof Elements.Text) {
            return "Text";
        } else {
            return null;
        }
    }

    //// Datasets ////
    public LineageDataset handleDataset(Dataset dataset) {
        Set<DatasetElement> dses = dataset.getFields().stream()
            .map(field -> handleField(field))
            .collect(Collectors.toSet());

        LineageDataset ld = new  LineageDataset (
                dataset.getName(),
                null,// description,
                null, // format,
                dses, // datasetElements,
                Collections.emptyMap(), // dgcDataConcepts,
                Collections.emptyMap(), // dgcDataDomains,
                null, // upstreamDatasets,
                null, // boundaries,
                null  // reports
        );

        return ld;
    }

    public DatasetElement handleField(Field field) {
        Optional<Element> element = Optional.ofNullable(this.elements.get(field.getElement()));
        Set<String> sdes = element
                .map(e -> e.getName())
                .map(n -> Collections.singleton(n))
                .orElse(Collections.emptySet());

        DatasetElement de = new DatasetElement(
            field.getName(), //name
            null, //description,
            sdes, //systemDataElements,
            null, //associatedDgcDataElements,
            null, //reportAttributes,
            null  //upstreamDatasetElements
        );

        return de;
    }

    public static void main(String[] args) {
        Optional<Metastore> o = MetastoreCLIProcessor.getInputMetastore(args);

        if(o.isEmpty()) {
            System.out.println("No input metastore found");
            return;
        }

        final Metastore inputMetastore = o.get();

        // Setup the router
        final Router router = new Router(Map.of(
                Element.class, inputMetastore,
                Dataset.class, inputMetastore
        ));

        List<Dataset> allDatasets = router.readAll(Dataset.class);
        List<Element> allElements = allDatasets.stream()
                .flatMap(dataset -> dataset.getFields().stream())
                .flatMap(field -> Identifier.from(field.getElement()).stream())
                .flatMap(id -> router.read(id, Element.class).stream())
                .collect(Collectors.toList())
                ;

        CasCEmitter cascEmitter = new CasCEmitter(
                allDatasets,
                allElements,
                MetastoreCLIProcessor.getArgument("--dgc", args)
        );

        CasCManifest casc = cascEmitter.get();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String cascJson = objectMapper.writeValueAsString(casc);

            // Write the output to a file
            Path outputPath = MetastoreCLIProcessor.getOutputPath(args)
                    .map(path -> path.resolve("casc.json"))
                    .orElse(Path.of("casc.json"));

            Files.write(outputPath, cascJson.getBytes(StandardCharsets.UTF_8));

            System.out.println("CasC JSON written to: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


