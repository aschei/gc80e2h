package de.aschei.probegenerator;

import java.util.Iterator;

class StaticElement implements GeneratorElement {

    static GeneratorElement of(String content) {
        return new StaticElement(content);
    }

    private String content;

    private StaticElement(String content) {
        this.content = content;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public String next() {
                hasNext = false;
                return content;
            }
        };
    }

    @Override
    public long size() {
        return 1;
    }
}
