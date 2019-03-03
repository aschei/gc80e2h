package de.aschei;

import de.aschei.probegenerator.ProbeGenerator;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            LOG.error("Usage: java ...  <pattern> <hash>");
            LOG.info(
                    "Example: ... \"{}\" {}", "N 50 3[1-5]\\.\\d\\d\\d E 010 2[0-4]\\.\\d\\d\\d",
                    "e09ce09149d8f14254ccfa3c4b1c6dc325734742");
            System.exit(1);
        }
        final String pattern = args[0];
        final String input = args[1];
        long start = System.currentTimeMillis();
        int returnValue = new Main(pattern, input).run();
        long stop = System.currentTimeMillis();
        LOG.info("Took me {} seconds.", ((stop - start) / 1000L));
        System.exit(returnValue);
    }


    private ProbeGenerator generator;
    private String input;

    private ThreadLocal<MessageDigest> md;

    private Main(String pattern, String input) {
        this.generator = new ProbeGenerator(pattern);
        this.input = input;
        md = ThreadLocal.withInitial(wrapException(() -> MessageDigest.getInstance("SHA-1")));
        DecimalFormat df = new DecimalFormat("#,###");
        LOG.info("The pattern '{}' contains {} probes.", pattern, df.format(generator.getNumberOfProbes()));
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

    /**
     * @return 0 if a pattern was found, 404 otherwise
     */
    private int run() {
        String result = generator.stream()      // stream the probes
                .parallel()                     // parallelize the stream
                .map(this::progress)            // measure progress
                .filter(this::doHashesMatch)    // filter probes matching the hash code
                .findFirst()                    // stop on first find
                .orElse(null);                  // or return nothing
        if (result == null) {
            LOG.info("No result has been found.");
            return 404;
        }
        return 0;
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
                    if (percentage > 0) {
                        long remaining = duration * (100L - percentage) / percentage;
                        LOG.info("Testing probes, {}% done, remaining time no longer than {} seconds", percentage,
                                remaining);
                    } else {
                        LOG.info("Testing probes, {}% done, ...", percentage);
                    }
                }
            }
        }
        return probe;
    }

    private boolean doHashesMatch(String probe) {
        String hash = getSha1Hash(probe);
        if (hash.equals(input)) {
            LOG.info("******");
            LOG.info("Match: " + probe);
            LOG.info("******");
            return true;
        }
        return false;
    }

    private String getSha1Hash(String xRepresentation) {
        byte[] digest = md.get().digest(xRepresentation.getBytes());
        return String.copyValueOf(Hex.encodeHex(digest));
    }
}
