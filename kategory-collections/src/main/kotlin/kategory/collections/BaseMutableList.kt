package kategory.collections

interface BaseMutableList<E> : BaseList<E>, MutableList<E> {

    override fun add(element: E): Boolean {
        append(element)
        return true
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        if (elements.isEmpty()) {
            return false
        }
        for (elem in elements) {
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

    override fun listIterator(index: Int): MutableListIterator<E> {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException("Expected an index between 0 and " + size +
                                            " but found: " + index)
        }
        return ListIteratorImpl(this, index)
    }

    override fun set(index: Int, element: E): E {
        val ret = get(index)
        replace(index, element)
        return ret
    }

    override fun subList(fromIndex: Int, toIndex: Int): BaseMutableList<E> {
        throw UnsupportedOperationException()
    }

    class ListIteratorImpl<T>(list: List<T>, idx: Int) :
            BaseList.UnmodListIteratorImpl<T>(list, idx), MutableListIterator<T> {
        override fun add(element: T) {
            throw UnsupportedOperationException()
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }

        override fun set(element: T) {
            throw UnsupportedOperationException()
        }
    }

}