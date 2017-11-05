import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.NoSuchElementException

import org.junit.Assert.*

/**
 * If these prove useful over time, they will be moved to the TestUtils project instead.
 */
object TestUtilities {

    /**
     * Tests if something is serializable.
     * Note: Lambdas and anonymous classes are NOT serializable in Java 8.  Only enums and classes that implement
     * Serializable are.  This might be the best reason to use enums for singletons.
     * @param obj the item to serialize and deserialize
     * @return whatever's left after serializing and deserializing the original item.  Sometimes things throw exceptions.
     */

    fun <T> serializeDeserialize(obj: T): T {

        // This method was started by sblommers.  Thanks for your help!
        // Mistakes are Glen's.
        // https://github.com/GlenKPeterson/Paguro/issues/10#issuecomment-242332099

        // Write
        val baos = ByteArrayOutputStream()

        try {
            val oos = ObjectOutputStream(baos)
            oos.writeObject(obj)

            val data = baos.toByteArray()

            // Read
            val baip = ByteArrayInputStream(data)
            val ois = ObjectInputStream(baip)
            return ois.readObject() as T
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }

    }

    /**
     * Call with two Iterators to test that they are equal
     * @param control the reference iterator
     * @param test the iterator under test.
     */
    fun <T> compareIterators(control: Iterator<T>, test: Iterator<T>) {
        var i = 0
        while (control.hasNext()) {
            assertTrue("Control[$i] had next, but test didn't", test.hasNext())
            val cNext = control.next()
            val tNext = test.next()
            assertEquals("Control[$i]=$cNext didn't equal test[$i]=$tNext",
                         cNext, tNext)
            i++
        }
        assertFalse("Test[$i] had extra elements", test.hasNext())
    }

    // TODO: Merge this with compareIterators above (I think the above is better).
    /**
     * Call with two Iterators to test that they are equal
     * @param a the reference iterator
     * @param b the iterator under test.
     */
    fun <A, B> iteratorTest(a: Iterator<A>, b: Iterator<B>) {
        while (a.hasNext()) {
            assertTrue("When a has a next, b should too", b.hasNext())
            assertEquals("a.next should equal b.next", a.next(), b.next())
        }
        assertFalse("When a has no next, b shouldn't either", b.hasNext())
    }

    fun <T : Throwable> assertEx(f: () -> Unit, beforeText: String,
                                 exType: Class<T>) {
        try {
            f()
        } catch (t: Throwable) {
            if (!exType.isInstance(t)) {
                fail("Expected " + beforeText + " to throw " + exType.simpleName +
                     " but threw " + t)
            }
            return
        }

        fail("Expected " + beforeText + " to throw " + exType.simpleName)
    }

    private fun <A, B> assertLiEq(a: ListIterator<A>, b: ListIterator<B>, afterText: String) {
        assertEquals("a.hasNext should equal b.hasNext " + afterText,
                     a.hasNext(), b.hasNext())
        assertEquals("a.hasPrevious should equal b.hasPrevious " + afterText,
                     a.hasPrevious(), b.hasPrevious())
        assertEquals("a.nextIndex should equal b.nextIndex " + afterText,
                     a.nextIndex().toLong(), b.nextIndex().toLong())
        assertEquals("a.previousIndex should equal b.previousIndex " + afterText,
                     a.previousIndex().toLong(), b.previousIndex().toLong())
    }

    /**
     * Call with two ListIterators to test that they are equal
     * @param aList the reference iterator
     * @param bList the iterator under test.
     */
    fun <A, B> listIteratorTest(aList: List<A>, bList: List<B>) {

        assertEx({ aList.listIterator(-1) }, "aList.listIterator(-1)",
                 IndexOutOfBoundsException::class.java)
        assertEx({ bList.listIterator(-1) }, "bList.listIterator(-1)",
                 IndexOutOfBoundsException::class.java)

        assertEx({ aList.listIterator(aList.size + 1) }, "aList.listIterator(aList.size() + 1)",
                 IndexOutOfBoundsException::class.java)
        assertEx({ bList.listIterator(aList.size + 1) }, "bList.listIterator(aList.size() + 1)",
                 IndexOutOfBoundsException::class.java)

        for (i in 0..aList.size) {
            val a = aList.listIterator(i)
            val b = bList.listIterator(i)

            assertLiEq(a, b, "at start (i = $i)")

            while (a.hasNext()) {
                assertTrue("When a has a next, b should too (started at $i)", b.hasNext())

                assertEquals("a.next should equal b.next (started at $i)",
                             a.next(), b.next())

                assertLiEq(a, b, "after calling next()")
            }
            assertFalse("When a has no next, b shouldn't either (started at $i)",
                        b.hasNext())

            assertLiEq(a, b, "after the last item")

            assertEx({ a.next() }, "a.next() after the last item", NoSuchElementException::class.java)
            assertEx({ b.next() }, "b.next() after the last item", NoSuchElementException::class.java)

            while (a.hasPrevious()) {
                assertTrue("When a hasPrevious, b should too. (started at $i)",
                           b.hasPrevious())

                assertEquals("a.previous should equal b.previous (started at $i)",
                             a.previous(), b.previous())

                assertLiEq(a, b, "after calling previous()")
            }
            assertFalse("When a has no previous, b shouldn't either (started at $i)",
                        b.hasPrevious())

            assertLiEq(a, b, "before first item")

            assertEx({ a.previous() }, "a.previous() before first item", NoSuchElementException::class.java)
            assertEx({ b.previous() }, "b.previous() before first item", NoSuchElementException::class.java)
        }

        // Check that indexing works when we start with the previous, then switch to the next()
        for (i in 0..aList.size) {
            val a = aList.listIterator(i)
            val b = bList.listIterator(i)

            assertLiEq(a, b, "at start (i = $i)")

            while (a.hasPrevious()) {
                assertTrue("When a hasPrevious, b should too. (started at $i)",
                           b.hasPrevious())

                assertEquals("a.previous should equal b.previous (started at $i)",
                             a.previous(), b.previous())

                assertLiEq(a, b, "after calling previous()")
            }
            assertFalse("When a has no previous, b shouldn't either (started at $i)",
                        b.hasPrevious())

            assertLiEq(a, b, "before first item")

            assertEx({ a.previous() }, "a.previous()", NoSuchElementException::class.java)
            assertEx({ b.previous() }, "b.previous()", NoSuchElementException::class.java)

            while (a.hasNext()) {
                assertTrue("When a has a next, b should too (started at $i)", b.hasNext())

                assertEquals("a.next should equal b.next (started at $i)",
                             a.next(), b.next())

                assertLiEq(a, b, "after calling next()")
            }
            assertFalse("When a has no next, b shouldn't either (started at $i)",
                        b.hasNext())

            assertLiEq(a, b, "after the last item")

            assertEx({ a.next() }, "a.next()", NoSuchElementException::class.java)
            assertEx({ b.next() }, "b.next()", NoSuchElementException::class.java)
        }
    }
}