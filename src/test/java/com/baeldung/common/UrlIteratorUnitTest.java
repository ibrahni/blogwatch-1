package com.baeldung.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UrlIteratorUnitTest {

    enum SampleTag {
        LIST1, LIST2, LIST3
    }

    @Test
    void whenMergeMultipleIterators_thenUrlIteratorTraversesCorrectly() {

        List<String> list1 = List.of("url1", "url2", "url3");
        List<String> list2 = List.of("url4", "url5", "url6");
        List<String> list3 = List.of("url7", "url8", "url9");

        List<String> urlSource = new ArrayList<>();
        urlSource.addAll(list1);
        urlSource.addAll(list2);
        urlSource.addAll(list3);

        final UrlIterator iterator = new UrlIterator();
        iterator.append(SampleTag.LIST1, new ArrayList<>(list1).iterator());
        iterator.append(SampleTag.LIST2, new ArrayList<>(list2).iterator());
        iterator.append(SampleTag.LIST3, new ArrayList<>(list3).iterator());

        while (iterator.hasNext()) {
            UrlIterator.UrlElement element = iterator.next();
            Assertions.assertTrue(urlSource.remove(element.url()));
            if (list1.contains(element.url())) {
                Assertions.assertEquals(SampleTag.LIST1, SampleTag.valueOf(element.tag()));
            }
            if (list2.contains(element.url())) {
                Assertions.assertEquals(SampleTag.LIST2, SampleTag.valueOf(element.tag()));
            }
            if (list3.contains(element.url())) {
                Assertions.assertEquals(SampleTag.LIST3, SampleTag.valueOf(element.tag()));
            }
        }
        Assertions.assertEquals(0, urlSource.size());
    }

    @Test
    void whenNoOrEmptyIterators_thenUrlIteratorIsEmpty() {

        UrlIterator iterator = new UrlIterator();
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iterator::next);

        iterator.append(SampleTag.LIST1, Collections.emptyIterator());
        iterator.append(SampleTag.LIST2, Collections.emptyIterator());
        iterator.append(SampleTag.LIST3, Collections.emptyListIterator());
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iterator::next);
    }

}
