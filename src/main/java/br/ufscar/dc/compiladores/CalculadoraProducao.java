package br.ufscar.dc.compiladores;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Responsável por calcular todos os requisitos para produzir um item final
public class CalculadoraProducao {

    private final Map<String, Receita> bancoDeReceitas;
    // Armazena o resultado final: o nome do item/máquina e a quantidade total necessária
    private final Map<String, Integer> requisitosFinais = new LinkedHashMap<>();

    public CalculadoraProducao(Map<String, Receita> bancoDeReceitas) {
        this.bancoDeReceitas = bancoDeReceitas;
    }

    // Atribui um valor numérico ao tipo de entidade para fins de ordenação na saída
    private int getEntityTypeValue(String item) {
        if (item.startsWith("montador")) {
            return 1;
        }
        if (item.startsWith("fornalha")) {
            return 2;
        }
        if (item.startsWith("mineradora")) {
            return 3;
        }
        return 4; // Recurso base (ex: minerio) ou item intermediário
    }

    public void calcular(String itemFinal) {
        System.out.println("Calculando requisitos para: " + itemFinal + "\n");
        resolverDependencias(itemFinal, 1); // Inicia a recursão
        
        System.out.println("--- REQUISITOS TOTAIS ---");

        // Lógica para ordenar a lista de requisitos antes de imprimir
        String maquinaFinal = bancoDeReceitas.get(itemFinal).entidadeProdutora;
        int qtdeMaquinaFinal = requisitosFinais.remove(maquinaFinal);
        System.out.println(qtdeMaquinaFinal + "x " + maquinaFinal); // Imprime a máquina principal primeiro

        // Ordena o resto por tipo de entidade e depois alfabeticamente
        List<Map.Entry<String, Integer>> sortedRequisitos = requisitosFinais.entrySet()
            .stream()
            .sorted(Comparator
                    .comparingInt((Map.Entry<String, Integer> entry) -> getEntityTypeValue(entry.getKey()))
                    .thenComparing(Map.Entry::getKey))
            .collect(Collectors.toList());
        
        // Imprime a lista final e ordenada
        for (var entry : sortedRequisitos) {
            System.out.println(entry.getValue() + "x " + entry.getKey());
        }
    }

    // Função para ordenar os ingredientes por "complexidade" antes da recursão
    private int getComplexidade(String item) {
        if (!bancoDeReceitas.containsKey(item)) return 4; // Recurso base
        
        String entidade = bancoDeReceitas.get(item).entidadeProdutora;
        if (entidade.startsWith("montador")) {
            return 1; // Item de montagem
        }
        if (entidade.startsWith("fornalha")) {
            return 2; // Item de fundição
        }
        if (entidade.startsWith("mineradora")) {
            return 3; // Item extraído
        }
        return 5; // Padrão
    }

    // Função recursiva que resolve as dependências
    private void resolverDependencias(String item, int quantidade) {
        // Condição de parada: o item é um recurso base (não tem receita)
        if (!bancoDeReceitas.containsKey(item)) {
            requisitosFinais.merge(item, quantidade, Integer::sum); // Adiciona o recurso à lista e retorna
            return;
        }

        // Adiciona a máquina que produz o item atual à lista de requisitos
        Receita receita = bancoDeReceitas.get(item);
        requisitosFinais.merge(receita.entidadeProdutora, quantidade, Integer::sum);

        // Ordena os ingredientes para processar os mais complexos primeiro
        List<Map.Entry<String, Integer>> ingredientesOrdenados = receita.ingredientes.entrySet()
            .stream()
            .sorted(Comparator.comparingInt(entry -> getComplexidade(entry.getKey())))
            .collect(Collectors.toList());
        
        // Chama a recursão para cada ingrediente da receita
        for (var ingrediente : ingredientesOrdenados) {
            String nomeIngrediente = ingrediente.getKey();
            int qtdeNecessaria = ingrediente.getValue() * quantidade;
            resolverDependencias(nomeIngrediente, qtdeNecessaria);
        }
    }
}