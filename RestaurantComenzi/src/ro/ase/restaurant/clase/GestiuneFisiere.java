package ro.ase.restaurant.clase;

import ro.ase.restaurant.exceptii.*;
import ro.ase.restaurant.model.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Operatiile cu fisiere ale aplicatiei:
 * <ul>
 * <li>produse.txt - fisier text, un produs pe linie, campurile separate prin virgula;</li>
 * <li>comenzi.dat - fisier binar, scris cu DataOutputStream;</li>
 * <li>raport.txt - fisier text cu raportul statistic generat.</li>
 * </ul>
 * Datele citite din fisiere sunt validate; liniile sau comenzile invalide
 * sunt semnalate si ignorate, fara a opri aplicatia.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class GestiuneFisiere {

	/** Fisierul text cu produsele restaurantului. */
	public static final String FISIER_PRODUSE = "produse.txt";
	/** Fisierul binar cu comenzile inregistrate. */
	public static final String FISIER_COMENZI = "comenzi.dat";
	/** Fisierul text in care se scrie raportul statistic. */
	public static final String FISIER_RAPORT = "raport.txt";

	/**
	 * Citeste produsele din fisierul text si le adauga in lista si in map-ul primite.
	 * Liniile invalide si identificatorii duplicati sunt semnalati si ignorati.
	 *
	 * @param fisier calea fisierului text
	 * @param listaProduse lista in care se adauga produsele
	 * @param mapProduse map-ul (id produs - produs) in care se adauga produsele
	 * @return numarul de linii invalide intalnite
	 * @throws FileNotFoundException daca fisierul nu exista
	 * @throws IOException daca apare o eroare la citire
	 */
	public static int citireProduse(String fisier, List<Produs> listaProduse,
			Map<Integer, Produs> mapProduse) throws FileNotFoundException, IOException {
		int nrErori = 0;
		int nrLinie = 0;
		String linie = "";
		BufferedReader br = null;
		try {
			FileInputStream fluxIn = new FileInputStream(fisier);
			InputStreamReader inputStreamReader = new InputStreamReader(fluxIn, "UTF-8");
			br = new BufferedReader(inputStreamReader);

			while ((linie = br.readLine()) != null) {
				nrLinie++;
				if (linie.trim().isEmpty())
					continue;
				try {
					Produs p = Produs.dinLinieText(linie);
					if (mapProduse.containsKey(p.getId()))
						throw new ExceptieDateInvalide("Identificator duplicat: " + p.getId());
					listaProduse.add(p);
					mapProduse.put(p.getId(), p);
				}
				catch (ExceptieDateInvalide e) {
					nrErori++;
					System.out.println("  [" + fisier + ", linia " + nrLinie + "] ignorata - "
							+ e.getMessage());
				}
			}
		}
		finally {
			if (br != null)
				br.close();
		}
		return nrErori;
	}

	/**
	 * Salveaza produsele in fisierul text (suprascrie fisierul de intrare).
	 *
	 * @param fisier calea fisierului text
	 * @param listaProduse produsele de salvat
	 * @throws FileNotFoundException daca fisierul nu poate fi creat
	 */
	public static void salvareProduse(String fisier, List<Produs> listaProduse)
			throws FileNotFoundException {
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(fisier), false, "UTF-8");
			for (Produs p : listaProduse)
				ps.append(p.inLinieText() + "\n");
		}
		catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (ps != null)
				ps.close();
		}
	}

	/**
	 * Citeste comenzile din fisierul binar. Fiecare comanda citita este validata:
	 * data si ora corecte, cantitati si preturi valide, produse existente in meniu.
	 * Comenzile invalide sunt semnalate si ignorate.
	 *
	 * @param fisier calea fisierului binar
	 * @param listaComenzi lista in care se adauga comenzile
	 * @param mapProduse produsele restaurantului, pentru verificarea existentei lor
	 * @return numarul de comenzi invalide intalnite
	 * @throws FileNotFoundException daca fisierul nu exista
	 * @throws IOException daca fisierul este deteriorat sau apare o eroare la citire
	 */
	public static int citireComenzi(String fisier, List<Comanda> listaComenzi,
			Map<Integer, Produs> mapProduse) throws FileNotFoundException, IOException {
		int nrErori = 0;
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(fisier));
			int nrComenzi = dis.readInt();
			if (nrComenzi < 0)
				throw new IOException("Numar de comenzi invalid in fisier: " + nrComenzi);

			for (int i = 0; i < nrComenzi; i++) {
				Comanda c = new Comanda();
				c.DeserializareObiect(dis);
				try {
					c.valideaza();
					if (c.getLinii().isEmpty())
						throw new ExceptieDateInvalide("comanda nu contine produse");
					for (LinieComanda l : c.getLinii()) {
						LinieComanda.validareCantitate(l.getCantitate());
						Articol.validarePret(l.getPretUnitar());
						if (!mapProduse.containsKey(l.getIdProdus()))
							throw new ExceptieProdusInexistent("produsul cu id " + l.getIdProdus()
									+ " nu exista in meniu");
					}
					listaComenzi.add(c);
				}
				catch (ExceptieDateInvalide | ExceptieProdusInexistent e) {
					nrErori++;
					System.out.println("  [" + fisier + ", comanda " + c.getId() + "] ignorata - "
							+ e.getMessage());
				}
			}
		}
		catch (EOFException e) {
			throw new IOException("Fisierul " + fisier + " este incomplet (sfarsit neasteptat).");
		}
		finally {
			if (dis != null)
				dis.close();
		}
		return nrErori;
	}

	/**
	 * Salveaza comenzile in fisierul binar (suprascrie fisierul de intrare).
	 * Formatul este: numarul de comenzi, urmat de fiecare comanda serializata.
	 *
	 * @param fisier calea fisierului binar
	 * @param listaComenzi comenzile de salvat
	 * @throws IOException daca apare o eroare la scriere
	 */
	public static void salvareComenzi(String fisier, List<Comanda> listaComenzi) throws IOException {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(new File(fisier)));
			dos.writeInt(listaComenzi.size());
			for (Comanda c : listaComenzi)
				c.SerializareObiect(dos);
		}
		finally {
			if (dos != null)
				dos.close();
		}
	}

	/**
	 * Scrie un text (de exemplu raportul statistic) intr-un fisier text.
	 *
	 * @param fisier calea fisierului
	 * @param continut textul de scris
	 * @throws FileNotFoundException daca fisierul nu poate fi creat
	 */
	public static void scriereText(String fisier, String continut) throws FileNotFoundException {
		PrintStream ps = new PrintStream(new File(fisier));
		ps.append(continut);
		ps.close();
	}
}
