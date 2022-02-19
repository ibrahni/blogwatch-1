package com.baeldung.site;

import java.util.ArrayList;
import java.util.List;


public class InvalidTitles {
    private final List<String> invalidTitles = new ArrayList<>();
    private final List<String> titlesWithInvalidDots = new ArrayList<>();

    public void addInvalidTitle(String title){
        invalidTitles.add(title);
    }

    public void addTitleWithInvalidDots(String title){
        titlesWithInvalidDots.add(title);
    }

    public List<String> invalidTitles(){
        return invalidTitles;
    }

    public List<String> titlesWithInvalidDots(){
        return titlesWithInvalidDots;
    }
}
