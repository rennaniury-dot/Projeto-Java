import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class TelaViagem {

    // Classe interna (nome diferente para não conflitar com o seu Participante.java)
    static class Pessoa {
        String nome;
        double orcamento;
        double[] gastos = new double[4];

        Pessoa(String nome, double orcamento) {
            this.nome = nome;
            this.orcamento = orcamento;
        }

        double totalGasto() {
            double total = 0;
            for (double g : gastos) total += g;
            return total;
        }

        double saldo() {
            return orcamento - totalGasto();
        }

        String situacao() {
            return saldo() >= 0 ? "Dentro do orçamento" : "Fora do orçamento";
        }
    }

    // ---- DESPESAS FIXAS (mude os valores aqui se precisar) ----
    static final String[] CATEGORIAS = {"Transporte", "Almoço", "Atividade recreativa", "Sobremesa"};
    static final double[] VALORES = {90, 20, 30, 10};
    // true  = valor TOTAL dividido entre os participantes marcados
    // false = valor POR PESSOA
    static final boolean[] DIVIDIDO = {true, false, true, false};

    private final List<Pessoa> pessoas = new ArrayList<>();
    private DefaultTableModel modelo;
    private DefaultListModel<String> modeloLista;
    private JList<String> listaPessoas;
    private JComboBox<String> comboCategoria;
    private JLabel infoDespesa;
    private JTextField campoNome, campoOrcamento;
    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaViagem().criar());
    }

    private void criar() {
        frame = new JFrame("Planejamento de Viagem");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // ---- Cadastro de participantes (topo) ----
        JPanel painelCadastro = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelCadastro.setBorder(BorderFactory.createTitledBorder("Cadastrar participante"));
        campoNome = new JTextField(12);
        campoOrcamento = new JTextField(8);
        JButton botaoPessoa = new JButton("Adicionar participante");
        painelCadastro.add(new JLabel("Nome:"));
        painelCadastro.add(campoNome);
        painelCadastro.add(new JLabel("Orçamento (R$):"));
        painelCadastro.add(campoOrcamento);
        painelCadastro.add(botaoPessoa);

        // ---- Tabela (centro) ----
        String[] colunas = {"Nome", "Orçamento", "Transporte", "Almoço",
                "Atividade recreativa", "Sobremesa", "Total gasto", "Saldo", "Situação"};
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabela = new JTable(modelo);

        // ---- Despesas (direita) ----
        JPanel painelDespesa = new JPanel(new BorderLayout(5, 5));
        painelDespesa.setBorder(BorderFactory.createTitledBorder("Aplicar despesa"));
        painelDespesa.setPreferredSize(new Dimension(260, 0));

        comboCategoria = new JComboBox<>(CATEGORIAS);
        infoDespesa = new JLabel();
        JPanel topoDespesa = new JPanel(new GridLayout(2, 1, 3, 3));
        topoDespesa.add(comboCategoria);
        topoDespesa.add(infoDespesa);

        modeloLista = new DefaultListModel<>();
        listaPessoas = new JList<>(modeloLista);
        listaPessoas.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Um clique marca/desmarca a pessoa (sem precisar do Ctrl)
        listaPessoas.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int i0, int i1) {
                if (i0 == i1) {
                    if (isSelectedIndex(i0)) removeSelectionInterval(i0, i1);
                    else addSelectionInterval(i0, i1);
                } else {
                    super.setSelectionInterval(i0, i1);
                }
            }
        });
        JScrollPane rolagemLista = new JScrollPane(listaPessoas);
        rolagemLista.setBorder(BorderFactory.createTitledBorder("Quem participa? (clique para marcar)"));

        JButton botaoTodos = new JButton("Selecionar todos");
        JButton botaoAplicar = new JButton("Aplicar despesa");
        JPanel botoesDespesa = new JPanel(new GridLayout(2, 1, 5, 5));
        botoesDespesa.add(botaoTodos);
        botoesDespesa.add(botaoAplicar);

        painelDespesa.add(topoDespesa, BorderLayout.NORTH);
        painelDespesa.add(rolagemLista, BorderLayout.CENTER);
        painelDespesa.add(botoesDespesa, BorderLayout.SOUTH);

        // ---- Salvar / Carregar / Resultado final (embaixo) ----
        JButton botaoSalvar = new JButton("Salvar CSV");
        JButton botaoCarregar = new JButton("Carregar CSV");
        JButton botaoResultado = new JButton("Ver resultado final");
        JPanel painelSul = new JPanel();
        painelSul.add(botaoSalvar);
        painelSul.add(botaoCarregar);
        painelSul.add(botaoResultado);

        frame.add(painelCadastro, BorderLayout.NORTH);
        frame.add(new JScrollPane(tabela), BorderLayout.CENTER);
        frame.add(painelDespesa, BorderLayout.EAST);
        frame.add(painelSul, BorderLayout.SOUTH);

        // ---- Ações ----
        botaoPessoa.addActionListener(e -> adicionarPessoa());
        botaoAplicar.addActionListener(e -> aplicarDespesa());
        botaoTodos.addActionListener(e -> {
            if (modeloLista.size() > 0) listaPessoas.addSelectionInterval(0, modeloLista.size() - 1);
        });
        botaoSalvar.addActionListener(e -> salvar());
        botaoCarregar.addActionListener(e -> carregar());
        botaoResultado.addActionListener(e -> mostrarResultado());
        comboCategoria.addActionListener(e -> atualizarInfoDespesa());

        atualizarInfoDespesa();
        frame.setVisible(true);
    }

    private void atualizarInfoDespesa() {
        int c = comboCategoria.getSelectedIndex();
        if (DIVIDIDO[c]) {
            infoDespesa.setText(moeda(VALORES[c]) + " no total, dividido entre os marcados");
        } else {
            infoDespesa.setText(moeda(VALORES[c]) + " por pessoa");
        }
    }

    private void adicionarPessoa() {
        String nome = campoNome.getText().trim();
        Double orcamento = lerValor(campoOrcamento.getText());

        if (nome.isEmpty()) {
            aviso("Digite o nome do participante.");
            return;
        }
        if (orcamento == null || orcamento < 0) {
            aviso("Digite um orçamento válido (exemplo: 70 ou 70,50).");
            return;
        }

        inserirPessoa(new Pessoa(nome, orcamento));

        campoNome.setText("");
        campoOrcamento.setText("");
        campoNome.requestFocus();
    }

    // Coloca uma pessoa na lista, na tabela e na caixa "Quem participa?"
    private void inserirPessoa(Pessoa p) {
        pessoas.add(p);
        modeloLista.addElement(p.nome);
        modelo.addRow(new Object[9]);
        atualizarLinha(pessoas.size() - 1);
    }

    private void aplicarDespesa() {
        int[] marcados = listaPessoas.getSelectedIndices();
        if (marcados.length == 0) {
            aviso("Marque pelo menos um participante na lista.");
            return;
        }

        int c = comboCategoria.getSelectedIndex();
        double valorPorPessoa = DIVIDIDO[c] ? VALORES[c] / marcados.length : VALORES[c];

        // Zera essa despesa para todos e aplica só para os marcados
        // (assim, aplicar de novo não cobra em dobro)
        for (Pessoa p : pessoas) p.gastos[c] = 0;
        for (int i : marcados) pessoas.get(i).gastos[c] = valorPorPessoa;

        for (int i = 0; i < pessoas.size(); i++) atualizarLinha(i);

        aviso(CATEGORIAS[c] + ": " + moeda(valorPorPessoa) + " para cada um dos "
                + marcados.length + " participante(s) marcado(s).");
    }

    private void atualizarLinha(int i) {
        Pessoa p = pessoas.get(i);
        modelo.setValueAt(p.nome, i, 0);
        modelo.setValueAt(moeda(p.orcamento), i, 1);
        for (int c = 0; c < 4; c++) {
            modelo.setValueAt(moeda(p.gastos[c]), i, c + 2);
        }
        modelo.setValueAt(moeda(p.totalGasto()), i, 6);
        modelo.setValueAt(moeda(p.saldo()), i, 7);
        modelo.setValueAt(p.situacao(), i, 8);
    }

    private void mostrarResultado() {
        if (pessoas.isEmpty()) {
            aviso("Nenhum participante cadastrado.");
            return;
        }
        StringBuilder sb = new StringBuilder("RESULTADO FINAL\n\n");
        for (Pessoa p : pessoas) {
            sb.append(p.nome).append(": gastou ").append(moeda(p.totalGasto()))
              .append(" de ").append(moeda(p.orcamento))
              .append(" | saldo ").append(moeda(p.saldo()))
              .append(" | ").append(p.situacao()).append("\n");
        }
        aviso(sb.toString());
    }

    // =====================================================
    //  SALVAR / CARREGAR EM CSV
    //  Formato (separador ponto e vírgula, abre no Excel):
    //  Nome;Orcamento;Transporte;Almoco;Atividade recreativa;Sobremesa
    //  Ana;70.0;30.0;20.0;15.0;10.0
    // =====================================================
    private void salvar() {
        if (pessoas.isEmpty()) {
            aviso("Nenhum participante para salvar.");
            return;
        }

        JFileChooser seletor = criarSeletor();
        if (seletor.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return; // o usuário cancelou
        }
        Path arquivo = seletor.getSelectedFile().toPath();
        if (!arquivo.getFileName().toString().toLowerCase().endsWith(".csv")) {
            arquivo = arquivo.resolveSibling(arquivo.getFileName() + ".csv");
        }

        List<String> linhas = new ArrayList<>();
        linhas.add("Nome;Orcamento;Transporte;Almoco;Atividade recreativa;Sobremesa");
        for (Pessoa p : pessoas) {
            linhas.add(p.nome.replace(";", ",") + ";" + p.orcamento
                    + ";" + p.gastos[0] + ";" + p.gastos[1]
                    + ";" + p.gastos[2] + ";" + p.gastos[3]);
        }

        try {
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
            aviso("Dados salvos em:\n" + arquivo);
        } catch (IOException e) {
            aviso("Não foi possível salvar o arquivo.");
        }
    }

    private void carregar() {
        JFileChooser seletor = criarSeletor();
        if (seletor.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return; // o usuário cancelou
        }
        Path arquivo = seletor.getSelectedFile().toPath();

        // Lê tudo primeiro: se o arquivo estiver errado, a tela atual não é alterada
        List<Pessoa> lidas = new ArrayList<>();
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                linha = linha.replace("\uFEFF", ""); // remove marca invisível que o Excel pode colocar
                if (linha.isBlank() || linha.startsWith("Nome;")) {
                    continue; // pula linha vazia e o cabeçalho
                }
                String[] c = linha.split(";");
                if (c.length != 6) {
                    throw new IllegalArgumentException("Linha com número de colunas errado");
                }
                Pessoa p = new Pessoa(c[0].trim(), numero(c[1]));
                for (int i = 0; i < 4; i++) {
                    p.gastos[i] = numero(c[i + 2]);
                }
                lidas.add(p);
            }
        } catch (IOException | IllegalArgumentException e) {
            aviso("Não foi possível carregar o arquivo.\nConfira se ele foi salvo por este programa.");
            return;
        }

        if (lidas.isEmpty()) {
            aviso("O arquivo não tem nenhum participante.");
            return;
        }

        if (!pessoas.isEmpty()) {
            int resposta = JOptionPane.showConfirmDialog(frame,
                    "Carregar vai substituir os dados que estão na tela. Continuar?",
                    "Carregar CSV", JOptionPane.YES_NO_OPTION);
            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        pessoas.clear();
        modeloLista.clear();
        modelo.setRowCount(0);
        for (Pessoa p : lidas) {
            inserirPessoa(p);
        }
        aviso(lidas.size() + " participante(s) carregado(s).");
    }

    private JFileChooser criarSeletor() {
        JFileChooser seletor = new JFileChooser();
        seletor.setFileFilter(new FileNameExtensionFilter("Arquivos CSV (*.csv)", "csv"));
        return seletor;
    }

    // Lê número aceitando ponto ou vírgula; lança erro (NumberFormatException) se for inválido
    private double numero(String texto) {
        return Double.parseDouble(texto.trim().replace(",", "."));
    }

    private Double lerValor(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String moeda(double valor) {
        return String.format("R$ %.2f", valor);
    }

    private void aviso(String mensagem) {
        JOptionPane.showMessageDialog(frame, mensagem);
    }
}
