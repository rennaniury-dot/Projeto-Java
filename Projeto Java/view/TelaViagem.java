package view;

import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import model.*;
import persistencia.*;

public class TelaViagem extends JFrame {

    private static final long serialVersionUID = 1L;

    private Planejamento planejamento;
    private final Persistencia persistencia = new ArquivoPersistencia();

    private final DefaultTableModel modeloPessoas = criarModelo("Código", "Nome", "Orçamento",
            "Total gasto", "Saldo", "Situação");

    private final DefaultTableModel modeloDespesas = criarModelo("ID", "Descrição", "Tipo",
            "Valor", "Participantes", "Total");

    private final JTable tabelaPessoas = new JTable(modeloPessoas);

    private final JTable tabelaDespesas = new JTable(modeloDespesas);

    private final JLabel resumo = new JLabel();

    public TelaViagem() throws Exception {

        planejamento = new Planejamento("Passeio da turma");

        setTitle("Planejamento de Viagem");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        JPanel painelPessoas = new JPanel(new BorderLayout());

        painelPessoas.setBorder(
                BorderFactory.createTitledBorder("Participantes"));

        painelPessoas.add(
                new JScrollPane(tabelaPessoas), BorderLayout.CENTER);

        JPanel botoesPessoas = new JPanel();

        botao(botoesPessoas, "Adicionar participante",
                () -> cadastrarPessoa());

        botao(botoesPessoas, "Alterar participante",
                () -> alterarPessoa());

        botao(botoesPessoas, "Excluir participante",
                () -> excluirPessoa());

        painelPessoas.add(botoesPessoas, BorderLayout.SOUTH);

        JPanel painelDespesas = new JPanel(new BorderLayout());

        painelDespesas.setBorder(
                BorderFactory.createTitledBorder("Despesas"));

        painelDespesas.add(
                new JScrollPane(tabelaDespesas), BorderLayout.CENTER);

        JPanel botoesDespesas = new JPanel();

        botao(botoesDespesas, "Adicionar despesa",
                () -> editarDespesa(false));

        botao(botoesDespesas, "Editar despesa",
                () -> editarDespesa(true));

        botao(botoesDespesas, "Excluir despesa",
                () -> excluirDespesa());

        painelDespesas.add(botoesDespesas, BorderLayout.SOUTH);

        JSplitPane divisao = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                painelPessoas, painelDespesas);

        divisao.setResizeWeight(0.5);

        JPanel rodape = new JPanel(new BorderLayout());
        JPanel acoes = new JPanel();

        botao(acoes, "Salvar", () -> salvar());
        botao(acoes, "Carregar", () -> carregar());

        botao(acoes, "Resultado final",
                () -> mostrarResultado());

        botao(acoes, "Simular alternativas",
                () -> simular());

        rodape.add(resumo, BorderLayout.NORTH);
        rodape.add(acoes, BorderLayout.CENTER);

        add(divisao, BorderLayout.CENTER);
        add(rodape, BorderLayout.SOUTH);

        atualizar();
    }

    private static DefaultTableModel criarModelo(
            String... colunas) {

        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    @FunctionalInterface
    private interface Acao {
        void executar() throws Exception;
    }

    private void botao(JPanel painel, String nome, Acao acao) {

        JButton b = new JButton(nome);

        b.addActionListener(e -> {
            try {
                acao.executar();
                atualizar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this, ex.getMessage(), "Atenção",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        painel.add(b);
    }

    private void cadastrarPessoa() throws Exception {

        JTextField codigo = new JTextField();
        JTextField nome = new JTextField();
        JTextField orcamento = new JTextField();

        Object[] campos = {
                "Código:", codigo,
                "Nome:", nome,
                "Orçamento (R$):", orcamento
        };

        if (confirmar(campos, "Cadastrar participante")) {

            planejamento.cadastrarParticipante(
                    Integer.parseInt(codigo.getText().trim()),
                    nome.getText(),
                    dinheiro(orcamento.getText()));
        }
    }

    private void alterarPessoa() throws Exception {

        int codigo = selecionado(tabelaPessoas);

        Participante p = planejamento.buscarParticipante(codigo);

        JTextField nome = new JTextField(p.getNome());

        JTextField orcamento = new JTextField(
                p.getOrcamento().toPlainString());

        Object[] campos = {
                "Nome:", nome,
                "Orçamento (R$):", orcamento
        };

        if (confirmar(campos, "Alterar participante")) {

            planejamento.alterarParticipante(
                    codigo, nome.getText(),
                    dinheiro(orcamento.getText()));
        }
    }

    private void excluirPessoa() throws Exception {

        int codigo = selecionado(tabelaPessoas);

        if (confirmar("Excluir este participante?", "Confirmar")) {
            planejamento.excluirParticipante(codigo);
        }
    }

    private void editarDespesa(boolean editar)
            throws Exception {

        List<Participante> pessoas = planejamento.getParticipantes();

        if (pessoas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cadastre participantes primeiro.");
        }

        Despesa antiga = editar
                ? planejamento.buscarDespesa(
                        selecionado(tabelaDespesas))
                : null;

        JTextField descricao = new JTextField(
                antiga == null ? "" : antiga.getDescricao());

        JTextField valor = new JTextField(
                antiga == null ? "" : antiga.getValor().toPlainString());

        JComboBox<TipoDespesa> tipo = new JComboBox<>(TipoDespesa.values());

        if (antiga != null) {
            tipo.setSelectedItem(antiga.getTipo());
        }

        DefaultListModel<String> nomes = new DefaultListModel<>();

        for (Participante p : pessoas) {
            nomes.addElement(
                    p.getCodigo() + " - " + p.getNome());
        }

        JList<String> lista = new JList<>(nomes);

        lista.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        if (antiga != null) {
            for (int i = 0; i < pessoas.size(); i++) {
                if (antiga.participa(pessoas.get(i))) {
                    lista.addSelectionInterval(i, i);
                }
            }
        }

        JButton todos = new JButton("Selecionar todos");

        todos.addActionListener(e -> lista.setSelectionInterval(0, pessoas.size() - 1));

        Object[] campos = {
                "Descrição:", descricao,
                "Tipo:", tipo,
                "Valor (R$):", valor,
                "Selecione os participantes (use Ctrl):",
                new JScrollPane(lista),
                todos
        };

        if (!confirmar(campos, "Despesa")) {
            return;
        }

        List<Integer> codigos = new ArrayList<>();

        for (int i : lista.getSelectedIndices()) {
            codigos.add(pessoas.get(i).getCodigo());
        }

        TipoDespesa tipoEscolhido = (TipoDespesa) tipo.getSelectedItem();

        if (editar) {
            if (antiga == null) {
                throw new IllegalStateException("Nenhuma despesa selecionada.");
            }
            planejamento.editarDespesa(
                    antiga.getId(),
                    tipoEscolhido,
                    descricao.getText(),
                    dinheiro(valor.getText()),
                    codigos);

        } else {

            planejamento.incluirDespesa(
                    tipoEscolhido,
                    descricao.getText(),
                    dinheiro(valor.getText()),
                    codigos);
        }
    }

    private void excluirDespesa() throws Exception {

        int id = selecionado(tabelaDespesas);

        if (confirmar("Excluir esta despesa?", "Confirmar")) {
            planejamento.excluirDespesa(id);
        }
    }

    private void atualizar() {

        modeloPessoas.setRowCount(0);
        modeloDespesas.setRowCount(0);

        for (Participante p : planejamento.getParticipantes()) {

            BigDecimal total = planejamento.totalDe(p);

            BigDecimal saldo = p.getOrcamento().subtract(total);

            modeloPessoas.addRow(new Object[] {
                    p.getCodigo(),
                    p.getNome(),
                    moeda(p.getOrcamento()),
                    moeda(total),
                    moeda(saldo),
                    planejamento.dentroDoOrcamento(p)
                            ? "Dentro do orçamento"
                            : "Fora do orçamento"
            });
        }

        for (Despesa d : planejamento.getDespesas()) {

            List<String> nomes = new ArrayList<>();

            for (Participante p : d.getParticipantes()) {
                nomes.add(p.getNome());
            }

            modeloDespesas.addRow(new Object[] {
                    d.getId(),
                    d.getDescricao(),
                    d.getTipo(),
                    moeda(d.getValor()),
                    String.join(", ", nomes),
                    moeda(d.custoTotal())
            });
        }

        resumo.setText(
                "   Custo total do planejamento: "
                        + moeda(planejamento.custoTotal()));
    }

    private void mostrarResultado() {

        StringBuilder texto = new StringBuilder("RESULTADO FINAL\n\n");

        for (Participante p : planejamento.getParticipantes()) {

            BigDecimal total = planejamento.totalDe(p);

            texto.append(p.getNome())
                    .append(" | Orçamento: ")
                    .append(moeda(p.getOrcamento()))
                    .append(" | Gasto: ")
                    .append(moeda(total))
                    .append(" | Saldo: ")
                    .append(moeda(p.getOrcamento().subtract(total)))
                    .append("\n");
        }

        texto.append("\nTotal geral: ")
                .append(moeda(planejamento.custoTotal()));

        exibir(texto.toString(), "Resultado final");
    }

    // OBJETIVO 7 - SIMULADOR DE ALTERNATIVAS

    private void simular() {

        List<Despesa> despesas = planejamento.getDespesas();

        if (despesas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cadastre despesas antes de simular.");
        }

        JPanel painel = new JPanel(new GridLayout(0, 2, 5, 5));

        Map<Integer, JTextField> campos = new LinkedHashMap<>();

        for (Despesa d : despesas) {

            String prioridade = "0";

            if (d.getDescricao().equalsIgnoreCase(
                    "Atividade recreativa")) {
                prioridade = "3";
            }

            if (d.getDescricao().equalsIgnoreCase("Sobremesa")) {
                prioridade = "1";
            }

            JTextField campo = new JTextField(prioridade);

            painel.add(new JLabel(d.getDescricao()));
            painel.add(campo);

            campos.put(d.getId(), campo);
        }

        JScrollPane rolagem = new JScrollPane(painel);
        rolagem.setPreferredSize(new Dimension(420, 220));

        Object[] opcoes = {
                "0 = despesa essencial",
                "Número inteiro positivo = despesa opcional",
                "Máximo de 10 despesas opcionais",
                rolagem
        };

        if (!confirmar(opcoes, "Classificar despesas")) {
            return;
        }

        Map<Integer, Integer> prioridades = new LinkedHashMap<>();

        for (Map.Entry<Integer, JTextField> item : campos.entrySet()) {

            prioridades.put(
                    item.getKey(),
                    Integer.parseInt(
                            item.getValue().getText().trim()));
        }

        ResultadoSimulacao r = SimuladorAlternativas.simular(
                planejamento.getParticipantes(),
                despesas,
                prioridades);

        StringBuilder texto = new StringBuilder("SIMULADOR DE ALTERNATIVAS\n\n");

        if (!r.viavel()) {

            texto.append("Não existe solução viável.\n");
            texto.append("As despesas essenciais excedem o orçamento.\n\n");

            for (Participante p : planejamento.getParticipantes()) {

                if (r.excessos().containsKey(p.getCodigo())) {

                    texto.append(p.getNome())
                            .append(": excesso de ")
                            .append(moeda(
                                    r.excessos().get(p.getCodigo())))
                            .append("\n");
                }
            }

        } else {

            texto.append("SOLUÇÃO ENCONTRADA!\n");
            texto.append("Prioridade total: ")
                    .append(r.prioridadeTotal())
                    .append("\n\n");

            texto.append("DESPESAS MANTIDAS:\n");

            for (Despesa d : r.mantidas()) {
                texto.append("- ")
                        .append(d.getDescricao())
                        .append("\n");
            }

            texto.append("\nDESPESAS RETIRADAS:\n");

            if (r.retiradas().isEmpty()) {
                texto.append("Nenhuma\n");
            }

            for (Despesa d : r.retiradas()) {
                texto.append("- ")
                        .append(d.getDescricao())
                        .append("\n");
            }
        }

        texto.append("\nANTES E DEPOIS:\n");

        BigDecimal totalAntes = BigDecimal.ZERO;
        BigDecimal totalDepois = BigDecimal.ZERO;

        for (Participante p : planejamento.getParticipantes()) {

            BigDecimal antes = r.totaisAntes().get(p.getCodigo());

            BigDecimal depois = r.totaisDepois().get(p.getCodigo());

            totalAntes = totalAntes.add(antes);
            totalDepois = totalDepois.add(depois);

            texto.append(p.getNome())
                    .append(": ")
                    .append(moeda(antes))
                    .append(" -> ")
                    .append(moeda(depois))
                    .append("\n");
        }

        texto.append("\nTotal original: ")
                .append(moeda(totalAntes));

        texto.append(r.viavel()
                ? "\nTotal simulado: "
                : "\nTotal apenas das essenciais: ")
                .append(moeda(totalDepois));

        texto.append("\n\nPlanejamento original preservado.");

        exibir(texto.toString(), "Simulador de alternativas");
    }

    // PERSISTENCIA DE DADOS

    private void salvar() throws Exception {

        JFileChooser seletor = criarSeletor();

        if (seletor.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivo = seletor.getSelectedFile();

        if (!arquivo.getName().toLowerCase(Locale.ROOT)
                .endsWith(".dat")) {
            arquivo = new File(arquivo.getPath() + ".dat");
        }

        if (arquivo.exists() &&
                !confirmar("Substituir arquivo existente?", "Salvar")) {
            return;
        }

        persistencia.salvar(planejamento, arquivo);

        JOptionPane.showMessageDialog(
                this, "Planejamento salvo com sucesso!");
    }

    private void carregar() throws Exception {

        JFileChooser seletor = criarSeletor();

        if (seletor.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        if (!confirmar(
                "Carregue apenas arquivos confiáveis do programa.\n"
                        + "Substituir o planejamento atual?",
                "Carregar")) {
            return;
        }

        Planejamento carregado = persistencia.carregar(seletor.getSelectedFile());

        planejamento = carregado;

        JOptionPane.showMessageDialog(
                this, "Planejamento carregado com sucesso!");
    }

    private JFileChooser criarSeletor() {

        JFileChooser seletor = new JFileChooser();

        seletor.setFileFilter(
                new FileNameExtensionFilter(
                        "Planejamento (*.dat)", "dat"));

        return seletor;
    }

    private int selecionado(JTable tabela) {

        int linha = tabela.getSelectedRow();

        if (linha < 0) {
            throw new IllegalArgumentException(
                    "Selecione uma linha da tabela primeiro.");
        }

        return (Integer) tabela.getValueAt(linha, 0);
    }

    private boolean confirmar(Object mensagem, String titulo) {

        return JOptionPane.showConfirmDialog(
                this, mensagem, titulo,
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    private BigDecimal dinheiro(String texto) {

        return new BigDecimal(
                texto.trim().replace(",", "."));
    }

    private String moeda(BigDecimal valor) {

        return NumberFormat.getCurrencyInstance(
                Locale.forLanguageTag("pt-BR")).format(valor);
    }

    private void exibir(String texto, String titulo) {

        JTextArea area = new JTextArea(texto, 22, 55);

        area.setEditable(false);
        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this, new JScrollPane(area),
                titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                new TelaViagem().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null, e.getMessage());
            }
        });
    }
}