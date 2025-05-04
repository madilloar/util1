package org.example;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class AddGroupColumnTest {

    @Test
    public void testAddGroupColumn() {
        // グループ化マスタ
        Map<String, String> groupMaster = Map.of(
                "A1", "GROUP-A",
                "A2", "GROUP-A",
                "A3", "GROUP-A",
                "B3", "GROUP-B",
                "B45", "GROUP-B");

        // トランザクションデータ
        List<Map<String, String>> transactions = List.of(
                Map.of("ID", "1", "ITEM1", "A1"),
                Map.of("ID", "2", "ITEM1", "A4"),
                Map.of("ID", "3", "ITEM1", "B3"));

        // 関数を適用
        List<Map<String, String>> result = transactions.stream()
                .map(Util.addGroupColumn.apply(groupMaster)) // 修正後のaddGroupColumn関数を適用
                .toList();
        // 検証
        assertEquals(3, result.size());

        // レコード1の検証
        assertEquals("1", result.get(0).get("ID"));
        assertEquals("A1", result.get(0).get("ITEM1"));
        assertEquals("GROUP-A", result.get(0).get("GROUP"));

        // レコード2の検証
        assertEquals("2", result.get(1).get("ID"));
        assertEquals("A4", result.get(1).get("ITEM1"));
        assertEquals("A4", result.get(1).get("GROUP")); // グループ化マスタにヒットしない場合はITEM1の値を使用

        // レコード3の検証
        assertEquals("3", result.get(2).get("ID"));
        assertEquals("B3", result.get(2).get("ITEM1"));
        assertEquals("GROUP-B", result.get(2).get("GROUP"));
    }
}