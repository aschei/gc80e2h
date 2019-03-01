package de.aschei;

import de.aschei.probegenerator.ProbeGenerator;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.text.DecimalFormat;

public class Main {

    public static void main(String[] args) throws Exception {
        String input = "e09ce09149d8f14254ccfa3c4b1c6dc325734742";
        String pattern = "N 5[d] 3[1,5].[d][d][d] E 010 2[0,4].[d][d][d]";
        long start = System.currentTimeMillis();
        new Main(pattern, input).run();
        long stop = System.currentTimeMillis();
        System.out.println("Took me " + ((stop - start) / 1000L) + " seconds.");
    }


    private ProbeGenerator generator;
    private String input;

    private MessageDigest md;

    private Main(String pattern, String input) throws Exception {
        this.generator = new ProbeGenerator(pattern);
        this.input = input;
        md = MessageDigest.getInstance("SHA-1");
        DecimalFormat df = new DecimalFormat("0,000");
        System.out.println(
                "The pattern '" + pattern + "' contains " + df.format(generator.getNumberOfProbes()) + " probes.");
    }

    private void run() {
        generator.accept((probe) -> {
            String hash = getSha1Hash(probe);
            if (hash.equals(input)) {
                System.out.println("\nMatch: " + probe);
                return false;
            }
            return true;
        });
    }

    private String getSha1Hash(String xRepresentation) {
        byte[] digest = md.digest(xRepresentation.getBytes());
        return String.copyValueOf(Hex.encodeHex(digest));
    }
}
