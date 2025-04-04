package ch.epfl.rechor.timetable;

/**
 * The interface Indexed, of the subpackage timetable, is intended to be extended by
 * all interfaces representing indexed data. By "indexed data" we mean all schedule data
 * that, conceptually at least, is stored in an array and identified by an index ranging from
 * 0 (inclusive) to the size of the array (exclusive).
 *
 * @author Guanting Wen (392412)
 * @author Ben Fall (373176)
 */
public interface Indexed {
    int size(); // which returns the size - i.e. number of elements - of the data
}
