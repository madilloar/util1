package org.example;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MyBatisTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
  }

  @Rule
  public TestName testName = new TestName(); // JUnitのTestNameルール

  @Before
  public void setupTestData() throws Exception {
    // MyBatis設定ファイルを読み込む
    try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    try (SqlSession session = sqlSessionFactory.openSession()) {
      // データベースをリセット
      session.getConnection().createStatement().execute("DROP ALL OBJECTS");

      // 基本スキーマをロード
      executeSqlFile(session, "schema_base.sql");

      // テストケースごとのデータをロード
      String testDataFile = getTestDataFileForCurrentTest();
      if (testDataFile != null) {
        executeSqlFile(session, testDataFile);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void executeSqlFile(SqlSession session, String fileName) throws Exception {
    String sql;
    try (BufferedReader bufferedReader = new BufferedReader(Resources.getResourceAsReader(fileName))) {
      sql = bufferedReader.lines().collect(Collectors.joining("\n"));
    }
    session.getConnection().createStatement().execute(sql);
  }

  private String getTestDataFileForCurrentTest() {
    // 現在のテストメソッド名を取得
    String testName = this.testName.getMethodName();

    // テストメソッド名に応じてデータファイルを返す
    switch (testName) {
      case "testProcessTransactions":
        return "data_case1.sql";
      case "testFindTransactionByConditions":
        return "data_case2.sql";
      default:
        return null; // デフォルトではデータファイルを使用しない
    }
  }

  /**
   * エンティティクラスを作りそれにマッピングする
   */
  @Test
  public void testSelectAllUsers() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      List<User> users = session.selectList("org.example.Mapper.selectAllUsers");
      assertEquals(3, users.size());
      assertEquals("Alice", users.get(0).getUserName());
    }
  }

  /**
   * List<Map<String,String>>にマッピングする
   */
  @Test
  public void testSelectAllUsersAsList() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      List<Map<String, String>> users = session.selectList("org.example.Mapper.selectAllUsersAsMap");
      assertEquals(3, users.size());
      assertEquals("Alice", users.get(0).get("USER_NAME")); // カラム名は大文字で返されることが多い
    }
  }

  /**
   * List<Map<String,String>>にマッピングする
   * カラム名が日本語のViewの場合は日本語のカラム名でgetできること
   */
  @Test
  public void testSelectAllFromUserView() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      List<Map<String, Object>> users = session.selectList("org.example.Mapper.selectAllFromUserView");
      assertEquals(3, users.size());
      assertEquals("Alice", users.get(0).get("名前")); // 日本語のカラム名をキーとして使用
      assertEquals(1, users.get(0).get("ユーザーID")); // IDも確認
    }
  }

  @Test
  public void testSelectAllUsersAsMap() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Map<Integer, Map<Object, Object>> users = session.selectMap("org.example.Mapper.selectAllUsersAsMap",
          "USER_ID");
      assertEquals(3, users.size());

      users.forEach((key, value) -> {
        System.out.println("Key: " + key + ", Value: " + value);
      });

      assertEquals("Alice", users.get(1).get("USER_NAME"));
    }
  }

  /**
   * `processTransactions`関数をテストする
   */
  @Test
  public void testProcessTransactions() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      // グループ化マスタを取得
      @SuppressWarnings("unchecked")
      Map<String, String> groupMaster = session.selectList("org.example.Mapper.selectGroupMaster").stream()
          .map(record -> (Map<String, String>) record)
          .collect(Collectors.toMap(
              record -> (String) record.get("ITEM1"),
              record -> (String) record.get("GROUP_NAME")));

      // フィルタセットを取得
      Set<String> filterSet = session.selectList("org.example.Mapper.selectColumnFilters").stream()
          .map(Object::toString)
          .collect(Collectors.toSet());

      // カラム順序
      List<String> columnOrder = List.of("ID", "ITEM1", "GROUP");

      // トランザクションデータを取得
      List<Map<String, String>> transactions = session.selectList("org.example.Mapper.selectAllTransactions");

      // 一連の関数を適用
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
  }

  @Test
  public void testSelectAllFromCommonCodeView() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Map<String, Map<String, Object>> commonCodeMap = session.selectMap(
          "org.example.Mapper.selectAllFromCommonCodeView",
          "ハッシュ");
      commonCodeMap.forEach((key, value) -> {
        System.out.println("Key: " + key + ", Value: " + value);
      });
      System.out.println(commonCodeMap.get(Util.getSHA256Hash.apply("abc")));
      System.out.println(commonCodeMap.get(Util.getSHA256Hash.apply("def")));
      System.out.println(commonCodeMap.get(Util.getSHA256Hash.apply("def1")));
    }
  }

  /**
   * 条件に基づいてトランザクションを検索する
   * <foreach collection="conditions" item="condition" separator="AND">のテスト
   */
  @Test
  public void testFindTransactionByConditions() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      List<Map<String, String>> conditions = new ArrayList<>();
      conditions.add(Map.of("searchExpression", "ITEM1 LIKE 'A%'"));
      conditions.add(Map.of("searchExpression", "ITEM2 LIKE 'value%'"));
      // <foreach collection="conditions" item="condition"
      // separator="AND">の「collection="conditions"」が期待している
      // "conditions" というキーのMapで渡す
      List<Map<String, String>> records = session.selectList("org.example.Mapper.findTransactionByConditions",
          Map.of("conditions", conditions));

      List<String> columnOrder = List.of("ID", "ITEM1", "ITEM2", "ITEM3", "ITEM4", "ITEM5", "ITEM6");
      records.stream().forEach(record -> {
        System.out.print("{");

        String jsonContent = columnOrder.stream()
            .map(key -> "\"" + key + "\":\"" + record.getOrDefault(key, "") + "\"")
            .collect(Collectors.joining(", "));

        System.out.print(jsonContent);
        System.out.println("}");
      });
    }
  }
}
