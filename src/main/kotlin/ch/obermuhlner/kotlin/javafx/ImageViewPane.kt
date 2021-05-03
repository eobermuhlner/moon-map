package ch.obermuhlner.kotlin.javafx

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.image.ImageView
import javafx.scene.layout.Region

class ImageViewPane @JvmOverloads constructor(imageView: ImageView = ImageView()) :
    Region() {
    private val imageViewProperty: ObjectProperty<ImageView> = SimpleObjectProperty()
    fun imageViewProperty(): ObjectProperty<ImageView> {
        return imageViewProperty
    }

    var imageView: ImageView
        get() = imageViewProperty.get()
        set(imageView) {
            imageViewProperty.set(imageView)
        }

    override fun layoutChildren() {
        val imageView = imageViewProperty.get()
        if (imageView != null) {
            imageView.fitWidth = width
            imageView.fitHeight = height
            layoutInArea(imageView, 0.0, 0.0, width, height, 0.0, HPos.CENTER, VPos.CENTER)
        }
        super.layoutChildren()
    }

    init {
        imageViewProperty.addListener { arg0, oldIV, newIV ->
            if (oldIV != null) {
                children.remove(oldIV)
            }
            if (newIV != null) {
                children.add(newIV)
            }
        }
        imageViewProperty.set(imageView)
    }
}