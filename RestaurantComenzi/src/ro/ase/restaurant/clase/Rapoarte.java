package ro.ase.restaurant.clase;

import ro.ase.restaurant.exceptii.*;
import ro.ase.restaurant.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rapoartele si situatiile statistice ale restaurantului, calculate pe baza
 * comenzilor inregistrate. Clasa foloseste:
 * <ul>
 * <li>vectori de tipuri fundamentale: float[] (valorile comenzilor),
 * int[] si float[] (cantitati si valori vandute pe produs), int[] (comenzi pe ore);</li>
 * <li>o matrice float[][] (vanzari pe categorii si zile ale saptamanii);</li>
 * <li>colectii: HashMap (vanzari pe categorii), HashSet (zilele cu comenzi).</li>
 * </ul>
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class Rapoarte {

	private static final String[] ZILE_SAPTAMANA = { "Luni", "Marti", "Miercuri", "Joi",
			"Vineri", "Sambata", "Duminica" };

	private Restaurant restaurant;

	/**
	 * Construieste generatorul de rapoarte pentru un restaurant.
	 *
	 * @param restaurant restaurantul ale carui comenzi se analizeaza
	 */
	public Rapoarte(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	/**
	 * Intoarce comenzile realizate intr-o anumita zi.
	 *
	 * @param data ziua, in formatul aaaa-ll-zz
	 * @return lista comenzilor din ziua respectiva (ordonate cronologic)
	 * @throws ExceptieDateInvalide daca data nu este valida
	 */
	public List<Comanda> comenziZi(String data) throws ExceptieDateInvalide {
		Comanda.validareData(data);
		List<Comanda> rezultat = new ArrayList<Comanda>();
		for (Comanda c : restaurant.getListaComenzi())
			if (c.getData().equals(data))
				rezultat.add(c);
		return rezultat;
	}

	/**
	 * Calculeaza valoarea totala a comenzilor dintr-o anumita zi.
	 *
	 * @param data ziua, in formatul aaaa-ll-zz
	 * @return valoarea totala, in lei
	 * @throws ExceptieDateInvalide daca data nu este valida
	 */
	public float valoareTotalaZi(String data) throws ExceptieDateInvalide {
		float suma = 0;
		for (Comanda c : comenziZi(data))
			suma += c.calculValoareTotala();
		return suma;
	}

	/**
	 * Intoarce comenzile realizate intr-o perioada (capetele incluse).
	 *
	 * @param dataInceput prima zi a perioadei
	 * @param dataSfarsit ultima zi a perioadei
	 * @return comenzile din perioada
	 * @throws ExceptieDateInvalide daca datele nu sunt valide sau inceputul este dupa sfarsit
	 */
	public List<Comanda> comenziPerioada(String dataInceput, String dataSfarsit) throws ExceptieDateInvalide {
		Comanda.validareData(dataInceput);
		Comanda.validareData(dataSfarsit);
		if (dataInceput.compareTo(dataSfarsit) > 0)
			throw new ExceptieDateInvalide("Data de inceput (" + dataInceput
					+ ") este dupa data de sfarsit (" + dataSfarsit + ")!");
		List<Comanda> rezultat = new ArrayList<Comanda>();
		for (Comanda c : restaurant.getListaComenzi())
			if (c.getData().compareTo(dataInceput) >= 0 && c.getData().compareTo(dataSfarsit) <= 0)
				rezultat.add(c);
		return rezultat;
	}

	/**
	 * Calculeaza numarul total de comenzi dintr-o perioada.
	 *
	 * @param dataInceput prima zi a perioadei
	 * @param dataSfarsit ultima zi a perioadei
	 * @return numarul de comenzi
	 * @throws ExceptieDateInvalide daca perioada nu este valida
	 */
	public int nrComenziPerioada(String dataInceput, String dataSfarsit) throws ExceptieDateInvalide {
		return comenziPerioada(dataInceput, dataSfarsit).size();
	}

	/**
	 * Calculeaza valoarea totala vanduta dintr-un anumit produs.
	 *
	 * @param idProdus identificatorul produsului
	 * @return valoarea totala, in lei
	 * @throws ExceptieProdusInexistent daca produsul nu exista
	 */
	public float valoareVanzariProdus(int idProdus) throws ExceptieProdusInexistent {
		restaurant.cautaProdus(idProdus);
		float suma = 0;
		for (Comanda c : restaurant.getListaComenzi())
			for (LinieComanda l : c.getLinii())
				if (l.getIdProdus() == idProdus)
					suma += l.calculValoare();
		return suma;
	}

	/**
	 * Calculeaza cantitatea totala vanduta dintr-un anumit produs.
	 *
	 * @param idProdus identificatorul produsului
	 * @return cantitatea totala
	 * @throws ExceptieProdusInexistent daca produsul nu exista
	 */
	public int cantitateVandutaProdus(int idProdus) throws ExceptieProdusInexistent {
		restaurant.cautaProdus(idProdus);
		int cant = 0;
		for (Comanda c : restaurant.getListaComenzi())
			for (LinieComanda l : c.getLinii())
				if (l.getIdProdus() == idProdus)
					cant += l.getCantitate();
		return cant;
	}

	/**
	 * Genereaza clasamentul celor mai vandute produse, dupa cantitate.
	 * Se folosesc vectorii paraleli Produs[], int[] (cantitati) si float[] (valori),
	 * sortati descrescator dupa cantitate prin metoda selectiei.
	 *
	 * @param n numarul de produse afisate in clasament
	 * @return textul clasamentului
	 */
	public String topProduseVandute(int n) {
		Produs[] vectProduse = restaurant.getListaProduse().toArray(new Produs[0]);
		int[] cantitati = new int[vectProduse.length];
		float[] valori = new float[vectProduse.length];

		for (int i = 0; i < vectProduse.length; i++)
			for (Comanda c : restaurant.getListaComenzi())
				for (LinieComanda l : c.getLinii())
					if (l.getIdProdus() == vectProduse[i].getId()) {
						cantitati[i] += l.getCantitate();
						valori[i] += l.calculValoare();
					}

		// sortare descrescatoare dupa cantitate (la egalitate, dupa valoare)
		for (int i = 0; i < vectProduse.length - 1; i++)
			for (int j = i + 1; j < vectProduse.length; j++)
				if (cantitati[j] > cantitati[i] || (cantitati[j] == cantitati[i] && valori[j] > valori[i])) {
					int auxC = cantitati[i]; cantitati[i] = cantitati[j]; cantitati[j] = auxC;
					float auxV = valori[i]; valori[i] = valori[j]; valori[j] = auxV;
					Produs auxP = vectProduse[i]; vectProduse[i] = vectProduse[j]; vectProduse[j] = auxP;
				}

		String s = "Top " + n + " cele mai vandute produse (dupa cantitate):\n";
		int afisate = 0;
		for (int i = 0; i < vectProduse.length && afisate < n; i++) {
			if (cantitati[i] == 0)
				break;
			afisate++;
			s += String.format("  %2d. %-28s %4d buc.  %10.2f lei\n", afisate,
					vectProduse[i].getDenumire(), cantitati[i], valori[i]);
		}
		if (afisate == 0)
			s += "  Nu exista vanzari.\n";
		return s;
	}

	/**
	 * Calculeaza statistici asupra valorilor comenzilor (numar, total, medie,
	 * minim, maxim, mediana), folosind un vector float[].
	 *
	 * @return textul cu statisticile
	 */
	public String statisticiValoriComenzi() {
		List<Comanda> comenzi = restaurant.getListaComenzi();
		if (comenzi.isEmpty())
			return "Nu exista comenzi inregistrate.\n";

		float[] valori = new float[comenzi.size()];
		for (int i = 0; i < valori.length; i++)
			valori[i] = comenzi.get(i).calculValoareTotala();

		float total = 0, min = valori[0], max = valori[0];
		for (int i = 0; i < valori.length; i++) {
			total += valori[i];
			if (valori[i] < min) min = valori[i];
			if (valori[i] > max) max = valori[i];
		}

		float[] sortate = valori.clone();
		java.util.Arrays.sort(sortate);
		float mediana = (sortate.length % 2 == 1) ? sortate[sortate.length / 2]
				: (sortate[sortate.length / 2 - 1] + sortate[sortate.length / 2]) / 2;

		String s = "Statistici comenzi:\n";
		s += String.format("  Numar comenzi:          %d\n", valori.length);
		s += String.format("  Valoare totala vanzari: %.2f lei\n", total);
		s += String.format("  Valoare medie comanda:  %.2f lei\n", total / valori.length);
		s += String.format("  Valoare mediana:        %.2f lei\n", mediana);
		s += String.format("  Comanda minima:         %.2f lei\n", min);
		s += String.format("  Comanda maxima:         %.2f lei\n", max);
		return s;
	}

	/**
	 * Calculeaza valoarea medie a unei comenzi.
	 *
	 * @return valoarea medie, sau 0 daca nu exista comenzi
	 */
	public float valoareMedieComanda() {
		List<Comanda> comenzi = restaurant.getListaComenzi();
		if (comenzi.isEmpty())
			return 0;
		float total = 0;
		for (Comanda c : comenzi)
			total += c.calculValoareTotala();
		return total / comenzi.size();
	}

	/**
	 * Calculeaza valoarea vanzarilor pe fiecare categorie de produse (HashMap).
	 *
	 * @return map-ul categorie - valoare vanduta
	 */
	public Map<CategorieProdus, Float> vanzariPeCategorii() {
		Map<CategorieProdus, Float> map = new HashMap<CategorieProdus, Float>();
		for (CategorieProdus cat : CategorieProdus.values())
			map.put(cat, 0.0f);
		for (Comanda c : restaurant.getListaComenzi())
			for (LinieComanda l : c.getLinii()) {
				Produs p = restaurant.getMapProduse().get(l.getIdProdus());
				if (p != null)
					map.put(p.getCategorie(), map.get(p.getCategorie()) + l.calculValoare());
			}
		return map;
	}

	/**
	 * Genereaza situatia vanzarilor pe categorii.
	 *
	 * @return textul situatiei
	 */
	public String raportCategorii() {
		Map<CategorieProdus, Float> map = vanzariPeCategorii();
		float total = 0;
		for (Float v : map.values())
			total += v;
		String s = "Vanzari pe categorii de produse:\n";
		for (CategorieProdus cat : CategorieProdus.values()) {
			float v = map.get(cat);
			s += String.format("  %-14s %10.2f lei  (%5.1f%%)\n", cat, v, total > 0 ? v * 100 / total : 0);
		}
		return s;
	}

	/**
	 * Construieste matricea vanzarilor: linii = categorii de produse,
	 * coloane = zilele saptamanii (luni - duminica).
	 *
	 * @return matricea float[nrCategorii][7]
	 */
	public float[][] matriceVanzariCategoriiZile() {
		CategorieProdus[] categorii = CategorieProdus.values();
		float[][] matrice = new float[categorii.length][7];
		for (Comanda c : restaurant.getListaComenzi()) {
			int zi = LocalDate.parse(c.getData()).getDayOfWeek().getValue() - 1;
			for (LinieComanda l : c.getLinii()) {
				Produs p = restaurant.getMapProduse().get(l.getIdProdus());
				if (p != null)
					matrice[p.getCategorie().ordinal()][zi] += l.calculValoare();
			}
		}
		return matrice;
	}

	/**
	 * Genereaza tabelul vanzarilor pe categorii si zile ale saptamanii,
	 * cu totaluri pe linii si pe coloane, pe baza matricei float[][].
	 *
	 * @return textul tabelului
	 */
	public String raportMatriceCategoriiZile() {
		float[][] m = matriceVanzariCategoriiZile();
		CategorieProdus[] categorii = CategorieProdus.values();
		String s = "Vanzari (lei) pe categorii si zile ale saptamanii:\n";
		s += String.format("  %-14s", "");
		for (int j = 0; j < 7; j++)
			s += String.format("%10s", ZILE_SAPTAMANA[j]);
		s += String.format("%11s\n", "TOTAL");

		float[] totalColoane = new float[7];
		for (int i = 0; i < m.length; i++) {
			float totalLinie = 0;
			s += String.format("  %-14s", categorii[i]);
			for (int j = 0; j < 7; j++) {
				s += String.format("%10.2f", m[i][j]);
				totalLinie += m[i][j];
				totalColoane[j] += m[i][j];
			}
			s += String.format("%11.2f\n", totalLinie);
		}
		float totalGeneral = 0;
		s += String.format("  %-14s", "TOTAL");
		for (int j = 0; j < 7; j++) {
			s += String.format("%10.2f", totalColoane[j]);
			totalGeneral += totalColoane[j];
		}
		s += String.format("%11.2f\n", totalGeneral);
		return s;
	}

	/**
	 * Determina numarul de comenzi pe fiecare ora a zilei (vector int[24])
	 * si ora de varf a restaurantului.
	 *
	 * @return textul situatiei
	 */
	public String raportOreDeVarf() {
		int[] comenziPeOra = new int[24];
		for (Comanda c : restaurant.getListaComenzi())
			comenziPeOra[Integer.parseInt(c.getOra().substring(0, 2))]++;

		int oraVarf = 0;
		for (int h = 1; h < 24; h++)
			if (comenziPeOra[h] > comenziPeOra[oraVarf])
				oraVarf = h;

		String s = "Numar de comenzi pe intervale orare:\n";
		for (int h = 0; h < 24; h++)
			if (comenziPeOra[h] > 0) {
				s += String.format("  %02d:00-%02d:59  %3d  ", h, h, comenziPeOra[h]);
				for (int k = 0; k < comenziPeOra[h]; k++)
					s += "#";
				s += "\n";
			}
		if (comenziPeOra[oraVarf] > 0)
			s += String.format("  Ora de varf: %02d:00-%02d:59 (%d comenzi)\n", oraVarf, oraVarf,
					comenziPeOra[oraVarf]);
		else
			s += "  Nu exista comenzi.\n";
		return s;
	}

	/**
	 * Determina zilele distincte in care s-au inregistrat comenzi (HashSet).
	 *
	 * @return multimea zilelor cu comenzi
	 */
	public Set<String> zileCuComenzi() {
		Set<String> zile = new HashSet<String>();
		for (Comanda c : restaurant.getListaComenzi())
			zile.add(c.getData());
		return zile;
	}

	/**
	 * Genereaza situatia vanzarilor pe fiecare zi cu comenzi.
	 *
	 * @return textul situatiei
	 */
	public String raportZilnic() {
		Set<String> zileSortate = new TreeSet<String>(zileCuComenzi());
		String s = "Vanzari pe zile (" + zileSortate.size() + " zile cu comenzi):\n";
		for (String zi : zileSortate) {
			try {
				int zs = LocalDate.parse(zi).getDayOfWeek().getValue() - 1;
				s += String.format("  %s %-9s %3d comenzi  %10.2f lei\n", zi, ZILE_SAPTAMANA[zs],
						comenziZi(zi).size(), valoareTotalaZi(zi));
			}
			catch (ExceptieDateInvalide e) {
				s += "  " + zi + " - data invalida\n";
			}
		}
		return s;
	}

	/**
	 * Intoarce comanda cu valoarea cea mai mare.
	 *
	 * @return comanda maxima sau null daca nu exista comenzi
	 */
	public Comanda comandaMaxima() {
		Comanda max = null;
		for (Comanda c : restaurant.getListaComenzi())
			if (max == null || c.calculValoareTotala() > max.calculValoareTotala())
				max = c;
		return max;
	}

	/**
	 * Genereaza raportul complet al restaurantului, cu toate situatiile statistice.
	 *
	 * @return textul raportului
	 */
	public String raportComplet() {
		String linie = "==========================================================================================\n";
		String s = linie + "RAPORT STATISTIC - GESTIUNEA COMENZILOR RESTAURANTULUI\n";
		s += "Generat la: " + java.time.LocalDateTime.now().withNano(0).toString().replace('T', ' ') + "\n";
		s += "Produse in meniu: " + restaurant.getListaProduse().size() + "\n" + linie;
		s += statisticiValoriComenzi() + "\n";
		s += raportZilnic() + "\n";
		s += topProduseVandute(10) + "\n";
		s += raportCategorii() + "\n";
		s += raportMatriceCategoriiZile() + "\n";
		s += raportOreDeVarf() + "\n";
		Comanda max = comandaMaxima();
		if (max != null)
			s += "Comanda cu cea mai mare valoare:\n" + max.genereazaBon() + "\n";
		s += linie;
		return s;
	}

	/**
	 * Intoarce numele zilei saptamanii pentru o data.
	 *
	 * @param data data, in formatul aaaa-ll-zz
	 * @return numele zilei in limba romana
	 */
	public static String numeZi(String data) {
		DayOfWeek d = LocalDate.parse(data).getDayOfWeek();
		return ZILE_SAPTAMANA[d.getValue() - 1];
	}
}
