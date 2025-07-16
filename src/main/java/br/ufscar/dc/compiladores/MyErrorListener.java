package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

// Classe para capturar e formatar erros de sintaxe do ANTLR
public class MyErrorListener extends BaseErrorListener {
    // Flag para garantir que a exceção seja lançada apenas uma vez
    public boolean houveErro = false;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        // Verifica se é a primeira vez que um erro ocorre para não poluir a saída
        if (!houveErro) {
            Token token = (Token) offendingSymbol;

            String tokenText = (token != null) ? token.getText() : "";

            // Identifica um erro léxico (um símbolo que não pertence à linguagem).
            if (msg.contains("token recognition error")) {
                 // Extrai o caractere inválido da mensagem de erro para uma saída mais clara
                String offendingChar = msg.split("'")[1];
                System.err.println("Linha " + line + ": " + offendingChar + " - simbolo não identificado");

            } else {
                // Trata um erro sintático (a estrutura do código está incorreta)
                System.err.println("Linha " + line + ": erro sintatico proximo a '" + tokenText + "'");
            }
            
            // Lança uma exceção para interromper o processo de compilação imediatamente
            throw new RuntimeException("Erro sintático ou léxico que interrompe a compilação");
        }
        houveErro = true; // Marca que um erro já foi reportado
    }
}