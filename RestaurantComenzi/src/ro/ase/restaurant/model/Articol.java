package ro.ase.restaurant.model;

import ro.ase.restaurant.exceptii.*;

/**
 * Clasa abstracta de baza pentru articolele din meniul restaurantului.
 * Pastreaza datele comune (identificator, denumire, pret) si defineste
 * ordinea naturala a articolelor: dupa denumire, apoi dupa pret.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public abstract class Articol implements Comparable<Articol> {

	private int id;
	private String denumire;
	private float pret;

	/**
	 * Construieste un articol, validand datele primite.
	 *
	 * @param id identificatorul articolului (strict pozitiv)
	 * @param denumire denumirea articolului (nevida, fara virgula)
	 * @param pret pretul articolului (intre 0 si 10000 lei)
	 * @throws ExceptieDateInvalide daca una dintre valori nu este valida
	 */
	public Articol(int id, String denumire, float pret) throws ExceptieDateInvalide {
		super();
		if (id <= 0)
			throw new ExceptieDateInvalide("Identificatorul trebuie sa fie pozitiv: " + id);
		validareDenumire(denumire);
		validarePret(pret);
		this.id = id;
		this.denumire = denumire.trim();
		this.pret = pret;
	}

	/**
	 * Verifica daca o denumire este valida.
	 *
	 * @param denumire denumirea de verificat
	 * @throws ExceptieDateInvalide daca denumirea este goala sau contine virgula
	 */
	public static void validareDenumire(String denumire) throws ExceptieDateInvalide {
		if (denumire == null || denumire.trim().isEmpty())
			throw new ExceptieDateInvalide("Denumirea nu poate fi goala!");
		if (denumire.contains(","))
			throw new ExceptieDateInvalide("Denumirea nu poate contine virgula!");
	}

	/**
	 * Verifica daca un pret este valid.
	 *
	 * @param pret pretul de verificat
	 * @throws ExceptieDateInvalide daca pretul nu este in intervalul (0, 10000]
	 */
	public static void validarePret(float pret) throws ExceptieDateInvalide {
		if (pret <= 0 || pret > 10000)
			throw new ExceptieDateInvalide("Pret invalid: " + pret + " (trebuie sa fie intre 0 si 10000 lei)");
	}

	/**
	 * Genereaza o descriere scurta a articolului, pentru afisarea in meniu.
	 *
	 * @return descrierea articolului
	 */
	public abstract String genereazaDescriere();

	public int getId() {
		return id;
	}

	public String getDenumire() {
		return denumire;
	}

	public void setDenumire(String denumire) throws ExceptieDateInvalide {
		validareDenumire(denumire);
		this.denumire = denumire.trim();
	}

	public float getPret() {
		return pret;
	}

	/**
	 * Modifica pretul articolului.
	 *
	 * @param pret noul pret
	 * @throws ExceptieDateInvalide daca pretul nu este valid
	 */
	public void setPret(float pret) throws ExceptieDateInvalide {
		validarePret(pret);
		this.pret = pret;
	}

	/**
	 * Compara doua articole: intai dupa denumire, apoi dupa pret.
	 */
	@Override
	public int compareTo(Articol a) {
		int lastCmp = denumire.compareToIgnoreCase(a.denumire);
		if (lastCmp == 0)
			if (pret < a.pret)
				return -1;
			else
				if (pret > a.pret)
					return 1;
				else return 0;
		return lastCmp;
	}

	@Override
	public String toString() {
		return "Articol [id=" + id + ", denumire=" + denumire + ", pret=" + pret + "]";
	}
}
