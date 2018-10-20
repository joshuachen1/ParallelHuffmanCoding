/**
 * @author Joshua Chen
 * Class: CS3700.01
 * Date: Oct 22, 2018
 */
public class FreqNode implements Comparable <FreqNode>{
    public int totalFreq;
    public FreqNode left, right;

    public FreqNode() {

    }

    public FreqNode(FreqNode left, FreqNode right) {
        this.totalFreq = left.totalFreq + right.totalFreq;
        this.left = left;
        this.right = right;
    }

    // PriorityQueue will use this method to sort the queue like a minheap
    @Override
    public int compareTo(FreqNode fNode) {
        return totalFreq - fNode.totalFreq;
    }
}
