package com.baeldung.site.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.common.ConsoleColors;
import com.baeldung.common.GlobalConstants;
import com.baeldung.common.Utils;

public interface ITitleAnalyzerStrategy {
    Logger logger = LoggerFactory.getLogger(ITitleAnalyzerStrategy.class);
    boolean isTitleValid(String title, List<String> tokens, List<String> emphasizedAndItalicTokens, List<String> tokenExceptions);

    static List<ITitleAnalyzerStrategy> titleAnalyzerStrategies = Arrays.asList(new ITitleAnalyzerStrategy[] { articlesConjunctionsShortPrepositionsAnalyserStrategy(), javaMethodNameAnalyserStrategy(), simpleTitleAnalyserStrategy()});
    static String regexForShortPrepositions = "a|an|and|as|at|but|by|en|for|in|nor|of|on|or|per|the|vs.?|via|out";
    static String regexForExceptions = "with|to|from|up|into|v.?|REST|if|using";

    static ITitleAnalyzerStrategy articlesConjunctionsShortPrepositionsAnalyserStrategy() {
        return (title, tokens, emphasizedAndItalicTokens, tokenExceptions) -> {
            String token = null;
            int firstTokenIndexStartingWithACharacter = Utils.getIndexOfFirstTokenStartingWithACharacter(title);
            String expectedToken = null;
            for (int j = 0; j < tokens.size(); j++) {
                token = tokens.get(j);
                if (emphasizedAndItalicTokens.contains(Utils.removeSpecialCharacterAtTheEnd(token))) {
                    continue;
                }
                if(tokenExceptions.contains(token)) {
                    continue;
                }
                if(token.equals(token.toUpperCase())) {
                    continue;
                }
                if (Pattern.compile(regexForShortPrepositions, Pattern.CASE_INSENSITIVE).matcher(token).matches()) {
                    if(isAtStartOrEndOftheTitle(j, firstTokenIndexStartingWithACharacter, tokens.size())) {
                        expectedToken = WordUtils.capitalize(token.toLowerCase());
                    } else {
                        expectedToken = token.toLowerCase();
                    }
                    if (!expectedToken.equals(token)) {
                        logFailure(title, expectedToken, token);
                        return false;
                    }
                }
            }
            return true;
        };
    }    

    static ITitleAnalyzerStrategy javaMethodNameAnalyserStrategy() {
        return (title, tokens, emphasizedAndItalicTokens, tokenExceptions) -> {

            for (String token : tokens) {
                if (token.contains("(") ) {
                    //we aren't handling methods with parameters
                    if (token.toUpperCase().equals(token) || token.charAt(0) == '(' || token.contains(GlobalConstants.SPACE_DELIMITER)) {
                        continue;
                    }
                    if(tokenExceptions.contains(Utils.removeSpecialCharacterAtTheEnd(token))) {
                        continue;
                    }
                    if(token.matches(".*<.*>.*")) {
                        continue;
                    }
                    if (token.contains(".")) {
                        final String expectedToken = Arrays.stream(token.split("\\."))
                            .map(WordUtils::uncapitalize)
                            .collect(Collectors.joining("."));
                        if (!Objects.equals(expectedToken, token) && !Objects.equals(WordUtils.capitalize(expectedToken), token)) {
                            logFailure(title, expectedToken, token);
                            return false;
                        }
                    }
                    else if (token.contains("#")) {
                        String expectedToken = WordUtils.capitalize(Arrays.asList(token.split("#")).stream().map(WordUtils::uncapitalize).collect(Collectors.joining("#")));
                        if (!expectedToken.equals(token)) {
                            logFailure(title, expectedToken, token);
                            return false;
                        }
                    }
                    else if (token.contains("::")) {
                        String expectedToken = WordUtils.capitalize(Arrays.asList(token.split("::")).stream().map(WordUtils::uncapitalize).collect(Collectors.joining("::")));
                        if (!expectedToken.equals(token)) {
                            logFailure(title, expectedToken, token);
                            return false;
                        }
                    }                    
                    else {
                        String expectedToken = WordUtils.uncapitalize(token, '$');
                        if (!expectedToken.equals(token)) {
                            logFailure(title, expectedToken, token);
                            return false;
                        }
                    }
                }
            }
            return true;
        };
    }

    static ITitleAnalyzerStrategy simpleTitleAnalyserStrategy() {
        return (title, tokens, emphasizedAndItalicTokens, tokenExceptions) -> {
            String token = null;
            int firstTokenIndexStartingWithACharacter = Utils.getIndexOfFirstTokenStartingWithACharacter(title);
            for (int j = 0; j < tokens.size(); j++) {
                token = tokens.get(j);

                // ignore regexForExceptions if not first and last word
                if (!isAtStartOrEndOftheTitle(j, firstTokenIndexStartingWithACharacter, tokens.size()) && Pattern.compile(regexForExceptions, Pattern.CASE_INSENSITIVE).matcher(token).matches()) {
                    continue;
                }

                if (Pattern.compile(regexForShortPrepositions, Pattern.CASE_INSENSITIVE).matcher(token).matches() || Utils.isEmpasized(Utils.removeSpecialCharacterAtTheEnd(token), emphasizedAndItalicTokens) || token.contains("(") || token.contains(".") || token.equals(token.toUpperCase())
                        || token.charAt(0) == '@' || (token.contains("-") && token.toLowerCase().equals(token))) {
                    continue;
                }
                
                if(tokenExceptions.contains(Utils.removeSpecialCharacterAtTheEnd(token))) {
                    continue;
                }
                
                String expectedToken = WordUtils.capitalize(token);
                if (!expectedToken.equals(token)) {
                    logFailure(title, expectedToken, token);
                    return false;
                }
            }

            return true;
        };
    }

    static ITitleAnalyzerStrategy dotsInTitleAnalyzer(){
        return (title, tokens, emphasizedAndItalicTokens, tokenExceptions) -> {
            if(tokens.size()>0){
                final String firstToken = tokens.get(0);
                if(!firstToken.matches("^(Q?\\d+\\.)+$")){
                    return false;
                }
            }
            return true;
        };
    }

    static boolean isAtStartOrEndOftheTitle(int j, int firstTokenIndexStartingWithACharacter, int totalTokenIntheTitle) {
        if (j == firstTokenIndexStartingWithACharacter || j == totalTokenIntheTitle - 1) {
            return true;
        }
        return false;
    }
    static void logFailure(String title, String expectedToken, String token) {
        logger.debug(ConsoleColors.magentaColordMessage("expectedToken: {}, but found: {}, title: {}"), expectedToken, token, title );        
    }
}
