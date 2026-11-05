# Gestiunea comenzilor unui restaurant

Aplicație Java pentru înregistrarea și prelucrarea comenzilor unui restaurant,
cu persistență exclusiv în fișiere (fără bază de date).

## Rulare

**IntelliJ IDEA / Eclipse:** se creează un proiect Java nou, se copiază folderul `src` și
fișierele `produse.txt` și `comenzi.dat` în rădăcina proiectului (lângă `src`, NU în `src`),
apoi se rulează clasa `ro.ase.restaurant.main.Main` (consolă) sau `ro.ase.restaurant.main.MainGUI` (interfață grafică Swing).

**Din linia de comandă** (din folderul proiectului):

```
javac -encoding UTF-8 -d out src/ro/ase/restaurant/*.java src/ro/ase/restaurant/*/*.java
java -cp out ro.ase.restaurant.main.Main      # consola
java -cp out ro.ase.restaurant.main.MainGUI   # interfata grafica
```

Fișierele de date se caută în folderul curent de lucru (în IntelliJ: rădăcina proiectului).

## Fișiere de date

| Fișier | Tip | Conținut |
|---|---|---|
| `produse.txt` | text | un produs pe linie: `id,denumire,categorie,pret,gramaj,vegetarian` |
| `comenzi.dat` | binar | comenzile, scrise cu `DataOutputStream` (32 de comenzi de test, 26.10–02.11.2026) |
| `raport.txt` | text | raportul statistic complet, generat din meniul de rapoarte |

Datele se încarcă la pornire, toate modificările se fac în memorie, iar la ieșire
(opțiunea 0) se salvează în aceleași fișiere de intrare.

## Pachete

| Pachet | Clase |
|---|---|
| `ro.ase.restaurant` | `Main`, `MainGUI` |
| `ro.ase.restaurant.model` | `Articol`, `Produs`, `CategorieProdus`, `Comanda`, `LinieComanda` |
| `ro.ase.restaurant.interfete` | `ICalcul`, `ISerializare` |
| `ro.ase.restaurant.exceptii` | `ExceptieDateInvalide`, `ExceptieProdusInexistent` |
| `ro.ase.restaurant.main` | `Restaurant`, `GestiuneFisiere`, `Rapoarte` |
| `ro.ase.restaurant.gui` | `FereastraPrincipala` |

## Clase

| Clasă | Rol |
|---|---|
| `Articol` | clasă abstractă, `Comparable<Articol>` – id, denumire, preț + validări |
| `Produs` | extinde `Articol` – categorie, gramaj, vegetarian; citire/scriere linie text |
| `CategorieProdus` | enum cu categoriile produselor |
| `LinieComanda` | produs + cantitate + preț la momentul comenzii; `ISerializare` |
| `Comanda` | `ICalcul`, `ISerializare`, `Comparable<Comanda>` – calcul valoare totală |
| `ICalcul`, `ISerializare` | interfețe pentru calcule și serializare binară |
| `Restaurant` | Singleton – `ArrayList<Produs>`, `HashMap<Integer, Produs>`, `ArrayList<Comanda>` |
| `GestiuneFisiere` | citire/scriere `produse.txt`, `comenzi.dat`, `raport.txt` |
| `Rapoarte` | rapoarte statistice; vectori `float[]`, `int[]`, matrice `float[][]`, `HashMap`, `HashSet` |
| `ExceptieDateInvalide`, `ExceptieProdusInexistent` | excepții definite de programator |
| `Main` | varianta de consolă: meniul aplicației și fluxul de la pornire până la închidere |
| `MainGUI` | varianta cu interfață grafică: încarcă datele și deschide fereastra |
| `FereastraPrincipala` | fereastra Swing (`JFrame`, `ActionListener`) cu tab-uri: produse, comandă nouă, comenzi, rapoarte |

Documentația Javadoc (HTML) este în folderul `javadoc` – se deschide `javadoc/index.html`.
