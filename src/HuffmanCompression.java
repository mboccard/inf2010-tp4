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
	// k est le nombre d'octets distincts.
	// h est la hauteur de l'arbre.
	// Complexité temporelle :
	// L'initialisation du tableau de 256 éléments coûte O(256).
	// La méthode récursive parcourt l'arbre et construit les codes en O(kh).
	// T(k,h) = O(256) + O(kh) = O(256 + kh).
	// Complexité spatiale :
	// Le tableau huffmanCodes contient 256 éléments.
	// Les k codes binaires occupent au maximum O(kh).
	// La pile récursive et les chaînes temporaires utilisent au plus O(h²).
	// S(k,h) = O(256 + kh + h²).
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
	// k est le nombre d'octets distincts.
	// h est la hauteur de l'arbre.
	// b est le nombre de bits compressés.
	// Complexité temporelle :
	// L'écriture de l'arbre parcourt 2k-1 noeuds en O(k).
	// L'écriture de la taille du message coûte O(1).
	// La boucle parcourt les b bits compressés en O(b).
	// T(k,b) = O(k) + O(1) + O(b) = O(k + b).
	// Complexité spatiale :
	// L'écriture récursive de l'arbre utilise une pile de profondeur h.
	// Les bits sont écrits directement dans le fichier.
	// S(h) = O(h).
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
	// n est le nombre d'octets du fichier original.
	// k est le nombre d'octets distincts.
	// h est la hauteur de l'arbre.
	// b est le nombre de bits compressés.
	// Complexité temporelle :
	// La reconstruction de l'arbre parcourt 2k-1 noeuds en O(k).
	// La lecture de la taille du message coûte O(1).
	// Le décodage parcourt les b bits compressés en O(b).
	// La création du tableau de sortie coûte O(n).
	// T(n,k,b) = O(k) + O(b) + O(n) = O(k + b + n).
	// Complexité spatiale :
	// L'arbre reconstruit occupe O(k).
	// La pile récursive occupe O(h).
	// Les données décompressées occupent O(n).
	// S(n,k,h) = O(k + h + n).
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
	// n est le nombre d'octets du fichier.
	// Complexité temporelle :
	// L'initialisation du tableau de 256 éléments coûte O(256).
	// La boucle parcourt les n octets du fichier en O(n).
	// T(n) = O(256) + O(n) = O(n).
	// Complexité spatiale :
	// Le tableau frequencies contient toujours 256 éléments.
	// S(n) = O(256) = O(1).
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
	// k est le nombre d'octets distincts.
	// Complexité temporelle :
	// L'initialisation parcourt 256 fréquences et ajoute k noeuds dans la file de priorité en O(256 + k log k).
	// La boucle while s'exécute k-1 fois.
	// Chaque itération effectue deux poll() et un add() en O(log k).
	// La création du parent coûte O(1).
	// T(k) = O(256 + k log k) + O(k log k) = O(256 + k log k)
	// Complexité spatiale :
	// La file de priorité contient au maximum k noeuds.
	// L'arbre Huffman contient 2k-1 noeuds.
	// S(k) = O(k).
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
	// k est le nombre d'octets distincts.
	// Complexité temporelle :
	// L'initialisation parcourt 256 fréquences et ajoute k noeuds dans une liste en O(256 + k).
	// La boucle while s'exécute k-1 fois.
	// Chaque itération recherche deux minimums en O(k).
	// La création du parent et son ajout coûtent O(1).
	// T(k) = O(256 + k) + O(k²) = O(256 + k²)
	// Complexité spatiale :
	// La liste contient au maximum k noeuds.
	// L'arbre Huffman contient 2k-1 noeuds.
	// S(k) = O(k).
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

	// n est le nombre d'octets du fichier original.
	// k est le nombre d'octets distincts.
	// h est la hauteur de l'arbre.
	// b est le nombre de bits compressés.
	// Complexité temporelle :
	// Le calcul des fréquences coûte O(n + 256).
	// La construction de l'arbre coûte O(256 + k log k) avec une file de priorité et O(256 + k²) sans file.
	// La génération des codes coûte O(256 + kh).
	// L'encodage et l'écriture des données coûtent O(n + b).
	// Avec file : T(n,k,h,b) = O(n + 256 + k log k + kh + b).
	// Sans file : T(n,k,h,b) = O(n + 256 + k² + kh + b).
	// Complexité spatiale :
	// Le tableau des fréquences occupe O(256).
	// L'arbre Huffman occupe O(k).
	// Les codes binaires et leurs chaînes temporaires
	// occupent au maximum O(256 + kh + h²).
	// Le message encodé occupe O(b).
	// Avec file : S(n,k,h,b) = O(256 + k + kh + h² + b).
	// Sans file : S(n,k,h,b) = O(256 + k + kh + h² + b).
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
	// n est le nombre d'octets du fichier original.
	// k est le nombre d'octets distincts.
	// h est la hauteur de l'arbre.
	// b est le nombre de bits compressés.
	// Complexité temporelle :
	// La reconstruction de l'arbre coûte O(k).
	// Le décodage des b bits compressés coûte O(b).
	// L'écriture des n octets décompressés coûte O(n).
	// T(n,k,b) = O(k) + O(b) + O(n) = O(k + b + n).
	// Complexité spatiale :
	// L'arbre reconstruit occupe O(k).
	// La pile récursive occupe O(h).
	// Les données décompressées occupent O(n).
	// S(n,k,h) = O(k + h + n).
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

	private void afficherStatistiques(String fichierOriginal, String fichierCompresse) throws IOException {
		int[] frequencies = getFrequencies(fichierOriginal);

		int k = 0;
		for (int frequency : frequencies) {
			if (frequency > 0) {
				k++;
			}
		}

		HuffmanNode root = generateHuffmanTreeWithPriorityQueue(frequencies);

		long tailleArbreBits = 10L * k - 1;
		long tailleDonneesBits = 0;

		generateHuffmanCodes(root);

		for (int i = 0; i < frequencies.length; i++) {
			if (frequencies[i] > 0) {
				tailleDonneesBits += (long) frequencies[i] * huffmanCodes[i].length();
			}
		}

		System.out.println("Octets distincts : " + k);
		System.out.println("Profondeur de l'arbre : " + computeMaxDepth(root));
		System.out.println("Taille de l'arbre : " + tailleArbreBits / 8.0 + " octets");
		System.out.println("Taille des donnees : " + tailleDonneesBits / 8.0 + " octets");
		System.out.println("Taille originale : " + Files.size(Path.of(fichierOriginal)) + " octets");
		System.out.println("Taille compressee : " + Files.size(Path.of(fichierCompresse)) + " octets");
	}

	public static void main(String[] args) throws IOException {

		// Test 1 : textuel, peu de caractères, faible diversité
		String contenu1 = "AB".repeat(50);
		Files.writeString(Path.of("peu_faible.txt"), contenu1);

		// Test 2 : textuel, peu de caractères, diversité élevée
		String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		String contenu2 = caracteres + caracteres.substring(0, 38);
		Files.writeString(Path.of("peu_eleve.txt"), contenu2);

		// Test 3 : textuel, beaucoup de caractères, faible diversité
		String contenu = "AB".repeat(500_000);
		Files.writeString(Path.of("beaucoup_faible.txt"), contenu);

		// Test 4 : textuel, beaucoup de caractères, diversité élevée
		String contenuVarie = caracteres.repeat(16_129) + caracteres.substring(0, 2);
		Files.writeString(Path.of("beaucoup_eleve.txt"), contenuVarie);

		// Test 5 : binaire, peu d'octets, faible diversité
		byte[] donnees = new byte[100];
		for (int i = 0; i < donnees.length; i++) {
			donnees[i] = (byte) (i % 2);
		}
		Files.write(Path.of("peu_faible.bin"), donnees);

		// Test 6 : binaire, peu d'octets, diversité élevée
		byte[] donneesVariees = new byte[100];
		for (int i = 0; i < donneesVariees.length; i++) {
			donneesVariees[i] = (byte) i;
		}
		Files.write(Path.of("peu_eleve.bin"), donneesVariees);

		// Test 7 : binaire, beaucoup d'octets, faible diversité
		byte[] donneesGrandes = new byte[1_000_000];
		for (int i = 0; i < donneesGrandes.length; i++) {
			donneesGrandes[i] = (byte) (i % 2);
		}
		Files.write(Path.of("beaucoup_faible.bin"), donneesGrandes);

		// Test 8 : binaire, beaucoup d'octets, diversité élevée
		byte[] donneesGrandesVariees = new byte[1_000_000];
		for (int i = 0; i < donneesGrandesVariees.length; i++) {
			donneesGrandesVariees[i] = (byte) (i % 256);
		}
		Files.write(Path.of("beaucoup_eleve.bin"), donneesGrandesVariees);


		HuffmanCompression hc = new HuffmanCompression();

		// Test 1 : textuel, peu de caractères, faible diversité
		hc.compress("peu_faible.txt", "compresse1.bin", true);
		System.out.println("=== Test 1 : Textuel, peu de caractères, faible diversité ===");
		System.out.println("Taille originale : " + Files.size(Path.of("peu_faible.txt")));
		System.out.println("Taille compressée : " + Files.size(Path.of("compresse1.bin")));

		// Test 2 : textuel, peu de caractères, diversité élevée
		hc.compress("peu_eleve.txt", "compresse2.bin", true);
		System.out.println("=== Test 2 : Textuel, peu de caractères, diversité élevée ===");
		System.out.println("Taille originale : " + Files.size(Path.of("peu_eleve.txt")));
		System.out.println("Taille compressée : " + Files.size(Path.of("compresse2.bin")));

		// Test 3 : textuel, beaucoup de caractères, faible diversité
		hc.compress("beaucoup_faible.txt", "compresse3.bin", true);
		System.out.println("=== Test 3 : Textuel, beaucoup de caractères, faible diversité ===");
		System.out.println("Taille originale : " + Files.size(Path.of("beaucoup_faible.txt")));
		System.out.println("Taille compressée : " + Files.size(Path.of("compresse3.bin")));

		// Test 4 : textuel, beaucoup de caractères, diversité élevée
		hc.compress("beaucoup_eleve.txt", "compresse4.bin", true);
		System.out.println("=== Test 4 : Textuel, beaucoup de caractères, diversité élevée ===");
		System.out.println("Taille originale : " + Files.size(Path.of("beaucoup_eleve.txt")));
		System.out.println("Taille compressée : " + Files.size(Path.of("compresse4.bin")));

		// Test 5 : binaire, peu d'octets, faible diversité
		hc.compress("peu_faible.bin", "compresse5.bin", true);
		System.out.println("=== Test 5 : Binaire, peu d'octets, faible diversité ===");
		System.out.println("Taille originale : " + Files.size(Path.of("peu_faible.bin")));
		System.out.println("Taille compressée : " + Files.size(Path.of("compresse5.bin")));

		// Test 6 : binaire, peu d'octets, diversité élevée
		hc.compress("peu_eleve.bin", "compresse6.bin", true);
		System.out.println("=== Test 6 : Binaire, peu d'octets, diversité élevée ===");
		System.out.println("Taille originale : " + Files.size(Path.of("peu_eleve.bin")));
		System.out.println("Taille compressée : " + Files.size(Path.of("compresse6.bin")));

		// Test 7 : binaire, beaucoup d'octets, faible diversité
		hc.compress("beaucoup_faible.bin", "compresse7.bin", true);
		System.out.println("=== Test 7 : Binaire, beaucoup d'octets, faible diversité ===");
		System.out.println("Taille originale : " + Files.size(Path.of("beaucoup_faible.bin")));
		System.out.println("Taille compressée : " + Files.size(Path.of("compresse7.bin")));

		// Test 8 : binaire, beaucoup d'octets, diversité élevée
		hc.compress("beaucoup_eleve.bin", "compresse8.bin", true);
		System.out.println("=== Test 8 : Binaire, beaucoup d'octets, diversité élevée ===");
		System.out.println("Taille originale : " + Files.size(Path.of("beaucoup_eleve.bin")));
		System.out.println("Taille compressée : " + Files.size(Path.of("compresse8.bin")));


		String[] fichiersOriginaux = {
				"peu_faible.txt", "peu_eleve.txt",
				"beaucoup_faible.txt", "beaucoup_eleve.txt",
				"peu_faible.bin", "peu_eleve.bin",
				"beaucoup_faible.bin", "beaucoup_eleve.bin"
		};

		for (int i = 0; i < fichiersOriginaux.length; i++) {
			System.out.println("\n=== Statistiques du test " + (i + 1) + " ===");
			hc.afficherStatistiques(fichiersOriginaux[i], "compresse" + (i + 1) + ".bin");
		}


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
