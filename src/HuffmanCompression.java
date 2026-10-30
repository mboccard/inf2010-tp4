import java.io.IOException;

public class HuffmanCompression {	
	
	// TODO: À compléter
	private class HuffmanNode implements Comparable<HuffmanNode> {
		
		public HuffmanNode() {
		}
		
		public boolean isLeaf() {
			return false;
		}

		@Override
		public int compareTo(HuffmanNode arg0) {
			return 0;
		}
	}
	
	// TODO: Donne la hauteur d'un arbre, utiliser pour les statistiques
	private int computeMaxDepth(HuffmanNode node) {
		return 0;		
	}
	
	// TODO: Génère les codes pour chaque octets
	//       Changer les entrées au besoin
	private void generateHuffmanCodes(HuffmanNode node) {
					
	}
	
	private BitStreamWriter huffmanWriter;
	private BitStreamReader huffmanReader;
	
	// TODO: Fait l'écriture de l'arbre dans le fichier de sortie (huffmanWriter)
	//       Un noeud intermédiare écrit un bit à 0 et une feuille un bit à 1
	private void recursiveWriteTree(HuffmanNode node) throws IOException {
	}
	
	// TODO: Fait la lecture de l'arbre dans le fichier d'entrée (huffmanReader)
	private HuffmanNode recursiveReadTree() throws IOException {
	}
	
	
	// TODO: Écrit le message compresser dans le fichier de sortie
	private void writeHuffmanCompressedFile(HuffmanNode tree, String compressedMessage, String outputFilePath) {
		try {
        	huffmanWriter = new BitStreamWriter(outputFilePath);        	
            
            // ...        	
        	            
            huffmanWriter.close();            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	// TODO: Fait la lecture d'un fichier compresser par l'algorithm de Huffman
	private byte[] readHuffmanCompressedFile(String inputFilePath) {
        try {
        	huffmanReader = new BitStreamReader(inputFilePath);
        	
        	// ...
        	
        	huffmanReader.close();
            return null; 
        	
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	// TODO: Calcul la fréquence de chaque occurence d'un octet dans le fichier
	private int[] getFrequencies(String inputFilePath) throws IOException {
        return null;
	}
	
	// TODO: Faite l'arbre de Huffman à l'aide d'une file de priorité
	private HuffmanNode generateHuffmanTreeWithPriorityQueue(int[] frequencies) {
        return null;
	}
	
	// TODO: Faite l'arbre de Huffman sans l'aide d'une file de priorité
	private HuffmanNode generateHuffmanTreeWithoutPriorityQueue(int[] frequencies) {
        return null;
	}
	
	// TODO: Compresse le fichier avec l'algorithme de Huffman, puis l'écrit sur le disque
	//       Faite une version avec et sans file de priorité
	public void compress(String inputFilePath, String outputFilePath) {		
	}
	
	// TODO: Décompresse le fichier avec l'algorithme de Huffman, puis l'écrit sur le disque.
	public void decompress(String inputFilePath, String outputFilePath) {		
	}
	
	public static void main(String[] args) {
		HuffmanCompression hc = new HuffmanCompression();

		// hc.compress("", "");
		
		// hc.decompress("", "");
		
	}
	
}
