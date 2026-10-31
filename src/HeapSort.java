import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Collections;

public class HeapSort {

	private enum SortingType {
		ASCENDING,
		DESCENDING
	}
	/*
	 * Complexité temporelle : O(n log n)
	 * Justification : L'insertion de n éléments dans la PriorityQueue prend O(n log n)
	 * (car chaque insertion dans un tas binaire prend O(log n)). L'extraction des n éléments
	 * via poll() prend également O(n log n). La complexité totale est donc O(n log n).
	 *
	 * Complexité spatiale : O(n)
	 * Justification : La PriorityQueue stocke l'ensemble des n éléments du tableau
	 * en mémoire additionnelle.
	 */
	static void heapSort(int[] arrayToSort, SortingType sort) {
		// TODO: Faire le triage par monceau sur le tableau en entrée
		PriorityQueue <Integer> heap;
		heap = (sort==SortingType.ASCENDING) ? new PriorityQueue<>() : new PriorityQueue<>(Collections.reverseOrder());

		for (int i = 0; i < arrayToSort.length; i++) {
			heap.add(arrayToSort[i]);
		}
		int index = 0;
		while (!heap.isEmpty()) {
			int element = heap.poll();
			arrayToSort[index++] = element;
		}
	}
	
	public static void main(String[] args) {
		
		// TODO: Faite le triage par monceau d'un tableau d'entier.
		//       Le tableau d'entier peut être généré aléatoirement et
		//       avoir une taille considérable (20 éléments désordonnées).
		//       Trier le même tableau d'entrée en ordre croissant et décroissant
		//       (faite une copie).
		//       Afficher les valeurs avant et après le triage.

		int lenght = 20;
		int[] array = new int[lenght];
		Random random = new Random();

		for (int i = 0; i < lenght; i++) {
			array[i] = random.nextInt(100);
		}

		int[] copieTableau = array.clone();
		System.out.println("Tableau avant le triage: " + Arrays.toString(array));

		heapSort(array, SortingType.ASCENDING);
		heapSort(copieTableau, SortingType.DESCENDING);

		System.out.println("Tableau après le triage croissant: " + Arrays.toString(array));

		System.out.println("Tableau après le triage décroissant: " + Arrays.toString(copieTableau));
	}
}
