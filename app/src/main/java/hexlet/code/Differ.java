package hexlet.code;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

public class Differ {
    public static String generate(String filepath1, String filepath2) throws IOException {
        return generate(filepath1, filepath2, "stylish");
    }

    public static String generate(String filepath1, String filepath2, String format) throws IOException {
        if (!checkingParameters(filepath1, filepath2)) {
            return "";
        }
        // формируем мапы из входных json-файлов
        Map<String, Object> mapFromFile1 = Parser.readFileToMap(filepath1);
        Map<String, Object> mapFromFile2 = Parser.readFileToMap(filepath2);
        // формируем общий список ключей
        TreeSet<String> keys = new TreeSet<>(mapFromFile1.keySet());
        keys.addAll(mapFromFile2.keySet());
        // формируем мапу различий
        Map<String, Object> resultMapDiff = new LinkedHashMap<>();
        for (String key: keys) {
            Map<String, Object> oneRecord = new LinkedHashMap<>();
            oneRecord = generateDiff(key, mapFromFile1, mapFromFile2);
            resultMapDiff.putAll(oneRecord);
        }
        String resultString = toString(resultMapDiff);
        return resultString;
    }

    public static boolean checkParameterFile(String file) throws IOException {
        // проверка файла, переданного в параметре
        if (file.isEmpty()) {
            System.out.println("Путь к файлу пуст");
            return false;
        }
        Path pathForFile = Paths.get(file);
        if (!Files.exists(pathForFile)) {
            System.out.println("Файл не существует");
            return false;
        }
        if (!Files.isReadable(pathForFile)) {
            System.out.println("Файл недоступен для чтения");
            return false;
        }
        if (Files.isDirectory(pathForFile)) {
            System.out.println("Это каталог, а не файл!");
            return false;
        }
        return true;
    }
//
//    public static Map<String, Object> readFileToMap(String filepath) throws IOException {
//        // читаем файл и формируем из его данных структуру Map
//        Path pathForFile = Paths.get(filepath);
//        File inFile = pathForFile.toFile();
//        String extensionFile = getExtensionFile(filepath);
//        switch (extensionFile) {
//            case ("json"), ("JSON"):
//                ObjectMapper mapperJson = new ObjectMapper();
//                return mapperJson.readValue(inFile, Map.class);
//            case ("yml"), ("YML"):
//                ObjectMapper mapperYml = new YAMLMapper();
//                return mapperYml.readValue(inFile, Map.class);
//            default:
//                throw new IllegalStateException("Unexpected value: " + extensionFile);
//        }
//    }

    public static Map<String, Object> generateDiff(String keyMap, Map<String, Object> map1, Map<String, Object> map2) {
        // генерируем одну запись различий для конкретного ключа, передаваемого в качестве параметра (или две записи)
        Map<String, Object> diffMap = new LinkedHashMap<>();
        Object valueMap = map1.get(keyMap);
        String newKeyMap = "";
        if (map1.keySet().contains(keyMap) && (!map2.keySet().contains(keyMap))) {
            // есть в первом файле и нет во втором, то есть ключ удален: -
            newKeyMap = "- " + keyMap;
            valueMap = map1.get(keyMap);
        }
        if (!map1.keySet().contains(keyMap) && (map2.keySet().contains(keyMap))) {
            // нет в первом файле, есть во втором? то есть ключ добавлен: +
            newKeyMap = "+ " + keyMap;
            valueMap = map2.get(keyMap);
        }
        if (map1.keySet().contains(keyMap) && (map2.keySet().contains(keyMap))) {
            // ключ есть и в первом, и во втором файле, надо сравнивать значения
            return generateDiff2(keyMap, map1, map2);
        }
        diffMap.put(newKeyMap, valueMap);
        return diffMap;
    }

    public static Map<String, Object> generateDiff2(String keyMap, Map<String, Object> map1, Map<String, Object> map2) {
        Map<String, Object> diffMap2 = new LinkedHashMap<>();
        Object valueMap = map1.get(keyMap);
        String newKeyMap = "";
        // ключ есть и в первом, и во втором файле, надо сравнивать значения
        valueMap = map1.get(keyMap);
        Object valueMap2 = map2.get(keyMap);
        // необходимо проверить значения обоих ключей на null
        if ((valueMap == null) || (valueMap2 == null) || (!valueMap.equals(valueMap2))) {
            String key1 = "- " + keyMap;
            String key2 = "+ " + keyMap;
            diffMap2.put(key1, valueMap);
            diffMap2.put(key2, valueMap2);
        } else {
            newKeyMap = "  " + keyMap;
            diffMap2.put(newKeyMap, valueMap);
        }
        return diffMap2;
    }


    public static String toString(Map<String, Object> inMap) {
        if (inMap.isEmpty()) {
            return "";
        }
        StringBuilder rezult = new StringBuilder();
        rezult.append("{\n");
        for (Map.Entry<String, Object> record: inMap.entrySet()) {
            String key = record.getKey();
            Object value = record.getValue();
            rezult.append("  " + key);
            if (value == null) {
                rezult.append(": null"  + "\n");
            } else {
                rezult.append(": " + value.toString() + "\n");
            }
        }
        rezult.append("}");
        return rezult.toString();
    }

    public static String getExtensionFile(String pathFile) {
        Path pathForFile = Paths.get(pathFile);
        File inFile = pathForFile.toFile();
        String nameFile = inFile.getName();
        int i = nameFile.lastIndexOf(".");
        String extension = i > 0 ? nameFile.substring(i + 1) : "";
        return extension;
    }

    public static boolean checkingParameters(String file1, String file2) throws IOException {
        // сначала проверим параметры как файлы по отдельности (что они существуют и доступны для чтения)
        if (!checkParameterFile(file1) || (!checkParameterFile(file2))) {
            return false;
        }
        // проверим, что сравниваем разные файлы
        if (file1.equals(file2)) {
            System.out.println("В обоих параметрах один и тот же файл!");
            return false;
        }
        // узнать расширение файла
        String extensionFile1 = getExtensionFile(file1);
        String extensionFile2 = getExtensionFile(file2);
        // проверим, что файлы имеют одинаковое расширение
        if (!extensionFile1.equals(extensionFile2)) {
            System.out.println("Файлы с разными расширениями (разного типа) !");
            return false;
        }
        return true;
    }
}
