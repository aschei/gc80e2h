package de.aschei.probegenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ProbeGenerator {

    private final long numberOfProbes;
    private final List<GeneratorElement> elements = new ArrayList<>();
    private final List<DecimalDigitGenerator> dynamicElements = new ArrayList<>();

    public ProbeGenerator(String pattern) {
        parse(pattern);
        numberOfProbes = calcNumberOfProbes();
    }

    public long getNumberOfProbes() {
        return numberOfProbes;
    }

    private long calcNumberOfProbes() {
        long result = 1;
        for (GeneratorElement el : elements) {
            result *= el.size();
        }
        return result;
    }

    private void parse(final String pattern) {
        String currentPattern = pattern;
        while (!currentPattern.isEmpty()) {
            int nextIndex = currentPattern.indexOf('[');
            if (nextIndex < 0) {
                elements.add(StaticElement.of(currentPattern));
                return;
            } else if (nextIndex > 0) {
                elements.add(StaticElement.of(currentPattern.substring(0, nextIndex)));
                currentPattern = currentPattern.substring(nextIndex);
            } else { // nextIndex = 0;
                int stopIndex = currentPattern.indexOf(']');
                if (stopIndex < 0) {
                    throw new RuntimeException("Invalid pattern, unmatched '[': " + pattern);
                }
                String innerPattern = currentPattern.substring(1, stopIndex);
                DecimalDigitGenerator dynamicElement = DecimalDigitGenerator.of(innerPattern);
                elements.add(dynamicElement);
                dynamicElements.add(dynamicElement);
                currentPattern = currentPattern.substring(stopIndex + 1);
            }
        }
    }

    public Stream<String> stream() {
        Spliterator<String> spliterator = new ProbeSpliterator(0L, numberOfProbes - 1);
        return StreamSupport.stream(spliterator, false); // let caller decide if this should be parallel
    }

    private class ProbeSpliterator implements Spliterator<String> {

        private long current, to;

        ProbeSpliterator(long from, long to) {
            this.current = from;
            this.to = to;
        }

        @Override
        public void forEachRemaining(Consumer<? super String> action) {
            while (current <= to) {
                action.accept(getProbeFromVector(getVectorForNthProbe(current)));
                current++;
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action) {
            if (current <= to) {
                action.accept(getProbeFromVector(getVectorForNthProbe(current)));
                current++;
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<String> trySplit() {
            // Split half between current and "to"
            if (to - current > 1L) {
                // current 50, to 52
                long myNewCurrent = current + (to - current) / 2 + 1; // 50 + (2)/2 +1 = 52
                long splitFrom = current; // 50
                long splitTo = myNewCurrent - 1; // 51
                current = myNewCurrent; // 52 to 52
                return new ProbeSpliterator(splitFrom, splitTo); //50 to 51
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return to - current + 1;
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
        }
    }

    int[] getVectorForNthProbe(long n) {
        /*
         * Goal is to have a fast function: long -> probe (given as a vector)
         *
         * Example: 3 dimensions of sizes 7, 10, 5, resulting in 350 probes
         * n < 7    -> [n      , 0      , 0    ]
         * n = 7-13 -> [n-7    , 1      , 0    ]
         * n < 70   -> [n%7    , n/7    , 0    ]
         * n = 70   -> [0      , 0      , 1    ]
         * n        -> [n%7    , n/7%10 , n/70 ]
         * n= 175   -> [0, 5, 2]
         */
        if (n >= numberOfProbes) {
            throw new RuntimeException("n (" + n + ") should be smaller than " + numberOfProbes);
        }
        int dimensionCount = dynamicElements.size();
        int[] result = new int[dimensionCount];
        long factorial = 1;
        for (int i = 0; i < dimensionCount; i++) {
            long sizeOfDimension = dynamicElements.get(i).size();
            result[i] = (int) ((n / factorial) % sizeOfDimension);
            factorial *= sizeOfDimension;
        }
        return result;
    }

    String getProbeFromVector(int[] vectorForNthProbe) {
        int numberOfDimension = 0;
        StringBuilder sb = new StringBuilder(200);
        for (GeneratorElement element : elements) {
            int n = 0;
            if (element instanceof DecimalDigitGenerator) {
                n = vectorForNthProbe[numberOfDimension++];
            }
            sb.append(element.getNthContent(n));
        }
        return sb.toString();
    }
}
