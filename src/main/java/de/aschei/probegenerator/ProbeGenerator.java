package de.aschei.probegenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ProbeGenerator {

    private final long numberOfProbes;
    private final List<GeneratorElement> elements = new ArrayList<>();

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
                elements.add(DecimalDigitGenerator.of(innerPattern));
                currentPattern = currentPattern.substring(stopIndex + 1);
            }
        }
    }

    public boolean accept(Function<String, Boolean> functor) {
        return accept(functor, "", elements, new ProgressState());
    }

    private boolean accept(Function<String, Boolean> functor, String prefix, List<GeneratorElement> elements,
                           ProgressState progressState) {
        if (elements.isEmpty()) {
            progressState.measureProgress();
            return functor.apply(prefix);
        }
        boolean result = true;
        GeneratorElement firstElement = elements.get(0);
        List<GeneratorElement> subElements = elements.subList(1, elements.size());
        for (String part : firstElement) {
            result = accept(functor, prefix + part, subElements, progressState);
            if (!result) {
                break;
            }
        }
        return result;
    }

    private class ProgressState {
        long currentProbeNumber = 0;
        long lastUpdateTime, firstUpdateTime = System.currentTimeMillis();

        void measureProgress() {
            currentProbeNumber++;
            long now = System.currentTimeMillis();
            if (now - lastUpdateTime > 2000) {
                lastUpdateTime = now;
                long percentage = (100L * currentProbeNumber / numberOfProbes);
                long duration = (lastUpdateTime - firstUpdateTime) / 1000L;
                System.out.print("Testing probes, " + percentage + "% done");
                if (percentage > 0) {
                    long remaining = duration * (100L - percentage) / percentage;
                    System.out.println(", remaining time no longer than " + remaining + " seconds");
                } else {
                    System.out.println("...");
                }
            }
        }
    }
}
