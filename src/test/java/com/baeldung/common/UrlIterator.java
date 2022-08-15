package com.baeldung.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.annotation.concurrent.ThreadSafe;

/**
 * To iterate {@link com.baeldung.site.SitePage} urls by combining multiple iterator in a thread safe way.
 */
@ThreadSafe
public class UrlIterator implements Iterator<UrlIterator.UrlElement> {

    Map<String, Iterator<String>> iterators = new HashMap<>();

    public synchronized void append(Object tag, Iterator<String> iterator) {
        if (tag == null) {
            throw new IllegalArgumentException("tag must not be null");
        }
        iterators.put(tag.toString(), iterator);
    }

    @Override
    public synchronized boolean hasNext() {
        if (this.iterators.size() == 0)
            return false;
        boolean hasNext = false;
        for (Iterator<String> it : iterators.values()) {
            hasNext |= it.hasNext();
        }
        return hasNext;
    }

    @Override
    public synchronized UrlElement next() {
        if (this.iterators.size() == 0)
            throw new NoSuchElementException();

        final String found = iterators.keySet()
            .stream()
            .filter(tag -> iterators.get(tag)
                .hasNext())
            .findAny()
            .orElseThrow();

        return new UrlElement(found, iterators.get(found).next());
    }

    /**
     * To get the next element in Optional. Thread-safe and exception-safe way to get.
     *
     * @return UrlElement
     */
    public synchronized Optional<UrlElement> getNext() {
        if (!hasNext()) {
            return Optional.empty();
        }
        return Optional.of(next());
    }

    public record UrlElement(String tag, String url) {
    }
}
