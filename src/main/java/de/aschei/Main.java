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
        String input = "e09ce09149d8f14254cc" + "fa3c4b1c6dc325734742";
        //String decimalInput = "1282312411506662821599389977499878440081124706114";
        System.out.println("Schöne Heimat: Ausblick #1".length());
        new Main(input).run();
    }

    private void run() {
        System.out.println(input);
        System.out.println("length: " + input.length());
        System.out.println("byte-length: " + input.length() / 2);
        System.out.println("bit-length: " + 8 * (input.length() / 2));
        System.out.println();
        toDecimalByByteLength(1);
        toDecimalByByteLength(2);
        toDecimalByByteLength(4);
        searchForHashes();
    }

    private void searchForHashes() {
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
                            System.exit(0);
                        }
                    }
                }
            }
        }
    }

    private String getSha1Hash(String xRepresentation) {
        byte[] digest = md.digest(xRepresentation.getBytes());
        String result = String.copyValueOf(Hex.encodeHex(digest));
        return result;
    }

    private void toDecimalByByteLength(int byteLength) {
        System.out.println("Input as decimals, grouping " + byteLength + " bytes each, will be " +
                (input.length() / (2 * byteLength)) + " numbers");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i = i + 2 * byteLength) {
            String part = input.substring(i, i + 2 * byteLength);
            long partAsNumber = Long.parseLong(part, 16);
            sb.append((char) partAsNumber);
            System.out.print(partAsNumber + " ");
        }
        System.out.println("\n" + sb.toString());
        System.out.println("\n");
    }
}
