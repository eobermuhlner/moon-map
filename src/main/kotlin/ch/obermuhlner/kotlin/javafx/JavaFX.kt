package ch.obermuhlner.kotlin.javafx

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import java.text.DecimalFormat
import java.text.Format
import java.util.function.Function

fun <T: Node> node(node: T, initializer: T.() -> Unit)
        = node.apply(initializer)


fun hbox(initializer: HBox.() -> Unit)
        = HBox().apply(initializer)


fun hbox(spacing: Double, initializer: HBox.() -> Unit)
        = HBox(spacing).apply(initializer)


fun vbox(initializer: VBox.() -> Unit)
        = VBox().apply(initializer)

fun vbox(spacing: Double, initializer: VBox.() -> Unit)
        = VBox(spacing).apply(initializer)


fun borderpane(initializer: BorderPane.() -> Unit)
        = BorderPane().apply(initializer)


fun label(initializer: Label.() -> Unit)
        = Label().apply(initializer)

fun label(text: String)
        = Label(text)

fun label(text: String, initializer: Label.() -> Unit)
        = Label(text).apply(initializer)


fun button(initializer: Button.() -> Unit)
        = Button().apply(initializer)

fun button(text: String, initializer: Button.() -> Unit)
        = Button(text).apply(initializer)


fun spinner(min: Double, max: Double, initialValue: Double, initializer: Spinner<Number>.() -> Unit)
        = Spinner<Number>(min, max, initialValue).apply(initializer)

fun spinner(min: Int, max: Int, initialValue: Int, initializer: Spinner<Number>.() -> Unit)
        = Spinner<Number>(min, max, initialValue).apply(initializer)


fun slider(min: Double, max: Double, initialValue: Double, initializer: Slider.() -> Unit)
        = Slider(min, max, initialValue).apply(initializer)


fun <T> combobox(items: Array<T>, initializer: ComboBox<T>.() -> Unit)
        = combobox(FXCollections.observableArrayList(*items), initializer)

fun <T> combobox(items: List<T>, initializer: ComboBox<T>.() -> Unit)
        = combobox(FXCollections.observableArrayList(items), initializer)

fun <T> combobox(items: ObservableList<T>, initializer: ComboBox<T>.() -> Unit)
        = ComboBox(items).apply(initializer)

fun checkbox(initializer: CheckBox.() -> Unit)
        = CheckBox().apply(initializer)

fun checkbox(booleanProperty: BooleanProperty, initializer: CheckBox.() -> Unit): CheckBox {
    val field = CheckBox().apply(initializer)
    field.selectedProperty().bindBidirectional(booleanProperty)
    return field
}

fun textfield(initializer: TextField.() -> Unit)
        = TextField().apply(initializer)

fun textfield(textProperty: StringProperty, initializer: TextField.() -> Unit): TextField {
    val field = textfield(initializer)
    field.textProperty().bindBidirectional(textProperty)
    return field
}

fun textfield(integerProperty: IntegerProperty, format: Format = INTEGER_FORMAT, initializer: TextField.() -> Unit): TextField {
    val field = textfield(initializer)
    field.textProperty().bindBidirectional(integerProperty, format)
    return field
}

fun textfield(doubleProperty: DoubleProperty, format: Format = DOUBLE_FORMAT, initializer: TextField.() -> Unit): TextField {
    val field = textfield(initializer)
    field.textProperty().bindBidirectional(doubleProperty, format)
    return field
}

fun imageview(image: Image, initializer: ImageView.() -> Unit)
        = ImageView(image).apply(initializer)

fun imageview(initializer: ImageView.() -> Unit)
        = ImageView().apply(initializer)

fun rectangle(width: Double, height: Double, initializer: Rectangle.() -> Unit)
        = Rectangle(width, height).apply(initializer)

fun circle(radius: Double, initializer: Circle.() -> Unit)
        = Circle(radius).apply(initializer)

//fun <S> tableview(initializer: TableView<S>.() -> Unit)
//    = TableViewContext<S>().apply(initializer)

fun <S> tableview(items: ObservableList<S>, initializer: TableViewContext<S>.() -> Unit)
        = TableViewContext(items).apply(initializer)

class TableViewContext<S>(items: ObservableList<S>) : TableView<S>(items) {
    fun <V> column(header: String, initializer: TableColumn<S, V>.() -> Unit): TableColumn<S, V> {
        val tableColumn = TableColumn<S, V>(header).apply(initializer)
        this.columns.add(tableColumn)
        return tableColumn
    }
    fun <V> column(header: String, valueFunction: Function<S, ObservableValue<V>>,  initializer: TableColumn<S, V>.() -> Unit): TableColumn<S, V> {
        val tableColumn = TableColumn<S, V>(header).apply(initializer)
        tableColumn.setCellValueFactory { cellData -> valueFunction.apply(cellData.value) }
        this.columns.add(tableColumn)
        return tableColumn
    }
}

fun menuitem(text: String, initializer: MenuItem.() -> Unit)
        = MenuItem(text).apply(initializer)


fun tabpane(initializer: TabPane.() -> Unit)
        = TabPane().apply(initializer)

fun tab(name: String, initializer: Tab.() -> Unit)
        = Tab(name).apply(initializer)


fun gridpane(initializer: GridPaneContext.() -> Unit): GridPane
        = GridPaneContext().apply(initializer)

class GridPaneContext : GridPane() {
    private var rowIndex = 0

    fun row(initializer: RowContext.() -> Unit): RowContext {
        return RowContext(this, rowIndex++).apply(initializer)
    }
}

class RowContext(private val gridPane: GridPane, private val rowIndex: Int) {
    private var colIndex = 0

    fun <T: Node> cell(creator: () -> T) {
        gridPane.add(creator.invoke(), colIndex++, rowIndex)
    }

    fun <T: Node> cell(colspan: Int, rowspan: Int, creator: () -> T) {
        gridPane.add(creator.invoke(), colIndex++, rowIndex, colspan, rowspan)
    }

}

val INTEGER_FORMAT = DecimalFormat("##0")
val DOUBLE_FORMAT = DecimalFormat("##0.000")
val PERCENT_FORMAT = DecimalFormat("##0.000%")
