package de.aschei.probegenerator;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecimalDigitGenerator implements GeneratorElement {
    private final static Pattern decimalDigitPattern = Pattern.compile("d");
    private final static Pattern decimalDigitRangePattern = Pattern.compile("(\\d),[\\s]*(\\d)");

    static DecimalDigitGenerator of(final String pattern) {
        if (decimalDigitPattern.matcher(pattern).matches()) {
            return new DecimalDigitGenerator(0, 9);
        }
        Matcher m = decimalDigitRangePattern.matcher(pattern);
        if (m.matches()) {
            int from = Integer.parseInt(m.group(1));
            int to = Integer.parseInt(m.group(2));
            if (from >= to) {
                throw new RuntimeException("Invalid range pattern, " + pattern);
            }
            return new DecimalDigitGenerator(from, to);
        }
        throw new RuntimeException("Unknown dynamic pattern, " + pattern);
    }

    private final int from;
    private final int to;

    private DecimalDigitGenerator(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            int current = from;

            @Override
            public boolean hasNext() {
                return current <= to;
            }

            @Override
            public String next() {
                return Integer.toString(current++);
            }
        };
    }

    @Override
    public long size() {
        return to - from + 1;
    }
}
