package ro.ase.restaurant.main;

import ro.ase.restaurant.clase.Restaurant;
import ro.ase.restaurant.gui.*;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punctul de intrare pentru varianta cu interfata grafica (Swing) a aplicatiei.
 * Incarca datele din fisiere si deschide fereastra principala; datele se salveaza
 * in fisierele de intrare la inchiderea ferestrei.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class MainGUI {

	/**
	 * Porneste aplicatia cu interfata grafica.
	 *
	 * @param args nu se folosesc
	 */
	public static void main(String[] args) {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
				if (info.getName().equals("Nimbus")) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
		}
		catch (Exception e) {
		}

		Restaurant restaurant = Restaurant.getRestaurant();
		restaurant.incarcareDate();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				FereastraPrincipala fereastra = new FereastraPrincipala(restaurant);
				fereastra.setVisible(true);
			}
		});
	}
}
