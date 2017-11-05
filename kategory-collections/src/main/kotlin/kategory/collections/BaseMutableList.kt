package kategory.collections

interface BaseMutableList<E>: BaseList<E>, MutableList<E> {

    override fun add(element: E): Boolean {
        append(element)
        return true
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        if (elements.isEmpty()) {
            return false
        }
        for(elem in elements) {
            add(index, elem)
        }
        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        if (elements.isEmpty()) {
            return false
        }
        concat(elements)
        return true
    }

    override fun listIterator(): MutableListIterator<E> = super.listIterator() as MutableListIterator<E>

    override fun listIterator(index: Int): MutableListIterator<E> = super.listIterator(index) as MutableListIterator<E>

    override fun set(index: Int, element: E): E {
        val ret = get(index)
        replace(index, element)
        return ret
    }

    override fun subList(fromIndex: Int, toIndex: Int): BaseMutableList<E> {
        throw UnsupportedOperationException()
    }
}