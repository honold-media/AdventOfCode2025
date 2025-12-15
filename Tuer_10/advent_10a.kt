import java.io.File
import java.util.*

// Globale Memo-Tabelle: null bedeutet "in Bearbeitung"
var memoTabelle: MutableMap<Vektor, Int?> = mutableMapOf()

fun main() {
    val bsp1: String = File("input_10_bsp.txt").readText().trim()
    val bsp2: String = File("input_10.txt").readText().trim()
    val einzelbeispiel3 = "[.##.] (3) (1,3) (2) (2,3) (0) (2) {3,5,4,7}"
    val einzelbeispiel1 = "[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}"
    val einzelbeispiel2 = "[.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}"
    val einzelbeispiel = "[...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}"

    // playground()
    val sum = zuAddierendes(bsp2)
    println ("=\n"+sum)
}

fun zuAddierendes(input: String): Int {
    // Hintereinander liegen pro Zeile: zB
    // [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
    // leerzeichengetrennt enthält jede Zeile:
    // 1 Einzelanleitung,
    // mehrere Tastenkombis in Klammern,
    // 1 Info in geschweiften Klammern für Teil 2

    // Jede Zeile liefert ein Ergebnis, diese sollen aufaddiert werden
    var summe = 0

    for (zeile in input.lineSequence()) {

        // Memo-Tabelle für jede Aufgabe zu Beginn zurücksetzen:
        // speichert für jeden Restvektor die minimale Anzahl an Tastenkombis
        memoTabelle = mutableMapOf<Vektor, Int?>()

        val einzelAufgabe = zeile.split(" ")
        val zielString = einzelAufgabe[0].trim().drop(1).dropLast(1) // zB .##. oder .###.#
        val n = zielString.length
        val zielvektor: Vektor = ermittle_zielvektor_binaer(zielString)

        // Tastenkombis
        val zuVerwendendeVektoren = ermittle_zu_verwendende_Vektoren_aus_Liste(einzelAufgabe, n)

        val ergebnis = minimale_Anzahl_fuer_zielvektor_aus_liste(zielvektor, zuVerwendendeVektoren)
        print(ergebnis.toString()+"+")
        summe += ergebnis
    }
    return summe
}

// Rekursive Funktion mit Memoization
// Finde minimale Anzahl Tastenkombis, mit der sich Zielvektor erhalten lässt
// minimale_Anzahl_fuer_zielvektor_aus_liste

fun minimale_Anzahl_fuer_zielvektor_aus_liste(ziel: Vektor, tastenkombiListe: List<Vektor>): Int {
    // Basisfälle
    if (ziel.istNullvektor()) {
        memoTabelle[ziel] = 0
        return 0
    }
    if (ziel in tastenkombiListe) {
        memoTabelle[ziel] = 1
        return 1
    }

    // Memo-Abfrage
    val memoWert = memoTabelle[ziel]
    if (memoWert != null) {
        // Ergebnis liegt vor
        return memoWert
    }
    if (memoTabelle.containsKey(ziel)) {
        // Schlüssel existiert, aber Wert ist null => in Bearbeitung
        return -1
    }

    // Markiere als "in Bearbeitung"
    memoTabelle[ziel] = null

    var bestesErgebnis = Int.MAX_VALUE

    for (kombi in tastenkombiListe) {
        val rest = ziel xor kombi // Addition mod 2
        val kandidat = minimale_Anzahl_fuer_zielvektor_aus_liste(rest, tastenkombiListe)
        if (kandidat != -1) {
            bestesErgebnis = minOf(bestesErgebnis, 1 + kandidat)
        }
    }

    // Ergebnis eintragen
    val ergebnis = if (bestesErgebnis == Int.MAX_VALUE) -1 else bestesErgebnis
    memoTabelle[ziel] = ergebnis
    return ergebnis
}

fun erzeuge_BinaerVektor_aus_Indizes(n: Int, indizes: List<Int>): List<Int> {
    // Erzeuge eine leere Ergebnisliste mit n+1 Nullen
    val ergebnis = MutableList(n) { 0 }

    // Gehe alle Zahlen in 'indizes' durch
    for (zahl in indizes) {
        // Setze an der Position (zahl-1) eine 1
        if (zahl in 0..n - 1) { 
	// Sicherheitsprüfung: nur gültige Indizes
            ergebnis[zahl] = 1
        }
    }

    // Gib die fertige Liste zurück
    return ergebnis
}

fun ermittle_zielvektor_binaer(v: String): Vektor {
    val liste = v.map { ch -> if (ch == '#') 1 else 0 }
    return Vektor(liste)
}

fun ermittle_zu_verwendende_Vektoren_aus_Liste(einzelvektoren: List<String>, n: Int): List<Vektor> {
    var zuVerwendendeVektoren = mutableListOf<Vektor>()
    val zuVerwendendeVektorenString = einzelvektoren.dropLast(1).drop(1)
    // enthält jetzt nur eine Liste von Strings wie (0,1,2,3,4) (0,3,4)
    // (0,1,2,4,5) (1,2)

    // Die Tastenkombis in zuVerwendendeVektorenString umwandeln in Bit-Vektoren
    // (0,3,4) wird zu (100110...0) etc
    for (tastenbelegung in zuVerwendendeVektorenString) {
        val umzuwandeln: List<Int> =
                tastenbelegung.drop(1).dropLast(1).split(",").map {
                    it.toInt()
                } // Klammern entfernen, Liste der Zahlen
        // Daraus soll ein Vektor werden: Erhalte zB 0,1,2,5 soll werden zu: 1,1,1,0,0,1
        val erzeugterVektor = Vektor(erzeuge_BinaerVektor_aus_Indizes(n, umzuwandeln))
        zuVerwendendeVektoren.add(erzeugterVektor)
    }
    return zuVerwendendeVektoren
}

fun playground() {
    // Ausprobieren, ob Vektor-Addition und Memoization wie gewünscht funktioniert
    // Einzelne Tastenkombination: hier die vier Einheitsvektoren
    val kombiBeispiel =
            listOf(
                    Vektor(listOf(1, 0, 0, 0)),
                    Vektor(listOf(0, 1, 0, 0)),
                    Vektor(listOf(0, 0, 1, 0)),
                    Vektor(listOf(0, 0, 0, 1))
            )

    // Zielvektor: wir wollen [.##.], also (0,1,1,0) darstellen
    val ziel = Vektor(listOf(0, 1, 1, 0))

    val ergebnis = minimale_Anzahl_fuer_zielvektor_aus_liste(ziel, kombiBeispiel)
    println("Minimale Tastenkombi-Anzahl für $ziel = $ergebnis")
}


// Ein einfacher Vektor in R^n mit ganzzahligen Komponenten
data class Vektor(val komponenten: List<Int>) {

    // Länge des Vektors (Dimension)
    val dimension: Int
        get() = komponenten.size

    // Zugriff auf einzelne Komponenten
    operator fun get(i: Int): Int = komponenten[i]

    // Subtraktion
    operator fun minus(other: Vektor): Vektor {
        require(dimension == other.dimension)
        return Vektor(komponenten.zip(other.komponenten) { a, b -> a - b })
    }

    fun nullvektor() = List(dimension) { 0 }
    fun einheitsvektor() = List(dimension) { 1 }

    fun alleKomponentenNichtnegativ() = komponenten.all { it >= 0 }
    fun istNullvektor() = komponenten.all { it == 0 }

    override fun toString(): String {
        return "V(${komponenten.joinToString(",")})"
    }

    infix fun xor(other: Vektor): Vektor {
        require(this.dimension == other.dimension) { "Vektordimensionen müssen übereinstimmen." }
        val neueKomponenten = MutableList(this.dimension) { i -> (this[i] + other[i]) % 2 }
        return Vektor(neueKomponenten)
    }
}
