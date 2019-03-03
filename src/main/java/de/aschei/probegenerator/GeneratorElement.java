package de.aschei.probegenerator;

/**
 * A part of the pattern. The number of different values for this part is given by "size". The nth value can be
 * retrieved with getNthContent(n), where n is between 0 and size()-1
 */
interface GeneratorElement {

    long size();

    String getNthContent(int n);
}
