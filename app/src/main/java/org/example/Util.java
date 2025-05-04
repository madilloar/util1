package org.example;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {
        /**
         * レコードの各カラムのスペースをトリムする関数.
         * 
         * @return スペースをトリムしたレコード
         */
        public static final Function<Map<String, String>, Map<String, String>> trimSpacesFromColumns = record -> record
                        .entrySet().stream()
                        .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue() != null ? entry.getValue().stripTrailing() : "" // null値を空文字列に置き換え、スペースをトリム
                        ));

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
        public static Function<Set<String>, Function<Map<String, String>, Map<String, String>>> filterRecordByColumns = filterSet -> record -> record
                        .entrySet().stream()
                        .filter(entry -> filterSet.contains(entry.getKey())) // フィルタセットに含まれるキーのみ
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // 結果をMapに収集

        /**
         * レコードをカンマ区切りの文字列に変換する関数.
         * 
         * @param columnOrder カラム順序
         * @param record      トランザクションレコード
         * @return カンマ区切りの文字列
         */
        public static final Function<List<String>, Function<Map<String, String>, String>> convertToCommaSeparatedRecord = columnOrder -> record -> columnOrder
                        .stream()
                        .map(key -> "\"" + (record.getOrDefault(key, "") != null ? record.get(key) : "") + "\"")
                        .collect(Collectors.joining(","));

        /**
         * トランザクションを処理する関数.
         * 
         * @param groupMaster       グループ化マスタ
         * @param filterSet         フィルタセット
         * @param columnOrder       カラム順序
         * @param transactionStream トランザクションレコードのストリーム
         * @return 処理されたトランザクションのリスト
         */
        public static Function<Map<String, String>, Function<Set<String>, Function<List<String>, Function<Stream<Map<String, String>>, List<String>>>>> processTransactions = groupMaster -> filterSet -> columnOrder -> transactionStream -> transactionStream
                        // 各カラムのスペースをトリム
                        .map(Util.trimSpacesFromColumns)
                        // GROUP列を追加
                        .map(Util.addGroupColumn.apply(groupMaster))
                        // 列をフィルタ
                        .map(Util.filterRecordByColumns.apply(filterSet))
                        // カンマ区切りの文字列に変換
                        .map(Util.convertToCommaSeparatedRecord.apply(columnOrder))
                        .collect(Collectors.toList());

        /**
         * SHA256ハッシュを計算するメソッド.
         * ビット数:256
         * バイト数:32
         * 16進数文字列に変換するので64文字の16進数文字列に変換される
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