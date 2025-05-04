package org.example;

import static org.junit.Assert.*;

import org.junit.Test;

public class FunctionalJsonRegexMacherTest {
    @Test
    public void testRegexMatcher() {
        // テスト用のRegexPatternデータを作成
        RegexPattern[] patterns = {
                new RegexPattern(new String[] { "^a", "b$" }, "match1"),
                new RegexPattern(new String[] { "^c" }, "match2")
        };

        // テストケース1: "a2" は "^a" にマッチする
        assertEquals("match1", FunctionalJsonRegexMatcher.regexMatcher.apply("a2", patterns));

        // テストケース2: "100" はどのパターンにもマッチしない
        assertNull(FunctionalJsonRegexMatcher.regexMatcher.apply("100", patterns));

        // テストケース3: "cc" は "^c" にマッチする
        assertEquals("match2", FunctionalJsonRegexMatcher.regexMatcher.apply("cc", patterns));

        // テストケース4: "b" は "b$" にマッチする
        assertEquals("match1", FunctionalJsonRegexMatcher.regexMatcher.apply("b", patterns));
    }
}
