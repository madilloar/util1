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
                assertEquals(2, filteredRecords.get(0).size());
                assertEquals("value1 ", filteredRecords.get(0).get("ITEM1"));
                assertEquals(" value2 ", filteredRecords.get(0).get("ITEM2"));
                assertNull(filteredRecords.get(0).get("ITEM3")); // フィルタセットに含まれない

                // レコード2の検証
                assertEquals(2, filteredRecords.get(1).size());
                assertEquals(" value4 ", filteredRecords.get(1).get("ITEM1"));
                assertEquals(" value5 ", filteredRecords.get(1).get("ITEM2"));
                assertNull(filteredRecords.get(1).get("ITEM3")); // フィルタセットに含まれない
        }

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
                assertEquals(2, filteredRecord.size());
                assertEquals("value1", filteredRecord.get("ITEM1"));
                assertEquals("value2", filteredRecord.get("ITEM2"));
        }

        @Test
        public void testEmptyRecord() {
                // フィルタセット
                Set<String> filterSet = Set.of("ITEM1", "ITEM2");

                // 空のレコードデータ
                Map<String, String> record = Map.of();

                // 関数を適用
                Map<String, String> filteredRecord = Util.filterRecordByColumns.apply(filterSet).apply(record);

                // 検証
                assertEquals(0, filteredRecord.size());
        }

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

                // 検証
                assertEquals(0, filteredRecord.size());
        }

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
                assertEquals(0, filteredRecord.size());
        }

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
                List<String> result = Util.processTransactions
                                .apply(groupMaster)
                                .apply(filterSet)
                                .apply(columnOrder)
                                .apply(transactions.stream());

                // 検証
                assertEquals(3, result.size());
                assertEquals("\"1\",\"A1\",\"GROUP-A\"", result.get(0)); // レコード1
                assertEquals("\"2\",\" A4\",\" A4\"", result.get(1)); // レコード2
                assertEquals("\"3\",\"B3\",\"GROUP-B\"", result.get(2)); // レコード3
        }

        @Test
        public void testSha256Hash() {
                String input = "test";
                String expectedHash = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

                assertEquals(expectedHash, Util.getSHA256Hash.apply(input));
        }

}