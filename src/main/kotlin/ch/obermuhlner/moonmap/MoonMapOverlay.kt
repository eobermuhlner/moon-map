package ch.obermuhlner.moonmap

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.imageio.ImageIO
import kotlin.math.*

class MoonMapOverlay(
    val image: BufferedImage,
    val centerX: Int,
    val centerY: Int,
    val radius: Int,
    val librationLatitude: Double,
    val librationLongitude: Double,
    val rotation: Double,
    val strokeWidth: Float
) {
    val pointsOfInterest: MutableSet<PointOfInterest> = mutableSetOf()

    fun loadMaria(filter: (PointOfInterest) -> Boolean = { true }) {
        loadPoints(PointType.Mare, "MoonMaria.csv", filter)
    }

    private val visibleCraterNames = setOf(
        "Tycho", "Copernicus", "Aristarchus", "Kepler", "Plato", "Ptolemaeus")
    fun loadVisibleCraters() {
        loadPoints(PointType.Crater, "MoonCraters.csv") {
            it.name in visibleCraterNames
        }
    }

    fun loadCraters(filter: (PointOfInterest) -> Boolean = { true }) {
        loadPoints(PointType.Crater, "MoonCraters.csv", filter)
    }

    fun loadPoints(type: PointType, resource: String, filter: (PointOfInterest) -> Boolean = { true }) {
        val resourceStream = this::class.java.classLoader.getResourceAsStream(resource)
        if (resourceStream != null) {
            for (line in BufferedReader(InputStreamReader(resourceStream)).lines()) {
                val cells = line.split(",")
                println(cells)
                val name = cells[0]
                val diameter = cells[1].toDouble()
                val latitude = cells[2].toDouble()
                val longitude = cells[3].toDouble()

                val pointOfInterest = PointOfInterest(type, name, diameter, latitude, -longitude)
                if (filter.invoke(pointOfInterest)) {
                    pointsOfInterest += pointOfInterest
                }
            }
        }
    }

    fun overlay(): BufferedImage {
        val output = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        val graphics = output.createGraphics()!!

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.stroke = BasicStroke(strokeWidth)
        graphics.font = graphics.font.deriveFont(graphics.font.size * strokeWidth)

        graphics.drawImage(image, 0, 0, null)

        graphics.color = Color.green.darker().darker()
        graphics.drawOval(centerX-radius, centerY-radius, radius*2, radius*2)
        drawCoordinateGrid(graphics)

//        graphics.color = Color.YELLOW
//        drawName(graphics, "Tycho", -43.3, 11.22, true)
//        drawName(graphics, "Copernicus", 9.62, 20.08, true)
//        drawName(graphics, "Aristarchus", 23.73, 47.49, true)
//        drawName(graphics, "Kepler", 8.12, 38.01, true)
//        drawName(graphics, "Plato", 51.62, 9.38, true)
//        drawName(graphics, "Ptolemaeus", -9.16, 1.84, true)

        graphics.color = Color.GREEN.brighter()
        for (point in pointsOfInterest) {
            drawPointOfInterest(graphics, point)
        }

        return output
    }

    private fun drawCoordinateGrid(graphics: Graphics2D) {
        for (latitude in -90 .. 90 step 10) {
            drawLatitude(graphics, latitude.toDouble())
        }
        for (longitude in -90 .. 90 step 10) {
            drawLongitude(graphics, longitude.toDouble())
        }
    }

    private fun drawLatitude(graphics: Graphics2D, latitude: Double) {
        var lastPoint: CartesianPoint? = null
        for (longitude in -90 .. 90 step 2) {
            val nextPoint = sphereToCartesianPoint(latitude, longitude.toDouble())
            if (lastPoint != null) {
                drawLine(graphics, lastPoint, nextPoint)
            }
            if (longitude == -90) {
                drawName(graphics, latitude.toString(), latitude, longitude.toDouble())
            }
            lastPoint = nextPoint
        }
    }

    private fun drawLongitude(graphics: Graphics2D, longitude: Double) {
        var lastPoint: CartesianPoint? = null
        for (latitude in -90 .. 90 step 2) {
            val nextPoint = sphereToCartesianPoint(latitude.toDouble(), longitude)
            if (lastPoint != null) {
                drawLine(graphics, lastPoint, nextPoint)
            }
            if (latitude == 0) {
                drawName(graphics, longitude.toString(), latitude.toDouble(), longitude)
            }
            lastPoint = nextPoint
        }
    }

    private fun drawLine(graphics: Graphics2D, begin: CartesianPoint, end: CartesianPoint) {
        graphics.drawLine(begin.x.toInt(), begin.y.toInt(), end.x.toInt(), end.y.toInt())
    }

    private fun drawPointOfInterest(graphics: Graphics2D, pointOfInterest: PointOfInterest) {
        graphics.color = when (pointOfInterest.type) {
            PointType.Mare -> Color.GREEN.brighter().brighter()
            PointType.Crater -> Color.YELLOW
        }
        drawName(graphics, pointOfInterest.name, pointOfInterest.latitude, pointOfInterest.longitude, pointOfInterest.type == PointType.Crater)
    }

    private fun drawName(graphics: Graphics2D, name: String, latitude: Double, longitude: Double, marker: Boolean = false) {
        val cartesianPoint = sphereToCartesianPoint(latitude, longitude)

        if (marker) {
            val markerRadius = graphics.font.size / 10
            graphics.fillOval(cartesianPoint.x.toInt() - markerRadius, cartesianPoint.y.toInt() - markerRadius, markerRadius*2, markerRadius*2)
        }

        val color = graphics.color
        graphics.color = Color.BLACK
        val step = graphics.font.size / 20
        graphics.drawString(name, cartesianPoint.x.toInt() - step, cartesianPoint.y.toInt() - step)
        graphics.drawString(name, cartesianPoint.x.toInt() - step, cartesianPoint.y.toInt() + step)
        graphics.drawString(name, cartesianPoint.x.toInt() + step, cartesianPoint.y.toInt() - step)
        graphics.drawString(name, cartesianPoint.x.toInt() + step, cartesianPoint.y.toInt() + step)

        graphics.color = color
        graphics.drawString(name, cartesianPoint.x.toInt(), cartesianPoint.y.toInt())
    }

    private fun sphereToCartesianPoint(latitude: Double, longitude: Double): CartesianPoint {
        val x = radius * cos(radians(-latitude + librationLatitude)) * cos(radians(longitude + librationLongitude + 90))
        //val y = radius * cos(radians(-latitude + librationLatitude)) * sin(radians(longitude + librationLongitude + 90))
        val z = radius * sin(radians(-latitude + librationLatitude))

        var cartesianPoint = CartesianPoint(x, z)

        if (rotation != 0.0) {
            val polarPoint = PolarPoint(cartesianPoint)
            cartesianPoint = CartesianPoint(PolarPoint(polarPoint.radius, polarPoint.angle + rotation))
        }

        return CartesianPoint(cartesianPoint.x + centerX, cartesianPoint.y + centerY)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            map1("images/moon_2021-04-23.jpg")
            map2("images/FullMoon2010.jpg")
        }

        fun map1(filepath: String) {
            val image = ImageIO.read(File(filepath))!!

            val offsetX = -60
            val offsetY = +120
            val centerX = image.width / 2 + offsetX
            val centerY = image.height / 2 + offsetY
            val radius = (min(image.width, image.height) * 0.8 / 2).toInt()
            val librationLatitude = -6.6
            val librationLongitude = -6.5
            val rotation = radians(-16.0)
            val strokeWidth = min(image.width, image.height) * 0.001f

            val map = MoonMapOverlay(image, centerX, centerY, radius, librationLatitude, librationLongitude, rotation, strokeWidth)
            map.loadMaria()
            map.loadVisibleCraters()
            map.loadCraters() { it.diameter > 120 }
            val overlayImage = map.overlay()

            ImageIO.write(overlayImage, "png", File("overlay1.png"))
        }

        fun map2(filepath: String) {
            val image = ImageIO.read(File(filepath))!!

            val offsetX = 0
            val offsetY = 20
            val centerX = image.width / 2 + offsetX
            val centerY = image.height / 2 + offsetY
            val radius = (min(image.width, image.height) * 0.87 / 2).toInt()
            val librationLatitude = -7.5
            val librationLongitude = 6.0
            val rotation = radians(10.0)
            val strokeWidth = min(image.width, image.height) * 0.001f

            val map = MoonMapOverlay(image, centerX, centerY, radius, librationLatitude, librationLongitude, rotation, strokeWidth)
            map.loadMaria()
            map.loadVisibleCraters()
            val overlayImage = map.overlay()

            ImageIO.write(overlayImage, "png", File("overlay2.png"))
        }

        fun radians(degree: Double): Double {
            return degree / 360.0 * 2 * PI
        }
    }
}

enum class PointType {
    Mare,
    Crater,
}

data class PointOfInterest(val type: PointType, val name: String, val diameter: Double, val latitude: Double, val longitude: Double)

data class CartesianPoint(val x: Double, val y: Double) {
    constructor(polarPoint: PolarPoint)
            : this(polarPoint.radius * cos(polarPoint.angle), polarPoint.radius * sin(polarPoint.angle))
}

data class PolarPoint(val radius: Double, val angle: Double) {
    constructor(cartesian: CartesianPoint)
            : this(sqrt(cartesian.x*cartesian.x + cartesian.y*cartesian.y), atan2(cartesian.y, cartesian.x))
}
