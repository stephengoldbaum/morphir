package datathread;

public class DataThreadUtils {
    public String escapeToDataThread(String s) {
        return s;
//        return s.replaceAll(" ", "_")
//                .replaceAll("&", "_and_")
//                .replaceAll(",", "_et_");
    }

    public String escapeFromDataThread(String s) {
        return s;
//        return s.replaceAll("_and_", "&")
//                .replaceAll("_", " ")
//                .replaceAll("_et_", ",");
    }
}
