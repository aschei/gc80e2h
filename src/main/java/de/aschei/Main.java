package de.aschei;

import de.aschei.probegenerator.ProbeGenerator;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {
        String input = "e09ce09149d8f14254ccfa3c4b1c6dc325734742";
        String pattern = "N 50 3[1-5]\\.\\d\\d\\d E 010 2[0-4]\\.\\d\\d\\d";
        long start = System.currentTimeMillis();
        new Main(pattern, input).run();
        long stop = System.currentTimeMillis();
        System.out.println("Took me " + ((stop - start) / 1000L) + " seconds.");
    }


    private ProbeGenerator generator;
    private String input;

    private ThreadLocal<MessageDigest> md;

    private Main(String pattern, String input) {
        this.generator = new ProbeGenerator(pattern);
        this.input = input;
        md = ThreadLocal.withInitial(wrapException(() -> MessageDigest.getInstance("SHA-1")));
        DecimalFormat df = new DecimalFormat("0,000");
        System.out.println(
                "The pattern '" + pattern + "' contains " + df.format(generator.getNumberOfProbes()) + " probes.");
    }

    private <T> Supplier<T> wrapException(Callable<T> o) {
        return () -> {
            try {
                return o.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private void run() {
        String result = generator.stream()      // stream the probes
                .parallel()                     // parallelize the stream
                .map(this::progress)            // measure progress
                .filter(this::doHashesMatch)    // filter probes matching the hash code
                .findFirst()                    // stop on first find
                .orElse(null);                  // or return nothing
        if (result == null) {
            System.out.println("No result has been found.");
        }
    }

    private AtomicLong currentProbeNumber = new AtomicLong(0);
    private volatile long lastUpdateTime, firstUpdateTime = System.currentTimeMillis();

    private String progress(String probe) {
        currentProbeNumber.incrementAndGet();
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime > 2000) {
            synchronized (this) {
                if (now - lastUpdateTime > 2000) {
                    lastUpdateTime = now;
                    long percentage = (100L * currentProbeNumber.get() / generator.getNumberOfProbes());
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
        return probe;
    }

    private boolean doHashesMatch(String probe) {
        String hash = getSha1Hash(probe);
        if (hash.equals(input)) {
            System.out.println("\nMatch: " + probe);
            return true;
        }
        return false;
    }

    private String getSha1Hash(String xRepresentation) {
        byte[] digest = md.get().digest(xRepresentation.getBytes());
        return String.copyValueOf(Hex.encodeHex(digest));
    }
}
