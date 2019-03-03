package de.aschei.probegenerator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProbeGeneratorTest {

    @Test
    void vectorCalculationWorks() {
        ProbeGenerator gen = new ProbeGenerator("[0,6][d][3,7]");
        assertArrayEquals(new int[]{0, 0, 0}, gen.getVectorForNthProbe(0));
        assertArrayEquals(new int[]{1, 0, 0}, gen.getVectorForNthProbe(1));
        assertArrayEquals(new int[]{0, 1, 0}, gen.getVectorForNthProbe(7));
        assertArrayEquals(new int[]{6, 1, 0}, gen.getVectorForNthProbe(13));
        assertArrayEquals(new int[]{0, 0, 1}, gen.getVectorForNthProbe(70));
        assertArrayEquals(new int[]{0, 5, 2}, gen.getVectorForNthProbe(175));
    }

    @Test
    void probeCalculationWorks() {
        ProbeGenerator gen = new ProbeGenerator("a[0,6]b[d]c[3,7]d");
        assertEquals("a0b0c3d", gen.getProbeFromVector(gen.getVectorForNthProbe(0)));
        assertEquals("a1b0c3d", gen.getProbeFromVector(gen.getVectorForNthProbe(1)));
        assertEquals("a0b1c3d", gen.getProbeFromVector(gen.getVectorForNthProbe(7)));
        assertEquals("a6b1c3d", gen.getProbeFromVector(gen.getVectorForNthProbe(13)));
        assertEquals("a0b0c4d", gen.getProbeFromVector(gen.getVectorForNthProbe(70)));
        assertEquals("a0b5c5d", gen.getProbeFromVector(gen.getVectorForNthProbe(175)));
    }



}