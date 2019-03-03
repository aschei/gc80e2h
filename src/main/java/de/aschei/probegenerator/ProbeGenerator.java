package de.aschei.probegenerator;

import com.mifmif.common.regex.Generex;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ProbeGenerator {

    private final Generex generex;

    public ProbeGenerator(String pattern) {
        generex = new Generex(pattern);
    }

    public long getNumberOfProbes() {
        return generex.matchedStringsSize();
    }

    public Stream<String> stream() {
        return StreamSupport.stream(
                Spliterators.spliterator(
                        wrap(generex.iterator()),
                        generex.matchedStringsSize(),
                        Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.DISTINCT),
                false);
    }

    private Iterator<String> wrap(final com.mifmif.common.regex.util.Iterator iterator) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public String next() {
                return iterator.next();
            }
        };
    }
}
