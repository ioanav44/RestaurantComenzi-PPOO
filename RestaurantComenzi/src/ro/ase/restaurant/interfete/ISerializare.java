package ro.ase.restaurant.interfete;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interfata pentru salvarea si restaurarea obiectelor intr-un fisier binar,
 * prin fluxurile DataOutputStream si DataInputStream.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public interface ISerializare {

	/**
	 * Scrie datele obiectului in fluxul binar.
	 *
	 * @param dos fluxul de iesire
	 * @throws IOException daca apare o eroare la scriere
	 */
	public void SerializareObiect(DataOutputStream dos) throws IOException;

	/**
	 * Citeste datele obiectului din fluxul binar.
	 *
	 * @param dis fluxul de intrare
	 * @throws IOException daca apare o eroare la citire
	 */
	public void DeserializareObiect(DataInputStream dis) throws IOException;
}
