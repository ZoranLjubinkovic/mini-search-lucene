package com.gd.lucene.service.utils;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class EdgeNGramAnalyzer extends Analyzer {

    int minNGram = 1;
    int maxNGram = 4;

    public EdgeNGramAnalyzer(int minNGram, int maxNGram) {
        this.minNGram = minNGram;
        this.maxNGram = maxNGram;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream filter = new LowerCaseFilter(tokenizer);

//        filter = new StopFilter(filter, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);

        ShingleFilter shingleFilter = new ShingleFilter(filter, maxNGram);
        boolean outputUnigrams = minNGram == 1;
        if (outputUnigrams) {
            shingleFilter.setMinShingleSize(2);
        } else {
            shingleFilter.setMinShingleSize(minNGram);
        }
        shingleFilter.setOutputUnigrams(outputUnigrams); // include unigrams if minNGram == 1

        return new TokenStreamComponents(tokenizer, shingleFilter);
    }


}
