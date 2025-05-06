package org.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.junit.Test;

public class FilterRecordByColumnsTest {
  /**
   * フィルタセットに基づいてレコードをフィルタリングするテスト.
   * フィルタセットに基づいてレコードをフィルタリングし、正しい結果が得られることを確認する.
   * フィルタセットに含まれるキーのみが残ることを確認する.
   * フィルタセットに含まれないキーはnullになることを確認する.
   */
  @Test
  public void testFilterRecords_NormalCase() {
    // フィルタセット
    Set<String> filterSet = Set.of("ITEM1", "ITEM2");

    // レコードデータ
    Map<String, String> record = Map.of(
        "ITEM1", "value1",
        "ITEM2", "value2",
        "ITEM3", "value3");

    // 関数を適用
    Map<String, String> filteredRecord = Util.filterRecordByColumns.apply(filterSet).apply(record);

    // 検証
    // フィルタセットに含まれるキーのみが残る
    assertEquals(2, filteredRecord.size());
    assertEquals("value1", filteredRecord.get("ITEM1"));
    assertEquals("value2", filteredRecord.get("ITEM2"));
    assertNull(filteredRecord.get("ITEM3")); // フィルタセットに含まれない
  }

  /**
   * フィルタセットに基づいてレコードをフィルタリングするテスト.
   * フィルタセットに基づいてレコードをフィルタリングし、正しい結果が得られることを確認する.
   * フィルタセットに含まれるキーのみが残ることを確認する.
   * フィルタセットに含まれないキーはnullになることを確認する.
   */
  @Test
  public void testFilterRecordByColumns_WithListOfRecords() {
    // レコードデータ
    List<Map<String, String>> records = List.of(
        Map.of("ITEM1", "value1 ", "ITEM2", " value2 ", "ITEM3", " value3 "),
        Map.of("ITEM1", " value4 ", "ITEM2", " value5 ", "ITEM3", " value6 "));

    // フィルタセット
    Set<String> filterSet = Set.of("ITEM1", "ITEM2");

    // 各レコードに対してフィルタリングを適用
    List<Map<String, String>> filteredRecords = records.stream()
        .map(Util.filterRecordByColumns.apply(filterSet))
        .collect(Collectors.toList());

    // 検証
    assertEquals(2, filteredRecords.size());

    // レコード1の検証
    // フィルタセットに含まれるキーのみが残る
    assertEquals(2, filteredRecords.get(0).size());
    assertEquals("value1 ", filteredRecords.get(0).get("ITEM1"));
    assertEquals(" value2 ", filteredRecords.get(0).get("ITEM2"));
    assertNull(filteredRecords.get(0).get("ITEM3")); // フィルタセットに含まれない

    // レコード2の検証
    // フィルタセットに含まれるキーのみが残る
    assertEquals(2, filteredRecords.get(1).size());
    assertEquals(" value4 ", filteredRecords.get(1).get("ITEM1"));
    assertEquals(" value5 ", filteredRecords.get(1).get("ITEM2"));
    assertNull(filteredRecords.get(1).get("ITEM3")); // フィルタセットに含まれない
  }

  /**
   * 空のレコードデータのテスト.
   * 空のレコードデータに対してフィルタリングを適用した場合、結果も空になることを確認する.
   */
  @Test
  public void testEmptyRecord() {
    // フィルタセット
    Set<String> filterSet = Set.of("ITEM1", "ITEM2");

    // 空のレコードデータ
    Map<String, String> record = Map.of();

    // 関数を適用
    Map<String, String> filteredRecord = Util.filterRecordByColumns.apply(filterSet).apply(record);

    // 検証
    // レコードが空なので、フィルタリング後も空
    assertEquals(0, filteredRecord.size());
  }

  /**
   * フィルタセットが空の場合のテスト.
   * フィルタセットが空の場合、フィルタリング後のレコードは空になることを確認する.
   */
  @Test
  public void testEmptyFilter() {
    // 空のフィルタセット
    Set<String> filterSet = Set.of();

    // レコードデータ
    Map<String, String> record = Map.of(
        "ITEM1", "value1",
        "ITEM2", "value2",
        "ITEM3", "value3");

    // 関数を適用
    Map<String, String> filteredRecord = Util.filterRecordByColumns.apply(filterSet).apply(record);

    filteredRecord.entrySet().forEach(entry -> {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    });

    // 検証
    // フィルタセットが空なので、フィルタリング後のレコードは空になる
    assertEquals(0, filteredRecord.size());
  }

  /**
   * フィルタセットに含まれるキーがレコードに存在しない場合のテスト.
   * フィルタセットに含まれるキーがレコードに存在しない場合、フィルタリング後のレコードは空になることを確認する.
   */
  @Test
  public void testFilterRecordByColumns_NoMatchingKeys() {
    // フィルタセット
    Set<String> filterSet = Set.of("ITEM4", "ITEM5");

    // レコードデータ
    Map<String, String> record = Map.of(
        "ITEM1", "value1",
        "ITEM2", "value2",
        "ITEM3", "value3");

    // 関数を適用
    Map<String, String> filteredRecord = Util.filterRecordByColumns.apply(filterSet).apply(record);

    // 検証
    // フィルタセットに含まれるキーがないため、フィルタリング後のレコードは空になる
    assertEquals(0, filteredRecord.size());
  }

  /**
   * フィルタセットにnullが含まれる場合のテスト.
   * フィルタセットにnullが含まれる場合、フィルタリング後のレコードはnullになることを確認する.
   */
  @Test
  public void testFilterRecordByColumns_FilterNull() {
    // フィルタセット
    Set<String> filterSet = new HashSet<>(Arrays.asList(null, "ITEM1"));

    // レコードデータ
    Map<String, String> record = Map.of(
        "ITEM1", "value1",
        "ITEM2", "value2",
        "ITEM3", "value3");

    // 関数を適用
    Map<String, String> filteredRecord = Util.filterRecordByColumns.apply(filterSet).apply(record);

    // 検証
    assertEquals(1, filteredRecord.size());
    assertEquals("value1", filteredRecord.get("ITEM1"));
  }

  @Test
  public void testSetNull() {
    // フィルタセット
    Set<String> filterSet = new HashSet<>(Arrays.asList(null, "ITEM1"));
    assertTrue(filterSet.contains(null));
    assertTrue(filterSet.contains("ITEM1"));
  }

}
