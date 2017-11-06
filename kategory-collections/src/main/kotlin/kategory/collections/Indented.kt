package kategory.collections

/**
 * Utilities for creating indented (pretty-print) strings cheaply.
 */
interface Indented {
    /**
     * Returns a string where line breaks extend the given amount of indentation.
     * @param indent the amount of indent to start at.  Pretty-printed subsequent lines may have
     * additional indent.
     * @return a string with the given starting offset (in spaces) for every line.
     */
    fun indentedStr(indent: Int): String

    companion object {

        // ========================================== STATIC ==========================================
        // Note, this is part of something completely different, but was especially useful for
        // debugging the above.  So much so, that I want to keep it when I'm done, but it needs
        // to move somewhere else before releasing.
        private val SPACES = arrayOf(
                "",
                " ",
                "  ",
                "   ",
                "    ",
                "     ",
                "      ",
                "       ",
                "        ",
                "         ",
                "          ",
                "           ",
                "            ",
                "             ",
                "              ",
                "               ",
                "                ",
                "                 ",
                "                  ",
                "                   ",
                "                    ",
                "                     ",
                "                      ",
                "                       ",
                "                        ",
                "                         ",
                "                          ",
                "                           ",
                "                            ",
                "                             ",
                "                              ",
                "                               ",
                "                                ",
                "                                 ",
                "                                  ",
                "                                   ",
                "                                    ",
                "                                     ",
                "                                      ",
                "                                       ",
                "                                        ",
                "                                         ",
                "                                          ",
                "                                           ",
                "                                            ",
                "                                             ",
                "                                              ",
                "                                               ",
                "                                                ")

        private val SPACES_LENGTH_MINUS_ONE = SPACES.size - 1

        /**
         * Creates a new StringBuilder with the given number of spaces and returns it.
         * @param length the number of spaces
         * @return a [StringBuilder] with the specified number of initial spaces.
         */
        fun indentSpace(length: Int): StringBuilder {
            var len = length
            val sB = StringBuilder()
            if (len < 1) {
                return sB
            }
            while (len > SPACES_LENGTH_MINUS_ONE) {
                sB.append(SPACES[SPACES_LENGTH_MINUS_ONE])
                len -= SPACES_LENGTH_MINUS_ONE
            }
            return sB.append(SPACES[len])
        }

        /**
         * There is Arrays.toString, but this is intended to produce Cymling code some day.
         */
        fun <T> arrayString(items: Array<T>): String {
            val sB = StringBuilder("A[")
            var isFirst = true
            for (item in items) {
                if (isFirst) {
                    isFirst = false
                } else {
                    sB.append(" ")
                }
                if (item is String) {
                    sB.append("\"").append(item).append("\"")
                } else {
                    sB.append(item)
                }
            }
            return sB.append("]").toString()
        }

        /**
         * There is Arrays.toString, but this is intended to produce Cymling code some day.
         */
        // TODO: We need one of these for each type of primitive for pretty-printing without commas.
        fun arrayString(items: IntArray): String {
            val sB = StringBuilder("i[")
            var isFirst = true
            for (item in items) {
                if (isFirst) {
                    isFirst = false
                } else {
                    sB.append(" ")
                }
                sB.append(item)
            }
            return sB.append("]").toString()
        }
    }
}