package com.gd.lucene.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class NoStemmingNoStopWordsAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream tokenStream = new LowerCaseFilter(tokenizer);
//        tokenStream = new StopFilter(tokenStream, STOP_WORDS);
        return new TokenStreamComponents(tokenizer, tokenStream);
//        return new TokenStreamComponents(tokenizer);
    }

    @Override
    public String toString() {
        return "LightAnalyzer{}";
    }
}
