package view

import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.GameComponentView
import tools.aqua.bgw.components.uicomponents.Orientation

/**
 * Contains multiple LinearLayouts
 */
class LinearLayoutContainer<T : GameComponentView>(
    var posX: Int = 0,
    var posY: Int = 0,
    val spacing: Int = 0,
    var orientation: Orientation = Orientation.HORIZONTAL,
    var backwards: Boolean = false,
    linearLayoutsInput: List<LinearLayout<T>> = listOf(),
    createLinearLayouts: Int = 0,
    createLinearLayoutsWidth: Int = 0,
    createLinearLayoutsHeight: Int = 0
) : MutableList<LinearLayout<T>> {

    private val linearLayouts: MutableList<LinearLayout<T>>

    init {

        linearLayouts = linearLayoutsInput.toMutableList()
        if (createLinearLayouts > 0) {
            require(createLinearLayoutsWidth != 0 && createLinearLayoutsHeight != 0)
            { "You need to specify the size of the linear layouts" }
            repeat(createLinearLayouts) {
                add(
                    LinearLayout(
                        width = createLinearLayoutsWidth,
                        height = createLinearLayoutsHeight,
                        orientation = orientation
                    )
                )
            }
        }
        val noSpacingOrEmpty = spacing != 0 || linearLayoutsInput.isNotEmpty()
        if (posX != 0 || posY != 0 || noSpacingOrEmpty) {
            updatePosition()
        }
    }


    /**
     * changes the Orientation to vertical or horizontal, and backwards or forward
     */
    fun changeOrientationTo(orientation: Orientation, backwards: Boolean = false) {
        this.orientation = orientation
        this.backwards = backwards
        updateOrientation()
        updatePosition()

    }

    /**
     * moves around the X, Y axis
     * @param posX the desired position on the X axis
     * @param posY the desired position on the X axis
     */
    fun move(posX: Int, posY: Int) {
        this.posX = posX
        this.posY = posY
        updatePosition()

    }

    private fun updatePosition() {
        checkIfEntriesAreValid()
        forEachIndexed { index, element ->
            if(orientation == Orientation.HORIZONTAL) {
                if(backwards) {
                    element.posX = this.posX.toDouble() - index * this.spacing - index * first().width
                    element.posY = this.posY.toDouble()
                } else {
                    element.posX = this.posX.toDouble() + index * this.spacing + index * first().width
                    element.posY = this.posY.toDouble()
                }
            } else {
                if(backwards) {
                    element.posX = this.posX.toDouble()
                    element.posY = this.posY.toDouble() + index * this.spacing + index * first().height
                } else {
                    element.posX = this.posX.toDouble()
                    element.posY = this.posY.toDouble() - index * this.spacing - index * first().height
                }

            }
        }

    }

    private fun updateOrientation() {
        linearLayouts.forEach {
            it.orientation = orientation
        }
    }

  //private fun getListOfAllEntries(): List<T> {
  //     val allEntries = mutableListOf<T>()
  //     forEach { layouts -> layouts.forEach { allEntries.add(it) } }
  //     return allEntries
  // }

    private fun checkIfEntriesAreValid() {
        if (isNotEmpty()) require(all {
            first().width == it.width && first().height == it.height
        }) { "Linear Layouts have different properties" }
    }

    //Ab hier nur Listenspezifische Operationen (mit kleinen anpassungen, da die position geupdatet wird)
    override fun iterator(): MutableIterator<LinearLayout<T>> {
        return linearLayouts.iterator()
    }

    override fun contains(element: LinearLayout<T>): Boolean {
        return linearLayouts.contains(element)
    }

    override fun containsAll(elements: Collection<LinearLayout<T>>): Boolean {
        return linearLayouts.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return linearLayouts.isEmpty()
    }

    override fun get(index: Int): LinearLayout<T> {
        return linearLayouts[index]
    }

    override fun indexOf(element: LinearLayout<T>): Int {
        return linearLayouts.indexOf(element)
    }

    override fun lastIndexOf(element: LinearLayout<T>): Int {
        return linearLayouts.lastIndexOf(element)
    }

    override fun listIterator(): MutableListIterator<LinearLayout<T>> {
        return linearLayouts.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<LinearLayout<T>> {
        return linearLayouts.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<LinearLayout<T>> {
        return linearLayouts.subList(fromIndex, toIndex)
    }

    override val size: Int
        get() = linearLayouts.size

    /**
     * Adds the specified element to the end of this list.
     *
     * @return `true` because the list is always modified as the result of this operation.
     */
    override fun add(element: LinearLayout<T>): Boolean {
        val completed = linearLayouts.add(element)
        updatePosition()
        return completed
    }

    /**
     * Inserts an element into the list at the specified [index].
     */
    override fun add(index: Int, element: LinearLayout<T>) {
        val completed = linearLayouts.add(index, element)
        updatePosition()
        return completed
    }

    /**
     * Inserts all of the elements of the specified collection [elements] into this list at the specified [index].
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    override fun addAll(index: Int, elements: Collection<LinearLayout<T>>): Boolean {
        val completed = linearLayouts.addAll(index, elements)
        updatePosition()
        return completed
    }

    /**
     * Adds all of the elements of the specified collection to the end of this list.
     *
     * The elements are appended in the order they appear in the [elements] collection.
     *
     * @return `true` if the list was changed as the result of the operation.
     */
    override fun addAll(elements: Collection<LinearLayout<T>>): Boolean {
        val completed = linearLayouts.addAll(elements)
        updatePosition()
        return completed
    }

    override fun clear() {
        linearLayouts.clear()
        updatePosition()
    }

    override fun remove(element: LinearLayout<T>): Boolean {
        val completed = linearLayouts.remove(element)
        updatePosition()
        return completed
    }

    override fun removeAll(elements: Collection<LinearLayout<T>>): Boolean {
        return linearLayouts.removeAll(elements)
    }

    /**
     * Removes an element at the specified [index] from the list.
     *
     * @return the element that has been removed.
     */
    override fun removeAt(index: Int): LinearLayout<T> {
        return linearLayouts.removeAt(index)
    }

    override fun retainAll(elements: Collection<LinearLayout<T>>): Boolean {
        return linearLayouts.retainAll(elements)
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @return the element previously at the specified position.
     */
    override fun set(index: Int, element: LinearLayout<T>): LinearLayout<T> {
        return linearLayouts.set(index, element)
    }
}