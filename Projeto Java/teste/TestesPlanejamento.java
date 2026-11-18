package teste;

import model.Despesa;
import model.Participante;
import model.Planejamento;
import model.RegraNegocioException;
import model.TipoDespesa;
import persistencia.ArquivoPersistencia;
import persistencia.Persistencia;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// Testes automatizados (sem biblioteca externa): rodam o caso principal da seção 4 e os 6 cenários
// adicionais. Para cada verificação registram: cenário, entrada, resultado esperado e resultado obtido.
//
// Como rodar (na pasta do projeto):
//   javac -encoding UTF-8 -d out model/*.java persistencia/*.java teste/*.java
//   java -cp out teste.TestesPlanejamento
public class TestesPlanejamento {

    private static final List<String[]> registros = new ArrayList<>();
    private static int falhas = 0;

    @FunctionalInterface
    private interface Acao {
        void executar() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        casoPrincipal();
        centavosNoRateio();
        entradaOuVinculoInvalido();
        alteracaoDeOrcamento();
        exclusaoDeDespesa();
        salvarEReiniciar();
        problemaNoArquivo();
        regrasAdicionais();
        imprimirRelatorio();
        System.exit(falhas == 0 ? 0 : 1);
    }

    // ------------------------------------------------------------------ CENÁRIOS

    private static void casoPrincipal() throws Exception {
        String c = "Caso principal (seção 4)";
        Planejamento p = criarCasoPrincipal();
        String entrada = "Ana 70,00; Bruno 60,00; Carla 70,00 | Transporte fixa 90,00 (1,2,3); "
                + "Almoço por pessoa 20,00 (1,2,3); Atividade recreativa fixa 30,00 (1,3); "
                + "Sobremesa por pessoa 10,00 (1,2,3)";

        verificar(c, entrada, "Ana: Transporte 30,00; Almoço 20,00; Atividade recreativa 15,00; Sobremesa 10,00; total 75,00", detalhe(p, 1));
        verificar(c, "Valores de Bruno", "Bruno: Transporte 30,00; Almoço 20,00; Atividade recreativa 0,00; Sobremesa 10,00; total 60,00", detalhe(p, 2));
        verificar(c, "Valores de Carla", "Carla: Transporte 30,00; Almoço 20,00; Atividade recreativa 15,00; Sobremesa 10,00; total 75,00", detalhe(p, 3));
        verificar(c, "Custo total do planejamento", "R$ 210,00", m(p.custoTotal()));
        verificar(c, "Situação de cada participante", "Ana: excede R$ 5,00; Bruno: dentro do orçamento; Carla: excede R$ 5,00", situacoes(p));
    }

    private static void centavosNoRateio() throws Exception {
        String c = "Centavos no rateio";
        Planejamento p = new Planejamento("Teste de centavos");
        p.cadastrarParticipante(1, "Ana", d("100.00"));
        p.cadastrarParticipante(2, "Bruno", d("100.00"));
        p.cadastrarParticipante(3, "Carla", d("100.00"));

        Despesa a = p.incluirDespesa(TipoDespesa.FIXA, "Hospedagem", d("100.00"), Arrays.asList(1, 2, 3));
        verificar(c, "Despesa fixa de 100,00 para os códigos 1, 2 e 3",
                "1=33,34; 2=33,33; 3=33,33; soma=100,00", rateio(a));

        Despesa b = p.incluirDespesa(TipoDespesa.FIXA, "Hospedagem (ordem trocada)", d("100.00"), Arrays.asList(3, 1, 2));
        verificar(c, "Mesma despesa com os códigos informados na ordem 3, 1, 2",
                "1=33,34; 2=33,33; 3=33,33; soma=100,00", rateio(b));

        Despesa e = p.incluirDespesa(TipoDespesa.FIXA, "Taxa mínima", d("0.01"), Arrays.asList(1, 2, 3));
        verificar(c, "Despesa fixa de 0,01 para os códigos 1, 2 e 3",
                "1=0,01; 2=0,00; 3=0,00; soma=0,01", rateio(e));
    }

    private static void entradaOuVinculoInvalido() throws Exception {
        String c = "Entrada ou vínculo inválido";
        Planejamento p = criarCasoPrincipal();

        recusar(c, p, "Despesa sem participantes", "Selecione pelo menos um participante.",
                () -> p.incluirDespesa(TipoDespesa.FIXA, "Hotel", d("100.00"), new ArrayList<Integer>()));
        recusar(c, p, "Despesa com valor negativo (-10,00)", "O valor da despesa deve ser maior que zero.",
                () -> p.incluirDespesa(TipoDespesa.FIXA, "Hotel", d("-10.00"), Arrays.asList(1)));
        recusar(c, p, "Despesa com valor zero", "O valor da despesa deve ser maior que zero.",
                () -> p.incluirDespesa(TipoDespesa.POR_PESSOA, "Hotel", d("0.00"), Arrays.asList(1)));
        recusar(c, p, "Despesa com descrição vazia", "O campo descrição não pode ficar vazio.",
                () -> p.incluirDespesa(TipoDespesa.FIXA, "   ", d("10.00"), Arrays.asList(1)));
        recusar(c, p, "Despesa com valor de 3 casas decimais (10,005)", "O valor da despesa deve ter no máximo 2 casas decimais.",
                () -> p.incluirDespesa(TipoDespesa.FIXA, "Hotel", d("10.005"), Arrays.asList(1)));
        recusar(c, p, "Despesa com participante inexistente (código 9)", "Não existe participante com o código 9.",
                () -> p.incluirDespesa(TipoDespesa.FIXA, "Hotel", d("10.00"), Arrays.asList(1, 9)));
        recusar(c, p, "Despesa com participante repetido (códigos 1 e 1)", "Participante repetido na despesa: código 1.",
                () -> p.incluirDespesa(TipoDespesa.FIXA, "Hotel", d("10.00"), Arrays.asList(1, 1)));
        recusar(c, p, "Participante com código duplicado (código 1)", "Já existe um participante com o código 1.",
                () -> p.cadastrarParticipante(1, "Outro", d("10.00")));
        recusar(c, p, "Participante com nome vazio", "O campo nome não pode ficar vazio.",
                () -> p.cadastrarParticipante(4, "", d("10.00")));
        recusar(c, p, "Participante com orçamento negativo (-1,00)", "O orçamento não pode ser negativo.",
                () -> p.cadastrarParticipante(4, "Diego", d("-1.00")));
    }

    private static void alteracaoDeOrcamento() throws Exception {
        String c = "Alteração de orçamento";
        Planejamento p = criarCasoPrincipal();
        p.alterarParticipante(1, "Ana", d("80.00"));
        Participante ana = p.buscarParticipante(1);

        verificar(c, "Orçamento de Ana alterado de 70,00 para 80,00",
                "orçamento R$ 80,00; custo R$ 75,00; dentro do orçamento",
                "orçamento " + m(ana.getOrcamento()) + "; custo " + m(p.totalDe(ana)) + "; " + situacao(p, ana));
        verificar(c, "Situação dos demais participantes após a alteração",
                "Ana: dentro do orçamento; Bruno: dentro do orçamento; Carla: excede R$ 5,00", situacoes(p));
    }

    private static void exclusaoDeDespesa() throws Exception {
        String c = "Exclusão de despesa";
        Planejamento p = criarCasoPrincipal();
        int id = 0;
        for (Despesa despesa : p.getDespesas()) {
            if (despesa.getDescricao().equals("Atividade recreativa")) {
                id = despesa.getId();
            }
        }
        p.excluirDespesa(id);

        verificar(c, "Excluir a atividade recreativa do caso principal (total por pessoa)",
                "Ana R$ 60,00; Bruno R$ 60,00; Carla R$ 60,00", totais(p));
        verificar(c, "Custo total após a exclusão", "R$ 180,00", m(p.custoTotal()));
        verificar(c, "Situação após a exclusão",
                "Ana: dentro do orçamento; Bruno: dentro do orçamento; Carla: dentro do orçamento", situacoes(p));
    }

    private static void salvarEReiniciar() throws Exception {
        String c = "Salvar e reiniciar";
        Planejamento original = criarCasoPrincipal();
        Persistencia persistencia = new ArquivoPersistencia();
        File arquivo = File.createTempFile("planejamento", ".dat");
        try {
            persistencia.salvar(original, arquivo);
            Planejamento q = persistencia.carregar(arquivo); // simula fechar e abrir o programa

            verificar(c, "Salvar o caso principal e carregar em um planejamento novo",
                    "3 participantes e 4 despesas", q.getParticipantes().size() + " participantes e " + q.getDespesas().size() + " despesas");
            verificar(c, "Total por pessoa após carregar", "Ana R$ 75,00; Bruno R$ 60,00; Carla R$ 75,00", totais(q));
            verificar(c, "Custo total após carregar", "R$ 210,00", m(q.custoTotal()));
            verificar(c, "Situação após carregar",
                    "Ana: excede R$ 5,00; Bruno: dentro do orçamento; Carla: excede R$ 5,00", situacoes(q));
            verificar(c, "Vínculos entre despesas e participantes preservados", "sim", vinculosPreservados(q) ? "sim" : "não");

            Planejamento q2 = persistencia.carregar(arquivo);
            verificar(c, "Carregar o mesmo arquivo uma segunda vez (sem duplicar registros)",
                    "3 participantes e 4 despesas", q2.getParticipantes().size() + " participantes e " + q2.getDespesas().size() + " despesas");

            Despesa nova = q.incluirDespesa(TipoDespesa.POR_PESSOA, "Souvenir", d("5.00"), Arrays.asList(1));
            verificar(c, "Nova despesa depois de carregar recebe o próximo número", "número 5", "número " + nova.getId());
        } finally {
            arquivo.delete();
        }
    }

    private static void problemaNoArquivo() throws Exception {
        String c = "Problema no arquivo";
        Persistencia persistencia = new ArquivoPersistencia();
        Planejamento[] emUso = { criarCasoPrincipal() };
        String antes = estado(emUso[0]);

        File ausente = new File(System.getProperty("java.io.tmpdir"), "nao_existe_planejamento.dat");
        ausente.delete();
        verificar(c, "Carregar um arquivo que não existe",
                "Arquivo não encontrado: nao_existe_planejamento.dat | planejamento em uso preservado",
                tentarCarregar(persistencia, ausente, emUso) + " | planejamento em uso " + (antes.equals(estado(emUso[0])) ? "preservado" : "ALTERADO"));

        File invalido = File.createTempFile("invalido", ".dat");
        try {
            Files.write(invalido.toPath(), "isto não é um planejamento".getBytes(StandardCharsets.UTF_8));
            verificar(c, "Carregar um arquivo com conteúdo inválido",
                    "O arquivo está corrompido ou não é um planejamento válido. | planejamento em uso preservado",
                    tentarCarregar(persistencia, invalido, emUso) + " | planejamento em uso " + (antes.equals(estado(emUso[0])) ? "preservado" : "ALTERADO"));
        } finally {
            invalido.delete();
        }

        Planejamento vazio = new Planejamento("Novo planejamento");
        verificar(c, "Iniciar um planejamento vazio", "0 participantes, 0 despesas, custo R$ 0,00",
                vazio.getParticipantes().size() + " participantes, " + vazio.getDespesas().size() + " despesas, custo " + m(vazio.custoTotal()));
    }

    private static void regrasAdicionais() throws Exception {
        String c = "Regras adicionais";

        Planejamento p = criarCasoPrincipal();
        recusar(c, p, "Excluir participante com despesas vinculadas (Ana)",
                "Não é possível excluir Ana: há despesas vinculadas.", () -> p.excluirParticipante(1));

        p.cadastrarParticipante(4, "Diego", d("50.00"));
        p.excluirParticipante(4);
        verificar(c, "Excluir participante sem despesas (Diego, código 4)", "3 participantes", p.getParticipantes().size() + " participantes");

        StringBuilder sb = new StringBuilder();
        for (Despesa despesa : p.getDespesas()) { // referência do tipo abstrato: polimorfismo
            if (sb.length() > 0) sb.append("; ");
            sb.append(despesa.getDescricao()).append(": ").append(despesa.getClass().getSimpleName())
              .append(" (").append(despesa.getTipo()).append("), custo ").append(m(despesa.custoTotal()));
        }
        verificar(c, "Polimorfismo: custo de cada despesa pela referência do tipo Despesa",
                "Transporte: DespesaFixa (Fixa), custo R$ 90,00; Almoço: DespesaPorPessoa (Por pessoa), custo R$ 60,00; "
                        + "Atividade recreativa: DespesaFixa (Fixa), custo R$ 30,00; Sobremesa: DespesaPorPessoa (Por pessoa), custo R$ 30,00",
                sb.toString());

        Planejamento q = criarCasoPrincipal();
        q.editarDespesa(2, TipoDespesa.FIXA, "Almoço", d("20.00"), Arrays.asList(1, 2, 3));
        verificar(c, "Editar o almoço de 'por pessoa' para 'fixa' (20,00 entre 3): totais recalculados",
                "Ana R$ 61,67; Bruno R$ 46,67; Carla R$ 61,66", totais(q));

        Planejamento r = criarCasoPrincipal();
        r.alterarParticipante(1, "Ana Souza", d("70.00"));
        verificar(c, "Renomear participante: o nome novo aparece nas despesas vinculadas",
                "Ana Souza", r.getDespesas().get(0).getParticipantes().get(0).getNome());
    }

    // ------------------------------------------------------------------ AUXILIARES

    private static Planejamento criarCasoPrincipal() throws RegraNegocioException {
        Planejamento p = new Planejamento("Passeio da turma");
        p.cadastrarParticipante(1, "Ana", d("70.00"));
        p.cadastrarParticipante(2, "Bruno", d("60.00"));
        p.cadastrarParticipante(3, "Carla", d("70.00"));
        p.incluirDespesa(TipoDespesa.FIXA, "Transporte", d("90.00"), Arrays.asList(1, 2, 3));
        p.incluirDespesa(TipoDespesa.POR_PESSOA, "Almoço", d("20.00"), Arrays.asList(1, 2, 3));
        p.incluirDespesa(TipoDespesa.FIXA, "Atividade recreativa", d("30.00"), Arrays.asList(1, 3));
        p.incluirDespesa(TipoDespesa.POR_PESSOA, "Sobremesa", d("10.00"), Arrays.asList(1, 2, 3));
        return p;
    }

    private static BigDecimal d(String texto) {
        return new BigDecimal(texto);
    }

    private static String num(BigDecimal v) {
        return v.toPlainString().replace('.', ',');
    }

    private static String m(BigDecimal v) {
        return "R$ " + num(v);
    }

    private static String detalhe(Planejamento p, int codigo) throws RegraNegocioException {
        Participante x = p.buscarParticipante(codigo);
        StringBuilder sb = new StringBuilder(x.getNome()).append(": ");
        for (Despesa despesa : p.getDespesas()) {
            sb.append(despesa.getDescricao()).append(' ').append(num(despesa.valorDe(x))).append("; ");
        }
        return sb.append("total ").append(num(p.totalDe(x))).toString();
    }

    private static String rateio(Despesa despesa) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, BigDecimal> parte : despesa.ratear().entrySet()) {
            sb.append(parte.getKey()).append('=').append(num(parte.getValue())).append("; ");
        }
        return sb.append("soma=").append(num(despesa.custoTotal())).toString();
    }

    private static String totais(Planejamento p) {
        StringBuilder sb = new StringBuilder();
        for (Participante x : p.getParticipantes()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(x.getNome()).append(' ').append(m(p.totalDe(x)));
        }
        return sb.toString();
    }

    private static String situacao(Planejamento p, Participante x) {
        return p.dentroDoOrcamento(x) ? "dentro do orçamento" : "excede " + m(p.excessoDe(x));
    }

    private static String situacoes(Planejamento p) {
        StringBuilder sb = new StringBuilder();
        for (Participante x : p.getParticipantes()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(x.getNome()).append(": ").append(situacao(p, x));
        }
        return sb.toString();
    }

    private static String estado(Planejamento p) {
        StringBuilder sb = new StringBuilder();
        sb.append(p.getParticipantes().size()).append(" participantes, ")
          .append(p.getDespesas().size()).append(" despesas, custo ").append(m(p.custoTotal()));
        for (Participante x : p.getParticipantes()) {
            sb.append(", ").append(x.getNome()).append(' ').append(m(p.totalDe(x))).append('/').append(m(x.getOrcamento()));
        }
        return sb.toString();
    }

    private static boolean vinculosPreservados(Planejamento q) throws RegraNegocioException {
        for (Despesa despesa : q.getDespesas()) {
            for (Participante x : despesa.getParticipantes()) {
                if (q.buscarParticipante(x.getCodigo()) != x) {
                    return false; // a despesa aponta para uma cópia, não para o participante do planejamento
                }
            }
        }
        return true;
    }

    private static String tentar(Acao acao) {
        try {
            acao.executar();
            return "aceita (sem erro)";
        } catch (RegraNegocioException e) {
            return "recusada: " + e.getMessage();
        } catch (Exception e) {
            return "erro inesperado: " + e;
        }
    }

    private static String tentarCarregar(Persistencia persistencia, File arquivo, Planejamento[] emUso) {
        try {
            emUso[0] = persistencia.carregar(arquivo); // só troca o planejamento em uso se der certo
            return "carregado";
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    // A operação precisa ser recusada com a mensagem esperada E sem alterar nenhum dado
    private static void recusar(String cenario, Planejamento p, String entrada, String mensagem, Acao acao) {
        String antes = estado(p);
        String resultado = tentar(acao);
        String depois = estado(p);
        verificar(cenario, entrada, "recusada: " + mensagem + " | estado inalterado",
                resultado + " | estado " + (antes.equals(depois) ? "inalterado" : "ALTERADO"));
    }

    private static void verificar(String cenario, String entrada, String esperado, String obtido) {
        boolean ok = esperado.equals(obtido);
        if (!ok) {
            falhas++;
        }
        registros.add(new String[] { cenario, entrada, esperado, obtido, ok ? "OK" : "FALHA" });
    }

    private static void imprimirRelatorio() throws IOException {
        StringBuilder sb = new StringBuilder();
        String cenarioAtual = "";
        int numero = 0;
        for (String[] r : registros) {
            if (!r[0].equals(cenarioAtual)) {
                cenarioAtual = r[0];
                sb.append("\n=== ").append(cenarioAtual).append(" ===\n");
            }
            numero++;
            sb.append(String.format("%2d. [%s] %s%n", numero, r[4], r[1]));
            sb.append("      esperado: ").append(r[2]).append('\n');
            sb.append("      obtido:   ").append(r[3]).append('\n');
        }
        int ok = registros.size() - falhas;
        sb.append("\nRESUMO: ").append(registros.size()).append(" verificações, ")
          .append(ok).append(" OK, ").append(falhas).append(" falha(s)\n");

        System.out.println(sb);
        Files.write(new File("resultados_testes.txt").toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
