package de.aschei;

import de.aschei.probegenerator.ProbeGenerator;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            showUsage();
            System.exit(1);
        }
        final String pattern = args[0];
        final String input = args[1];
        final String algorithm = args.length >= 3 ? args[2] : "SHA-1";
        int returnValue = new Main(pattern, input, algorithm).run();
        System.exit(returnValue);
    }

    static void showUsage() {
        LOG.info("gc80e2h is a tool to search for a message with a certain hash code.");
        LOG.info("  Given a regular expression that describes all the messages to be searched");
        LOG.info("  and the target hash code and a hash function, gc80e2h brute-force-generates all");
        LOG.info("  messages, and compares their hashes with the given target hash.");
        LOG.info("");
        LOG.info("Usage: java ...  <pattern> <hash> [hash-algorithm]");
        LOG.info("   <pattern> is a Java regexp that describes the message space.");
        LOG.info("   <hash> is the target hash.");
        LOG.info("   <hash-algorithm> defaults to SHA-1. Full list of supported algorithms see below.");
        LOG.info("");
        LOG.info("Example 1: ... \"{}\" {}", "N 50 3[1-5]\\.\\d\\d\\d E 010 2[0-4]\\.\\d\\d\\d",
                "e09ce09149d8f14254ccfa3c4b1c6dc325734742");
        LOG.info("Example 2: ... \"{}\" {} {}",
                "N 50 3[1-5]\\.\\d\\d\\d E 010 2[0-4]\\.\\d\\d\\d",
                "07917342c561b6e11bf651a41f2bd2b543b755bdec76e468c627d1f17e959576",
                "SHA-2");
        LOG.info("");
        LOG.info("Return values: ");
        LOG.info("  0 a match was found");
        LOG.info("  1 general, unexpected error");
        LOG.info("  3 unsupported hash algorithm specified");
        LOG.info("  4 no match was found");
        LOG.info("");
        LOG.info("Listing supported hash algorithms: ");
        explainSupportedAlgorithms();
    }

    private static void explainSupportedAlgorithms() {
        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            explainHashAlgorithms(provider);
        }
    }

    private static void explainHashAlgorithms(Provider provider) {
        String type = MessageDigest.class.getSimpleName();

        List<Provider.Service> algorithms = new ArrayList<>();

        Set<Provider.Service> services = provider.getServices();
        for (Provider.Service service : services) {
            if (service.getType().equalsIgnoreCase(type)) {
                algorithms.add(service);
            }
        }

        if (!algorithms.isEmpty()) {
            LOG.info(" --- Provider {}, version {} ---", provider.getName(), provider.getVersionStr());
            for (Provider.Service service : algorithms) {
                LOG.info("  Name: \"{}\"", service.getAlgorithm());
            }
        }
    }

    private final ProbeGenerator generator;
    private final String pattern;
    private final String input;
    private final String algorithm;

    private ThreadLocal<MessageDigest> md;

    private Main(String pattern, String input, final String algo) {
        this.generator = new ProbeGenerator(pattern);
        this.pattern = pattern;
        this.input = input;
        this.algorithm = algo;
    }

    /**
     * @return 0 if a pattern was found, 3 if the hash algorithm is invalid, 4 if no pattern was found
     */
    private int run() {
        if (!checkAlgorithm(algorithm)) {
            return 3;
        }
        md = ThreadLocal.withInitial(wrapException(() -> MessageDigest.getInstance(algorithm)));
        DecimalFormat df = new DecimalFormat("#,###");
        LOG.info("Checking all probes for pattern '{}'", pattern);
        LOG.info("comparing {} with {}", algorithm, input);
        LOG.info("Pattern contains {} probes", df.format(generator.getNumberOfProbes()));
        long start = System.currentTimeMillis();
        String result = generator.stream()      // stream the probes
                .parallel()                     // parallelize the stream
                .map(this::progress)            // measure progress
                .filter(this::doHashesMatch)    // filter probes matching the hash code
                .findFirst()                    // stop on first find
                .orElse(null);                  // or return nothing
        long stop = System.currentTimeMillis();
        LOG.info("Took me {} seconds.", ((stop - start) / 1000L));
        if (result == null) {
            LOG.info("No result has been found.");
            return 4;
        }
        return 0;
    }

    private boolean checkAlgorithm(String algorithm) {
        try {
            MessageDigest.getInstance(algorithm);
            return true;
        } catch (NoSuchAlgorithmException e) {
            LOG.error("The hash-algorithm '{}' is not supported", algorithm);
            showUsage();
            return false;
        }
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
        String hash = getHash(probe);
        if (hash.equals(input)) {
            LOG.info("******");
            LOG.info("Match: " + probe);
            LOG.info("******");
            return true;
        }
        return false;
    }

    private String getHash(String xRepresentation) {
        byte[] digest = md.get().digest(xRepresentation.getBytes());
        return String.copyValueOf(Hex.encodeHex(digest));
    }
}
