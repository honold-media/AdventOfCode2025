import java.io.File

fun main() {
    val bsp1: String = File("advent_09_input_bsp.txt").readText().trim()
    val bsp2: String = File("advent_09_input.txt").readText().trim()
    // groesste_flaeche(bsp1)
    groesste_flaeche(tiles)
}

/* Flächeninhalt :
Jeweils für Höhe und Breite = Höherer Spaltenwert minus niedrigerer
Wenn gleich? Dann 1 => Absolutwerte nehmen
*/


fun groesste_flaeche(input: String): Long {
    var iAnz=0
    var maximum = 0L
    var max_x_1 = 0L
    var max_y_1 = 0L
    var max_x_2 = 0L
    var max_y_2 = 0L

    val n = input.lines().count()

    val xs = LongArray(n)
    val ys = LongArray(n)

    // Einlesen
    input.lines().forEachIndexed { i, line ->
        val koordinaten = line.split(",")
        xs[i] = koordinaten[0].toLong()
        ys[i] = koordinaten[1].toLong()
    }

    // Rechtecksfläche berechnen
    for (i in xs.indices) {
        for (j in i + 1 until xs.size) {
            iAnz++
            val x1 = xs[i]
            val y1 = ys[i]
            val x2 = xs[j]
            val y2 = ys[j]
            // print("Die Ecken ("+x1+","+y1+") und ("+x2+","+y2+") ergeben ")

            val breite = kotlin.math.abs(x1 - x2)+1
            val hoehe = kotlin.math.abs(y1 - y2)+1
            val flaeche = breite * hoehe
            // println (flaeche)

            if (maximum < flaeche) {
                maximum = flaeche
                max_x_1 = x1
                max_y_1 = y1
                max_x_2 = x2
                max_y_2 = y2
            }
        }
    }

    println("Die maximale Flaeche (" + maximum + ") wurde erreicht mit den Koordinaten ")
    println(max_x_1.toString() + ", " + max_y_1.toString())
    println(" und ")
    println(max_x_2.toString() + ", " + max_y_2.toString())
    println("Anzahl untersuchter Kombnationen: "+iAnz)

    return maximum
}
