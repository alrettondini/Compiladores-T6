package br.ufscar.dc.compiladores;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalculadoraProducao {

    private final Map<String, Receita> bancoDeReceitas;
    private final Map<String, Integer> requisitosFinais = new LinkedHashMap<>();

    public CalculadoraProducao(Map<String, Receita> bancoDeReceitas) {
        this.bancoDeReceitas = bancoDeReceitas;
    }

    /**
     * Função auxiliar para obter um valor de ordenação para um item.
     * A ordem é: Montador < Fornalha < Mineradora < Recurso Base.
     * Retorna um número menor para categorias mais "complexas".
     */
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
        resolverDependencias(itemFinal, 1);
        
        System.out.println("--- REQUISITOS TOTAIS ---");

        // --- INÍCIO DA NOVA LÓGICA DE ORDENAÇÃO ---
        // Pega a máquina que produz o item final para exibi-la primeiro.
        String maquinaFinal = bancoDeReceitas.get(itemFinal).entidadeProdutora;
        int qtdeMaquinaFinal = requisitosFinais.remove(maquinaFinal);
        System.out.println(qtdeMaquinaFinal + "x " + maquinaFinal);

        // Ordena o restante dos requisitos.
        List<Map.Entry<String, Integer>> sortedRequisitos = requisitosFinais.entrySet()
            .stream()
            .sorted(Comparator
                    .comparingInt((Map.Entry<String, Integer> entry) -> getEntityTypeValue(entry.getKey()))
                    .thenComparing(Map.Entry::getKey))
            .collect(Collectors.toList());
        // --- FIM DA NOVA LÓGICA DE ORDENAÇÃO ---
        
        for (var entry : sortedRequisitos) {
            System.out.println(entry.getValue() + "x " + entry.getKey());
        }
    }

    /**
     * Função auxiliar para classificar um item por sua "complexidade".
     * Itens de montadores são mais complexos que os de fornalhas, etc.
     * Retorna um número menor para itens mais complexos.
     */
    private int getComplexidade(String item) {
        if (!bancoDeReceitas.containsKey(item)) {
            return 4; // Recurso base (ex: minerio)
        }
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

    /**
     * Coração da lógica. Esta nova versão ordena os ingredientes de uma receita
     * por complexidade antes de processá-los, garantindo uma ordem lógica na saída.
     */
    private void resolverDependencias(String item, int quantidade) {
        if (!bancoDeReceitas.containsKey(item)) {
            requisitosFinais.merge(item, quantidade, Integer::sum);
            return;
        }

        Receita receita = bancoDeReceitas.get(item);
        requisitosFinais.merge(receita.entidadeProdutora, quantidade, Integer::sum);

        List<Map.Entry<String, Integer>> ingredientesOrdenados = receita.ingredientes.entrySet()
            .stream()
            .sorted(Comparator.comparingInt(entry -> getComplexidade(entry.getKey())))
            .collect(Collectors.toList());
        
        for (var ingrediente : ingredientesOrdenados) {
            String nomeIngrediente = ingrediente.getKey();
            int qtdeNecessaria = ingrediente.getValue() * quantidade;
            resolverDependencias(nomeIngrediente, qtdeNecessaria);
        }
    }
}