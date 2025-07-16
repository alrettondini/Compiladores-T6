package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Main {
    public static void main(String[] args) {
        try {
            CharStream cs = CharStreams.fromFileName(args[0]);
            FactorioPlannerLexer lexer = new FactorioPlannerLexer(cs);
            
            MyErrorListener errorListener = new MyErrorListener();

            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FactorioPlannerParser parser = new FactorioPlannerParser(tokens);

            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            FactorioPlannerParser.ProgramaContext arvore = parser.programa();

            if (!errorListener.houveErro) {
                AnalisadorSemantico analisador = new AnalisadorSemantico();
                analisador.visit(arvore);
                analisador.realizarVerificacoesPosAnalise(arvore);

                if (!analisador.temErros()) {
                    String itemFinal = arvore.producao_final().item_simples().IDENT().getText();
                    CalculadoraProducao calc = new CalculadoraProducao(analisador.bancoDeReceitas);
                    calc.calcular(itemFinal);
                } else {
                    System.err.println("Erros semânticos encontrados:");
                    analisador.imprimirErros();
                }
            }

        } catch (Exception e) {
        }
    }
}