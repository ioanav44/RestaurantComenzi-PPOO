package ro.ase.restaurant.model;

import ro.ase.restaurant.exceptii.*;

/**
 * Produs din meniul restaurantului. Pe langa datele unui {@link Articol},
 * pastreaza categoria, gramajul (sau volumul, in ml, pentru bauturi)
 * si daca produsul este vegetarian.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class Produs extends Articol {

	private CategorieProdus categorie;
	private int gramaj;
	private boolean vegetarian;

	/**
	 * Construieste un produs, validand datele primite.
	 *
	 * @param id identificatorul produsului
	 * @param denumire denumirea produsului
	 * @param categorie categoria produsului
	 * @param pret pretul unitar, in lei
	 * @param gramaj gramajul in grame (sau volumul in ml), intre 1 si 5000
	 * @param vegetarian true daca produsul este vegetarian
	 * @throws ExceptieDateInvalide daca una dintre valori nu este valida
	 */
	public Produs(int id, String denumire, CategorieProdus categorie, float pret,
			int gramaj, boolean vegetarian) throws ExceptieDateInvalide {
		super(id, denumire, pret);
		if (categorie == null)
			throw new ExceptieDateInvalide("Categoria produsului lipseste!");
		validareGramaj(gramaj);
		this.categorie = categorie;
		this.gramaj = gramaj;
		this.vegetarian = vegetarian;
	}

	/**
	 * Construieste un produs pornind de la o linie din fisierul produse.txt,
	 * de forma: id,denumire,categorie,pret,gramaj,vegetarian
	 *
	 * @param linie linia citita din fisier
	 * @return produsul creat
	 * @throws ExceptieDateInvalide daca linia nu are formatul corect
	 */
	public static Produs dinLinieText(String linie) throws ExceptieDateInvalide {
		String[] vs = linie.split(",");
		if (vs.length != 6)
			throw new ExceptieDateInvalide("Linia trebuie sa aiba 6 campuri, are " + vs.length);
		try {
			int id = Integer.parseInt(vs[0].trim());
			String denumire = vs[1].trim();
			CategorieProdus categorie = CategorieProdus.valueOf(vs[2].trim().toUpperCase());
			float pret = Float.parseFloat(vs[3].trim());
			int gramaj = Integer.parseInt(vs[4].trim());
			String veg = vs[5].trim().toLowerCase();
			if (!veg.equals("true") && !veg.equals("false"))
				throw new ExceptieDateInvalide("Campul vegetarian trebuie sa fie true/false: " + vs[5]);
			return new Produs(id, denumire, categorie, pret, gramaj, Boolean.parseBoolean(veg));
		}
		catch (NumberFormatException e) {
			throw new ExceptieDateInvalide("Valoare numerica invalida: " + e.getMessage());
		}
		catch (IllegalArgumentException e) {
			throw new ExceptieDateInvalide("Categorie inexistenta: " + vs[2]);
		}
	}

	/**
	 * Transforma produsul intr-o linie pentru fisierul produse.txt.
	 *
	 * @return linia de text corespunzatoare produsului
	 */
	public String inLinieText() {
		return getId() + "," + getDenumire() + "," + categorie + "," + getPret() + ","
				+ gramaj + "," + vegetarian;
	}

	/**
	 * Verifica daca un gramaj este valid.
	 *
	 * @param gramaj gramajul de verificat
	 * @throws ExceptieDateInvalide daca gramajul nu este intre 1 si 5000
	 */
	public static void validareGramaj(int gramaj) throws ExceptieDateInvalide {
		if (gramaj <= 0 || gramaj > 5000)
			throw new ExceptieDateInvalide("Gramaj invalid: " + gramaj + " (trebuie sa fie intre 1 si 5000)");
	}

	public CategorieProdus getCategorie() {
		return categorie;
	}

	public void setCategorie(CategorieProdus categorie) {
		this.categorie = categorie;
	}

	public int getGramaj() {
		return gramaj;
	}

	public void setGramaj(int gramaj) throws ExceptieDateInvalide {
		validareGramaj(gramaj);
		this.gramaj = gramaj;
	}

	public boolean isVegetarian() {
		return vegetarian;
	}

	public void setVegetarian(boolean vegetarian) {
		this.vegetarian = vegetarian;
	}

	@Override
	public String genereazaDescriere() {
		String um = (categorie == CategorieProdus.BAUTURA) ? "ml" : "g";
		String s = String.format("%3d. %-28s %-14s %5d %-2s %8.2f lei", getId(), getDenumire(),
				categorie, gramaj, um, getPret());
		if (vegetarian)
			s += "  (V)";
		return s;
	}

	@Override
	public String toString() {
		return "Produs [categorie=" + categorie + ", gramaj=" + gramaj + ", vegetarian="
				+ vegetarian + ", toString()=" + super.toString() + "]";
	}
}
