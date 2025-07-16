package br.ufscar.dc.compiladores;

import java.util.*;
import br.ufscar.dc.compiladores.FactorioPlannerParser.Regra_entidadeContext;

// Classe auxiliar para armazenar os detalhes de cada receita
class Receita {
    String itemProduzido;
    String entidadeProdutora;
    Map<String, Integer> ingredientes = new LinkedHashMap<>();
}

// Analisador Semântico
public class AnalisadorSemantico extends FactorioPlannerBaseVisitor<Void> {
    // Armazena todas as receitas declaradas no arquivo
    public final Map<String, Receita> bancoDeReceitas = new HashMap<>();
    private final List<String> errosSemanticos = new ArrayList<>();

    // Métodos utilitários para controle de erros
    public void addErro(String mensagem) {
        errosSemanticos.add(mensagem);
    }

    public boolean temErros() {
        return !errosSemanticos.isEmpty();
    }

    public void imprimirErros() {
        errosSemanticos.forEach(System.err::println);
    }

    // Método chamado ao visitar cada regra de entidade
    @Override
    public Void visitRegra_entidade(Regra_entidadeContext ctx) {
        String nomeEntidade = ctx.IDENT().getText();
        String itemProduzido = ctx.produz.IDENT().getText();

        // ERRO: Verifica se o item já possui uma receita definida
        if (bancoDeReceitas.containsKey(itemProduzido)) {
            addErro("Erro Semântico: A receita para '" + itemProduzido + "' já foi definida.");
        } else {
            // Se não houver erro, cria e armazena a nova receita
            Receita r = new Receita();
            r.entidadeProdutora = nomeEntidade;
            r.itemProduzido = itemProduzido;

            // Processa a lista de ingredientes (consumo)
            if (ctx.consumo_s != null) { // Caso de consumo simples (um ingrediente)
                String nomeIngrediente = ctx.consumo_s.IDENT().getText();
                r.ingredientes.put(nomeIngrediente, 1);
            } else if (ctx.consumo_l != null) { // Caso de consumo múltiplo
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

    // Realiza checagens globais após a árvore ter sido percorrida por completo
    public void realizarVerificacoesPosAnalise(FactorioPlannerParser.ProgramaContext ctx) {
        // ERRO: Verifica se o item final declarado na produção possui uma receita
        String itemFinal = ctx.producao_final().item_simples().IDENT().getText();
        if (!bancoDeReceitas.containsKey(itemFinal)) {
            addErro("Erro Semântico: O item final a ser produzido '" + itemFinal + "' não possui uma receita definida.");
        }

        // ERRO: Inicia a detecção de ciclos de produção para cada item
        for (String item : bancoDeReceitas.keySet()) {
            List<String> caminho = new ArrayList<>();
            caminho.add(item);
            detectarCicloRecursivo(item, caminho);
        }
    }

    // Função recursiva para encontrar dependências circulares
    private void detectarCicloRecursivo(String item, List<String> caminho) {
        // Condição de parada: item é um recurso base (não tem receita)
        if (!bancoDeReceitas.containsKey(item)) {
            return;
        }

        Receita r = bancoDeReceitas.get(item);
        for (String ingrediente : r.ingredientes.keySet()) {
            // Se o ingrediente já está no caminho, um ciclo foi encontrado
            if (caminho.contains(ingrediente)) {
                addErro("Erro Semântico: Ciclo de produção detectado! " + ingrediente + " -> " + String.join(" -> ", caminho));
                return; // Para evitar múltiplos erros do mesmo ciclo
            }
            // Continua a busca em profundidade
            List<String> novoCaminho = new ArrayList<>(caminho);
            novoCaminho.add(0, ingrediente);
            detectarCicloRecursivo(ingrediente, novoCaminho);
        }
    }
}