package ro.ase.restaurant.main;

import ro.ase.restaurant.clase.GestiuneFisiere;
import ro.ase.restaurant.clase.Rapoarte;
import ro.ase.restaurant.clase.Restaurant;
import ro.ase.restaurant.exceptii.*;
import ro.ase.restaurant.model.*;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Clasa principala a aplicatiei de gestiune a comenzilor unui restaurant.
 * Fluxul aplicatiei: incarcarea datelor din fisiere, meniul principal
 * (toate modificarile se fac in memorie), apoi salvarea datelor in fisierele
 * de intrare la inchiderea aplicatiei.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class Main {

	private static Scanner sc = new Scanner(System.in);
	private static Restaurant restaurant = Restaurant.getRestaurant();
	private static Rapoarte rapoarte = new Rapoarte(restaurant);

	/**
	 * Punctul de intrare in aplicatie.
	 *
	 * @param args nu se folosesc
	 */
	public static void main(String[] args) {
		System.out.println("=== Aplicatie pentru gestiunea comenzilor unui restaurant ===");
		restaurant.incarcareDate();

		// daca aplicatia este oprita fortat (Ctrl+C), datele modificate se salveaza oricum
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (restaurant.isModificat()) {
					System.out.println("\nAplicatia se inchide - se salveaza datele...");
					restaurant.salvareDate();
				}
			}
		});

		boolean continua = true;
		while (continua) {
			afisareMeniuPrincipal();
			try {
				int optiune = citesteInt("Alegeti optiunea: ");
				switch (optiune) {
				case 1: afisareProduse(); break;
				case 2: adaugareProdus(); break;
				case 3: modificarePret(); break;
				case 4: inregistrareComanda(); break;
				case 5: afisareComenzi(); break;
				case 6: detaliiComanda(); break;
				case 7: anulareComanda(); break;
				case 8: meniuRapoarte(); break;
				case 0: continua = false; break;
				default: System.out.println("Optiune inexistenta! Alegeti un numar din meniu.");
				}
			}
			catch (NoSuchElementException e) {
				// nu mai exista date la intrare (ex. consola inchisa)
				continua = false;
			}
		}

		System.out.println("\nSe inchide aplicatia...");
		restaurant.salvareDate();
		System.out.println("La revedere!");
	}

	private static void afisareMeniuPrincipal() {
		System.out.println("\n================ MENIU PRINCIPAL ================");
		System.out.println(" 1. Afisare meniu produse");
		System.out.println(" 2. Adaugare produs nou");
		System.out.println(" 3. Modificare pret produs");
		System.out.println(" 4. Inregistrare comanda noua");
		System.out.println(" 5. Afisare comenzi");
		System.out.println(" 6. Detalii comanda (bon)");
		System.out.println(" 7. Anulare comanda");
		System.out.println(" 8. Rapoarte si statistici");
		System.out.println(" 0. Iesire (cu salvarea datelor)");
		System.out.println("=================================================");
	}

	// --------------------------------------- citire date

	/**
	 * Citeste de la consola un numar intreg; repeta cererea pana la o valoare corecta.
	 *
	 * @param mesaj mesajul afisat utilizatorului
	 * @return numarul citit
	 */
	private static int citesteInt(String mesaj) {
		while (true) {
			System.out.print(mesaj);
			String linie = sc.nextLine().trim();
			try {
				return Integer.parseInt(linie);
			}
			catch (NumberFormatException e) {
				System.out.println("Valoare invalida: \"" + linie + "\". Introduceti un numar intreg.");
			}
		}
	}

	/**
	 * Citeste de la consola un numar real; accepta si virgula ca separator zecimal.
	 *
	 * @param mesaj mesajul afisat utilizatorului
	 * @return numarul citit
	 */
	private static float citesteFloat(String mesaj) {
		while (true) {
			System.out.print(mesaj);
			String linie = sc.nextLine().trim().replace(',', '.');
			try {
				return Float.parseFloat(linie);
			}
			catch (NumberFormatException e) {
				System.out.println("Valoare invalida: \"" + linie + "\". Introduceti un numar (ex. 24.50).");
			}
		}
	}

	/**
	 * Citeste de la consola o data in formatul aaaa-ll-zz.
	 *
	 * @param mesaj mesajul afisat utilizatorului
	 * @return data citita, validata
	 */
	private static String citesteData(String mesaj) {
		while (true) {
			System.out.print(mesaj);
			String data = sc.nextLine().trim();
			try {
				Comanda.validareData(data);
				return data;
			}
			catch (ExceptieDateInvalide e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private static String citesteText(String mesaj) {
		System.out.print(mesaj);
		return sc.nextLine().trim();
	}

	private static boolean citesteDaNu(String mesaj) {
		while (true) {
			String r = citesteText(mesaj + " (d/n): ").toLowerCase();
			if (r.equals("d") || r.equals("da"))
				return true;
			if (r.equals("n") || r.equals("nu"))
				return false;
			System.out.println("Raspundeti cu d sau n.");
		}
	}

	// ---------------------------------------------- produse

	private static void afisareProduse() {
		List<Produs> produse = restaurant.getProduseSortate();
		if (produse.isEmpty()) {
			System.out.println("Meniul nu contine produse.");
			return;
		}
		System.out.println("\nMENIUL RESTAURANTULUI ((V) = vegetarian)");
		for (CategorieProdus cat : CategorieProdus.values()) {
			boolean antet = false;
			for (Produs p : produse)
				if (p.getCategorie() == cat) {
					if (!antet) {
						System.out.println("--- " + cat + " ---");
						antet = true;
					}
					System.out.println(p.genereazaDescriere());
				}
		}
	}

	private static void adaugareProdus() {
		System.out.println("\nADAUGARE PRODUS NOU");
		// fiecare camp se cere din nou pana cand valoarea introdusa este valida
		String denumire;
		while (true) {
			denumire = citesteText("Denumire: ");
			try {
				Articol.validareDenumire(denumire);
				break;
			}
			catch (ExceptieDateInvalide e) {
				System.out.println(e.getMessage());
			}
		}

		CategorieProdus[] categorii = CategorieProdus.values();
		for (int i = 0; i < categorii.length; i++)
			System.out.println("  " + (i + 1) + ". " + categorii[i]);
		int nrCat;
		while (true) {
			nrCat = citesteInt("Categoria (1-" + categorii.length + "): ");
			try {
				if (nrCat < 1 || nrCat > categorii.length)
					throw new ExceptieDateInvalide("Categorie inexistenta: " + nrCat);
				break;
			}
			catch (ExceptieDateInvalide e) {
				System.out.println(e.getMessage());
			}
		}

		float pret;
		while (true) {
			pret = citesteFloat("Pret (lei): ");
			try {
				Articol.validarePret(pret);
				break;
			}
			catch (ExceptieDateInvalide e) {
				System.out.println(e.getMessage());
			}
		}

		String um = (categorii[nrCat - 1] == CategorieProdus.BAUTURA) ? "ml" : "g";
		int gramaj;
		while (true) {
			gramaj = citesteInt("Gramaj (" + um + "): ");
			try {
				Produs.validareGramaj(gramaj);
				break;
			}
			catch (ExceptieDateInvalide e) {
				System.out.println(e.getMessage());
			}
		}
		boolean vegetarian = citesteDaNu("Produs vegetarian?");

		try {

			Produs p = new Produs(restaurant.genereazaIdProdus(), denumire, categorii[nrCat - 1],
					pret, gramaj, vegetarian);
			restaurant.adaugaProdus(p);
			System.out.println("Produs adaugat: " + p.genereazaDescriere());
		}
		catch (ExceptieDateInvalide e) {
			System.out.println("Produsul nu a fost adaugat: " + e.getMessage());
		}
	}

	private static void modificarePret() {
		System.out.println("\nMODIFICARE PRET PRODUS");
		try {
			int id = citesteInt("Id produs: ");
			Produs p = restaurant.cautaProdus(id);
			System.out.println("Pret actual: " + p.getDenumire() + " - " + p.getPret() + " lei");
			float pretNou = citesteFloat("Pret nou (lei): ");
			restaurant.modificaPretProdus(id, pretNou);
			System.out.println("Pretul a fost modificat. Comenzile deja inregistrate nu sunt afectate.");
		}
		catch (ExceptieProdusInexistent | ExceptieDateInvalide e) {
			System.out.println(e.getMessage());
		}
	}

	// ---------------------------------------------- comenzi

	private static void inregistrareComanda() {
		if (restaurant.getListaProduse().isEmpty()) {
			System.out.println("Nu exista produse in meniu - nu se pot inregistra comenzi.");
			return;
		}
		System.out.println("\nINREGISTRARE COMANDA NOUA");
		DateTimeFormatter fd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter fo = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime acum = LocalDateTime.now();

		Comanda comanda = null;
		while (comanda == null) {
			String data = citesteText("Data (aaaa-ll-zz, Enter = azi " + acum.format(fd) + "): ");
			String ora = citesteText("Ora (HH:mm, Enter = acum " + acum.format(fo) + "): ");
			if (data.isEmpty()) data = acum.format(fd);
			if (ora.isEmpty()) ora = acum.format(fo);
			try {
				comanda = new Comanda(restaurant.genereazaIdComanda(), data, ora);
			}
			catch (ExceptieDateInvalide e) {
				System.out.println(e.getMessage());
			}
		}

		afisareProduse();
		System.out.println("\nIntroduceti produsele comandate (id 0 = finalizare comanda).");
		while (true) {
			int id = citesteInt("Id produs: ");
			if (id == 0)
				break;
			try {
				Produs p = restaurant.cautaProdus(id);
				int cant = citesteInt("Cantitate " + p.getDenumire() + ": ");
				comanda.adaugaProdus(p, cant);
				System.out.printf("Adaugat. Total curent: %.2f lei%n", comanda.calculValoareTotala());
			}
			catch (ExceptieProdusInexistent e) {
				System.out.println(e.getMessage() + " Alegeti un produs din meniu.");
			}
			catch (ExceptieDateInvalide e) {
				System.out.println(e.getMessage());
			}
		}

		try {
			restaurant.adaugaComanda(comanda);
			System.out.println("\nComanda a fost inregistrata:");
			System.out.println(comanda.genereazaBon());
		}
		catch (ExceptieDateInvalide e) {
			System.out.println("Comanda nu a fost inregistrata: " + e.getMessage());
		}
	}

	private static void afisareComenzi() {
		List<Comanda> comenzi = restaurant.getListaComenzi();
		if (comenzi.isEmpty()) {
			System.out.println("Nu exista comenzi inregistrate.");
			return;
		}
		System.out.println("\nCOMENZI INREGISTRATE (" + comenzi.size() + ")");
		for (Comanda c : comenzi)
			System.out.println(c);
	}

	private static void detaliiComanda() {
		int id = citesteInt("Id comanda: ");
		Comanda c = restaurant.cautaComanda(id);
		if (c == null)
			System.out.println("Nu exista comanda cu id-ul " + id + ".");
		else
			System.out.println(c.genereazaBon());
	}

	private static void anulareComanda() {
		int id = citesteInt("Id comanda de anulat: ");
		Comanda c = restaurant.cautaComanda(id);
		if (c == null) {
			System.out.println("Nu exista comanda cu id-ul " + id + ".");
			return;
		}
		System.out.println(c.genereazaBon());
		if (citesteDaNu("Sigur anulati aceasta comanda?")) {
			restaurant.stergeComanda(id);
			System.out.println("Comanda a fost anulata.");
		}
		else
			System.out.println("Anulare renuntata.");
	}

	// ------------------------------------------------------------ rapoarte

	private static void meniuRapoarte() {
		boolean continua = true;
		while (continua) {
			System.out.println("\n------------- RAPOARTE SI STATISTICI -------------");
			System.out.println(" 1. Valoarea totala a comenzilor dintr-o zi");
			System.out.println(" 2. Comenzile realizate intr-o zi");
			System.out.println(" 3. Numarul de comenzi dintr-o perioada");
			System.out.println(" 4. Cele mai vandute produse");
			System.out.println(" 5. Vanzarile pentru un anumit produs");
			System.out.println(" 6. Valoarea medie a unei comenzi si alte statistici");
			System.out.println(" 7. Vanzari pe categorii de produse");
			System.out.println(" 8. Vanzari pe categorii si zile ale saptamanii");
			System.out.println(" 9. Comenzi pe intervale orare (ora de varf)");
			System.out.println("10. Vanzari pe zile");
			System.out.println("11. Generare raport complet in " + GestiuneFisiere.FISIER_RAPORT);
			System.out.println(" 0. Inapoi la meniul principal");
			System.out.println("--------------------------------------------------");
			int optiune = citesteInt("Alegeti raportul: ");
			try {
				switch (optiune) {
				case 1: {
					String data = citesteData("Data (aaaa-ll-zz): ");
					System.out.printf("Valoarea comenzilor din %s (%s): %.2f lei (%d comenzi)%n", data,
							Rapoarte.numeZi(data), rapoarte.valoareTotalaZi(data), rapoarte.comenziZi(data).size());
					break;
				}
				case 2: {
					String data = citesteData("Data (aaaa-ll-zz): ");
					List<Comanda> comenzi = rapoarte.comenziZi(data);
					if (comenzi.isEmpty())
						System.out.println("Nu exista comenzi in " + data + ".");
					for (Comanda c : comenzi)
						System.out.println(c.genereazaBon());
					break;
				}
				case 3: {
					String d1 = citesteData("Data de inceput (aaaa-ll-zz): ");
					String d2 = citesteData("Data de sfarsit (aaaa-ll-zz): ");
					List<Comanda> comenzi = rapoarte.comenziPerioada(d1, d2);
					float total = 0;
					for (Comanda c : comenzi)
						total += c.calculValoareTotala();
					System.out.printf("In perioada %s - %s: %d comenzi, valoare totala %.2f lei%n", d1, d2,
							rapoarte.nrComenziPerioada(d1, d2), total);
					break;
				}
				case 4: {
					int n = citesteInt("Cate produse in clasament? ");
					if (n <= 0)
						throw new ExceptieDateInvalide("Numarul trebuie sa fie pozitiv!");
					System.out.print(rapoarte.topProduseVandute(n));
					break;
				}
				case 5: {
					int id = citesteInt("Id produs: ");
					Produs p = restaurant.cautaProdus(id);
					System.out.printf("%s: %d buc. vandute, valoare totala %.2f lei%n", p.getDenumire(),
							rapoarte.cantitateVandutaProdus(id), rapoarte.valoareVanzariProdus(id));
					break;
				}
				case 6: System.out.print(rapoarte.statisticiValoriComenzi()); break;
				case 7: System.out.print(rapoarte.raportCategorii()); break;
				case 8: System.out.print(rapoarte.raportMatriceCategoriiZile()); break;
				case 9: System.out.print(rapoarte.raportOreDeVarf()); break;
				case 10: System.out.print(rapoarte.raportZilnic()); break;
				case 11: {
					String raport = rapoarte.raportComplet();
					GestiuneFisiere.scriereText(GestiuneFisiere.FISIER_RAPORT, raport);
					System.out.print(raport);
					System.out.println("Raportul a fost salvat in " + GestiuneFisiere.FISIER_RAPORT);
					break;
				}
				case 0: continua = false; break;
				default: System.out.println("Optiune inexistenta!");
				}
			}
			catch (ExceptieDateInvalide | ExceptieProdusInexistent e) {
				System.out.println(e.getMessage());
			}
			catch (FileNotFoundException e) {
				System.out.println("Raportul nu a putut fi salvat: " + e.getMessage());
			}
		}
	}
}
