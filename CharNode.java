/**
 * @author Joshua Chen
 * Class: CS3700.01
 * Date: Oct 22, 2018
 *
 * Extend FreqNode to be able to combine with other FreqNodes
 */
public class CharNode extends FreqNode {
    int freq;
    char character;

    public CharNode(int freq, char character) {
        this.totalFreq = freq;
        this.freq = freq;
        this.character = character;
    }
}
