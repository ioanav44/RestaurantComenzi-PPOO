package ro.ase.restaurant.gui;

import ro.ase.restaurant.clase.GestiuneFisiere;
import ro.ase.restaurant.clase.Rapoarte;
import ro.ase.restaurant.clase.Restaurant;
import ro.ase.restaurant.exceptii.*;
import ro.ase.restaurant.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Fereastra principala a aplicatiei (interfata grafica Swing), organizata pe tab-uri:
 * meniul de produse, comanda noua, comenzile inregistrate si rapoartele.
 * Toate modificarile se fac in memorie, prin clasa {@link Restaurant}; la inchiderea
 * ferestrei datele se salveaza in fisierele de intrare.
 *
 * @author Ioana-Delia Voicu
 * @version 1.0
 */
public class FereastraPrincipala extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);

	private Restaurant restaurant;
	private Rapoarte rapoarte;

	// --- meniul ferestrei
	private JMenuItem miSalvare, miIesire, miDespre;

	// --- tab Produse
	private DefaultTableModel modelProduse;
	private JTable tabelProduse;
	private JTextField tfDenumire, tfPret, tfGramaj, tfPretNou;
	private JComboBox<CategorieProdus> cbCategorie;
	private JCheckBox chkVegetarian;
	private JButton btnAdaugaProdus, btnModificaPret;

	// --- tab Comanda noua
	private JTextField tfData, tfOra, tfCantitate;
	private JComboBox<String> cbProdusComanda;
	private Produs[] vectProduseCombo = new Produs[0];
	private DefaultTableModel modelLinii;
	private JTable tabelLinii;
	private JLabel lblTotal;
	private JButton btnAdaugaLinie, btnStergeLinie, btnGolesteComanda, btnInregistreaza;
	private Comanda comandaCurenta;

	// --- tab Comenzi
	private DefaultTableModel modelComenzi;
	private JTable tabelComenzi;
	private JTextArea taBon;
	private JButton btnAnuleazaComanda;

	// --- tab Rapoarte
	private JTextField tfZi, tfDeLa, tfPanaLa, tfTopN;
	private JComboBox<String> cbProdusRaport;
	private Produs[] vectProduseRaport = new Produs[0];
	private JButton btnValoareZi, btnComenziZi, btnPerioada, btnTop, btnVanzariProdus, btnStatistici,
			btnCategorii, btnMatrice, btnOre, btnZilnic, btnRaportComplet;
	private JTextArea taRaport;

	private JLabel status;

	/**
	 * Construieste fereastra principala pentru un restaurant ale carui date sunt deja incarcate.
	 *
	 * @param restaurant restaurantul cu datele aplicatiei
	 */
	public FereastraPrincipala(Restaurant restaurant) {
		this.restaurant = restaurant;
		this.rapoarte = new Rapoarte(restaurant);

		setTitle("Gestiunea comenzilor unui restaurant");
		setSize(1150, 720);
		setMinimumSize(new Dimension(900, 600));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new MyWindowAdapter());

		setJMenuBar(creeazaMeniu());

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Meniu produse", creeazaTabProduse());
		tabs.addTab("Comanda noua", creeazaTabComandaNoua());
		tabs.addTab("Comenzi", creeazaTabComenzi());
		tabs.addTab("Rapoarte", creeazaTabRapoarte());
		add(tabs, BorderLayout.CENTER);

		status = new JLabel();
		status.setBorder(new EmptyBorder(4, 8, 4, 8));
		add(status, BorderLayout.SOUTH);

		comandaNouaGoala();
		actualizeazaTot();
	}

	// =================================================================== constructia interfetei

	private JMenuBar creeazaMeniu() {
		JMenuBar bara = new JMenuBar();
		JMenu mFisier = new JMenu("Fisier");
		miSalvare = new JMenuItem("Salvare si iesire");
		miIesire = new JMenuItem("Iesire");
		miSalvare.addActionListener(this);
		miIesire.addActionListener(this);
		mFisier.add(miSalvare);
		mFisier.add(miIesire);
		JMenu mAjutor = new JMenu("Ajutor");
		miDespre = new JMenuItem("Despre aplicatie");
		miDespre.addActionListener(this);
		mAjutor.add(miDespre);
		bara.add(mFisier);
		bara.add(mAjutor);
		return bara;
	}

	private JPanel creeazaTabProduse() {
		JPanel panou = new JPanel(new BorderLayout(8, 8));
		panou.setBorder(new EmptyBorder(8, 8, 8, 8));

		modelProduse = creeazaModel(new String[] { "Id", "Denumire", "Categorie", "Pret (lei)", "Gramaj", "Vegetarian" });
		tabelProduse = creeazaTabel(modelProduse);
		tabelProduse.getColumnModel().getColumn(0).setMaxWidth(50);
		tabelProduse.getColumnModel().getColumn(1).setPreferredWidth(240);
		panou.add(new JScrollPane(tabelProduse), BorderLayout.CENTER);

		JPanel jos = new JPanel(new GridLayout(1, 2, 8, 0));

		JPanel pAdauga = new JPanel(new GridBagLayout());
		pAdauga.setBorder(new TitledBorder("Adaugare produs nou"));
		tfDenumire = new JTextField(18);
		cbCategorie = new JComboBox<CategorieProdus>(CategorieProdus.values());
		tfPret = new JTextField(8);
		tfGramaj = new JTextField(8);
		chkVegetarian = new JCheckBox("Vegetarian");
		btnAdaugaProdus = new JButton("Adauga produs");
		btnAdaugaProdus.addActionListener(this);
		adaugaRand(pAdauga, 0, "Denumire", tfDenumire);
		adaugaRand(pAdauga, 1, "Categorie", cbCategorie);
		adaugaRand(pAdauga, 2, "Pret (lei)", tfPret);
		adaugaRand(pAdauga, 3, "Gramaj (g / ml)", tfGramaj);
		adaugaRand(pAdauga, 4, "", chkVegetarian);
		adaugaRand(pAdauga, 5, "", btnAdaugaProdus);
		jos.add(pAdauga);

		JPanel pPret = new JPanel(new GridBagLayout());
		pPret.setBorder(new TitledBorder("Modificare pret"));
		tfPretNou = new JTextField(8);
		btnModificaPret = new JButton("Modifica pretul produsului selectat");
		btnModificaPret.addActionListener(this);
		adaugaRand(pPret, 0, "", new JLabel("Selectati un produs din tabel."));
		adaugaRand(pPret, 1, "Pret nou (lei)", tfPretNou);
		adaugaRand(pPret, 2, "", btnModificaPret);
		adaugaRand(pPret, 3, "", new JLabel("<html><i>Comenzile deja inregistrate isi pastreaza pretul.</i></html>"));
		jos.add(pPret);

		panou.add(jos, BorderLayout.SOUTH);
		return panou;
	}

	private JPanel creeazaTabComandaNoua() {
		JPanel panou = new JPanel(new BorderLayout(8, 8));
		panou.setBorder(new EmptyBorder(8, 8, 8, 8));

		JPanel sus = new JPanel();
		sus.setLayout(new BoxLayout(sus, BoxLayout.Y_AXIS));
		sus.setBorder(new TitledBorder("Date comanda si produse"));
		tfData = new JTextField(9);
		tfOra = new JTextField(5);
		cbProdusComanda = new JComboBox<String>();
		cbProdusComanda.setPreferredSize(new Dimension(360, 26));
		tfCantitate = new JTextField("1", 3);
		btnAdaugaLinie = new JButton("Adauga in comanda");
		btnAdaugaLinie.addActionListener(this);
		tfCantitate.addActionListener(this);
		sus.add(rand(new JLabel("Data (aaaa-ll-zz)"), tfData, new JLabel("   Ora (HH:mm)"), tfOra));
		sus.add(rand(new JLabel("Produs"), cbProdusComanda, new JLabel("   Cantitate"), tfCantitate, btnAdaugaLinie));
		panou.add(sus, BorderLayout.NORTH);

		modelLinii = creeazaModel(new String[] { "Produs", "Cantitate", "Pret unitar (lei)", "Valoare (lei)" });
		tabelLinii = creeazaTabel(modelLinii);
		tabelLinii.getColumnModel().getColumn(0).setPreferredWidth(300);
		panou.add(new JScrollPane(tabelLinii), BorderLayout.CENTER);

		JPanel jos = new JPanel(new BorderLayout());
		JPanel butoane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		btnStergeLinie = new JButton("Sterge produsul selectat");
		btnGolesteComanda = new JButton("Goleste comanda");
		btnStergeLinie.addActionListener(this);
		btnGolesteComanda.addActionListener(this);
		butoane.add(btnStergeLinie);
		butoane.add(btnGolesteComanda);
		jos.add(butoane, BorderLayout.WEST);

		JPanel dreapta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
		lblTotal = new JLabel();
		lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
		btnInregistreaza = new JButton("Inregistreaza comanda");
		btnInregistreaza.setFont(btnInregistreaza.getFont().deriveFont(Font.BOLD));
		btnInregistreaza.addActionListener(this);
		dreapta.add(lblTotal);
		dreapta.add(btnInregistreaza);
		jos.add(dreapta, BorderLayout.EAST);
		panou.add(jos, BorderLayout.SOUTH);
		return panou;
	}

	private JPanel creeazaTabComenzi() {
		JPanel panou = new JPanel(new BorderLayout(8, 8));
		panou.setBorder(new EmptyBorder(8, 8, 8, 8));

		modelComenzi = creeazaModel(new String[] { "Nr.", "Data", "Ora", "Nr. produse", "Valoare (lei)" });
		tabelComenzi = creeazaTabel(modelComenzi);
		int[] latimi = { 45, 95, 55, 80, 100 };
		for (int i = 0; i < latimi.length; i++)
			tabelComenzi.getColumnModel().getColumn(i).setPreferredWidth(latimi[i]);
		tabelComenzi.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
				afiseazaBonSelectat();
		});

		taBon = new JTextArea();
		taBon.setEditable(false);
		taBon.setFont(FONT_MONO);
		JPanel pBon = new JPanel(new BorderLayout(4, 4));
		pBon.setBorder(new TitledBorder("Bonul comenzii selectate"));
		pBon.add(new JScrollPane(taBon), BorderLayout.CENTER);
		btnAnuleazaComanda = new JButton("Anuleaza comanda selectata");
		btnAnuleazaComanda.addActionListener(this);
		pBon.add(btnAnuleazaComanda, BorderLayout.SOUTH);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tabelComenzi), pBon);
		split.setResizeWeight(0.5);
		split.setDividerLocation(430);
		panou.add(split, BorderLayout.CENTER);
		return panou;
	}

	private JPanel creeazaTabRapoarte() {
		JPanel panou = new JPanel(new BorderLayout(8, 8));
		panou.setBorder(new EmptyBorder(8, 8, 8, 8));

		JPanel stanga = new JPanel();
		stanga.setLayout(new BoxLayout(stanga, BoxLayout.Y_AXIS));

		tfZi = new JTextField(9);
		btnValoareZi = new JButton("Valoarea comenzilor din zi");
		btnComenziZi = new JButton("Comenzile din zi");
		stanga.add(grup("Intr-o anumita zi", rand(new JLabel("Data"), tfZi), rand(btnValoareZi), rand(btnComenziZi)));

		tfDeLa = new JTextField(9);
		tfPanaLa = new JTextField(9);
		btnPerioada = new JButton("Nr. comenzi in perioada");
		stanga.add(grup("Intr-o perioada", rand(new JLabel("De la"), tfDeLa), rand(new JLabel("Pana la"), tfPanaLa), rand(btnPerioada)));

		cbProdusRaport = new JComboBox<String>();
		cbProdusRaport.setPreferredSize(new Dimension(230, 26));
		tfTopN = new JTextField("5", 3);
		btnVanzariProdus = new JButton("Vanzarile produsului");
		btnTop = new JButton("Cele mai vandute");
		stanga.add(grup("Produse", rand(cbProdusRaport), rand(btnVanzariProdus), rand(new JLabel("Top"), tfTopN, btnTop)));

		btnStatistici = new JButton("Valoare medie si statistici");
		btnCategorii = new JButton("Vanzari pe categorii");
		btnMatrice = new JButton("Categorii x zile saptamana");
		btnOre = new JButton("Ore de varf");
		btnZilnic = new JButton("Vanzari pe zile");
		btnRaportComplet = new JButton("Raport complet -> raport.txt");
		stanga.add(grup("Situatii generale", rand(btnStatistici), rand(btnCategorii), rand(btnMatrice),
				rand(btnOre), rand(btnZilnic), rand(btnRaportComplet)));

		JButton[] butoane = { btnValoareZi, btnComenziZi, btnPerioada, btnVanzariProdus, btnTop, btnStatistici,
				btnCategorii, btnMatrice, btnOre, btnZilnic, btnRaportComplet };
		for (JButton b : butoane)
			b.addActionListener(this);

		taRaport = new JTextArea();
		taRaport.setEditable(false);
		taRaport.setFont(FONT_MONO);

		JScrollPane scrollStanga = new JScrollPane(stanga, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollStanga.setPreferredSize(new Dimension(300, 0));
		scrollStanga.getVerticalScrollBar().setUnitIncrement(16);
		panou.add(scrollStanga, BorderLayout.WEST);
		panou.add(new JScrollPane(taRaport), BorderLayout.CENTER);
		return panou;
	}

	private DefaultTableModel creeazaModel(String[] coloane) {
		return new DefaultTableModel(coloane, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
	}

	private JTable creeazaTabel(DefaultTableModel model) {
		JTable t = new JTable(model);
		t.setRowHeight(24);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.getTableHeader().setReorderingAllowed(false);
		return t;
	}

	private void adaugaRand(JPanel p, int y, String eticheta, JComponent c) {
		GridBagConstraints g = new GridBagConstraints();
		g.insets = new Insets(3, 6, 3, 6);
		g.anchor = GridBagConstraints.WEST;
		g.gridx = 0;
		g.gridy = y;
		p.add(new JLabel(eticheta), g);
		g.gridx = 1;
		p.add(c, g);
	}

	private JPanel rand(JComponent... comp) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		for (JComponent c : comp)
			p.add(c);
		return p;
	}

	private JPanel grup(String titlu, JPanel... randuri) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(new TitledBorder(titlu));
		for (JPanel r : randuri)
			p.add(r);
		return p;
	}

	// ============================================================ actualizarea datelor afisate

	private void actualizeazaTot() {
		actualizeazaTabelProduse();
		actualizeazaComboProduse();
		actualizeazaTabelComenzi();
		actualizeazaStatus();
	}

	private void actualizeazaTabelProduse() {
		modelProduse.setRowCount(0);
		for (Produs p : restaurant.getProduseSortate())
			modelProduse.addRow(new Object[] { p.getId(), p.getDenumire(), p.getCategorie(),
					String.format("%.2f", p.getPret()),
					p.getGramaj() + (p.getCategorie() == CategorieProdus.BAUTURA ? " ml" : " g"),
					p.isVegetarian() ? "da" : "nu" });
	}

	private void actualizeazaComboProduse() {
		List<Produs> lista = restaurant.getProduseSortate();
		vectProduseCombo = lista.toArray(new Produs[0]);
		vectProduseRaport = vectProduseCombo;
		cbProdusComanda.removeAllItems();
		cbProdusRaport.removeAllItems();
		for (Produs p : vectProduseCombo) {
			cbProdusComanda.addItem(String.format("%d. %s - %.2f lei", p.getId(), p.getDenumire(), p.getPret()));
			cbProdusRaport.addItem(p.getId() + ". " + p.getDenumire());
		}
	}

	private void actualizeazaTabelComenzi() {
		modelComenzi.setRowCount(0);
		for (Comanda c : restaurant.getListaComenzi())
			modelComenzi.addRow(new Object[] { c.getId(), c.getData(), c.getOra(), c.calculNrProduse(),
					String.format("%.2f", c.calculValoareTotala()) });
		taBon.setText("");
	}

	private void actualizeazaComandaCurenta() {
		modelLinii.setRowCount(0);
		for (LinieComanda l : comandaCurenta.getLinii())
			modelLinii.addRow(new Object[] { l.getDenumireProdus(), l.getCantitate(),
					String.format("%.2f", l.getPretUnitar()), String.format("%.2f", l.calculValoare()) });
		lblTotal.setText(String.format("Total: %.2f lei", comandaCurenta.calculValoareTotala()));
	}

	private void actualizeazaStatus() {
		status.setText("Produse in meniu: " + restaurant.getListaProduse().size()
				+ "     Comenzi inregistrate: " + restaurant.getListaComenzi().size()
				+ "     " + (restaurant.isModificat() ? "Modificari nesalvate - se salveaza la inchiderea aplicatiei"
						: "Nicio modificare fata de fisiere"));
	}

	private void comandaNouaGoala() {
		LocalDateTime acum = LocalDateTime.now();
		tfData.setText(acum.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		tfOra.setText(acum.format(DateTimeFormatter.ofPattern("HH:mm")));
		try {
			// comanda temporara, doar pentru colectarea produselor; datele finale se iau la inregistrare
			comandaCurenta = new Comanda(1, tfData.getText(), tfOra.getText());
		}
		catch (ExceptieDateInvalide e) {
			throw new RuntimeException(e);
		}
		tfCantitate.setText("1");
		actualizeazaComandaCurenta();
	}

	private void afiseazaBonSelectat() {
		int rand = tabelComenzi.getSelectedRow();
		if (rand < 0) {
			taBon.setText("");
			return;
		}
		Comanda c = restaurant.cautaComanda((Integer) modelComenzi.getValueAt(rand, 0));
		taBon.setText(c == null ? "" : c.genereazaBon());
		taBon.setCaretPosition(0);
	}

	// =============================================================== tratarea evenimentelor

	@Override
	public void actionPerformed(ActionEvent e) {
		Object sursa = e.getSource();
		try {
			if (sursa == btnAdaugaProdus) adaugaProdus();
			else if (sursa == btnModificaPret) modificaPret();
			else if (sursa == btnAdaugaLinie || sursa == tfCantitate) adaugaLinie();
			else if (sursa == btnStergeLinie) stergeLinie();
			else if (sursa == btnGolesteComanda) comandaNouaGoala();
			else if (sursa == btnInregistreaza) inregistreazaComanda();
			else if (sursa == btnAnuleazaComanda) anuleazaComanda();
			else if (sursa == miSalvare || sursa == miIesire) inchideAplicatia();
			else if (sursa == miDespre) afiseazaDespre();
			else trateazaRaport(sursa);
		}
		catch (ExceptieDateInvalide | ExceptieProdusInexistent ex) {
			eroare(ex.getMessage());
		}
	}

	private void adaugaProdus() throws ExceptieDateInvalide {
		String denumire = tfDenumire.getText();
		Articol.validareDenumire(denumire);
		float pret = citesteFloat(tfPret, "Pretul");
		int gramaj = citesteInt(tfGramaj, "Gramajul");
		Produs p = new Produs(restaurant.genereazaIdProdus(), denumire,
				(CategorieProdus) cbCategorie.getSelectedItem(), pret, gramaj, chkVegetarian.isSelected());
		restaurant.adaugaProdus(p);
		tfDenumire.setText("");
		tfPret.setText("");
		tfGramaj.setText("");
		chkVegetarian.setSelected(false);
		actualizeazaTabelProduse();
		actualizeazaComboProduse();
		actualizeazaStatus();
		info("Produsul \"" + p.getDenumire() + "\" a fost adaugat cu id-ul " + p.getId() + ".");
	}

	private void modificaPret() throws ExceptieDateInvalide, ExceptieProdusInexistent {
		int rand = tabelProduse.getSelectedRow();
		if (rand < 0)
			throw new ExceptieDateInvalide("Selectati din tabel produsul caruia ii modificati pretul.");
		int id = (Integer) modelProduse.getValueAt(rand, 0);
		float pretNou = citesteFloat(tfPretNou, "Pretul nou");
		restaurant.modificaPretProdus(id, pretNou);
		tfPretNou.setText("");
		actualizeazaTabelProduse();
		actualizeazaComboProduse();
		actualizeazaStatus();
	}

	private void adaugaLinie() throws ExceptieDateInvalide, ExceptieProdusInexistent {
		int index = cbProdusComanda.getSelectedIndex();
		if (index < 0)
			throw new ExceptieProdusInexistent("Nu exista produse in meniu.");
		Produs p = restaurant.cautaProdus(vectProduseCombo[index].getId());
		int cant = citesteInt(tfCantitate, "Cantitatea");
		comandaCurenta.adaugaProdus(p, cant);
		tfCantitate.setText("1");
		actualizeazaComandaCurenta();
	}

	private void stergeLinie() throws ExceptieDateInvalide {
		int rand = tabelLinii.getSelectedRow();
		if (rand < 0)
			throw new ExceptieDateInvalide("Selectati produsul pe care doriti sa il stergeti din comanda.");
		comandaCurenta.getLinii().remove(rand);
		actualizeazaComandaCurenta();
	}

	private void inregistreazaComanda() throws ExceptieDateInvalide, ExceptieProdusInexistent {
		if (comandaCurenta.getLinii().isEmpty())
			throw new ExceptieDateInvalide("Comanda nu contine niciun produs!");
		Comanda c = new Comanda(restaurant.genereazaIdComanda(), tfData.getText().trim(), tfOra.getText().trim());
		for (LinieComanda l : comandaCurenta.getLinii())
			c.adaugaProdus(restaurant.cautaProdus(l.getIdProdus()), l.getCantitate());
		restaurant.adaugaComanda(c);
		actualizeazaTabelComenzi();
		actualizeazaStatus();
		info("Comanda a fost inregistrata.\n\n" + c.genereazaBon());
		comandaNouaGoala();
	}

	private void anuleazaComanda() throws ExceptieDateInvalide {
		int rand = tabelComenzi.getSelectedRow();
		if (rand < 0)
			throw new ExceptieDateInvalide("Selectati comanda pe care doriti sa o anulati.");
		int id = (Integer) modelComenzi.getValueAt(rand, 0);
		int r = JOptionPane.showConfirmDialog(this, "Sigur anulati comanda nr. " + id + "?",
				"Confirmare anulare", JOptionPane.YES_NO_OPTION);
		if (r == JOptionPane.YES_OPTION) {
			restaurant.stergeComanda(id);
			actualizeazaTabelComenzi();
			actualizeazaStatus();
		}
	}

	private void trateazaRaport(Object sursa) throws ExceptieDateInvalide, ExceptieProdusInexistent {
		String rezultat = "";
		if (sursa == btnValoareZi) {
			String zi = tfZi.getText().trim();
			rezultat = String.format("Valoarea comenzilor din %s (%s): %.2f lei (%d comenzi)%n", zi,
					zi.isEmpty() ? "" : numeZiSigur(zi), rapoarte.valoareTotalaZi(zi), rapoarte.comenziZi(zi).size());
		}
		else if (sursa == btnComenziZi) {
			String zi = tfZi.getText().trim();
			List<Comanda> comenzi = rapoarte.comenziZi(zi);
			rezultat = "Comenzile din " + zi + " (" + comenzi.size() + "):\n\n";
			for (Comanda c : comenzi)
				rezultat += c.genereazaBon() + "\n\n";
		}
		else if (sursa == btnPerioada) {
			String d1 = tfDeLa.getText().trim(), d2 = tfPanaLa.getText().trim();
			float total = 0;
			for (Comanda c : rapoarte.comenziPerioada(d1, d2))
				total += c.calculValoareTotala();
			rezultat = String.format("In perioada %s - %s: %d comenzi, valoare totala %.2f lei%n", d1, d2,
					rapoarte.nrComenziPerioada(d1, d2), total);
		}
		else if (sursa == btnVanzariProdus) {
			int index = cbProdusRaport.getSelectedIndex();
			if (index < 0)
				throw new ExceptieProdusInexistent("Nu exista produse in meniu.");
			Produs p = vectProduseRaport[index];
			rezultat = String.format("%s: %d buc. vandute, valoare totala %.2f lei%n", p.getDenumire(),
					rapoarte.cantitateVandutaProdus(p.getId()), rapoarte.valoareVanzariProdus(p.getId()));
		}
		else if (sursa == btnTop) {
			int n = citesteInt(tfTopN, "Numarul de produse din top");
			if (n <= 0)
				throw new ExceptieDateInvalide("Numarul de produse din top trebuie sa fie pozitiv!");
			rezultat = rapoarte.topProduseVandute(n);
		}
		else if (sursa == btnStatistici) rezultat = rapoarte.statisticiValoriComenzi();
		else if (sursa == btnCategorii) rezultat = rapoarte.raportCategorii();
		else if (sursa == btnMatrice) rezultat = rapoarte.raportMatriceCategoriiZile();
		else if (sursa == btnOre) rezultat = rapoarte.raportOreDeVarf();
		else if (sursa == btnZilnic) rezultat = rapoarte.raportZilnic();
		else if (sursa == btnRaportComplet) {
			rezultat = rapoarte.raportComplet();
			try {
				GestiuneFisiere.scriereText(GestiuneFisiere.FISIER_RAPORT, rezultat);
				info("Raportul a fost salvat in " + GestiuneFisiere.FISIER_RAPORT + ".");
			}
			catch (FileNotFoundException ex) {
				eroare("Raportul nu a putut fi salvat: " + ex.getMessage());
			}
		}
		taRaport.setText(rezultat);
		taRaport.setCaretPosition(0);
	}

	private String numeZiSigur(String data) {
		try {
			return Rapoarte.numeZi(data);
		}
		catch (Exception e) {
			return "?";
		}
	}

	// =================================================================== citire si validare campuri

	private int citesteInt(JTextField tf, String camp) throws ExceptieDateInvalide {
		try {
			return Integer.parseInt(tf.getText().trim());
		}
		catch (NumberFormatException e) {
			throw new ExceptieDateInvalide(camp + " trebuie sa fie un numar intreg (ati introdus \""
					+ tf.getText() + "\").");
		}
	}

	private float citesteFloat(JTextField tf, String camp) throws ExceptieDateInvalide {
		try {
			return Float.parseFloat(tf.getText().trim().replace(',', '.'));
		}
		catch (NumberFormatException e) {
			throw new ExceptieDateInvalide(camp + " trebuie sa fie un numar (ex. 24.50), ati introdus \""
					+ tf.getText() + "\".");
		}
	}

	private void eroare(String mesaj) {
		JOptionPane.showMessageDialog(this, mesaj, "Date invalide", JOptionPane.ERROR_MESSAGE);
	}

	private void info(String mesaj) {
		JTextArea ta = new JTextArea(mesaj);
		ta.setEditable(false);
		ta.setFont(FONT_MONO);
		ta.setOpaque(false);
		JOptionPane.showMessageDialog(this, ta, "Informatie", JOptionPane.INFORMATION_MESSAGE);
	}

	private void afiseazaDespre() {
		JOptionPane.showMessageDialog(this, "Aplicatie Java pentru gestiunea comenzilor unui restaurant\n"
				+ "Proiect PPOO 2026/2027 - Ioana-Delia Voicu\n\n"
				+ "Datele se pastreaza in produse.txt si comenzi.dat\n"
				+ "si se salveaza automat la inchiderea aplicatiei.", "Despre", JOptionPane.INFORMATION_MESSAGE);
	}

	// ========================================================== inchiderea aplicatiei

	/**
	 * Salveaza datele in fisierele de intrare si inchide aplicatia. Daca salvarea
	 * esueaza, utilizatorul poate alege sa ramana in aplicatie.
	 */
	private void inchideAplicatia() {
		boolean ok = restaurant.salvareDate();
		if (!ok) {
			int r = JOptionPane.showConfirmDialog(this,
					"Datele nu au putut fi salvate (detalii in consola).\nInchideti oricum aplicatia?",
					"Eroare la salvare", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (r != JOptionPane.YES_OPTION)
				return;
		}
		dispose();
		System.exit(0);
	}

	class MyWindowAdapter extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			inchideAplicatia();
		}
	}
}
