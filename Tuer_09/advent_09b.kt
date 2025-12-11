import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.geom.Line2D
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


fun main() {
    // val bsp1: String = File("advent_09_bsp_input.txt").readText().trim()
    val bsp2: String = File("advent_09_input.txt").readText().trim()
    
    // tuer_09b(bsp1)
    tuer_09b(bsp2)
}

fun tuer_09b(input: String): Long {
    var maximum = 0L
    var max_x_1 = 0
    var max_y_1 = 0
    var max_x_2 = 0
    var max_y_2 = 0

    val n = input.lines().count()

    val xs = IntArray(n)
    val ys = IntArray(n)

    // Einlesen
    input.lines().forEachIndexed { i, line ->
        val koordinaten = line.split(",")
        xs[i] = koordinaten[0].toInt()
        ys[i] = koordinaten[1].toInt()
    }

    // Polygon mit Koordinaten definieren
    val myPolygon =
            Polygon(
                    xs, // x-Koordinaten
                    ys, // y-Koordinaten
                    n // Anzahl der Punkte
            )

    // Rechtecksfläche zwischen 2 roten Teilen berechnen, wenn...
    for (i in xs.indices) {
        for (j in i + 1 until xs.size) {
            val x1 = xs[i]
            val y1 = ys[i]
            val x2 = xs[j]
            val y2 = ys[j]
            // ... wenn das
            // print("Rechteck mit den Ecken (" + x1 + "," + y1 + ") und (" + x2 + "," + y2 + ") ")
            // im Polygon liegt:

            val breite = kotlin.math.abs(x1 - x2) + 1
            val hoehe = kotlin.math.abs(y1 - y2) + 1
            if ( rechteck_enthalten_einschliesslich_Kanten(x1, y1, x2, y2, myPolygon)) {
                val flaeche: Long = (breite * hoehe).toLong()
                if (maximum < flaeche) {
                    maximum = flaeche
                    max_x_1 = x1
                    max_y_1 = y1
                    max_x_2 = x2
                    max_y_2 = y2
                }
                // println("enthalten; Flaeche = " + flaeche)
            } // else println("nicht enthalten")
        }
    }

    println("Die maximale Flaeche (" + maximum + ") wurde erreicht mit den Koordinaten ")
    println(max_x_1.toString() + ", " + max_y_1.toString())
    println(" und ")
    println(max_x_2.toString() + ", " + max_y_2.toString())

    return maximum
}

private const val EPS = 1e-9

fun rechteck_enthalten_einschliesslich_Kanten(x1: Int, y1: Int, x2: Int, y2: Int, poly: Polygon): Boolean {
    val minX = min(x1, x2)
    val maxX = max(x1, x2)
    val minY = min(y1, y2)
    val maxY = max(y1, y2)

    // 1) Ecken prüfen: müssen innen liegen oder auf Polygon-Kante
    val ecken = listOf(
        Point(minX, minY),
        Point(maxX, minY),
        Point(minX, maxY),
        Point(maxX, maxY)
    )
    val ecken_Ok = ecken.all { p ->
        poly.contains(p) || punkt_liegt_auf_Polygonkante(p, poly)
    }
    if (!ecken_Ok) return false

    // 2) Es darf keine echte Kreuzung zwischen Rechteckkanten und Polygonkanten geben
    val rechteckKanten = listOf(
        Line2D.Double(minX.toDouble(), minY.toDouble(), maxX.toDouble(), minY.toDouble()), // oben
        Line2D.Double(minX.toDouble(), maxY.toDouble(), maxX.toDouble(), maxY.toDouble()), // unten
        Line2D.Double(minX.toDouble(), minY.toDouble(), minX.toDouble(), maxY.toDouble()), // links
        Line2D.Double(maxX.toDouble(), minY.toDouble(), maxX.toDouble(), maxY.toDouble())  // rechts
    )

    for (re in rechteckKanten) {
        for (i in 0 until poly.npoints) {
            val xA = poly.xpoints[i].toDouble()
            val yA = poly.ypoints[i].toDouble()
            val xB = poly.xpoints[(i + 1) % poly.npoints].toDouble()
            val yB = poly.ypoints[(i + 1) % poly.npoints].toDouble()
            val polygonKante = Line2D.Double(xA, yA, xB, yB)

            if (linien_schneiden_sich(re, polygonKante)) {
                return false
            }
        }
    }

    return true
}

// Punkt liegt exakt auf irgendeiner Polygonkante (inkl. Endpunkte)
private fun punkt_liegt_auf_Polygonkante(p: Point, poly: Polygon): Boolean {
    for (i in 0 until poly.npoints) {
        val x1 = poly.xpoints[i].toDouble()
        val y1 = poly.ypoints[i].toDouble()
        val x2 = poly.xpoints[(i + 1) % poly.npoints].toDouble()
        val y2 = poly.ypoints[(i + 1) % poly.npoints].toDouble()
        if (pointOnSegment(p.x.toDouble(), p.y.toDouble(), x1, y1, x2, y2)) return true
    }
    return false
}

// Kollinear & innerhalb der Segment-Box
private fun pointOnSegment(px: Double, py: Double, x1: Double, y1: Double, x2: Double, y2: Double): Boolean {
    val cross = (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1)
    if (abs(cross) > EPS) return false
    val withinX = px >= min(x1, x2) - EPS && px <= max(x1, x2) + EPS
    val withinY = py >= min(y1, y2) - EPS && py <= max(y1, y2) + EPS
    return withinX && withinY
}

// Echte Kreuzung: Segmente schneiden sich in einem Punkt, der nicht nur kollineares Anliegen ist
private fun linien_schneiden_sich(a: Line2D, b: Line2D): Boolean {
    // Orientations
    fun orient(ax: Double, ay: Double, bx: Double, by: Double, cx: Double, cy: Double): Double {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)
    }

    val o1 = orient(a.x1, a.y1, a.x2, a.y2, b.x1, b.y1)
    val o2 = orient(a.x1, a.y1, a.x2, a.y2, b.x2, b.y2)
    val o3 = orient(b.x1, b.y1, b.x2, b.y2, a.x1, a.y1)
    val o4 = orient(b.x1, b.y1, b.x2, b.y2, a.x2, a.y2)

    // Strikte Kreuzung: unterschiedliche Seiten für beide Paare
    val strict = (o1 * o2 < -EPS) && (o3 * o4 < -EPS)
    if (strict) return true

    // Sonderfälle kollinear/berührend (Endpunkte oder Überlappung): NICHT als Kreuzung werten
    // Endpunkte auf jeweils anderem Segment?
    if (abs(o1) <= EPS && pointOnSegment(b.x1, b.y1, a.x1, a.y1, a.x2, a.y2)) return false
    if (abs(o2) <= EPS && pointOnSegment(b.x2, b.y2, a.x1, a.y1, a.x2, a.y2)) return false
    if (abs(o3) <= EPS && pointOnSegment(a.x1, a.y1, b.x1, b.y1, b.x2, b.y2)) return false
    if (abs(o4) <= EPS && pointOnSegment(a.x2, a.y2, b.x1, b.y1, b.x2, b.y2)) return false

    // Kollineare Überlappung: nicht als Kreuzung zählen (liegt auf der Kante)
    val collinear = abs(o1) <= EPS && abs(o2) <= EPS && abs(o3) <= EPS && abs(o4) <= EPS
    if (collinear) return false

    // Sonst keine Kreuzung
    return false
}