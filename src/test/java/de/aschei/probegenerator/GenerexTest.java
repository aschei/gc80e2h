package de.aschei.probegenerator;

import com.mifmif.common.regex.Generex;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenerexTest {

    @Test
    void generexCalculatesTheCorrectProbeSize() {
        String pattern = "N 50 3[1-5]\\.\\d\\d\\d E 010 2[0-4]\\.\\d\\d\\d";
        Generex generex = new Generex(pattern);
        assertEquals(25000000L, generex.matchedStringsSize());
    }

    @Test
    void generexSupportsStreaming() {
        String pattern = "N 50 3[1-5]\\.\\d\\d\\d E 010 2[0-4]\\.\\d\\d\\d";
        Generex generex = new Generex(pattern);
        String probe = StreamSupport.stream(
                Spliterators.spliterator(wrap(generex.iterator()), generex.matchedStringsSize(), 0),
                false).findFirst().orElseThrow();
        assertNotNull(probe);
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
