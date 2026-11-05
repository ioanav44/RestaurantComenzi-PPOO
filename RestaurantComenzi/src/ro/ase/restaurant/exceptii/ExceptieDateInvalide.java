package ro.ase.restaurant.exceptii;

/**
 * Exceptie definita de programator, aruncata cand datele introduse de utilizator
 * sau citite din fisiere nu sunt valide (pret, cantitate, gramaj, data, denumire etc.).
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class ExceptieDateInvalide extends Exception {

	private static final long serialVersionUID = 1L;
	public ExceptieDateInvalide()
	{
		super();
	}
	public ExceptieDateInvalide(String mesaj)
	{
		super(mesaj);
	}
}
