import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * SistemaCafe é a classe principal que representa o sistema de ponto de venda (PDV) do café.
 * Ele gerencia pedidos, estoque e vendas através de uma interface gráfica simples.
 */
public class CafeSystem extends JFrame {
    // Componentes da interface gráfica
    private JTextArea areaTextoPedido, areaTextoEstoque, areaTextoVendas;
    private JLabel rotuloTotal;

    // Variáveis para gerenciar dados
    private double total = 0.0;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private Map<String, Integer> estoque = new HashMap<>();
    private Map<String, Integer> vendas = new HashMap<>();

    // Constantes
    private static final double TAXA_IMPOSTO = 0.08; // 8% de imposto
    private static final String[] ITENS_MENU = {"Café", "Chá", "Pão de Queijo", "Sanduíche"};


     // Construtor da classe CafeSystem.
     // Configura a janela principal e inicializa o sistema.

    public CafeSystem() {
        setTitle("Sistema de PDV do Café");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        criarPainelAbas();
        inicializarSistema();

        setVisible(true);
    }


     // Cria o painel principal com abas para Pedido, Estoque e Vendas.
     // Também adiciona o botão para reiniciar o sistema.

    private void criarPainelAbas() {
        JTabbedPane painelAbas = new JTabbedPane();
        painelAbas.addTab("Pedido", criarPainelPedido());
        painelAbas.addTab("Estoque", criarPainelEstoque());
        painelAbas.addTab("Vendas", criarPainelVendas());
        add(painelAbas, BorderLayout.CENTER);

        JButton botaoReiniciar = new JButton("Reiniciar Sistema");
        botaoReiniciar.addActionListener(e -> reiniciarSistema());
        add(botaoReiniciar, BorderLayout.SOUTH);
    }


    // Inicializa o sistema carregando os dados de estoque e vendas.

    private void inicializarSistema() {
        carregarEstoque();
        carregarVendas();
    }


    //Cria o painel de pedidos com botões para cada item do menu.

    private JPanel criarPainelPedido() {
        JPanel painelPedido = new JPanel(new BorderLayout());
        JPanel painelMenu = new JPanel(new GridLayout(0, 2, 10, 10));
        double[] precos = {2.50, 2.00, 1.50, 4.00};

        for (int i = 0; i < ITENS_MENU.length; i++) {
            final String item = ITENS_MENU[i];
            final double preco = precos[i];
            JButton botao = new JButton(item + " - R$" + preco);
            botao.addActionListener(e -> adicionarAoPedido(item, preco));
            painelMenu.add(botao);
        }

        painelPedido.add(painelMenu, BorderLayout.NORTH);

        areaTextoPedido = new JTextArea(10, 30);
        areaTextoPedido.setEditable(false);
        painelPedido.add(new JScrollPane(areaTextoPedido), BorderLayout.CENTER);

        rotuloTotal = new JLabel("Total (com imposto): R$0,00");
        painelPedido.add(rotuloTotal, BorderLayout.SOUTH);

        JButton botaoFinalizarPedido = new JButton("Finalizar Pedido");
        botaoFinalizarPedido.addActionListener(e -> finalizarPedido());
        painelPedido.add(botaoFinalizarPedido, BorderLayout.EAST);

        return painelPedido;
    }


    // Cria o painel de exibição do estoque.

    private JPanel criarPainelEstoque() {
        JPanel painelEstoque = new JPanel(new BorderLayout());
        areaTextoEstoque = new JTextArea(10, 30);
        areaTextoEstoque.setEditable(false);
        painelEstoque.add(new JScrollPane(areaTextoEstoque), BorderLayout.CENTER);
        atualizarExibicaoEstoque();
        return painelEstoque;
    }


    // Cria o painel de exibição das vendas.
    private JPanel criarPainelVendas() {
        JPanel painelVendas = new JPanel(new BorderLayout());
        areaTextoVendas = new JTextArea(10, 30);
        areaTextoVendas.setEditable(false);
        painelVendas.add(new JScrollPane(areaTextoVendas), BorderLayout.CENTER);
        atualizarExibicaoVendas();
        return painelVendas;
    }

    // Adiciona um item ao pedido atual.

    private void adicionarAoPedido(String item, double preco) {
        try {
            if (estoque.getOrDefault(item, 0) <= 0) {
                throw new ExcecaoForaDeEstoque(item);
            }
            areaTextoPedido.append(item + " - R$" + preco + "\n");
            total += preco;
            double totalComImposto = total * (1 + TAXA_IMPOSTO);
            rotuloTotal.setText("Total (com imposto): R$" + df.format(totalComImposto));
            estoque.put(item, estoque.get(item) - 1);
            atualizarExibicaoEstoque();
        } catch (ExcecaoForaDeEstoque e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Finaliza o pedido atual, atualizando vendas e estoque.

    private void finalizarPedido() {
        if (total > 0) {
            double totalComImposto = total * (1 + TAXA_IMPOSTO);
            String recibo = "Recibo:\n" + areaTextoPedido.getText() +
                    "Subtotal: R$" + df.format(total) + "\n" +
                    "Imposto: R$" + df.format(total * TAXA_IMPOSTO) + "\n" +
                    "Total: R$" + df.format(totalComImposto);
            JOptionPane.showMessageDialog(this, recibo, "Pedido Finalizado", JOptionPane.INFORMATION_MESSAGE);

            // Atualizar vendas
            for (String linha : areaTextoPedido.getText().split("\n")) {
                String item = linha.split(" - ")[0];
                vendas.put(item, vendas.getOrDefault(item, 0) + 1);
            }
            atualizarExibicaoVendas();
            salvarVendas();
            salvarEstoque();

            areaTextoPedido.setText("");
            total = 0.0;
            rotuloTotal.setText("Total (com imposto): R$0,00");
        } else {
            JOptionPane.showMessageDialog(this, "Não há itens no pedido!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Atualiza a exibição do estoque na interface.

    private void atualizarExibicaoEstoque() {
        areaTextoEstoque.setText("");
        for (Map.Entry<String, Integer> entrada : estoque.entrySet()) {
            areaTextoEstoque.append(entrada.getKey() + ": " + entrada.getValue() + "\n");
            if (entrada.getValue() <= 5) {
                areaTextoEstoque.append("ALERTA DE ESTOQUE BAIXO!\n");
            }
        }
    }


    // Atualiza a exibição das vendas na interface.

    private void atualizarExibicaoVendas() {
        areaTextoVendas.setText("");
        for (Map.Entry<String, Integer> entrada : vendas.entrySet()) {
            areaTextoVendas.append(entrada.getKey() + ": " + entrada.getValue() + " vendidos\n");
        }
    }


    // Reinicia o sistema, redefinindo estoque e vendas para os valores iniciais.
    private void reiniciarSistema() {
        // Reiniciar estoque
        for (String item : ITENS_MENU) {
            estoque.put(item, 10);
        }
        atualizarExibicaoEstoque();
        salvarEstoque();

        // Reiniciar vendas
        for (String item : ITENS_MENU) {
            vendas.put(item, 0);
        }
        atualizarExibicaoVendas();
        salvarVendas();

        // Reiniciar pedido
        areaTextoPedido.setText("");
        total = 0.0;
        rotuloTotal.setText("Total (com imposto): R$0,00");

        JOptionPane.showMessageDialog(this, "O sistema foi reiniciado. Todos os itens foram reabastecidos para 10 unidades e as vendas foram zeradas.", "Sistema Reiniciado", JOptionPane.INFORMATION_MESSAGE);
    }


    // Carrega os dados de estoque do arquivo ou inicializa com valores padrão.
    private void carregarEstoque() {
        File arquivo = new File("estoque.txt");
        if (!arquivo.exists()) {
            for (String item : ITENS_MENU) {
                estoque.put(item, 10);  // Inicializa com 10 unidades se o arquivo não existir
            }
            salvarEstoque();
        } else {
            try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    String[] partes = linha.split(":");
                    estoque.put(partes[0], Integer.parseInt(partes[1]));
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar estoque: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
        atualizarExibicaoEstoque();
    }


    // Salva os dados de estoque em um arquivo.

    private void salvarEstoque() {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter("estoque.txt"))) {
            for (Map.Entry<String, Integer> entrada : estoque.entrySet()) {
                escritor.write(entrada.getKey() + ":" + entrada.getValue() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar estoque: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Carrega os dados de vendas do arquivo ou inicializa com valores padrão.
    private void carregarVendas() {
        File arquivo = new File("vendas.txt");
        if (!arquivo.exists()) {
            for (String item : ITENS_MENU) {
                vendas.put(item, 0);  // Inicializa com 0 vendas se o arquivo não existir
            }
            salvarVendas();
        } else {
            try (BufferedReader leitor = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    String[] partes = linha.split(":");
                    vendas.put(partes[0], Integer.parseInt(partes[1]));
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar vendas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
        atualizarExibicaoVendas();
    }


     // Salva os dados de vendas em um arquivo.

    private void salvarVendas() {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter("vendas.txt"))) {
            for (Map.Entry<String, Integer> entrada : vendas.entrySet()) {
                escritor.write(entrada.getKey() + ":" + entrada.getValue() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar vendas: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

     // Exceção personalizada para itens fora de estoque.

    private static class ExcecaoForaDeEstoque extends Exception {
        public ExcecaoForaDeEstoque(String item) {
            super(item + " está fora de estoque!");
        }
    }


     // Método principal para iniciar o aplicativo.
    // Usamos o invokeLater para evitar problemas de sincronização entre a janela e o carregamento de dados
    public static void main(String[] args) {
        SwingUtilities.invokeLater(CafeSystem::new);
    }
}