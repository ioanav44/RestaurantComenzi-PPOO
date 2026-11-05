package ro.ase.restaurant.model;

import ro.ase.restaurant.exceptii.*;
import ro.ase.restaurant.interfete.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Comanda inregistrata in restaurant: identificator, data, ora si
 * lista de linii (produs + cantitate). Valoarea totala se calculeaza
 * pe baza produselor si cantitatilor comandate.
 * Comenzile sunt ordonate cronologic (data, ora, apoi identificator).
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class Comanda implements ICalcul, ISerializare, Comparable<Comanda> {

	private int id;
	private String data;
	private String ora;
	private List<LinieComanda> linii = new ArrayList<LinieComanda>();

	/**
	 * Constructor fara parametri, folosit la deserializare.
	 */
	public Comanda() {
		this.id = 0;
		this.data = null;
		this.ora = null;
	}

	/**
	 * Construieste o comanda goala, fara produse.
	 *
	 * @param id identificatorul comenzii
	 * @param data data comenzii, in formatul aaaa-ll-zz
	 * @param ora ora comenzii, in formatul HH:mm
	 * @throws ExceptieDateInvalide daca identificatorul, data sau ora nu sunt valide
	 */
	public Comanda(int id, String data, String ora) throws ExceptieDateInvalide {
		this.id = id;
		this.data = data;
		this.ora = ora;
		valideaza();
	}

	/**
	 * Verifica datele de identificare ale comenzii (folosita si dupa citirea din fisier).
	 *
	 * @throws ExceptieDateInvalide daca identificatorul, data sau ora nu sunt valide
	 */
	public void valideaza() throws ExceptieDateInvalide {
		if (id <= 0)
			throw new ExceptieDateInvalide("Identificator de comanda invalid: " + id);
		validareData(data);
		try {
			LocalTime.parse(ora);
		}
		catch (DateTimeParseException | NullPointerException e) {
			throw new ExceptieDateInvalide("Ora invalida: " + ora + " (format corect HH:mm)");
		}
	}

	/**
	 * Verifica daca un sir de caractere este o data valida, in formatul aaaa-ll-zz.
	 *
	 * @param data data de verificat
	 * @throws ExceptieDateInvalide daca data nu este valida
	 */
	public static void validareData(String data) throws ExceptieDateInvalide {
		try {
			LocalDate.parse(data);
		}
		catch (DateTimeParseException | NullPointerException e) {
			throw new ExceptieDateInvalide("Data invalida: " + data + " (format corect aaaa-ll-zz)");
		}
	}

	/**
	 * Adauga un produs in comanda. Daca produsul exista deja in comanda,
	 * cantitatea se aduna la cea existenta.
	 *
	 * @param produs produsul de adaugat
	 * @param cantitate cantitatea comandata
	 * @throws ExceptieDateInvalide daca cantitatea (sau cantitatea cumulata) nu este valida
	 */
	public void adaugaProdus(Produs produs, int cantitate) throws ExceptieDateInvalide {
		for (LinieComanda l : linii)
			if (l.getIdProdus() == produs.getId()) {
				l.setCantitate(l.getCantitate() + cantitate);
				return;
			}
		linii.add(new LinieComanda(produs, cantitate));
	}

	/**
	 * Calculeaza valoarea totala a comenzii, ca suma valorilor liniilor.
	 */
	@Override
	public float calculValoareTotala() {
		float suma = 0.0f;
		for (LinieComanda l : linii)
			suma += l.calculValoare();
		return suma;
	}

	/**
	 * Calculeaza numarul total de bucati comandate.
	 */
	@Override
	public int calculNrProduse() {
		int nr = 0;
		for (LinieComanda l : linii)
			nr += l.getCantitate();
		return nr;
	}

	/**
	 * Genereaza bonul comenzii, cu toate liniile si totalul.
	 *
	 * @return textul bonului
	 */
	public String genereazaBon() {
		String s = "Comanda nr. " + id + " din " + data + " ora " + ora + "\n";
		for (LinieComanda l : linii)
			s += "   " + l + "\n";
		s += String.format("   TOTAL: %.2f lei (%d produse)", calculValoareTotala(), calculNrProduse());
		return s;
	}

	public int getId() {
		return id;
	}

	public String getData() {
		return data;
	}

	public String getOra() {
		return ora;
	}

	public List<LinieComanda> getLinii() {
		return linii;
	}

	@Override
	public void SerializareObiect(DataOutputStream dos) throws IOException {
		dos.writeInt(id);
		dos.writeUTF(data);
		dos.writeUTF(ora);
		dos.writeInt(linii.size());
		for (LinieComanda l : linii)
			l.SerializareObiect(dos);
	}

	@Override
	public void DeserializareObiect(DataInputStream dis) throws IOException {
		id = dis.readInt();
		data = dis.readUTF();
		ora = dis.readUTF();
		int nrLinii = dis.readInt();
		if (nrLinii < 0 || nrLinii > 1000)
			throw new IOException("Numar de linii invalid in fisier: " + nrLinii);
		linii = new ArrayList<LinieComanda>();
		for (int i = 0; i < nrLinii; i++) {
			LinieComanda l = new LinieComanda();
			l.DeserializareObiect(dis);
			linii.add(l);
		}
	}

	/**
	 * Compara doua comenzi cronologic: dupa data si ora, apoi dupa identificator.
	 */
	@Override
	public int compareTo(Comanda c) {
		int lastCmp = (data + " " + ora).compareTo(c.data + " " + c.ora);
		if (lastCmp == 0)
			if (id < c.id)
				return -1;
			else
				if (id > c.id)
					return 1;
				else return 0;
		return lastCmp;
	}

	@Override
	public String toString() {
		return String.format("Comanda nr. %-4d %s %s  %2d produse  %9.2f lei", id, data, ora,
				calculNrProduse(), calculValoareTotala());
	}
}
