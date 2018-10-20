import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * @author Joshua Chen
 * Class: CS3700.01
 * Date: Oct 22, 2018
 */
public class HuffmanConstitution {
    public static void main(String[] args) throws IOException {

        String constitution = new String(Files.readAllBytes(Paths.get("/Users/student/IdeaProjects/Huffman/src/Constitution.txt")));

        // Create a frequency array of each char's ascii value used for the index of the array
        // and increment the value of that index by 1 when the char is read
        int[] charFreq = new int[256];
        for (char c : constitution.toCharArray()) {
            charFreq[c] += 1;
        }

        // Minheap of FreqNode
        PriorityQueue<FreqNode> huffTree = new PriorityQueue<>();

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

        HashMap<Character, String> compressedChar = new HashMap<>();

        generateCompressedChar(compressedChar, new StringBuffer(), huffTree.peek());

        FileOutputStream compressedConstitution = new FileOutputStream("/Users/student/IdeaProjects/Huffman/src/CompressedConstitution.txt");

        StringBuffer encodeConst = new StringBuffer();
        for (int i = 0; i < constitution.length(); i++) {
            encodeConst.append(compressedChar.get(constitution.charAt(i)));
        }

        compressedConstitution.write(encodeConst.toString().getBytes());
        compressedConstitution.close();

        System.out.println("Original Size contains " + constitution.length() + " bytes.");
        System.out.println("Compressed Size contains " + encodeConst.toString().length() / 8.0 + " bytes.");
        System.out.printf("Original Compressed by %.2f%%.", 100 -(encodeConst.toString().length() / 8.0) / constitution.length() * 100);
    }

    // Recursively determine the compressed binary string of each character in the tree
    // String buffers are thread safe
    public static void generateCompressedChar(HashMap<Character, String> hm, StringBuffer compressedStr, FreqNode currentNode) {
        if (currentNode instanceof CharNode) {
            hm.put(((CharNode) currentNode).character, compressedStr.toString());

            System.out.printf("%5c \t %5d \t %15s\n", ((CharNode) currentNode).character, ((CharNode) currentNode).freq, compressedStr.toString());
        }
        else if (currentNode != null) {
            // Traverse left of currentNode
            compressedStr.append('0');
            generateCompressedChar(hm, compressedStr, currentNode.left);

            // Delete the char that was added further down the tree
            // Going down another branch of the tree
            compressedStr.deleteCharAt(compressedStr.length() - 1);

            // Traverse right of currentNOde
            compressedStr.append('1');
            generateCompressedChar(hm, compressedStr, currentNode.right);

            // Delete the char that was added further down the tree
            // Going down another branch of the tree
            compressedStr.deleteCharAt(compressedStr.length() - 1);
        }
    }
}
