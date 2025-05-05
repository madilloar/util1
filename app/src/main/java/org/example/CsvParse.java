package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.Gson;

public class CsvParse {
  public class Config {
    private List<ValuePair> valuePairs;
    private Condition condition;

    // Getter and Setter for valuePairs
    public List<ValuePair> getValuePairs() {
      return valuePairs;
    }

    public void setValuePairs(List<ValuePair> valuePairs) {
      this.valuePairs = valuePairs;
    }

    // Getter and Setter for condition
    public Condition getCondition() {
      return condition;
    }

    public void setCondition(Condition condition) {
      this.condition = condition;
    }

    // Nested class for ValuePair
    public static class ValuePair {
      private String item;
      private String state;

      // Getter and Setter for item
      public String getItem() {
        return item;
      }

      public void setItem(String item) {
        this.item = item;
      }

      // Getter and Setter for state
      public String getState() {
        return state;
      }

      public void setState(String state) {
        this.state = state;
      }
    }

    // Nested class for Condition
    public static class Condition {
      private String pattern;
      private String replacement;

      // Getter and Setter for pattern
      public String getPattern() {
        return pattern;
      }

      public void setPattern(String pattern) {
        this.pattern = pattern;
      }

      // Getter and Setter for replacement
      public String getReplacement() {
        return replacement;
      }

      public void setReplacement(String replacement) {
        this.replacement = replacement;
      }
    }
  }

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

  public static void processCsv(String inputFilePath, String outputFilePath, Config config) throws IOException {
    // CSVファイルを読み込む
    try (Reader reader = new InputStreamReader(
        CsvParse.class.getClassLoader().getResourceAsStream(inputFilePath), StandardCharsets.UTF_8);
        CSVParser csvParser = new CSVParser(reader,
            CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

      // ヘッダーとレコードを取得
      List<String> headers = csvParser.getHeaderNames();
      List<Map<String, String>> records = csvParser.getRecords().stream()
          .map(record -> headers.stream()
              .collect(Collectors.toMap(header -> header, record::get)))
          .collect(Collectors.toList());

      // 出力用のヘッダーを準備
      // 一旦inputと同じカラム分出力するとした。
      List<String> newHeaders = headers;

      // Utilクラスの関数を準備
      Map<String, String> valuePairs = config.getValuePairs().stream()
          .collect(Collectors.toMap(Config.ValuePair::getItem, Config.ValuePair::getState));
      String[] transformConditions = { config.getCondition().getPattern(), config.getCondition().getReplacement() };

      // レコードを処理
      List<List<String>> newRecords = records.stream()
          // transformValuesを適用
          .map(Util.transformValues.apply(valuePairs).apply(transformConditions))
          // transformedRecordを利用して新しい行を作成
          .map(transformedRecord -> newHeaders.stream()
              // newHeadersで定義したカラムだけを新しいレコードにするためにgetしている
              .map(transformedRecord::get)
              .collect(Collectors.toList()))
          .collect(Collectors.toList()); // CSVファイルを書き出す

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