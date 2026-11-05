package ro.ase.restaurant.exceptii;

/**
 * Exceptie definita de programator, aruncata cand se cere un produs
 * care nu exista in meniul restaurantului.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class ExceptieProdusInexistent extends Exception {

	private static final long serialVersionUID = 1L;
	public ExceptieProdusInexistent()
	{
		super();
	}
	public ExceptieProdusInexistent(String mesaj)
	{
		super(mesaj);
	}
}
