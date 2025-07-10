package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

public class MyErrorListener extends BaseErrorListener {
    public boolean houveErro = false;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if (!houveErro) {
            Token token = (Token) offendingSymbol;
            System.err.println("Linha " + line + ": erro sintatico proximo a '" + token.getText() + "'");
        }
        houveErro = true;
    }
}