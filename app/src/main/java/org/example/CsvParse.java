package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.Gson;

public class CsvParse {

    public static void main(String[] args) {
        String inputFilePath = "input.csv"; // 入力CSVファイルのパス
        String outputFilePath = "output.csv"; // 出力CSVファイルのパス
        String configFilePath = "config.json"; // 設定JSONファイルのパス

        try {
            // 設定をJSONファイルから読み込む
            Config config = loadConfig(configFilePath);

            // CSVファイルを処理
            processCsv(inputFilePath, outputFilePath, config);
            System.out.println("CSVファイルの処理が完了しました。出力ファイル: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("CSVファイルの処理中にエラーが発生しました: " + e.getMessage());
        }
    }

    /**
     * 設定をJSONファイルから読み込む
     */
    public static Config loadConfig(String filePath) throws IOException {
        try (Reader reader = new InputStreamReader(
                CsvParse.class.getClassLoader().getResourceAsStream(filePath), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, Config.class);
        }
    }

    /**
     * CSVファイルを処理
     */
    public static void processCsv(String inputFilePath, String outputFilePath, Config config) throws IOException {
        // CSVファイルを読み込む
        try (Reader reader = new InputStreamReader(
                CsvParse.class.getClassLoader().getResourceAsStream(inputFilePath), StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // ヘッダーとレコードを取得
            List<String> headers = csvParser.getHeaderNames();
            List<CSVRecord> records = csvParser.getRecords();

            // 出力用のヘッダーを準備
            List<String> newHeaders = config.getValuePairs().stream()
                    .map(ValuePair::getItem)
                    .collect(Collectors.toList());
            newHeaders.addAll(config.getValuePairs().stream()
                    .map(ValuePair::getState)
                    .collect(Collectors.toList()));

            // レコードを処理
            List<List<String>> newRecords = records.stream()
                    .map(record -> {
                        // 各レコードを処理して新しい行を作成
                        List<String> newRow = config.getValuePairs().stream()
                                .map(pair -> {
                                    String itemValue = record.get(pair.getItem());
                                    String stateValue = record.get(pair.getState());
                                    // 条件に基づいて値を置き換える
                                    return config.getConditions().stream()
                                            .filter(condition -> Pattern.matches(condition.getPattern(), stateValue))
                                            .findFirst()
                                            .map(Condition::getReplacement)
                                            .orElse(itemValue);
                                })
                                .collect(Collectors.toList());

                        // 状態列をそのまま追加
                        newRow.addAll(config.getValuePairs().stream()
                                .map(pair -> record.get(pair.getState()))
                                .collect(Collectors.toList()));

                        return newRow;
                    })
                    .collect(Collectors.toList());

            // CSVファイルを書き出す
            try (Writer writer = new FileWriter(outputFilePath);
                    CSVPrinter csvPrinter = new CSVPrinter(writer,
                            CSVFormat.DEFAULT.withHeader(newHeaders.toArray(new String[0])))) {

                newRecords.forEach(row -> {
                    try {
                        csvPrinter.printRecord(row);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
    }
}

/**
 * 設定クラス
 */
class Config {
    private List<ValuePair> valuePairs;
    private List<Condition> conditions;

    public List<ValuePair> getValuePairs() {
        return valuePairs;
    }

    public List<Condition> getConditions() {
        return conditions;
    }
}

/**
 * 項目名と状態項目名のペア
 */
class ValuePair {
    private String item;
    private String state;

    public String getItem() {
        return item;
    }

    public String getState() {
        return state;
    }
}

/**
 * 条件クラス
 */
class Condition {
    private String pattern;
    private String replacement;

    public String getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }
}