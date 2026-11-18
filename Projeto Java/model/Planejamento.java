package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// O planejamento da atividade em grupo. Concentra o cadastro, o cálculo e as validações.
//
// RELAÇÕES (justificativa):
//  - Planejamento 1 --- * Participante : composição. Os participantes só existem dentro de um
//    planejamento; por isso só o Planejamento os cria, altera e exclui.
//  - Planejamento 1 --- * Despesa      : composição, pelo mesmo motivo.
//  - Despesa      * --- * Participante : associação. Uma despesa envolve vários participantes e
//    um participante pode estar em várias despesas. A despesa guarda só a referência.
//
// COLEÇÕES GENÉRICAS (justificativa):
//  - Map<Integer, Participante> com TreeMap: a chave é o código, então o código único é garantido
//    pela própria estrutura, e a iteração vem sempre em ordem crescente de código. Essa ordem é
//    a mesma exigida para distribuir os centavos no rateio e para mostrar a tabela.
//  - List<Despesa> com ArrayList: mantém a ordem em que as despesas foram incluídas e é
//    suficiente para o tamanho do problema. O tipo Despesa é abstrato: a lista guarda
//    DespesaFixa e DespesaPorPessoa juntas (polimorfismo).
public class Planejamento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nome;
    private final Map<Integer, Participante> participantes = new TreeMap<>();
    private final List<Despesa> despesas = new ArrayList<>();
    private int proximoIdDespesa = 1;

    public Planejamento(String nome) throws RegraNegocioException {
        this.nome = textoObrigatorio(nome, "nome do planejamento");
    }

    // ------------------------------------------------------------------ PARTICIPANTES

    public void cadastrarParticipante(int codigo, String nome, BigDecimal orcamento) throws RegraNegocioException {
        if (codigo <= 0) {
            throw new RegraNegocioException("O código deve ser um número inteiro maior que zero.");
        }
        if (participantes.containsKey(codigo)) {
            throw new RegraNegocioException("Já existe um participante com o código " + codigo + ".");
        }
        String nomeLimpo = textoObrigatorio(nome, "nome");
        BigDecimal orcamentoValido = validarOrcamento(orcamento);
        participantes.put(codigo, new Participante(codigo, nomeLimpo, orcamentoValido));
    }

    public void alterarParticipante(int codigo, String nome, BigDecimal orcamento) throws RegraNegocioException {
        Participante p = buscarParticipante(codigo);
        String nomeLimpo = textoObrigatorio(nome, "nome");
        BigDecimal orcamentoValido = validarOrcamento(orcamento);
        p.atualizar(nomeLimpo, orcamentoValido); // só altera depois de validar tudo
    }

    public void excluirParticipante(int codigo) throws RegraNegocioException {
        Participante p = buscarParticipante(codigo);
        for (Despesa d : despesas) {
            if (d.participa(p)) {
                throw new RegraNegocioException(
                        "Não é possível excluir " + p.getNome() + ": há despesas vinculadas.");
            }
        }
        participantes.remove(codigo);
    }

    public Participante buscarParticipante(int codigo) throws RegraNegocioException {
        Participante p = participantes.get(codigo);
        if (p == null) {
            throw new RegraNegocioException("Não existe participante com o código " + codigo + ".");
        }
        return p;
    }

    // ------------------------------------------------------------------ DESPESAS

    public Despesa incluirDespesa(TipoDespesa tipo, String descricao, BigDecimal valor,
                                  Collection<Integer> codigos) throws RegraNegocioException {
        // Valida TUDO antes de alterar qualquer coisa: se der erro, o estado fica como estava
        String descricaoLimpa = textoObrigatorio(descricao, "descrição");
        BigDecimal valorValido = validarValorDespesa(valor);
        List<Participante> envolvidos = resolverParticipantes(codigos);
        exigirTipo(tipo);

        Despesa despesa = criar(proximoIdDespesa, tipo, descricaoLimpa, valorValido, envolvidos);
        despesas.add(despesa);
        proximoIdDespesa++;
        return despesa;
    }

    public Despesa editarDespesa(int id, TipoDespesa tipo, String descricao, BigDecimal valor,
                                 Collection<Integer> codigos) throws RegraNegocioException {
        int posicao = posicaoDaDespesa(id);
        String descricaoLimpa = textoObrigatorio(descricao, "descrição");
        BigDecimal valorValido = validarValorDespesa(valor);
        List<Participante> envolvidos = resolverParticipantes(codigos);
        exigirTipo(tipo);

        Despesa nova = criar(id, tipo, descricaoLimpa, valorValido, envolvidos);
        despesas.set(posicao, nova); // o tipo pode mudar: troca o objeto, mantendo o número
        return nova;
    }

    public void excluirDespesa(int id) throws RegraNegocioException {
        despesas.remove(posicaoDaDespesa(id));
    }

    public Despesa buscarDespesa(int id) throws RegraNegocioException {
        return despesas.get(posicaoDaDespesa(id));
    }

    // ------------------------------------------------------------------ CONSULTAS E TOTAIS

    public String getNome() {
        return nome;
    }

    public List<Participante> getParticipantes() {
        return Collections.unmodifiableList(new ArrayList<>(participantes.values()));
    }

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }

    public BigDecimal custoTotal() {
        BigDecimal total = Dinheiro.zero();
        for (Despesa d : despesas) {
            total = total.add(d.custoTotal());
        }
        return total;
    }

    public BigDecimal totalDe(Participante p) {
        BigDecimal total = Dinheiro.zero();
        for (Despesa d : despesas) {
            total = total.add(d.valorDe(p));
        }
        return total;
    }

    // Total igual ao orçamento está dentro do limite
    public boolean dentroDoOrcamento(Participante p) {
        return totalDe(p).compareTo(p.getOrcamento()) <= 0;
    }

    // Valor que passou do orçamento (zero se estiver dentro)
    public BigDecimal excessoDe(Participante p) {
        BigDecimal diferenca = totalDe(p).subtract(p.getOrcamento());
        return diferenca.signum() > 0 ? diferenca : Dinheiro.zero();
    }

    // ------------------------------------------------------------------ VALIDAÇÕES INTERNAS

    private static String textoObrigatorio(String texto, String campo) throws RegraNegocioException {
        if (texto == null || texto.isBlank()) {
            throw new RegraNegocioException("O campo " + campo + " não pode ficar vazio.");
        }
        return texto.trim();
    }

    private static BigDecimal validarOrcamento(BigDecimal orcamento) throws RegraNegocioException {
        BigDecimal valor = Dinheiro.normalizar(orcamento, "orçamento");
        if (valor.signum() < 0) {
            throw new RegraNegocioException("O orçamento não pode ser negativo.");
        }
        return valor;
    }

    private static BigDecimal validarValorDespesa(BigDecimal valor) throws RegraNegocioException {
        BigDecimal valido = Dinheiro.normalizar(valor, "valor da despesa");
        if (valido.signum() <= 0) {
            throw new RegraNegocioException("O valor da despesa deve ser maior que zero.");
        }
        return valido;
    }

    private static void exigirTipo(TipoDespesa tipo) throws RegraNegocioException {
        if (tipo == null) {
            throw new RegraNegocioException("Escolha o tipo da despesa.");
        }
    }

    // Cada despesa precisa de pelo menos um participante cadastrado, sem repetição
    private List<Participante> resolverParticipantes(Collection<Integer> codigos) throws RegraNegocioException {
        if (codigos == null || codigos.isEmpty()) {
            throw new RegraNegocioException("Selecione pelo menos um participante.");
        }
        Set<Integer> vistos = new HashSet<>();
        List<Participante> envolvidos = new ArrayList<>();
        for (Integer codigo : codigos) {
            if (!vistos.add(codigo)) {
                throw new RegraNegocioException("Participante repetido na despesa: código " + codigo + ".");
            }
            envolvidos.add(buscarParticipante(codigo));
        }
        return envolvidos;
    }

    private int posicaoDaDespesa(int id) throws RegraNegocioException {
        for (int i = 0; i < despesas.size(); i++) {
            if (despesas.get(i).getId() == id) {
                return i;
            }
        }
        throw new RegraNegocioException("Não existe despesa com o número " + id + ".");
    }

    private static Despesa criar(int id, TipoDespesa tipo, String descricao, BigDecimal valor,
                                 List<Participante> envolvidos) {
        switch (tipo) {
            case FIXA:
                return new DespesaFixa(id, descricao, valor, envolvidos);
            case POR_PESSOA:
                return new DespesaPorPessoa(id, descricao, valor, envolvidos);
            default:
                throw new IllegalStateException("Tipo de despesa desconhecido: " + tipo);
        }
    }
}
