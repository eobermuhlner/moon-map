package ch.obermuhlner.moonmap.javafx

import javafx.application.Application
import javafx.scene.Node
import javafx.scene.Scene
import javafx.stage.Stage
import ch.obermuhlner.kotlin.javafx.*
import ch.obermuhlner.moonmap.MoonMapOverlay
import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.event.EventHandler
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.stage.FileChooser
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

class MoonMapApplication : Application() {

    private val imageWidthProperty: IntegerProperty = SimpleIntegerProperty()
    private val imageHeightProperty: IntegerProperty = SimpleIntegerProperty()

    private val centerXProperty: IntegerProperty = SimpleIntegerProperty()
    private val centerYProperty: IntegerProperty = SimpleIntegerProperty()
    private val radiusProperty: IntegerProperty = SimpleIntegerProperty()
    private val rotationProperty: DoubleProperty = SimpleDoubleProperty()
    private val phaseProperty: DoubleProperty = SimpleDoubleProperty()
    private val librationLatitudeProperty: DoubleProperty = SimpleDoubleProperty()
    private val librationLongitudeProperty: DoubleProperty = SimpleDoubleProperty()
    private val strokeWidthProperty: DoubleProperty = SimpleDoubleProperty()

    private var homeDirectory = Paths.get(System.getProperty("user.home", "."))

    private var currentBufferedImage = BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB)

    private val currentImageView = imageview {
        isPreserveRatio = true
        fitWidth = IMAGE_WIDTH.toDouble()
        fitHeight = IMAGE_HEIGHT.toDouble()
    }

    private val upIcon = Image("icons/baseline_keyboard_arrow_up_black_18dp.png")
    private val downIcon = Image("icons/baseline_keyboard_arrow_down_black_18dp.png")
    private val leftIcon = Image("icons/baseline_keyboard_arrow_left_black_18dp.png")
    private val rightIcon = Image("icons/baseline_keyboard_arrow_right_black_18dp.png")

    override fun start(primaryStage: Stage) {
        val scene = Scene(createUserInterface(primaryStage))

        primaryStage.scene = scene
        primaryStage.show()

        initializeListeners()
    }

    private fun createUserInterface(stage: Stage): Parent {
        return borderpane {
            top = createToolbar(stage)
            center = createImageViewer()
            right = createEditor()
        }
    }

    private fun createToolbar(stage: Stage): Node {
        return hbox {
            children += button("Load Image ...") {
                onAction = EventHandler {
                    openImageFile(stage)
                }
            }
            children += button("Save Image ...") {
                onAction = EventHandler {

                }
            }
        }
    }

    private fun createImageViewer(): Node {
        return ImageViewPane(currentImageView)
    }

    private fun createEditor(): Node {
        return gridpane {
            row {
                cell {
                    label("Width:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), imageWidthProperty, INTEGER_FORMAT)
                        isEditable = false
                    }
                }
            }
            row {
                cell {
                    label("Height:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), imageHeightProperty, INTEGER_FORMAT)
                        isEditable = false
                    }
                }
            }
            row {
                cell {
                    label("Center X:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), centerXProperty, INTEGER_FORMAT)
                    }
                }
                cell {
                    slider(0.0, IMAGE_WIDTH.toDouble(), IMAGE_WIDTH.toDouble()/2) {
                        isShowTickMarks = true
                        prefWidth = 360.0
                        Bindings.bindBidirectional(valueProperty(), centerXProperty)
                        maxProperty().bind(imageWidthProperty)
                    }
                }
                cell {
                    button {
                        graphic = ImageView(leftIcon)
                        onAction = EventHandler {
                            centerXProperty.set(centerXProperty.get() - 1)
                        }
                    }
                }
                cell {
                    button {
                        graphic = ImageView(rightIcon)
                        onAction = EventHandler {
                            centerXProperty.set(centerXProperty.get() + 1)
                        }
                    }
                }
            }
            row {
                cell {
                    label("Center Y:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), centerYProperty, INTEGER_FORMAT)
                    }
                }
                cell {
                    slider(0.0, IMAGE_HEIGHT.toDouble(), IMAGE_HEIGHT.toDouble()/2) {
                        isShowTickMarks = true
                        Bindings.bindBidirectional(valueProperty(), centerYProperty)
                        maxProperty().bind(imageHeightProperty)
                    }
                }
                cell {
                    button {
                        graphic = ImageView(upIcon)
                        onAction = EventHandler {
                            centerYProperty.set(centerYProperty.get() - 1)
                        }
                    }
                }
                cell {
                    button {
                        graphic = ImageView(downIcon)
                        onAction = EventHandler {
                            centerYProperty.set(centerYProperty.get() + 1)
                        }
                    }
                }
            }
            row {
                cell {
                    label("Radius:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), radiusProperty, INTEGER_FORMAT)
                    }
                }
                cell {
                    slider(0.0, IMAGE_HEIGHT.toDouble(), IMAGE_HEIGHT.toDouble() * 0.8 * 0.5) {
                        isShowTickMarks = true
                        Bindings.bindBidirectional(valueProperty(), radiusProperty)
                        maxProperty().bind(imageWidthProperty.multiply(2))
                    }
                }
                cell {
                    button {
                        graphic = ImageView(leftIcon)
                        onAction = EventHandler {
                            radiusProperty.set(radiusProperty.get() - 1)
                        }
                    }
                }
                cell {
                    button {
                        graphic = ImageView(rightIcon)
                        onAction = EventHandler {
                            radiusProperty.set(radiusProperty.get() + 1)
                        }
                    }
                }
            }
            row {
                cell {
                    label("Rotation:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), rotationProperty, DOUBLE_FORMAT)
                    }
                }
                cell {
                    slider(0.0, 360.0, 0.0) {
                        isShowTickMarks = true
                        isShowTickLabels = true
                        majorTickUnit = 10.0
                        Bindings.bindBidirectional(valueProperty(), rotationProperty)
                    }
                }
                cell {
                    button {
                        graphic = ImageView(leftIcon)
                        onAction = EventHandler {
                            rotationProperty.set(rotationProperty.get() - 0.1)
                        }
                    }
                }
                cell {
                    button {
                        graphic = ImageView(rightIcon)
                        onAction = EventHandler {
                            rotationProperty.set(rotationProperty.get() + 0.1)
                        }
                    }
                }
            }
            row {
                cell {
                    label("Phase:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), phaseProperty, DOUBLE_FORMAT)
                    }
                }
                cell {
                    slider(-100.0, 100.0, 0.0) {
                        isShowTickMarks = true
                        majorTickUnit = 10.0
                        minorTickCount = 10
                        Bindings.bindBidirectional(valueProperty(), phaseProperty)
                    }
                }
                cell {
                    button {
                        graphic = ImageView(leftIcon)
                        onAction = EventHandler {
                            phaseProperty.set(phaseProperty.get() - 0.1)
                        }
                    }
                }
                cell {
                    button {
                        graphic = ImageView(rightIcon)
                        onAction = EventHandler {
                            phaseProperty.set(phaseProperty.get() + 0.1)
                        }
                    }
                }
            }
            row {
                cell {
                    label("Libration Latitude:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), librationLatitudeProperty, DOUBLE_FORMAT)
                    }
                }
                cell {
                    slider(-8.0, 8.0, 0.0) {
                        isShowTickMarks = true
                        isShowTickLabels = true
                        majorTickUnit = 1.0
                        minorTickCount = 10
                        Bindings.bindBidirectional(valueProperty(), librationLatitudeProperty)
                    }
                }
                cell {
                    button {
                        graphic = ImageView(upIcon)
                        onAction = EventHandler {
                            librationLatitudeProperty.set(librationLatitudeProperty.get() + 0.1)
                        }
                    }
                }
                cell {
                    button {
                        graphic = ImageView(downIcon)
                        onAction = EventHandler {
                            librationLatitudeProperty.set(librationLatitudeProperty.get() - 0.1)
                        }
                    }
                }
            }
            row {
                cell {
                    label("Libration Longitude:")
                }
                cell {
                    textfield {
                        Bindings.bindBidirectional(textProperty(), librationLongitudeProperty, DOUBLE_FORMAT)
                    }
                }
                cell {
                    slider(-7.0, 7.0, 0.0) {
                        isShowTickMarks = true
                        isShowTickLabels = true
                        majorTickUnit = 1.0
                        minorTickCount = 10
                        Bindings.bindBidirectional(valueProperty(), librationLongitudeProperty)
                    }
                }
                cell {
                    button {
                        graphic = ImageView(leftIcon)
                        onAction = EventHandler {
                            librationLongitudeProperty.set(librationLongitudeProperty.get() + 0.1)
                        }
                    }
                }
                cell {
                    button {
                        graphic = ImageView(rightIcon)
                        onAction = EventHandler {
                            librationLongitudeProperty.set(librationLongitudeProperty.get() - 0.1)
                        }
                    }
                }
            }
        }
    }

    private fun openImageFile(stage: Stage) {
        val fileChooser = FileChooser()
        fileChooser.initialDirectory = homeDirectory.toFile()
        fileChooser.title = "Open Input Image"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image", "*.tif", "*.tiff", "*.png", "*.jpg", "*.jpeg"))
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("All", "*"))
        val chosenFile = fileChooser.showOpenDialog(stage)
        if (chosenFile != null) {
            try {
                loadImage(chosenFile)
                homeDirectory = chosenFile.parentFile.toPath()
                stage.title = chosenFile.name
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun initializeListeners() {
        centerXProperty.addListener { _, _, _ -> updateMoonMap() }
        centerYProperty.addListener { _, _, _ -> updateMoonMap() }
        radiusProperty.addListener { _, _, _ -> updateMoonMap() }
        rotationProperty.addListener { _, _, _ -> updateMoonMap() }
        phaseProperty.addListener { _, _, _ -> updateMoonMap() }
        librationLatitudeProperty.addListener { _, _, _ -> updateMoonMap() }
        librationLongitudeProperty.addListener { _, _, _ -> updateMoonMap() }
        strokeWidthProperty.addListener { _, _, _ -> updateMoonMap() }
    }

    private fun loadImage(file: File) {
        currentBufferedImage = ImageIO.read(file)

        imageWidthProperty.set(currentBufferedImage.width)
        imageHeightProperty.set(currentBufferedImage.height)
        val imageSize = min(currentBufferedImage.width, currentBufferedImage.height)

        centerXProperty.set(currentBufferedImage.width / 2)
        centerYProperty.set(currentBufferedImage.height / 2)
        radiusProperty.set((imageSize / 2 * 0.8).toInt())
        rotationProperty.set(0.0)
        phaseProperty.set(0.0)
        librationLatitudeProperty.set(0.0)
        librationLongitudeProperty.set(0.0)
        strokeWidthProperty.set(max(imageSize * 0.001, 1.0))

        updateMoonMap()
    }

    private fun updateMoonMap() {
        val moonMapOverlay = MoonMapOverlay(
            centerXProperty.get(),
            centerYProperty.get(),
            radiusProperty.get(),
            rotationProperty.get(),
            librationLatitudeProperty.get(),
            librationLongitudeProperty.get(),
            phaseProperty.get() / 100.0,
            strokeWidthProperty.get().toFloat()
        )
        moonMapOverlay.loadMaria()
        moonMapOverlay.loadVisibleCraters()

        val overlayBufferedImage = moonMapOverlay.overlay(currentBufferedImage)

        val overlayImage = WritableImage(overlayBufferedImage.width, overlayBufferedImage.height)
        val pixelWriter = overlayImage.pixelWriter
        for (y in 0 until overlayBufferedImage.height) {
            for (x in 0 until overlayBufferedImage.width) {
                pixelWriter.setArgb(x, y, -0x1000000 or overlayBufferedImage.getRGB(x, y))
            }
        }
        currentImageView.image = overlayImage
    }

    companion object {
        private const val IMAGE_WIDTH = 600
        private const val IMAGE_HEIGHT = 600

        @JvmStatic
        fun main(args: Array<String>) {
            launch(MoonMapApplication::class.java)
        }
    }
}