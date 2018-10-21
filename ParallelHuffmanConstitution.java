import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.Phaser;

/**
 * @author Joshua Chen
 * Class: CS3700.01
 * Date: Oct 22, 2018
 */
public class ParallelHuffmanConstitution {
    public static void main(String[] args) throws IOException, InterruptedException {

        String constitution = new String(Files.readAllBytes(Paths.get("/Users/student/IdeaProjects/Huffman/src/Constitution.txt")));

        // Minheap of FreqNode
        PriorityQueue<FreqNode> huffTree = new PriorityQueue<>();

        long timeIn1 = System.nanoTime();
        generateHuffmanTree(huffTree, constitution);
        long timeOut1 = System.nanoTime() - timeIn1;

        HashMap<Character, String> compressedChar = new HashMap<>();


        FileOutputStream compressedConstitution = new FileOutputStream("/Users/student/IdeaProjects/Huffman/src/CompressedConstitution.txt");

        generateCompressedChar(compressedChar, new StringBuffer(), huffTree.peek());


        // Parallel Huffman Encoding begins
        long timeIn2 = System.nanoTime();
        int numThreads = 3;
        Phaser p = new Phaser(numThreads);

        StringBuffer[] encodedStr = new StringBuffer[numThreads];

        for (int i = 0, j = 0, k = constitution.length() / numThreads; i < encodedStr.length; i++, j += k) {
            encodedStr[i] = new StringBuffer();

            if (j + k > constitution.length()) {
                new ParallelHuffmanConstitution().compressString(p, compressedChar, encodedStr[i], constitution.substring(j));
            }
            else {
                new ParallelHuffmanConstitution().compressString(p, compressedChar, encodedStr[i], constitution.substring(j, j + k));
            }
        }

        // Wait for threads to catch up
        Thread.sleep(5000);

        StringBuffer encodeConst = new StringBuffer();

        for (StringBuffer s : encodedStr) {
            encodeConst.append(s);
        }
        compressedConstitution.write(encodeConst.toString().getBytes());
        compressedConstitution.close();

        long timeOut2 = System.nanoTime() - timeIn2;

        System.out.println("Parallel Threading Total Time : " + ((((timeOut1 + timeOut2)) * 1e-6) - 10000) + " (ms).");
    }

    public static void generateHuffmanTree(PriorityQueue<FreqNode> huffTree, String constitution) throws InterruptedException {

        // Create a frequency array of each char's ascii value used for the index of the array
        // and increment the value of that index by 1 when the char is read
        int[] charFreq = new int[256];
        for (char c : constitution.toCharArray()) {
            charFreq[c] += 1;
        }

        Thread t0 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < charFreq.length / 2; i++) {
                    if (charFreq[i] > 0) {
                        huffTree.offer(new CharNode(charFreq[i], (char) i));
                    }
                }
            }
        });

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = charFreq.length / 2; i < charFreq.length; i++) {
                    if (charFreq[i] > 0) {
                        huffTree.offer(new CharNode(charFreq[i], (char) i));
                    }
                }
            }
        });

        t0.start();
        t1.start();

        t0.join();
        t1.join();


        // Wait for threads to catch up
        Thread.sleep(5000);

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

    // Recursively determine the compressed binary string of each character in the tree
    // String buffers are thread safe
    public static void generateCompressedChar(HashMap<Character, String> hm, StringBuffer compressedStr, FreqNode currentNode) {
        if (currentNode instanceof CharNode) {
            hm.put(((CharNode) currentNode).character, compressedStr.toString());

            // Display the Character, Frequency in Original text, and new Binary String to encode the character
            /*
            if (((CharNode) currentNode).character == '\n') {
                System.out.printf("%5s \t %5d \t %15s\n", "\\n", ((CharNode) currentNode).freq, compressedStr.toString());
            }
            else {
                System.out.printf("%5c \t %5d \t %15s\n", ((CharNode) currentNode).character, ((CharNode) currentNode).freq, compressedStr.toString());
            }
            */
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

    private void compressString(Phaser p, HashMap<Character, String> hm, StringBuffer encodedStr, String str) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < str.length(); i++) {
                    encodedStr.append(hm.get(str.charAt(i)));
                }
                p.arriveAndAwaitAdvance();
            }
        }).start();
    }
}
