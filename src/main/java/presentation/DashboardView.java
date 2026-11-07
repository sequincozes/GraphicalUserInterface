package presentation;

import usecase.GerenciarProjetos;
import domain.ProjetoAcademico;
import domain.TipoProjeto;
import domain.RegraNegocioException;
import domain.PersistenciaException;
import java.math.BigDecimal;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Janela principal do sistema que integra identidade visual UNIPAMPA,
 * layout responsivo MigLayout e fluxos assíncronos sem bloquear a EDT.
 */
public class DashboardView extends JFrame {

    private final GerenciarProjetos useCase;
    private DefaultTableModel tableModel;
    private JTable tabela;
    private JTextField txtBusca;
    private KpiCard cardTotalProjetos;
    private KpiCard cardTotalBolsistas;
    private KpiCard cardOrcamentoTotal;
    private JLabel lblStatusRodape;
    private boolean darkMode = false;
    private long versaoBusca;
    private static final long serialVersionUID = 1L;

    /**
     * Constrói a interface gráfica principal injetando o caso de uso da aplicação.
     *
     * @param useCase instância do caso de uso para operações de negócio.
     */
    public DashboardView(GerenciarProjetos useCase) {
        this.useCase = java.util.Objects.requireNonNull(useCase);
        setTitle("UNIPAMPA • Sistema de Gestão de Projetos Acadêmicos");
        setSize(1240, 760);
        setMinimumSize(new Dimension(1024, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new MigLayout("insets 0, gap 0, fill", "[250!][grow, fill]", "[grow, fill]"));
        root.add(criarSidebar(), "growy");
        root.add(criarPainelConteudo(), "grow");

        setContentPane(root);
        executarBuscaAssincrona();
    }

    private JPanel criarSidebar() {
        JPanel sidebar = new JPanel(new MigLayout("insets 28 20 24 20, fillx, wrap 1, gapy 10"));
        sidebar.setBackground(Color.decode("#003F2D"));

        JLabel logo = new JLabel("UNIPAMPA");
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 24f));

        JLabel sub = new JLabel("PROPESQ & PROEXT");
        sub.setForeground(Color.decode("#FDB913"));
        sub.setFont(sub.getFont().deriveFont(Font.BOLD, 12f));

        sidebar.add(logo);
        sidebar.add(sub, "gapbottom 24");

        JButton btnNovoSidebar = new JButton("+ Novo Projeto (Alt+N)");
        btnNovoSidebar.setMnemonic(KeyEvent.VK_N);
        btnNovoSidebar.putClientProperty(FlatClientProperties.STYLE,
                "font: bold; background: #FDB913; foreground: #141A17; arc: 10");
        btnNovoSidebar.addActionListener(e -> abrirModalNovoProjeto());
        sidebar.add(btnNovoSidebar, "growx, h 42!, gapbottom 16");

        JLabel infoAtalhos = new JLabel("<html><body style='color:#D8F3DC; font-size:11px;'>"
                + "<b>Atalhos de Teclado:</b><br><br>"
                + "• <b>Alt + N</b>: Cadastrar Projeto<br>"
                + "• <b>Alt + A</b>: Aprovar Selecionado<br>"
                + "• <b>Alt + B</b>: Alocar +1 Bolsista<br>"
                + "• <b>Alt + E</b>: Excluir Selecionado<br>"
                + "• <b>Enter</b>: Filtrar na Busca"
                + "</body></html>");
        sidebar.add(infoAtalhos, "growx, gaptop 10");

        JButton btnTema = new JButton("Alternar Tema Claro/Escuro");
        btnTema.addActionListener(e -> alternarTema());
        sidebar.add(btnTema, "pushy, aligny bottom, growx, h 38!");

        return sidebar;
    }

    private JPanel criarPainelConteudo() {
        JPanel content = new JPanel(new MigLayout("insets 24 32 18 32, fill, wrap 1", "[grow, fill]", "[][][grow, fill][]"));

        // 1. Cabeçalho e Busca
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][320!][130!]"));
        header.setOpaque(false);

        JLabel titulo = new JLabel("Portfólio de Projetos de Pesquisa & Extensão");
        titulo.putClientProperty(FlatClientProperties.STYLE, "font: bold +6");

        txtBusca = new JTextField();
        txtBusca.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Filtrar por código, título, campus ou docente...");
        txtBusca.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        txtBusca.addActionListener(e -> executarBuscaAssincrona());

        JButton btnBuscar = new JButton("Atualizar");
        btnBuscar.addActionListener(e -> executarBuscaAssincrona());

        header.add(titulo);
        header.add(txtBusca, "h 38!");
        header.add(btnBuscar, "h 38!");

        // 2. Linha de Cards KPI (Polimorfismo calculado sobre PoliticaOrcamento)
        JPanel kpiRow = new JPanel(new MigLayout("insets 10 0 12 0, fillx, gap 18", "[grow, fill][grow, fill][grow, fill]"));
        kpiRow.setOpaque(false);
        cardTotalProjetos = new KpiCard("Projetos Listados", "0", "Pesquisa & Extensão", Color.decode("#005C43"), Color.decode("#003F2D"));
        cardTotalBolsistas = new KpiCard("Discentes Bolsistas", "0", "Limite máx: 10 por projeto", Color.decode("#0B7285"), Color.decode("#084C59"));
        cardOrcamentoTotal = new KpiCard("Orçamento Estimado", "R$ 0,00", "Estimativa anual de financiamento", Color.decode("#1F2937"), Color.decode("#111827"));

        kpiRow.add(cardTotalProjetos, "h 125!");
        kpiRow.add(cardTotalBolsistas, "h 125!");
        kpiRow.add(cardOrcamentoTotal, "h 125!");

        // 3. Tabela Polimórfica
        String[] colunas = {"ID", "Código", "Título", "Modalidade", "Campus", "Coordenador(a)", "Detalhe Específico", "Bolsas", "Orçamento Anual", "Status"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(tableModel);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(85);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(250);
        tabela.getColumnModel().getColumn(9).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(tabela);

        // 4. Barra de Ações de Negócio (CRUD + Regras de Domínio)
        JPanel actionBar = new JPanel(new MigLayout("insets 8 0 0 0, fillx", "[grow][][][]"));
        actionBar.setOpaque(false);

        lblStatusRodape = new JLabel("Preparando consulta...");

        JButton btnAprovar = new JButton("Aprovar Projeto (Alt+A)");
        btnAprovar.setMnemonic(KeyEvent.VK_A);
        btnAprovar.putClientProperty(FlatClientProperties.STYLE, "font: bold; background: #005C43; foreground: #FFFFFF");
        btnAprovar.addActionListener(e -> acaoAprovarSelecionado());

        JButton btnBolsista = new JButton("+1 Bolsista (Alt+B)");
        btnBolsista.setMnemonic(KeyEvent.VK_B);
        btnBolsista.addActionListener(e -> acaoAlocarBolsista());

        JButton btnExcluir = new JButton("Excluir (Alt+E)");
        btnExcluir.setMnemonic(KeyEvent.VK_E);
        btnExcluir.putClientProperty(FlatClientProperties.STYLE, "foreground: #C92A2A");
        btnExcluir.addActionListener(e -> acaoExcluirSelecionado());

        actionBar.add(lblStatusRodape);
        actionBar.add(btnAprovar, "h 38!");
        actionBar.add(btnBolsista, "h 38!");
        actionBar.add(btnExcluir, "h 38!");

        content.add(header, "growx");
        content.add(kpiRow, "growx");
        content.add(scroll, "grow, push");
        content.add(actionBar, "growx");

        return content;
    }

    private Long obterIdSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selecione primeiro um projeto na tabela para realizar esta operação.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return (Long) tableModel.getValueAt(tabela.convertRowIndexToModel(linha), 0);
    }

    private void acaoAprovarSelecionado() {
        Long id = obterIdSelecionado();
        if (id == null) return;

        executarOperacaoAssincrona(() -> {
            useCase.aprovarProjeto(id);
            return "Projeto ID " + id + " aprovado com sucesso!";
        });
    }

    private void acaoAlocarBolsista() {
        Long id = obterIdSelecionado();
        if (id == null) return;

        executarOperacaoAssincrona(() -> {
            useCase.adicionarBolsista(id);
            return "Novo bolsista alocado ao projeto ID " + id + "!";
        });
    }

    private void acaoExcluirSelecionado() {
        Long id = obterIdSelecionado();
        if (id == null) return;

        int confirma = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir o projeto selecionado (ID " + id + ")?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            executarOperacaoAssincrona(() -> {
                useCase.excluirProjeto(id);
                return "Projeto ID " + id + " excluído permanentemente.";
            });
        }
    }

    private void abrirModalNovoProjeto() {
        JDialog dialog = new JDialog(this, "Cadastrar Novo Projeto Acadêmico", true);
        dialog.setSize(520, 470);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new MigLayout("insets 24, fillx, wrap 2", "[140!][grow, fill]", "[]12[]12[]12[]12[]12[]12[]20[]"));

        JTextField txtCodigo = new JTextField("PRJ-" + (100 + (int) (Math.random() * 899)));
        JTextField txtTitulo = new JTextField();
        JComboBox<String> cbCampus = new JComboBox<>(new String[]{
            "Alegrete", "Bagé", "Caçapava do Sul", "Dom Pedrito", "Itaqui",
            "Jaguarão", "Santana do Livramento", "São Borja", "São Gabriel", "Uruguaiana"
        });
        JTextField txtCoordenador = new JTextField();
        JSpinner spBolsistas = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
        JComboBox<TipoProjeto> cbTipo = new JComboBox<>(TipoProjeto.values());
        JLabel lblDetalhe = new JLabel("Agência de Fomento:");
        JTextField txtDetalhe = new JTextField("CNPq / FAPERGS");

        cbTipo.addActionListener(e -> {
            if (cbTipo.getSelectedItem() == TipoProjeto.PESQUISA) {
                lblDetalhe.setText("Agência de Fomento:");
                txtDetalhe.setText("CNPq / FAPERGS");
            } else {
                lblDetalhe.setText("Comunidade Atendida:");
                txtDetalhe.setText("Escolas Públicas da Região");
            }
        });

        dialog.add(new JLabel("Código Institucional:"));
        dialog.add(txtCodigo);
        dialog.add(new JLabel("Título do Projeto:"));
        dialog.add(txtTitulo);
        dialog.add(new JLabel("Campus UNIPAMPA:"));
        dialog.add(cbCampus);
        dialog.add(new JLabel("Docente Coordenador:"));
        dialog.add(txtCoordenador);
        dialog.add(new JLabel("Nº de Bolsistas (0-10):"));
        dialog.add(spBolsistas);
        dialog.add(new JLabel("Modalidade:"));
        dialog.add(cbTipo);
        dialog.add(lblDetalhe);
        dialog.add(txtDetalhe);

        JButton btnCancelar = new JButton("Cancelar (Esc)");
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnSalvar = new JButton("Salvar (Enter)");
        btnSalvar.putClientProperty(FlatClientProperties.STYLE, "font: bold; background: #005C43; foreground: #FFFFFF");
        btnSalvar.addActionListener(e -> {
            String codigo = txtCodigo.getText();
            String titulo = txtTitulo.getText();
            String campus = (String) cbCampus.getSelectedItem();
            String coordenador = txtCoordenador.getText();
            int bolsistas = (Integer) spBolsistas.getValue();
            TipoProjeto tipo = (TipoProjeto) cbTipo.getSelectedItem();
            String detalhe = txtDetalhe.getText();
            btnSalvar.setEnabled(false);
            btnCancelar.setEnabled(false);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    useCase.cadastrarProjeto(codigo, titulo, campus, coordenador,
                        bolsistas, tipo, detalhe);
                    return null;
                }
                protected void done() {
                    try {
                        get();
                        dialog.dispose();
                        executarBuscaAssincrona();
                        JOptionPane.showMessageDialog(DashboardView.this,
                            "Projeto cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        apresentarErro(dialog, ex);
                    } finally {
                        btnSalvar.setEnabled(true);
                        btnCancelar.setEnabled(true);
                        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    }
                }
            }.execute();
        });

        JPanel botoes = new JPanel(new MigLayout("insets 0, fillx", "[grow][][]"));
        botoes.add(new JLabel(""));
        botoes.add(btnCancelar, "h 38!");
        botoes.add(btnSalvar, "h 38!");
        dialog.add(botoes, "span 2, growx");

        // Suporte a Enter e Esc no modal para acessibilidade por teclado
        dialog.getRootPane().setDefaultButton(btnSalvar);
        dialog.getRootPane().registerKeyboardAction(e -> { if (btnSalvar.isEnabled()) dialog.dispose(); },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setVisible(true);
    }

    @FunctionalInterface
    private interface AcaoAssincrona {
        String executar() throws RegraNegocioException, PersistenciaException;
    }

    private void executarOperacaoAssincrona(AcaoAssincrona acao) {
        lblStatusRodape.setText("Processando operação no PostgreSQL...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return acao.executar();
            }

            @Override
            protected void done() {
                try {
                    String msg = get();
                    lblStatusRodape.setText(msg);
                    executarBuscaAssincrona();
                } catch (Exception ex) {
                    lblStatusRodape.setText("Não foi possível concluir a operação.");
                    apresentarErro(DashboardView.this, ex);
                }
            }
        }.execute();
    }

    private void executarBuscaAssincrona() {
        String termo = txtBusca.getText();
        long requisicao = ++versaoBusca;
        lblStatusRodape.setText("Sincronizando dados com o PostgreSQL...");

        new SwingWorker<List<ProjetoAcademico>, Void>() {
            @Override
            protected List<ProjetoAcademico> doInBackground() throws Exception {
                return useCase.listarProjetos(termo);
            }

            @Override
            protected void done() {
                try {
                    List<ProjetoAcademico> projetos = get();
                    if (requisicao != versaoBusca) return;
                    preencherTabelaEKpis(projetos);
                    lblStatusRodape.setText("Sincronizado • " + projetos.size() + " registro(s) carregado(s).");
                } catch (Exception ex) {
                    if (requisicao != versaoBusca) return;
                    lblStatusRodape.setText("Não foi possível carregar os projetos.");
                    apresentarErro(DashboardView.this, ex);
                }
            }
        }.execute();
    }

    private void apresentarErro(Component parent, Exception erro) {
        if (erro instanceof InterruptedException) Thread.currentThread().interrupt();
        Throwable causa = erro instanceof java.util.concurrent.ExecutionException
            && erro.getCause() != null ? erro.getCause() : erro;
        String mensagem;
        if (causa instanceof RegraNegocioException || causa instanceof PersistenciaException) {
            mensagem = causa.getMessage();
        } else {
            mensagem = "Ocorreu um erro inesperado. Consulte o registro da aplicação.";
        }
        java.util.logging.Logger.getLogger(DashboardView.class.getName())
            .log(java.util.logging.Level.WARNING, "Falha na operação", causa);
        JOptionPane.showMessageDialog(parent, mensagem, "Operação não concluída",
            JOptionPane.WARNING_MESSAGE);
    }

    private void preencherTabelaEKpis(List<ProjetoAcademico> projetos) {
        NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        DateTimeFormatter fmtData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        tableModel.setRowCount(0);
        int somaBolsas = 0;
        BigDecimal somaOrcamento = BigDecimal.ZERO;

        for (ProjetoAcademico p : projetos) {
            BigDecimal orcamento = p.calcularOrcamentoTotal();
            somaBolsas += p.getBolsistas();
            somaOrcamento = somaOrcamento.add(orcamento);

            tableModel.addRow(new Object[]{
                p.getId(),
                p.getCodigo(),
                p.getTitulo(),
                p.getTipo().getDescricao(),
                p.getCampus(),
                p.getCoordenador(),
                p.getDetalheEspecifico() + " (" + p.getDataCadastro().format(fmtData) + ")",
                p.getBolsistas(),
                moeda.format(orcamento),
                p.getStatus().name()
            });
        }

        cardTotalProjetos.setValor(String.valueOf(projetos.size()));
        cardTotalBolsistas.setValor(String.valueOf(somaBolsas));
        cardOrcamentoTotal.setValor(moeda.format(somaOrcamento));
    }

    private void alternarTema() {
        FlatAnimatedLafChange.showSnapshot();
        try {
            darkMode = !darkMode;
            if (darkMode) FlatMacDarkLaf.setup();
            else FlatMacLightLaf.setup();
            FlatLaf.updateUI();
        } finally {
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        }
    }

    /**
     * Card visual customizado com pintura vetorial Graphics2D e Anti-Aliasing.
     */

    /**
     * Renderizador Flyweight para exibir pílulas de status coloridas na JTable.
     */

}