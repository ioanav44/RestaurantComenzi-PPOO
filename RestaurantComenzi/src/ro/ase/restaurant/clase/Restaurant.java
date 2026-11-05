package ro.ase.restaurant.clase;

import ro.ase.restaurant.exceptii.*;
import ro.ase.restaurant.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Restaurantul - clasa Singleton care pastreaza in memorie toate datele aplicatiei:
 * <ul>
 * <li>lista de produse (ArrayList&lt;Produs&gt;), pentru afisare si sortare;</li>
 * <li>map-ul de produse (HashMap&lt;Integer, Produs&gt;), pentru cautarea rapida dupa id;</li>
 * <li>lista de comenzi (ArrayList&lt;Comanda&gt;).</li>
 * </ul>
 * Toate modificarile se fac in memorie; datele se incarca din fisiere la pornire
 * si se salveaza in aceleasi fisiere la inchiderea aplicatiei.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public final class Restaurant {

	private static Restaurant obj;

	private List<Produs> listaProduse = new ArrayList<Produs>();
	private Map<Integer, Produs> mapProduse = new HashMap<Integer, Produs>();
	private List<Comanda> listaComenzi = new ArrayList<Comanda>();
	private boolean modificat = false;

	private Restaurant() {
	}

	/**
	 * Intoarce unica instanta a restaurantului.
	 *
	 * @return instanta restaurantului
	 */
	public static Restaurant getRestaurant() {
		if (obj == null)
			obj = new Restaurant();
		return obj;
	}

	/**
	 * Incarca produsele si comenzile din fisiere. Erorile de citire sunt tratate
	 * aici: aplicatia porneste si daca fisierele lipsesc sau sunt deteriorate.
	 */
	public void incarcareDate() {
		System.out.println("Se incarca datele...");
		try {
			int erori = GestiuneFisiere.citireProduse(GestiuneFisiere.FISIER_PRODUSE, listaProduse, mapProduse);
			System.out.println("  Produse incarcate: " + listaProduse.size()
					+ (erori > 0 ? " (linii ignorate: " + erori + ")" : ""));
		}
		catch (FileNotFoundException e) {
			System.out.println("  Fisierul " + GestiuneFisiere.FISIER_PRODUSE
					+ " nu exista. Se porneste cu meniul gol.");
		}
		catch (IOException e) {
			System.out.println("  Eroare la citirea produselor: " + e.getMessage());
		}

		try {
			int erori = GestiuneFisiere.citireComenzi(GestiuneFisiere.FISIER_COMENZI, listaComenzi, mapProduse);
			System.out.println("  Comenzi incarcate: " + listaComenzi.size()
					+ (erori > 0 ? " (comenzi ignorate: " + erori + ")" : ""));
		}
		catch (FileNotFoundException e) {
			System.out.println("  Fisierul " + GestiuneFisiere.FISIER_COMENZI
					+ " nu exista. Nu exista comenzi salvate.");
		}
		catch (IOException e) {
			System.out.println("  Eroare la citirea comenzilor: " + e.getMessage());
			System.out.println("  Se pastreaza comenzile citite corect: " + listaComenzi.size());
		}
		Collections.sort(listaComenzi);
	}

	/**
	 * Salveaza produsele si comenzile in fisierele de intrare, daca au existat modificari.
	 *
	 * @return true daca salvarea s-a realizat cu succes (sau nu a fost necesara)
	 */
	public boolean salvareDate() {
		if (!modificat) {
			System.out.println("Nu exista modificari de salvat.");
			return true;
		}
		boolean ok = true;
		try {
			GestiuneFisiere.salvareProduse(GestiuneFisiere.FISIER_PRODUSE, listaProduse);
			System.out.println("Produsele au fost salvate in " + GestiuneFisiere.FISIER_PRODUSE);
		}
		catch (FileNotFoundException e) {
			ok = false;
			System.out.println("Eroare la salvarea produselor: " + e.getMessage());
		}
		try {
			GestiuneFisiere.salvareComenzi(GestiuneFisiere.FISIER_COMENZI, listaComenzi);
			System.out.println("Comenzile au fost salvate in " + GestiuneFisiere.FISIER_COMENZI);
		}
		catch (IOException e) {
			ok = false;
			System.out.println("Eroare la salvarea comenzilor: " + e.getMessage());
		}
		if (ok)
			modificat = false;
		return ok;
	}

	/**
	 * Cauta un produs dupa identificator.
	 *
	 * @param id identificatorul cautat
	 * @return produsul gasit
	 * @throws ExceptieProdusInexistent daca nu exista niciun produs cu acest id
	 */
	public Produs cautaProdus(int id) throws ExceptieProdusInexistent {
		Produs p = mapProduse.get(id);
		if (p == null)
			throw new ExceptieProdusInexistent("Nu exista niciun produs cu id-ul " + id + "!");
		return p;
	}

	/**
	 * Adauga un produs nou in meniu (in memorie).
	 *
	 * @param p produsul de adaugat
	 * @throws ExceptieDateInvalide daca exista deja un produs cu acelasi id sau aceeasi denumire
	 */
	public void adaugaProdus(Produs p) throws ExceptieDateInvalide {
		if (mapProduse.containsKey(p.getId()))
			throw new ExceptieDateInvalide("Exista deja un produs cu id-ul " + p.getId());
		for (Produs pr : listaProduse)
			if (pr.getDenumire().equalsIgnoreCase(p.getDenumire()))
				throw new ExceptieDateInvalide("Exista deja produsul \"" + p.getDenumire() + "\"");
		listaProduse.add(p);
		mapProduse.put(p.getId(), p);
		modificat = true;
	}

	/**
	 * Modifica pretul unui produs (in memorie). Comenzile deja inregistrate
	 * isi pastreaza pretul de la momentul comenzii.
	 *
	 * @param id identificatorul produsului
	 * @param pretNou noul pret
	 * @throws ExceptieProdusInexistent daca produsul nu exista
	 * @throws ExceptieDateInvalide daca pretul nu este valid
	 */
	public void modificaPretProdus(int id, float pretNou) throws ExceptieProdusInexistent, ExceptieDateInvalide {
		Produs p = cautaProdus(id);
		p.setPret(pretNou);
		modificat = true;
	}

	/**
	 * Inregistreaza o comanda noua (in memorie).
	 *
	 * @param c comanda de inregistrat
	 * @throws ExceptieDateInvalide daca comanda nu contine produse
	 */
	public void adaugaComanda(Comanda c) throws ExceptieDateInvalide {
		if (c.getLinii().isEmpty())
			throw new ExceptieDateInvalide("Comanda nu contine niciun produs!");
		listaComenzi.add(c);
		Collections.sort(listaComenzi);
		modificat = true;
	}

	/**
	 * Cauta o comanda dupa identificator.
	 *
	 * @param id identificatorul comenzii
	 * @return comanda gasita sau null daca nu exista
	 */
	public Comanda cautaComanda(int id) {
		for (Comanda c : listaComenzi)
			if (c.getId() == id)
				return c;
		return null;
	}

	/**
	 * Sterge (anuleaza) o comanda (in memorie).
	 *
	 * @param id identificatorul comenzii
	 * @return true daca a fost stearsa, false daca nu exista
	 */
	public boolean stergeComanda(int id) {
		Comanda c = cautaComanda(id);
		if (c == null)
			return false;
		listaComenzi.remove(c);
		modificat = true;
		return true;
	}

	/**
	 * Genereaza urmatorul identificator liber pentru un produs.
	 *
	 * @return cel mai mare id existent + 1
	 */
	public int genereazaIdProdus() {
		int max = 0;
		for (Produs p : listaProduse)
			if (p.getId() > max)
				max = p.getId();
		return max + 1;
	}

	/**
	 * Genereaza urmatorul identificator liber pentru o comanda.
	 *
	 * @return cel mai mare id existent + 1
	 */
	public int genereazaIdComanda() {
		int max = 0;
		for (Comanda c : listaComenzi)
			if (c.getId() > max)
				max = c.getId();
		return max + 1;
	}

	/**
	 * Intoarce produsele sortate dupa ordinea naturala (denumire, apoi pret).
	 *
	 * @return o lista noua, sortata
	 */
	public List<Produs> getProduseSortate() {
		List<Produs> lista = new ArrayList<Produs>(listaProduse);
		Collections.sort(lista);
		return lista;
	}

	public List<Produs> getListaProduse() {
		return listaProduse;
	}
	public Map<Integer, Produs> getMapProduse() {
		return mapProduse;
	}
	public List<Comanda> getListaComenzi() {
		return listaComenzi;
	}
	public boolean isModificat() {
		return modificat;
	}
}
