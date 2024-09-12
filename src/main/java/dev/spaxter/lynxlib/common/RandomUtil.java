package dev.spaxter.lynxlib.common;

import java.util.*;

/**
 * Utility methods for random-related tasks.
 */
public class RandomUtil {

    private static final Random random = new Random();

    /**
     * Choose {@code count} number of elements randomly from a list with no repeats.
     *
     * @param source The list to pick from
     * @param count  The number of elements to pick
     * @param <T>    Type for the list passed
     * @return List of randomly picked elements.
     */
    public static <T> List<T> chooseRandom(final List<T> source, final int count) {
        int size = source.size();
        if (count > size) {
            throw new IndexOutOfBoundsException("'count' exceeded size of passed list");
        }

        Set<T> chosenElements = new HashSet<>();

        while (chosenElements.size() < count) {
            T element = source.get(random.nextInt(size));
            chosenElements.add(element);
        }

        return new ArrayList<>(chosenElements);
    }
}
