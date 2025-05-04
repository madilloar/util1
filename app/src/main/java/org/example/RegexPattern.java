package org.example;

class RegexPattern {
    private String[] patterns;
    private String result;

    public RegexPattern(String[] patterns, String result) {
        this.patterns = patterns;
        this.result = result;
    }

    public String[] getPatterns() {
        return patterns;
    }

    public String getResult() {
        return result;
    }
}