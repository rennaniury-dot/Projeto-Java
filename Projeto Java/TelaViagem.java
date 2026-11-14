import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

        // ---- Resultado final (embaixo) ----
        JButton botaoResultado = new JButton("Ver resultado final");
        JPanel painelSul = new JPanel();
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

        Pessoa p = new Pessoa(nome, orcamento);
        pessoas.add(p);
        modeloLista.addElement(nome);
        modelo.addRow(new Object[9]);
        atualizarLinha(pessoas.size() - 1);

        campoNome.setText("");
        campoOrcamento.setText("");
        campoNome.requestFocus();
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
