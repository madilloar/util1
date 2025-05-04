package org.example;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import com.google.gson.Gson;

public class FunctionalJsonRegexMatcher {
    public static void main(String[] args) throws IOException {
        FunctionalJsonRegexMatcher matcher = new FunctionalJsonRegexMatcher();
        matcher.execute();
    }

    public static BiFunction<String, RegexPattern[], String> regexMatcher = (transaction, regexPatterns) -> Arrays
            .stream(regexPatterns)
            .filter(regexPattern -> Arrays.stream(regexPattern.getPatterns())
                    .anyMatch(pattern -> Pattern.compile(pattern).matcher(transaction).find()))
            .map(RegexPattern::getResult)
            .findFirst()
            .orElse(null);

    public void execute() throws IOException {
        Gson gson = new Gson();
        final RegexPattern[] regexPatterns;
        try (var reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("regex_patterns.json"), StandardCharsets.UTF_8)) {
            regexPatterns = gson.fromJson(reader, RegexPattern[].class);
        }

        // トランザクションデータ
        String[] transactions = { "a2", "100", "cc" };

        // JSONデータを確認
        System.out.println("Parsed RegexPatterns:");
        for (RegexPattern regexPattern : regexPatterns) {
            System.out.println("Result: " + regexPattern.getResult());
            System.out.println("Patterns: " + Arrays.toString(regexPattern.getPatterns()));
        }

        // 正規表現テスト
        var matchedResults = Arrays.stream(transactions)
                .map(transaction -> new String[] { transaction, regexMatcher.apply(transaction, regexPatterns) })
                .toList();

        // 結果表示
        matchedResults.forEach(result -> System.out.println("[" + result[0] + ", " + result[1] + "]"));
    }
}
