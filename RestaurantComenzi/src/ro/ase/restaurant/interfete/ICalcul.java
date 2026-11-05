package ro.ase.restaurant.interfete;

/**
 * Interfata pentru calculele asociate unei comenzi.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public interface ICalcul {

	/**
	 * Calculeaza valoarea totala.
	 *
	 * @return valoarea totala, in lei
	 */
	float calculValoareTotala();

	/**
	 * Calculeaza numarul total de produse (bucati).
	 *
	 * @return numarul de produse
	 */
	int calculNrProduse();
}
