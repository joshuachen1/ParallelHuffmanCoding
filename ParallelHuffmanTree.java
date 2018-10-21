import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.concurrent.Phaser;

/**
 * @author Joshua Chen
 * Class: CS3700.01
 * Date: Oct 22, 2018
 */
public class ParallelHuffmanTree {
    public static void main(String[] args) throws IOException {

        String constitution = new String(Files.readAllBytes(Paths.get("/Users/student/IdeaProjects/Huffman/src/Constitution.txt")));

        // Minheap of FreqNode
        PriorityQueue<FreqNode> huffTree = new PriorityQueue<>();

        long timeIn = System.nanoTime();
        generateHuffmanTree(huffTree, constitution);
        long timeOut = System.nanoTime() - timeIn;

        System.out.println("Parallel Thread Creation of Tree Time: " + timeOut * 1e-6 + " (ms).");
    }

    public static void generateHuffmanTree(PriorityQueue<FreqNode> huffTree, String constitution) {
        Phaser p = new Phaser();

        // Create a frequency array of each char's ascii value used for the index of the array
        // and increment the value of that index by 1 when the char is read
        int[] charFreq = new int[256];
        for (char c : constitution.toCharArray()) {
            charFreq[c] += 1;
        }

        // Make each letter its own mini tree sorted with the head of the minheap as the lowest frequency
        for (int i = 0; i < charFreq.length; i++) {
            if (charFreq[i] > 0) {
                huffTree.offer(new CharNode(charFreq[i], (char) i));
            }
        }

        // Combining the mini trees in the minheap until only one tree containing everything remains
        while (huffTree.size() > 1) {
            // Least frequent letter in minheap
            FreqNode fNode1 = huffTree.poll();
            // Second least frequent letter in minheap
            FreqNode fNode2 = huffTree.poll();

            // Assure that the next second least node is not null
            assert fNode2 != null;
            huffTree.offer(new FreqNode(fNode1, fNode2));
        }
    }
}
