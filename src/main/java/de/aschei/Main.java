package de.aschei;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.text.DecimalFormat;

public class Main {

    private String input;

    MessageDigest md;

    private Main(String input) throws Exception {
        this.input = input;
        md = MessageDigest.getInstance("SHA-1");
    }

    public static void main(String[] args) throws Exception {
        String input = "e09ce09149d8f14254ccfa3c4b1c6dc325734742";
        long start = System.currentTimeMillis();
        new Main(input).run();
        long stop  = System.currentTimeMillis();
        System.out.println("Took me "+ ((stop-start) / 1000L) + " seconds.");
    }

    private void run() {
        //  N50 31...35.XXX und E010 20...24.XXX
        DecimalFormat dfThreeDigits = new DecimalFormat("000");
        for (int n1 = 1; n1 <= 5; n1++) {
            for (int n2 = 0; n2 < 1000; n2++) {
                for (int e1 = 0; e1 <= 4; e1++) {
                    for (int e2 = 0; e2 < 1000; e2++) {
                        String coord = "N 50 3" + n1 +
                                "." + dfThreeDigits.format(n2) + " E 010 2" + e1 +
                                "." + dfThreeDigits.format(e2);
                        String hash = getSha1Hash(coord);
                        if (hash.equals(input)) {
                            System.out.println("TADAA!! " + coord);
                            return;
                        }
                    }
                }
            }
        }
    }

    private String getSha1Hash(String xRepresentation) {
        byte[] digest = md.digest(xRepresentation.getBytes());
        return String.copyValueOf(Hex.encodeHex(digest));
    }
}
