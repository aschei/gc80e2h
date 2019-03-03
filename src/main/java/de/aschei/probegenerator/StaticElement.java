package de.aschei.probegenerator;

/**
 * Static part of the pattern
 */
class StaticElement implements GeneratorElement {

    static GeneratorElement of(String content) {
        return new StaticElement(content);
    }

    private String content;

    private StaticElement(String content) {
        this.content = content;
    }

    @Override
    public String getNthContent(int n) {
        return content;
    }

    @Override
    public long size() {
        return 1;
    }
}
