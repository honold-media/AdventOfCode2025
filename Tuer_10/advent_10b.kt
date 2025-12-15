import java.io.File
// Anregung für die Vorgehensweise stammt von tenthmascot:
// https://www.reddit.com/r/adventofcode/comments/1pk87hl/comment/ntp4njq/

/*
Bestimmte Schalter (Bitvektoren für je 1 Tastendruck) erhöhen Zähler erhöhen und es soll ein Zielvektor erreicht werden. 
Es soll die minimale Anzahl an Tastendrücken gefunden.

Zunächst werden alle möglichen Kombinationen von Schaltern gebildet. 
Für jede Kombination wird berechnet, wie sich die Zählerstände verändern, wenn diese Schalter einmal gedrückt werden. 
Das Ergebnis ist ein Muster mit einer bestimmten Länge, die als Kosten gespeichert wird. 
Anschließend wird rekursiv gearbeitet: 
* Für ein Ziel wird geprüft, ob es bereits gelöst ist. 
* Wenn alle Werte null sind, ist keine weitere Aktion nötig. 
* Andernfalls werden alle Muster betrachtet. 
* Ein Muster ist nur gültig, wenn es nicht größer als das Ziel ist 
    und die gerade‑ungerade‑Eigenschaft passt. 
    Dann wird ein neues Ziel berechnet, indem die Differenz genommen 
    und durch zwei geteilt wird. 
    Die Kosten ergeben sich aus der Länge des Musters 
    plus dem doppelten Wert der Lösung des neuen Ziels. 
Durch diese Halbierung wird das Problem Schritt für Schritt kleiner, 
bis nur noch Nullen übrig sind. 

*/

fun main() {
    // Beispiel-Dateien einlesen und Leerzeichen am Anfang/Ende entfernen
    val beispiel1: String = File("input_10_bsp.txt").readText().trim()
    val beispiel2: String = File("input_10.txt").readText().trim()

    // Zum Test: ein einzelnes Beispiel als String
    val einzelBeispiel = "[####] (0) (1,2,3) (0,1) (0,2,3) (2,3) {39,30,40,40}"

    println("Willkommen bei Teil 2 von Tag 10")
    println("Aufgabe: ")
    loesung_10_b(beispiel2)
}

/**
 * Hilfsfunktion: erzeugt alle möglichen Kombinationen der Länge k 
 * von Elementen einer Liste (Reihenfolge ohne Belang, [1,2]=[2,1]).
 * Beispiel: Kombinationen von [1,2,3] mit Länge 2 sind [[1,2],[1,3],[2,3]]
 */
fun <T> kombinationen(liste: List<T>, k: Int): List<List<T>> {
    if (k == 0) return listOf(emptyList())
    if (liste.isEmpty()) return emptyList()
    val kopf = liste.first()
    val rest = liste.drop(1)
    val kombisOhneKopf = kombinationen(rest, k)
    val kombisMitKopf = kombinationen(rest, k - 1).map { listOf(kopf) + it }
    return kombisMitKopf + kombisOhneKopf
}

/**
 * Berechnet alle möglichen Muster (Summen von Koeffizienten).
 * Für jede Kombination von 0-1-"Schaltern" (Bitvektoren) wird ein Muster erzeugt.
 */
fun musterBerechnung(koeffizienten: List<List<Int>>): Map<List<Int>, Int> {
    val ergebnis = mutableMapOf<List<Int>, Int>()
    val anzahlBitvektoren = koeffizienten.size
    val anzahlVariablen = koeffizienten[0].size

    // Für jede mögliche Länge von Kombinationen
    for (musterLaenge in 0..anzahlBitvektoren) {
        for (knoepfe in kombinationen((0 until anzahlBitvektoren).toList(), musterLaenge)) {
            val muster = MutableList(anzahlVariablen) { 0 }
            // Summiere die Koeffizienten für die gewählten Knöpfe
            for (i in knoepfe) {
                for (j in 0 until anzahlVariablen) {
                    muster[j] += koeffizienten[i][j]
                }
            }
            // Speichere das Muster mit seiner Länge (Kosten)
            ergebnis.putIfAbsent(muster, musterLaenge)
        }
    }
    return ergebnis
}

/**
 * Löse eine einzelne Aufgabe mit Memoisierung
 * unter Nutzung der Teilbarkeit durch zwei in jedem Schritt (siehe Erklärung oben)
 * Ziel: Finde minimale Anzahl von Mustern, die das Ziel erfüllen.
 */
fun loeseEinzeln(koeffizienten: List<List<Int>>, ziel: List<Int>): Int {
    val musterKosten = musterBerechnung(koeffizienten)
    val cache = mutableMapOf<List<Int>, Int>()

    fun loeseHilfs(ziel: List<Int>): Int {
        // Falls schon berechnet, aus dem Cache zurückgeben
        cache[ziel]?.let { return it }

        // Basisfall: Ziel ist nur Nullen -> keine Kosten
        if (ziel.all { it == 0 }) return 0

        var antwort = 1_000_000

        // Für jedes Muster prüfen, ob es zum Ziel passt
        for ((muster, kosten) in musterKosten) {
            if (muster.zip(ziel).all { (i, j) -> i <= j && i % 2 == j % 2 }) {
                // Neues Ziel berechnen
                val neuesZiel = muster.zip(ziel).map { (i, j) -> (j - i) / 2 }
                val kandidat = kosten + 2 * loeseHilfs(neuesZiel)
                if (kandidat < antwort) antwort = kandidat
            }
        }

        cache[ziel] = antwort
        return antwort
    }

    return loeseHilfs(ziel)
}

/**
 * Hauptfunktion zum Lösen aller Zeilen einer Eingabedatei.
 */
fun loesung_10_b(rohdaten: String) {
    var gesamt = 0
    val zeilen = rohdaten.lines().filter { it.isNotBlank() }

    for ((index, zeile) in zeilen.withIndex()) {
        val teile = zeile.split(" ")
        val koeffTeil = teile.drop(1).dropLast(1)
        val zielString = teile.last()

        // Zielwerte aus geschweiften Klammern extrahieren
        val ziel = zielString.removePrefix("{").removeSuffix("}")
            .split(",").map { it.toInt() }

        // Koeffizientenmatrix erstellen
        val koeffizienten = koeffTeil.map { r ->
            val zahlen = r.removePrefix("(").removeSuffix(")")
                .split(",").map { it.toInt() }
            List(ziel.size) { i -> if (zahlen.contains(i)) 1 else 0 }
        }

        val teilAntwort = loeseEinzeln(koeffizienten, ziel)
        println("Zeile ${index + 1}/${zeilen.size}: Antwort $teilAntwort")
        gesamt += teilAntwort
    }

    println("Gesamtergebnis: $gesamt")
}