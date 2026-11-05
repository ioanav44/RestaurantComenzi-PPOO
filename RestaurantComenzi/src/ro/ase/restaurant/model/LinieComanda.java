package ro.ase.restaurant.model;

import ro.ase.restaurant.exceptii.*;
import ro.ase.restaurant.interfete.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * O linie dintr-o comanda: un produs si cantitatea comandata.
 * Pretul unitar este retinut la momentul comenzii, astfel incat o modificare
 * ulterioara a pretului din meniu nu schimba valoarea comenzilor vechi.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class LinieComanda implements ISerializare {

	/** Cantitatea maxima permisa pentru un produs pe o comanda. */
	public static final int CANTITATE_MAXIMA = 50;

	private int idProdus;
	private String denumireProdus;
	private int cantitate;
	private float pretUnitar;

	/**
	 * Constructor fara parametri, folosit la deserializare.
	 */
	public LinieComanda() {
		this.idProdus = 0;
		this.denumireProdus = null;
		this.cantitate = 0;
		this.pretUnitar = 0;
	}

	/**
	 * Construieste o linie de comanda pentru un produs existent.
	 *
	 * @param produs produsul comandat
	 * @param cantitate cantitatea comandata
	 * @throws ExceptieDateInvalide daca cantitatea nu este valida
	 */
	public LinieComanda(Produs produs, int cantitate) throws ExceptieDateInvalide {
		validareCantitate(cantitate);
		this.idProdus = produs.getId();
		this.denumireProdus = produs.getDenumire();
		this.pretUnitar = produs.getPret();
		this.cantitate = cantitate;
	}

	/**
	 * Verifica daca o cantitate este valida.
	 *
	 * @param cantitate cantitatea de verificat
	 * @throws ExceptieDateInvalide daca nu este intre 1 si {@value #CANTITATE_MAXIMA}
	 */
	public static void validareCantitate(int cantitate) throws ExceptieDateInvalide {
		if (cantitate <= 0 || cantitate > CANTITATE_MAXIMA)
			throw new ExceptieDateInvalide("Cantitate invalida: " + cantitate
					+ " (trebuie sa fie intre 1 si " + CANTITATE_MAXIMA + ")");
	}

	/**
	 * Calculeaza valoarea liniei: cantitate x pret unitar.
	 *
	 * @return valoarea liniei, in lei
	 */
	public float calculValoare() {
		return cantitate * pretUnitar;
	}

	public int getIdProdus() {
		return idProdus;
	}

	public String getDenumireProdus() {
		return denumireProdus;
	}

	public int getCantitate() {
		return cantitate;
	}

	/**
	 * Modifica cantitatea liniei.
	 *
	 * @param cantitate noua cantitate
	 * @throws ExceptieDateInvalide daca cantitatea nu este valida
	 */
	public void setCantitate(int cantitate) throws ExceptieDateInvalide {
		validareCantitate(cantitate);
		this.cantitate = cantitate;
	}

	public float getPretUnitar() {
		return pretUnitar;
	}

	@Override
	public void SerializareObiect(DataOutputStream dos) throws IOException {
		dos.writeInt(idProdus);
		dos.writeUTF(denumireProdus);
		dos.writeInt(cantitate);
		dos.writeFloat(pretUnitar);
	}

	@Override
	public void DeserializareObiect(DataInputStream dis) throws IOException {
		idProdus = dis.readInt();
		denumireProdus = dis.readUTF();
		cantitate = dis.readInt();
		pretUnitar = dis.readFloat();
	}

	@Override
	public String toString() {
		return String.format("%-28s x%-3d %8.2f lei = %9.2f lei", denumireProdus, cantitate,
				pretUnitar, calculValoare());
	}
}
