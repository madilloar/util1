package org.example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {
  /**
   * レコードの各カラムのスペースをトリムする関数.
   * 
   * @return スペースをトリムしたレコード
   */
  public static final Function<Map<String, String>, Map<String, String>> trimSpacesFromColumns = record -> record
      .entrySet().stream().collect(
          Collectors.toMap(Map.Entry::getKey,
              entry -> entry.getValue() != null ? entry.getValue().stripTrailing() : "" // null値を空文字列に置き換え、スペースをトリム
          ));

  /**
   * 項目名と状態項目名のペアに基づいて値を変換する関数.
   * 
   * @param valuePairs 項目名と状態項目名のペア (例: {"項目A": "状態-項目A", ...})
   * @param conditions 条件 (例: {"1|3": "@"})
   * @return 変換後のレコード
   */
  public static final Function<Map<String, String>, Function<String[], Function<Map<String, String>, Map<String, String>>>> transformValues = valuePairs -> conditions -> record -> {
    return record.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> {
              String columnName = entry.getKey();
              String columnValue = entry.getValue();

              // valuePairsに対応する状態列がある場合のみ変換を試みる
              if (valuePairs.containsKey(columnName)) {
                String stateColumnName = valuePairs.get(columnName);
                String stateValue = record.get(stateColumnName);

                // conditionsは2要素の配列という前提
                String pattern = conditions[0];
                String replacement = conditions[1];

                // stateValueがnullの場合や条件に一致しない場合は元の値を返す
                if (stateValue != null && Pattern.matches(pattern, stateValue)) {
                  return replacement;
                }
              }

              // 変換対象でない場合は元の値をそのまま返す
              return columnValue;
            }));
  };

  /**
   * 条件に基づいて値を空文字列に置き換える関数.
   * 
   * @param conditions 条件 (例: {"ITEM1": "^[0-9]+$","ITEM2":"^a$"...})
   * @return 変換後のレコード
   * @param record トランザクションレコード
   * @return 条件に基づいて値を空文字列に置き換えたレコード
   */
  public static final Function<Map<String, String>, Function<Map<String, String>, Map<String, String>>> clearValuesByConditions = conditions -> record -> {
    return record.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> {
              String columnName = entry.getKey();
              String columnValue = entry.getValue();

              // conditionsに項目名が含まれている場合のみチェック
              if (conditions.containsKey(columnName)) {
                String pattern = conditions.get(columnName);

                // 正規表現にマッチした場合、値を空文字列に置き換える
                if (columnValue != null && columnValue.matches(pattern)) {
                  return "";
                }
              }

              // 条件に一致しない場合は元の値をそのまま返す
              return columnValue;
            }));
  };
  /**
   * グループ化マスタを追加する関数.
   * 
   * @param groupMaster グループ化マスタ
   * @param record      トランザクションレコード
   * @return グループ列を追加したトランザクションレコード
   */
  public static final Function<Map<String, String>, Function<Map<String, String>, Map<String, String>>> addGroupColumn = groupMaster -> record -> {
    // オリジナルのレコードに列を追加するため、変更可能なMapを作成
    Map<String, String> mutableTransaction = new HashMap<>(record);

    // ITEM1の値を取得
    // TODO:本当の名前に変更すること
    String item1 = mutableTransaction.get("ITEM1");

    // グループ化マスタからグループを取得（ヒットしない場合はITEM1の値を使用）
    String group = groupMaster.getOrDefault(item1, item1);

    // トランザクションデータにGROUP列を追加
    // TODO:本当の名前に変更すること
    mutableTransaction.put("GROUP", group);

    return mutableTransaction;
  };

  /**
   * レコードから必要な列だけを残す関数.
   * 
   * @param filterSet 必要な列を定義したフィルタセット
   * @param record    トランザクションレコード
   * @return 必要な列だけを残したトランザクションレコード
   */
  public static final Function<Set<String>, Function<Map<String, String>, Map<String, String>>> filterRecordByColumns = filterSet -> record -> record
      .entrySet().stream().filter(entry -> filterSet.contains(entry.getKey())) // フィルタセットに含まれるキーのみ
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // 結果をMapに収集

  /**
   * レコードをカンマ区切りの文字列に変換する関数.
   * 
   * @param columnOrder カラム順序
   * @param record      トランザクションレコード
   * @return カンマ区切りの文字列
   */
  public static final Function<List<String>, Function<Map<String, String>, String>> convertToCommaSeparatedRecord = columnOrder -> record -> columnOrder
      .stream().map(key -> "\"" + (record.getOrDefault(key, "") != null ? record.get(key) : "") + "\"")
      .collect(Collectors.joining(","));

  /**
   * SHA256ハッシュを計算するメソッド. ビット数:256 バイト数:32 16進数文字列に変換するので64文字の16進数文字列に変換される
   * 
   * @throws NoSuchAlgorithmException SHA256アルゴリズムが見つからない場合
   * @param input 入力文字列
   * @return SHA256ハッシュ値
   */
  public static final Function<String, String> getSHA256Hash = input -> {
    try {
      // MessageDigestインスタンスを取得
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      // 入力文字列をバイト配列に変換してハッシュを計算
      byte[] hashBytes = digest.digest(input.getBytes());

      // ハッシュ値を16進数文字列に変換
      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        // バイトを符号なし整数に変換している
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0'); // 1桁の場合は先頭に0を追加
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256アルゴリズムが見つかりません", e);
    }
  };
}
