package ch.obermuhlner.moonmap

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import java.awt.image.BufferedImage
import java.io.File
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

    private val argDate by parser.storing(
        "-d", "--date",
        help = "date in format yyyy-mm-dd"
    ).default("")

    private val argTime by parser.storing(
        "-t", "--time",
        help = "time in format hh:mm:ss"
    ).default("22:00:00")

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
        "--rotation",
        help = "rotation in degrees"
    ).default("0")

    private val argLibrationLatitude by parser.storing(
        "--libration-latitude",
        help = "libration latitude in degrees"
    ).default("0")

    private val argLibrationLongitude by parser.storing(
        "--libration-longitude",
        help = "libration latitude in degrees"
    ).default("0")

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

        for (filename in filenames) {
            val image = ImageIO.read(File(filename))

            val centerX = parseCenterX(image)
            val centerY = parseCenterY(image)
            val radius = parseRadius(image)
            val rotation = parseRotation()
            val librationLatitude = parseLibrationLatitude()
            val librationLongitude = parseLibrationLongitude()
            val strokeWidth = parseStrokeWidth(image)

            val moonMapOverlay = MoonMapOverlay(
                image,
                centerX,
                centerY,
                radius,
                librationLatitude,
                librationLongitude,
                rotation,
                strokeWidth)

            val overlayImage = moonMapOverlay.overlay()

            ImageIO.write(overlayImage, "png", File("$filename$argOutputSuffix.png"))
        }
    }

    private fun parseCenterX(image: BufferedImage): Int {
        if (argCenterX.isEmpty()) {
            return image.width / 2
        }
        return argCenterX.toInt()
    }

    private fun parseCenterY(image: BufferedImage): Int {
        if (argCenterY.isEmpty()) {
            return image.height / 2
        }
        return argCenterY.toInt()
    }

    private fun parseRadius(image: BufferedImage): Int {
        if (argRadius.isEmpty()) {
            return (min(image.width, image.height) * 0.8).toInt()
        }
        if (argRadius.endsWith("%")) {
            val percent = argRadius.substring(0, argRadius.lastIndex - 1).toDouble()
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