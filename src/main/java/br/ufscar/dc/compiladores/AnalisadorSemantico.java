package br.ufscar.dc.compiladores;

import java.util.*;
import br.ufscar.dc.compiladores.FactorioPlannerParser.Regra_entidadeContext;

class Receita {
    String itemProduzido;
    String entidadeProdutora;
    Map<String, Integer> ingredientes = new LinkedHashMap<>();
}

public class AnalisadorSemantico extends FactorioPlannerBaseVisitor<Void> {

    public final Map<String, Receita> bancoDeReceitas = new HashMap<>();
    private final List<String> errosSemanticos = new ArrayList<>();

    public void addErro(String mensagem) {
        errosSemanticos.add(mensagem);
    }

    public boolean temErros() {
        return !errosSemanticos.isEmpty();
    }

    public void imprimirErros() {
        errosSemanticos.forEach(System.err::println);
    }

    @Override
    public Void visitRegra_entidade(Regra_entidadeContext ctx) {
        String nomeEntidade = ctx.IDENT().getText();
        
        String itemProduzido = ctx.produz.IDENT().getText();

        if (bancoDeReceitas.containsKey(itemProduzido)) {
            addErro("Erro Semântico: A receita para '" + itemProduzido + "' já foi definida.");
        } else {
            Receita r = new Receita();
            r.entidadeProdutora = nomeEntidade;
            r.itemProduzido = itemProduzido;

            if (ctx.consumo_s != null) {
                String nomeIngrediente = ctx.consumo_s.IDENT().getText();
                r.ingredientes.put(nomeIngrediente, 1);
            } else if (ctx.consumo_l != null) {
                for (var itemCtx : ctx.consumo_l.item_quantificado()) {
                    String nomeIngrediente = itemCtx.IDENT().getText();
                    int qtde = Integer.parseInt(itemCtx.NUM().getText());
                    r.ingredientes.put(nomeIngrediente, qtde);
                }
            }
            bancoDeReceitas.put(r.itemProduzido, r);
        }
        return null;
    }
    
    public void realizarVerificacoesPosAnalise(FactorioPlannerParser.ProgramaContext ctx) {
        for (Receita r : bancoDeReceitas.values()) {
            for (String ingrediente : r.ingredientes.keySet()) {
                if (!bancoDeReceitas.containsKey(ingrediente)) {
                }
            }
        }

        String itemFinal = ctx.producao_final().item_simples().IDENT().getText();
        if (!bancoDeReceitas.containsKey(itemFinal)) {
            addErro("Erro Semântico: O item final a ser produzido '" + itemFinal + "' não possui uma receita definida.");
        }

        for (String item : bancoDeReceitas.keySet()) {
            List<String> caminho = new ArrayList<>();
            caminho.add(item);
            detectarCicloRecursivo(item, caminho);
        }
    }

    private void detectarCicloRecursivo(String item, List<String> caminho) {
        if (!bancoDeReceitas.containsKey(item)) {
            return;
        }
        Receita r = bancoDeReceitas.get(item);
        for (String ingrediente : r.ingredientes.keySet()) {
            if (caminho.contains(ingrediente)) {
                addErro("Erro Semântico: Ciclo de produção detectado! " + ingrediente + " -> " + String.join(" -> ", caminho));
                return;
            }
            List<String> novoCaminho = new ArrayList<>(caminho);
            novoCaminho.add(0, ingrediente);
            detectarCicloRecursivo(ingrediente, novoCaminho);
        }
    }
}