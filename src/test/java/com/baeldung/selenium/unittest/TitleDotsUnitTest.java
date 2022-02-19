package com.baeldung.selenium.unittest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.baeldung.common.Utils;
import com.baeldung.site.strategy.ITitleAnalyzerStrategy;

public class TitleDotsUnitTest {

    @Test
    void givenATitleHavingDotsAtTheEndAndBetweenNumber_WhenTitleAnalysed_thenItIsValid() {
        String title = "4.1. The Let() Method";
        List<String> tokens = Utils.titleTokenizer(title);

        assertTrue(ITitleAnalyzerStrategy.dotsInTitleAnalyzer().isTitleValid(title, tokens, Collections.emptyList(), Collections.emptyList()));

    }

    @Test
    void givenATitleHavingDotsAtTheEndAndBetweenNumberWithMoreThanOneDigit_WhenTitleAnalysed_thenItIsValid() {
        String title = "14.10. The Let() Method";
        List<String> tokens = Utils.titleTokenizer(title);

        assertTrue(ITitleAnalyzerStrategy.dotsInTitleAnalyzer().isTitleValid(title, tokens, Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    void givenATitleHavingDotsAtTheEndAndStartsWithQ_WhenTitleAnalysed_thenItIsValid() {
        String title = "Q14.10. The Let() Method";
        List<String> tokens = Utils.titleTokenizer(title);

        assertTrue(ITitleAnalyzerStrategy.dotsInTitleAnalyzer().isTitleValid(title, tokens, Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    void givenATitleHavingMissingDotsAtTheEndAnd_WhenTitleAnalysed_thenItIsNotValid() {
        String title = "4.1 The Let() Method";
        List<String> tokens = Utils.titleTokenizer(title);

        assertFalse(ITitleAnalyzerStrategy.dotsInTitleAnalyzer().isTitleValid(title, tokens, Collections.emptyList(), Collections.emptyList()));
    }
}
