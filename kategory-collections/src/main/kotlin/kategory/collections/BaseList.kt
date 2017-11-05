package kategory.collections

import kategory.Option
import java.util.NoSuchElementException

/**
Adds copy-on-write, "fluent interface" methods to {@link kotlin.collections.CollectionsKt.List}.
 */
interface BaseList<E> : List<E> {
    /**
     * Adds one item to the end of the ImList.
     *
     * @param item the value to insert
     * @return a new ImList with the additional item at the end.
     */
    // TODO: Rename to "plus"
    fun append(item: E): BaseList<E>

    /**
     * Efficiently adds items to the end of this ImList.
     *
     * @param es the values to insert
     * @return a new ImList with the additional items at the end.
     */
    fun concat(es: Iterable<E>): BaseList<E>

    // I don't know if this is a good idea or not and I don't want to have to support it if not.
//    /**
//     * Returns the item at this index, but takes any Number as an argument.
//     * @param n the zero-based index to get from the vector.
//     * @return the value at that index.
//     */
//    default E get(Number n) { return get(n.intValue()); }

    override fun contains(element: E): Boolean {
        for (item in this) {
            if (item == element) {
                return true
            }
        }
        return false
    }

    // Faster to create a HashSet and call containsAll on that because it's
    // O(this size PLUS that size), whereas looping through both would be
    // O(this size TIMES that size).
    override fun containsAll(elements: Collection<E>): Boolean = when {
        elements.isEmpty() -> true
        isEmpty() -> false
        // (ts instanceof Set) ? ((Set) ts).containsAll(c) :
        // (ts instanceof Map) ? ((Map) ts).entrySet().containsAll(c) :
        else -> HashSet(this).containsAll(elements)
    }

    override fun indexOf(element: E): Int {
        for (i in 0 until size) {
            if (get(i) == element) {
                return i
            }
        }
        return -1
    }

    override fun isEmpty(): Boolean = size < 1

    override fun lastIndexOf(element: E): Int {
        for (i in size - 1 downTo 0) {
            if (get(i) == element) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns the item at this index.
     * @param i the zero-based index to get from the vector.
     * @param notFound the value to return if the index is out of bounds.
     * @return the value at that index, or the notFound value.
     */
    operator fun get(i: Int, notFound: E): E? = if (i in 0 until size) get(i) else notFound

    /** {@inheritDoc}  */
    fun head(): Option<E> = if (size > 0) Option.Some(get(0)) else Option.None

    /**
     * Replace the item at the given index.  Note: i.replace(i.size(), o) used to be equivalent to
     * i.concat(o), but it probably won't be for the RRB tree implementation, so this will change too.
     *
     * @param index the index where the value should be stored.
     * @param item the value to store
     * @return a new ImList with the replaced item
     */
    // TODO: Don't make i.replace(i.size(), o) equivalent to i.concat(o)
    fun replace(index: Int, item: E): BaseList<E>

//    /** Returns a reversed copy of this list.  */
//    fun reverse(): BaseList<E>

    /** {@inheritDoc}  */
    override fun listIterator(): ListIterator<E> = listIterator(0)

    /** {@inheritDoc}  Subclasses should override this when they can do so more efficiently.  */
    override fun listIterator(index: Int): ListIterator<E> {
        if (index < 0 || index > size) {
            throw IndexOutOfBoundsException("Expected an index between 0 and " + size +
                                            " but found: " + index)
        }
        return UnmodListIteratorImpl(this, index)
    }

    class UnmodListIteratorImpl<T>(private val list: List<T>, private var idx: Int) : ListIterator<T> {
        private val sz = list.size

        override operator fun hasNext(): Boolean = idx < sz

        override operator fun next(): T {
            // I think this temporary variable i gets compiled to a register access
            // Load memory value from idx to register.  This is the index we will use against
            // our internal data.
            val i = idx
            // Throw based on value in register
            if (i >= sz) {
                throw NoSuchElementException()
            }
            // Store incremented register value back to memory.  Note that this is the
            // next index value we will access.
            idx = i + 1
            // call get() using the old value of idx (before our increment).
            // i should still be in the register, not in memory.
            return list[i]
        }

        override fun hasPrevious(): Boolean = idx > 0

        override fun previous(): T {
            // I think this temporary variable i gets compiled to a register access
            // retrieve idx, subtract 1, leaving result in register.  The JVM only has one
            // register.
            val i = idx - 1
            // throw if item in register is < 0
            if (i < 0) {
                throw NoSuchElementException()
            }
            // Write register to memory location
            idx = i
            // retrieve item at the index in the register.
            return list[i]
        }

        override fun nextIndex(): Int = idx

        override fun previousIndex(): Int = idx - 1

    }

    override fun subList(fromIndex: Int, toIndex: Int): BaseList<E> {
        throw UnsupportedOperationException()
    }
}