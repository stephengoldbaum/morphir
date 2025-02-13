package datathread.metastore;

import datathread.Identifier;
import datathread.grammar.Element;
import datathread.grammar.ElementType;
import datathread.grammar.Elements;

public class TestUtils {
    public static Element basicTextElement(String name, String domain) {
        Identifier id = new Identifier("element", new String[]{domain}, name);
        Element element = new Element();
        ElementType elementType = new Elements.Text();

        return element;
    }
}
