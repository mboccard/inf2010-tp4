import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.PriorityQueue;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.nio.file.StandardCopyOption;

public class HuffmanCompression {	
	
	// TODO: À compléter
	private class HuffmanNode implements Comparable<HuffmanNode> {
		byte value;
		int frequency;
		HuffmanNode left, right;
		public HuffmanNode(byte value, int frequency) {
			this.value = value;
			this.frequency = frequency;
		}
		
		public boolean isLeaf() {
			if (left == null && right == null)
				return true;
			else
				return false;
		}

		@Override
		public int compareTo(HuffmanNode arg0) {
			return Integer.compare(this.frequency, arg0.frequency);
		}
	}
	
	// TODO: Donne la hauteur d'un arbre, utiliser pour les statistiques
	private int computeMaxDepth(HuffmanNode node) {
		if (node == null)
			return 0;
		return 1 + Math.max(computeMaxDepth(node.left), computeMaxDepth(node.right));
	}
	
	// TODO: Génère les codes pour chaque octets
	//       Changer les entrées au besoin
	private void generateHuffmanCodes(HuffmanNode node) {
		huffmanCodes = new String[256];
		generateCodesRecursive(node, "");
	}

	private void generateCodesRecursive(HuffmanNode node, String code) {
		if (node == null)
			return;

		if (node.isLeaf()) {
			huffmanCodes[node.value & 0xFF] = code.isEmpty() ? "0" : code;
			return;
		}
		generateCodesRecursive(node.left, code + "0");
		generateCodesRecursive(node.right, code + "1");
	}

	private String[] huffmanCodes;
	private BitStreamWriter huffmanWriter;
	private BitStreamReader huffmanReader;
	
	// TODO: Fait l'écriture de l'arbre dans le fichier de sortie (huffmanWriter)
	//       Un noeud intermédiare écrit un bit à 0 et une feuille un bit à 1
	private void recursiveWriteTree(HuffmanNode node) throws IOException {
		if (node == null)
			return;
		if (node.isLeaf()) {
			huffmanWriter.writeBit((byte)1);
			huffmanWriter.writeByte((byte)(node.value & 0xFF));
		} else {
			huffmanWriter.writeBit((byte)0);
			recursiveWriteTree(node.left);
			recursiveWriteTree(node.right);
		}
	}
	
	// TODO: Fait la lecture de l'arbre dans le fichier d'entrée (huffmanReader)
	private HuffmanNode recursiveReadTree() throws IOException {
		int bit = huffmanReader.readBit();
		if (bit == 1) {
			int value = huffmanReader.readByte() & 0xFF;
			return new HuffmanNode((byte) value, 0);
		}
		if (bit == 0) {
			HuffmanNode node = new HuffmanNode((byte) 0, 0);
			node.left = recursiveReadTree();
			node.right = recursiveReadTree();
			return node;
		}
		return null;
	}
	
	
	// TODO: Écrit le message compresser dans le fichier de sortie
	private void writeHuffmanCompressedFile(HuffmanNode tree, String compressedMessage, String outputFilePath) {
		try {
        	huffmanWriter = new BitStreamWriter(outputFilePath);

			recursiveWriteTree(tree);
			huffmanWriter.writeSize(compressedMessage.length());
			for (int i = 0; i < compressedMessage.length(); i++) {
				huffmanWriter.writeBit((byte) (compressedMessage.charAt(i) == '1' ? 1 : 0));
			}
        	            
            huffmanWriter.close();            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	// TODO: Fait la lecture d'un fichier compresser par l'algorithm de Huffman
	private byte[] readHuffmanCompressedFile(String inputFilePath) {
        try {
        	huffmanReader = new BitStreamReader(inputFilePath);

			HuffmanNode root = recursiveReadTree();
			int sizeInBits = huffmanReader.readSize();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (root.isLeaf()) {
				for (int i = 0; i < sizeInBits; i++) {
					huffmanReader.readBit();
					out.write(root.value);
				}
			} else {
				HuffmanNode current = root;
				for (int i = 0; i < sizeInBits; i++) {
					current = (huffmanReader.readBit() == 0) ? current.left : current.right;
					if (current.isLeaf()) {
						out.write(current.value);
						current = root;
					}
				}
			}
        	
        	huffmanReader.close();
			return out.toByteArray();
        	
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	// TODO: Calcul la fréquence de chaque occurence d'un octet dans le fichier
	private int[] getFrequencies(String inputFilePath) throws IOException {
		int[] frequencies = new int[256];
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(inputFilePath);
			int octet;

			while ((octet = fileInputStream.read()) != -1) {
				frequencies[octet]++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileInputStream != null)
					fileInputStream.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return frequencies;
	}
	
	// TODO: Faite l'arbre de Huffman à l'aide d'une file de priorité
	private HuffmanNode generateHuffmanTreeWithPriorityQueue(int[] frequencies) {
		PriorityQueue<HuffmanNode> queue = new PriorityQueue<HuffmanNode>();
		for (int i = 0; i < frequencies.length; i++) {
			if (frequencies[i] > 0) {
				HuffmanNode node = new HuffmanNode((byte) i, frequencies[i]);
				queue.add(node);
			}
		}

		if (queue.isEmpty()) {
			return null;
		}

		while (queue.size() > 1) {
			HuffmanNode smallest = queue.poll();
			HuffmanNode secondSmallest = queue.poll();
			HuffmanNode parent = new HuffmanNode((byte) 0, smallest.frequency + secondSmallest.frequency);
			parent.left = smallest;
			parent.right = secondSmallest;

			queue.add(parent);
		}
		return queue.poll();
	}
	
	// TODO: Faite l'arbre de Huffman sans l'aide d'une file de priorité
	private HuffmanNode generateHuffmanTreeWithoutPriorityQueue(int[] frequencies) {
		List<HuffmanNode> nodes = new ArrayList<>();
		for (int i = 0; i < frequencies.length; i++) {
			if (frequencies[i] > 0)
				nodes.add(new HuffmanNode((byte) i, frequencies[i]));
		}
		if (nodes.isEmpty())
			return null;

		while (nodes.size() > 1) {
			HuffmanNode first = removeMinimum(nodes);
			HuffmanNode second = removeMinimum(nodes);
			HuffmanNode parent = new HuffmanNode((byte) 0, first.frequency + second.frequency);
			parent.left = first;
			parent.right = second;
			nodes.add(parent);
		}
		return nodes.get(0);
	}

	private HuffmanNode removeMinimum(List<HuffmanNode> nodes) {
		int minIndex = 0;
		for (int i = 1; i < nodes.size(); i++) {
			if (nodes.get(i).frequency < nodes.get(minIndex).frequency)
				minIndex = i;
		}
		return nodes.remove(minIndex);
	}
	
	// TODO: Compresse le fichier avec l'algorithme de Huffman, puis l'écrit sur le disque
	//       Faite une version avec et sans file de priorité
	public void compress(String inputFilePath, String outputFilePath) {
		compress(inputFilePath, outputFilePath, true);
	}

	public void compress(String inputFilePath, String outputFilePath, boolean usePriorityQueue) {
		try {
			int[] frequencies = getFrequencies(inputFilePath);
			HuffmanNode root = usePriorityQueue ? generateHuffmanTreeWithPriorityQueue(frequencies) : generateHuffmanTreeWithoutPriorityQueue(frequencies);
			if (root == null)
				return;

			generateHuffmanCodes(root);

			StringBuilder bits = new StringBuilder();
			try (FileInputStream in = new FileInputStream(inputFilePath)) {
				int octet;
				while ((octet = in.read()) != -1)
					bits.append(huffmanCodes[octet]);
			}
			writeHuffmanCompressedFile(root, bits.toString(), outputFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// TODO: Décompresse le fichier avec l'algorithme de Huffman, puis l'écrit sur le disque.
	public void decompress(String inputFilePath, String outputFilePath) {
		byte[] data = readHuffmanCompressedFile(inputFilePath);
		if (data == null)
			return;
		try (FileOutputStream out = new FileOutputStream(outputFilePath)) {
			out.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		HuffmanCompression hc = new HuffmanCompression();

		// Nous avons vérifié que la compression suivie de la décompression redonne le fichier d’origine
		hc.compress("test.txt", "test_priority.bin", true);
		hc.compress("test.txt", "test_no_priority.bin", false);
		hc.decompress("test_priority.bin", "test_priority.out");
		hc.decompress("test_no_priority.bin", "test_no_priority.out");
		byte[] original = Files.readAllBytes(Path.of("test.txt"));
		System.out.println("Avec file de priorité : " + Arrays.equals(original, Files.readAllBytes(Path.of("test_priority.out"))));
		System.out.println("Sans file de priorité : " + Arrays.equals(original, Files.readAllBytes(Path.of("test_no_priority.out"))));

		//Test du type
		hc.decompress("fichierSecret.bin", "secret_decode.bin");

		byte[] secret = Files.readAllBytes(Path.of("secret_decode.bin"));
		for (int i = 0; i < 16; i++)
			System.out.printf("%02X ", secret[i]);
		System.out.println();

		Files.copy(Path.of("secret_decode.bin"), Path.of("secret.bmp"), StandardCopyOption.REPLACE_EXISTING);

		//Tableau des fréquences
		int[] frequencies = hc.getFrequencies("secret.bmp");
		try (PrintWriter writer = new PrintWriter("frequences.csv")) {
			writer.println("Octet (décimal);Octet (hex);Fréquence");
			for (int i = 0; i < frequencies.length; i++) {
				if (frequencies[i] > 0)
					writer.printf("%d;0x%02X;%d%n", i, i, frequencies[i]);
			}
		}
	}
	
}
