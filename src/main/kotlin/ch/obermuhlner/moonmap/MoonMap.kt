package ch.obermuhlner.moonmap

import ch.obermuhlner.moonmap.javafx.MoonMapApplication
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import java.awt.image.BufferedImage
import java.io.File
import java.time.ZonedDateTime
import javax.imageio.ImageIO
import kotlin.math.min

object MoonMap {
    @JvmStatic
    fun main(args: Array<String>) = mainBody {
        ArgParser(args).parseInto(::MoonMapCli).run {
            execute()
        }
    }
}

class MoonMapCli(parser: ArgParser) {

    private val versionMode by parser.flagging(
        "--version",
        help = "print version"
    )

    private val verboseMode by parser.flagging(
        "-v", "--verbose",
        help = "enable verbose mode"
    )

    private val interactiveMode by parser.flagging(
        "-i", "--interactive",
        help = "enable interactive mode"
    )

    private val argDate by parser.storing(
        "-d", "--date",
        help = "date in format yyyy-mm-ddThh:mm:ss"
    ).default("")

    private val argCenterX by parser.storing(
        "-x", "--center-x",
        help = "moon center pixel on x-axis"
    ).default("")

    private val argCenterY by parser.storing(
        "-y", "--center-y",
        help = "moon center pixel on y-axis"
    ).default("")

    private val argRadius by parser.storing(
        "-r", "--radius",
        help = "moon radius in pixels"
    ).default("")

    private val argRotation by parser.storing(
        "-a", "--rotate",
        help = "rotation in degrees"
    ).default("0")

    private val argLibrationLatitude by parser.storing(
        "-b", "--libration-latitude",
        help = "libration latitude in degrees"
    ).default("0")

    private val argLibrationLongitude by parser.storing(
        "-l", "--libration-longitude",
        help = "libration latitude in degrees"
    ).default("0")

    private val argPhase by parser.storing(
        "-p", "--phase",
        help = "moon phase in percent"
    ).default("")

    private val argOutputSuffix: String by parser.storing(
        "-o", "--output-suffice",
        help = "output suffix"
    ).default("_overlay")

    private val filenames by parser.positionalList(
        "FILES",
        help = "image files to process",
        0..Int.MAX_VALUE)

    fun execute() {
        if (versionMode) {
            println(VERSION)
            return
        }

        if (interactiveMode) {
            MoonMapApplication.main(arrayOf())
            return
        }

        for (filename in filenames) {
            val image = ImageIO.read(File(filename))

            val centerX = parseCenterX(image)
            val centerY = parseCenterY(image)
            val radius = parseRadius(image)
            val rotation = parseRotation()
            val phase = parsePhase()
            val librationLatitude = parseLibrationLatitude()
            val librationLongitude = parseLibrationLongitude()
            val strokeWidth = parseStrokeWidth(image)

            println("Phase: $phase")

            val moonMapOverlay = MoonMapOverlay(
                centerX,
                centerY,
                radius,
                rotation,
                librationLatitude,
                librationLongitude,
                phase,
                strokeWidth
            )

            moonMapOverlay.loadMaria()
            moonMapOverlay.loadVisibleCraters()
            //moonMapOverlay.loadCraters() { it.diameter > 200 }

            val overlayImage = moonMapOverlay.overlay(image)

            ImageIO.write(overlayImage, "png", File("$filename$argOutputSuffix.png"))
        }
    }

    private fun parseCenterX(image: BufferedImage): Int {
        val center = image.width / 2
        if (argCenterX.isEmpty()) {
            return center
        }
        if (argCenterX.startsWith("+") || argCenterX.startsWith("-")) {
            return center + argCenterX.toInt()
        }
        return argCenterX.toInt()
    }

    private fun parseCenterY(image: BufferedImage): Int {
        val center = image.height / 2
        if (argCenterY.isEmpty()) {
            return center
        }
        if (argCenterY.startsWith("+") || argCenterY.startsWith("-")) {
            return center + argCenterY.toInt()
        }
        return argCenterY.toInt()
    }

    private fun parseRadius(image: BufferedImage): Int {
        if (argRadius.isEmpty()) {
            return (min(image.width, image.height) / 2 * 0.8).toInt()
        }
        if (argRadius.endsWith("%")) {
            val percent = argRadius.substring(0, argRadius.lastIndex).toDouble()
            val factor = percent / 100.0
            val pixels = min(image.width, image.height) / 2 * factor
            return pixels.toInt()
        }
        return argRadius.toInt()
    }

    private fun parseLibrationLatitude(): Double {
        return argLibrationLatitude.toDouble()
    }

    private fun parseLibrationLongitude(): Double {
        return argLibrationLongitude.toDouble()
    }

    private fun parseDateTime(): ZonedDateTime {
        if (argDate.isEmpty()) {
            return ZonedDateTime.now()
        }
        return ZonedDateTime.parse(argDate)
    }

    private fun parsePhase(): Double {
        if (argPhase.isEmpty()) {
            return MoonCalculator.phase(parseDateTime())
        }
        if (argPhase.endsWith("%")) {
            val percent = argPhase.substring(0, argPhase.lastIndex).toDouble()
            val factor = percent / 100.0
            return factor
        }
        return argPhase.toDouble()
    }

    private fun parseRotation(): Double {
        return argRotation.toDouble()
    }

    private fun parseStrokeWidth(image: BufferedImage): Float {
        return min(image.width, image.height) * 0.001f
    }

    companion object {
        const val VERSION = "0.0.1"
    }
}