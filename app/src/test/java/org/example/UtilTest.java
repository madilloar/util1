package org.example;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class UtilTest {

  @Test
  public void testTrimSpacesFromColumns_NormalCase() {
    List<Map<String, String>> records = List.of(
        Map.of("ITEM1", "value1 ", "ITEM2", "value2 ", "ITEM3", "value3 "),
        Map.of("ITEM1", "value4 ", "ITEM2", "value5 ", "ITEM3", "value6 "));

    List<Map<String, String>> trimmedRecords = records.stream()
        .map(Util.trimSpacesFromColumns)
        .collect(Collectors.toList());

    assertEquals("value1", trimmedRecords.get(0).get("ITEM1"));
    assertEquals("value2", trimmedRecords.get(0).get("ITEM2"));
    assertEquals("value3", trimmedRecords.get(0).get("ITEM3"));
    assertEquals("value4", trimmedRecords.get(1).get("ITEM1"));
    assertEquals("value5", trimmedRecords.get(1).get("ITEM2"));
    assertEquals("value6", trimmedRecords.get(1).get("ITEM3"));
  }

  @Test
  public void testTrimSpacesFromColumns_NullValues() {
    List<Map<String, String>> records = List.of(
        new HashMap<>() {
          {
            put("ITEM1", null);
            put("ITEM2", "value2 ");
            put("ITEM3", null);
          }
        });

    List<Map<String, String>> trimmedRecords = records.stream()
        .map(Util.trimSpacesFromColumns)
        .collect(Collectors.toList());

    assertEquals("", trimmedRecords.get(0).get("ITEM1")); // nullが空文字列に置き換えられる
    assertEquals("value2", trimmedRecords.get(0).get("ITEM2"));
    assertEquals("", trimmedRecords.get(0).get("ITEM3")); // nullが空文字列に置き換えられる
  }

  @Test
  public void testTrimSpacesFromColumns_EmptyStrings() {
    List<Map<String, String>> records = List.of(
        Map.of("ITEM1", "", "ITEM2", " ", "ITEM3", "  "));

    List<Map<String, String>> trimmedRecords = records.stream()
        .map(Util.trimSpacesFromColumns)
        .collect(Collectors.toList());

    assertEquals("", trimmedRecords.get(0).get("ITEM1"));
    assertEquals("", trimmedRecords.get(0).get("ITEM2")); // スペースがトリムされる
    assertEquals("", trimmedRecords.get(0).get("ITEM3")); // スペースがトリムされる
  }

  @Test
  public void testTrimSpacesFromColumns_EmptyList() {
    List<Map<String, String>> records = List.of();

    List<Map<String, String>> trimmedRecords = records.stream()
        .map(Util.trimSpacesFromColumns)
        .collect(Collectors.toList());
    assertTrue(trimmedRecords.isEmpty());
  }

  @Test
  public void testTrimSpacesFromColumns_EmptyMap() {
    List<Map<String, String>> records = List.of(
        Map.of());
    List<Map<String, String>> trimmedRecords = records.stream()
        .map(Util.trimSpacesFromColumns)
        .collect(Collectors.toList());

    assertTrue(trimmedRecords.get(0).isEmpty());
  }

  @Test
  public void testConvertToCommaSeparatedRecords_NormalCase() {
    List<Map<String, String>> records = List.of(
        Map.of("ITEM1", "value1", "ITEM2", "value2", "ITEM3", "value3"),
        Map.of("ITEM1", "value4", "ITEM2", "value5", "ITEM3", "value6"));

    // カラム順序
    List<String> columnOrder = List.of("ITEM1", "ITEM2", "ITEM3");

    List<String> result = records.stream()
        .map(Util.convertToCommaSeparatedRecord.apply(columnOrder))
        .collect(Collectors.toList());

    assertEquals(2, result.size());
    assertEquals("\"value1\",\"value2\",\"value3\"", result.get(0));
    assertEquals("\"value4\",\"value5\",\"value6\"", result.get(1));
  }

  @Test
  public void testConvertToCommaSeparatedRecords_NullValues() {

    List<Map<String, String>> records = List.of(
        // Map.of()を使わないのはMapのValueがnullを許容しないため
        new HashMap<>() {
          {
            put("ITEM1", "value1");

            put("ITEM2", null); // ITEM2がnull
            put("ITEM3", "value3");
          }
        },
        new HashMap<>() {
          {
            put("ITEM1", null); // ITEM1がnull
            put("ITEM2", "value5");
            put("ITEM3", null); // ITEM3がnull
          }
        });

    // カラム順序
    List<String> columnOrder = List.of("ITEM1", "ITEM2", "ITEM3");

    List<String> result = records.stream()
        .map(Util.convertToCommaSeparatedRecord.apply(columnOrder))
        .collect(Collectors.toList());

    assertEquals(2, result.size());
    assertEquals("\"value1\",\"\",\"value3\"", result.get(0)); // nullは空文字列に置き換え
    assertEquals("\"\",\"value5\",\"\"", result.get(1)); // nullは空文字列に置き換え
  }

  @Test
  public void testConvertToCommaSeparatedRecords_EmptyRecords() {
    List<Map<String, String>> records = List.of();

    // カラム順序
    List<String> columnOrder = List.of("ID", "ITEM1", "GROUP");
    List<String> result = records.stream()
        .map(Util.convertToCommaSeparatedRecord.apply(columnOrder))
        .collect(Collectors.toList());

    assertTrue(result.isEmpty()); // 空のリストの場合、結果も空
  }

  @Test
  public void testConvertToCommaSeparatedRecords_EmptyMap() {
    List<Map<String, String>> records = List.of(
        Map.of() // 空のMapを作成
    );
    // カラム順序
    List<String> columnOrder = List.of("ID", "ITEM1", "GROUP");

    List<String> result = records.stream()
        .map(Util.convertToCommaSeparatedRecord.apply(columnOrder))
        .collect(Collectors.toList());

    assertEquals(1, result.size());
    assertEquals("\"null\",\"null\",\"null\"", result.get(0)); // 空のMapは空文字列として扱われる
  }

  @Test
  public void testProcessTransactions() {
    // グループ化マスタ
    Map<String, String> groupMaster = Map.of(
        "A1", "GROUP-A",
        "A2", "GROUP-A",
        "A3", "GROUP-A",
        "B3", "GROUP-B",
        "B45", "GROUP-B");

    // フィルタセット
    Set<String> filterSet = Set.of("ID", "ITEM1", "GROUP");
    // カラム順序
    List<String> columnOrder = List.of("ID", "ITEM1", "GROUP");
    // トランザクションデータ
    List<Map<String, String>> transactions = List.of(
        Map.of("ID", "1", "ITEM1", "A1 ", "ITEM2", " value2 ", "ITEM3", " value3 "),
        Map.of("ID", "2", "ITEM1", " A4", "ITEM2", " value5 ", "ITEM3", " value6 "),
        Map.of("ID", "3", "ITEM1", "B3 ", "ITEM2", " value8 ", "ITEM3", " value9 "));

    // 関数を適用
    List<String> result = transactions.stream()
        // 各カラムのスペースをトリム
        .map(Util.trimSpacesFromColumns)
        // GROUP列を追加
        .map(Util.addGroupColumn.apply(groupMaster))
        // 列をフィルタ
        .map(Util.filterRecordByColumns.apply(filterSet))
        // カンマ区切りの文字列に変換
        .map(Util.convertToCommaSeparatedRecord.apply(columnOrder)).collect(Collectors.toList());

    // 検証
    assertEquals(3, result.size());
    assertEquals("\"1\",\"A1\",\"GROUP-A\"", result.get(0)); // レコード1
    assertEquals("\"2\",\" A4\",\" A4\"", result.get(1)); // レコード2
    assertEquals("\"3\",\"B3\",\"GROUP-B\"", result.get(2)); // レコード3
  }

  @Test
  public void testProcessTransactions2() {
    // サンプルの valuePairs
    Map<String, String> valuePairs = Map.of(
        "ITEM2", "STAT-ITEM2");

    // サンプルの conditions
    String[] conditions = new String[] { "1|3", "@" };

    // グループ化マスタ
    Map<String, String> groupMaster = Map.of(
        "A1", "GROUP-A",
        "A2", "GROUP-A",
        "A3", "GROUP-A",
        "B3", "GROUP-B",
        "B45", "GROUP-B");

    // フィルタセット
    Set<String> filterSet = Set.of("ID", "ITEM1", "ITEM2", "GROUP");

    // カラム順序
    List<String> columnOrder = List.of("ID", "ITEM1", "ITEM2", "GROUP");

    // トランザクションデータ
    List<Map<String, String>> transactions = List.of(
        Map.of("ID", "1", "ITEM1", "A1 ", "ITEM2", " value2 ", "STAT-ITEM2", "3"),
        Map.of("ID", "2", "ITEM1", " A4", "ITEM2", " value5 ", "STAT-ITEM2", "1"),
        Map.of("ID", "3", "ITEM1", "B3 ", "ITEM2", " value8 ", "STAT-ITEM2", "A"));

    // 関数を適用
    List<String> result = transactions.stream()
        // 各カラムのスペースをトリム
        .map(Util.trimSpacesFromColumns)
        // 値と状態のペアで変換
        .map(Util.transformValues
            .apply(valuePairs)
            .apply(conditions))
        // GROUP列を追加
        .map(Util.addGroupColumn.apply(groupMaster))
        // 列をフィルタ
        .map(Util.filterRecordByColumns.apply(filterSet))
        // カンマ区切りの文字列に変換
        .map(Util.convertToCommaSeparatedRecord.apply(columnOrder)).collect(Collectors.toList());

    // 検証
    assertEquals(3, result.size());
    assertEquals("\"1\",\"A1\",\"@\",\"GROUP-A\"", result.get(0)); // レコード1
    assertEquals("\"2\",\" A4\",\"@\",\" A4\"", result.get(1)); // レコード2
    assertEquals("\"3\",\"B3\",\" value8\",\"GROUP-B\"", result.get(2)); // レコード3
  }

  @Test
  public void testProcessTransactions3() {
    // サンプルの valuePairs
    Map<String, String> valuePairs = Map.of(
        "ITEM2", "STAT-ITEM2");

    // サンプルの conditions（transformValues用）
    String[] transformConditions = new String[] { "1|3", "@" };

    // サンプルの conditions（clearValuesByConditions用）
    Map<String, String> clearConditions = Map.of(
        "ITEM1", "^A.*", // ITEM1が"A"で始まる場合
        "ITEM2", "^va.+$" // ITEM2が"va"で始まり1文字以上続く場合
    );

    // グループ化マスタ
    Map<String, String> groupMaster = Map.of(
        "A1", "GROUP-A",
        "A2", "GROUP-A",
        "A3", "GROUP-A",
        "B3", "GROUP-B",
        "B45", "GROUP-B");

    // フィルタセット
    Set<String> filterSet = Set.of("ID", "ITEM1", "ITEM2", "GROUP");

    // カラム順序
    List<String> columnOrder = List.of("ID", "ITEM1", "ITEM2", "GROUP");

    // トランザクションデータ
    List<Map<String, String>> transactions = List.of(
        Map.of("ID", "1", "ITEM1", "A1 ", "ITEM2", "value2", "STAT-ITEM2", "3"),
        Map.of("ID", "2", "ITEM1", "A4", "ITEM2", "value5", "STAT-ITEM2", "1"),
        Map.of("ID", "3", "ITEM1", "B3", "ITEM2", " v8 ", "STAT-ITEM2", "A"));

    // 関数を適用
    List<String> result = transactions.stream()
        // 各カラムのスペースをトリム
        .map(Util.trimSpacesFromColumns)
        // 値と状態のペアで変換
        .map(Util.transformValues
            .apply(valuePairs)
            .apply(transformConditions))
        // 条件に基づいて値を空文字列に置き換える
        .map(Util.clearValuesByConditions.apply(clearConditions))
        // GROUP列を追加
        .map(Util.addGroupColumn.apply(groupMaster))
        // 列をフィルタ
        .map(Util.filterRecordByColumns.apply(filterSet))
        // カンマ区切りの文字列に変換
        .map(Util.convertToCommaSeparatedRecord.apply(columnOrder))
        .collect(Collectors.toList());

    // 検証
    assertEquals(3, result.size());
    assertEquals("\"1\",\"\",\"@\",\"\"", result.get(0)); // レコード1
    assertEquals("\"2\",\"\",\"@\",\"\"", result.get(1)); // レコード2
    assertEquals("\"3\",\"B3\",\" v8\",\"GROUP-B\"", result.get(2)); // レコード3
  }

  @Test
  public void testClearValuesByConditions() {
    Map<String, String> conditions = Map.of(
        "ITEM1", "^a.*",
        "ITEM2", "^va.+$");

    List<Map<String, String>> transactions = List.of(
        Map.of("ITEM1", "apple", "ITEM2", "value2", "ITEM3", "other"),
        Map.of("ITEM1", "banana", "ITEM2", "valid", "ITEM3", "other"),
        Map.of("ITEM1", "avocado", "ITEM2", "value", "ITEM3", "other"));

    List<Map<String, String>> result = transactions.stream()
        .map(Util.clearValuesByConditions.apply(conditions))
        .collect(Collectors.toList());

    // 検証
    assertEquals("", result.get(0).get("ITEM1"));
    assertEquals("", result.get(0).get("ITEM2"));
    assertEquals("other", result.get(0).get("ITEM3"));

    assertEquals("banana", result.get(1).get("ITEM1"));
    assertEquals("", result.get(1).get("ITEM2"));
    assertEquals("other", result.get(1).get("ITEM3"));

    assertEquals("", result.get(2).get("ITEM1"));
    assertEquals("", result.get(2).get("ITEM2"));
    assertEquals("other", result.get(2).get("ITEM3"));
  }

  @Test
  public void testSha256Hash() {
    String input = "test";
    String expectedHash = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

    assertEquals(expectedHash, Util.getSHA256Hash.apply(input));
  }

  @Test
  public void testTransformValues() {
    // サンプルの valuePairs
    Map<String, String> valuePairs = Map.of(
        "ITEM1", "STAT-ITEM1",
        "ITEM2", "STAT-ITEM2"); // ITEM2の状態列は存在しない

    // サンプルの conditions
    String[] conditions = new String[] { "1|3", "@" };

    // サンプルのレコード
    List<Map<String, String>> transactions = List.of(
        Map.of("ID", "1", "ITEM1", "A1 ", "ITEM2", " value2 ", "STAT-ITEM1", "3"),
        Map.of("ID", "2", "ITEM1", " A4", "ITEM2", " value5 ", "STAT-ITEM1", "1"),
        Map.of("ID", "3", "ITEM1", "B3 ", "ITEM2", " value8 ")); // STAT-ITEM1とSTAT-ITEM2が存在しない

    // 関数を適用
    List<Map<String, String>> result = transactions.stream()
        .map(Util.transformValues
            .apply(valuePairs)
            .apply(conditions))
        .collect(Collectors.toList());

    // 結果を出力
    result.forEach(System.out::println);

    // 検証
    assertEquals(3, result.size());
    assertEquals("@", result.get(0).get("ITEM1")); // 条件に一致して置換
    assertEquals(" value2 ", result.get(0).get("ITEM2")); // 状態列がないのでそのまま
    assertEquals("@", result.get(1).get("ITEM1")); // 条件に一致して置換
    assertEquals(" value5 ", result.get(1).get("ITEM2")); // 状態列がないのでそのまま
    assertEquals("B3 ", result.get(2).get("ITEM1")); // 条件に一致しないのでそのまま
    assertEquals(" value8 ", result.get(2).get("ITEM2")); // 状態列がないのでそのまま }
  }
}