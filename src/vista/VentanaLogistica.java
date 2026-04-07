package vista;

import modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class VentanaLogistica extends JFrame {

    private final Logistica logistica = new Logistica();

    // Campos del formulario
    private JTextField txtNumero, txtCliente, txtPeso, txtDistancia;
    private JComboBox<String> cmbTipo;
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private static final NumberFormat FMT =
        NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public VentanaLogistica() {
        setTitle("Operador Logístico");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout(8, 8));
        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(crearPanelTabla(),     BorderLayout.CENTER);

        cargarDatosDemostracion();
    }

    // ── Formulario superior ───────────────────────────────────────────
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 4, 10),
            BorderFactory.createTitledBorder("Datos del envío")));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        txtNumero    = new JTextField(10);
        txtCliente   = new JTextField(18);
        txtPeso      = new JTextField(8);
        txtDistancia = new JTextField(8);
        cmbTipo      = new JComboBox<>(new String[]{"Terrestre", "Aéreo", "Marítimo"});

        // Fila 0
        g.gridx=0; g.gridy=0; panel.add(new JLabel("Número:"), g);
        g.gridx=1;             panel.add(txtNumero, g);
        g.gridx=2;             panel.add(new JLabel("Tipo:"), g);
        g.gridx=3;             panel.add(cmbTipo, g);

        // Fila 1
        g.gridx=0; g.gridy=1; panel.add(new JLabel("Cliente:"), g);
        g.gridx=1; g.gridwidth=1; panel.add(txtCliente, g);
        g.gridx=2;             panel.add(new JLabel("Distancia Km:"), g);
        g.gridx=3;             panel.add(txtDistancia, g);

        // Fila 2
        g.gridx=0; g.gridy=2; panel.add(new JLabel("Peso (kg):"), g);
        g.gridx=1;             panel.add(txtPeso, g);

        // Botones
        JButton btnGuardar   = new JButton("💾 Guardar");
        JButton btnRetirar   = new JButton("🗑 Retirar");
        JButton btnLimpiar   = new JButton("✖ Cancelar");

        btnGuardar.setBackground(new Color(34, 139, 34));
        btnGuardar.setForeground(Color.WHITE);
        btnRetirar.setBackground(new Color(180, 40, 40));
        btnRetirar.setForeground(Color.WHITE);

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelBtns.add(btnGuardar);
        panelBtns.add(btnRetirar);
        panelBtns.add(btnLimpiar);

        g.gridx=1; g.gridy=3; g.gridwidth=3; panel.add(panelBtns, g);

        btnGuardar.addActionListener(e -> guardar());
        btnRetirar.addActionListener(e -> retirar());
        btnLimpiar.addActionListener(e -> limpiar());

        return panel;
    }

    // ── Tabla inferior ────────────────────────────────────────────────
    private JScrollPane crearPanelTabla() {
        String[] columnas = {"Tipo", "Código", "Cliente", "Peso (kg)", "Distancia (km)", "Costo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int fila = tabla.getSelectedRow();
                txtNumero.setText(modeloTabla.getValueAt(fila, 1).toString());
                txtCliente.setText(modeloTabla.getValueAt(fila, 2).toString());
                txtPeso.setText(modeloTabla.getValueAt(fila, 3).toString());
                txtDistancia.setText(modeloTabla.getValueAt(fila, 4).toString());
                String tipo = modeloTabla.getValueAt(fila, 0).toString();
                cmbTipo.setSelectedItem(tipo);
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 10, 10, 10),
            BorderFactory.createTitledBorder("Listado de envíos")));
        return scroll;
    }

    // ── Lógica de botones ────────────────────────────────────────────
    private void guardar() {
        try {
            String codigo    = txtNumero.getText().trim();
            String cliente   = txtCliente.getText().trim();
            String pesoStr   = txtPeso.getText().trim();
            String distStr   = txtDistancia.getText().trim();

            if (codigo.isEmpty() || cliente.isEmpty() || pesoStr.isEmpty() || distStr.isEmpty()) {
                mostrarError("Todos los campos son obligatorios.");
                return;
            }
            if (logistica.existeCodigo(codigo)) {
                mostrarError("Ya existe un envío con el código: " + codigo);
                return;
            }

            double peso      = Double.parseDouble(pesoStr);
            double distancia = Double.parseDouble(distStr);
            String tipo      = cmbTipo.getSelectedItem().toString();

            Envio envio = switch (tipo) {
                case "Aéreo"    -> new Aereo(codigo, cliente, peso, distancia);
                case "Marítimo" -> new Maritimo(codigo, cliente, peso, distancia);
                default          -> new Terrestre(codigo, cliente, peso, distancia);
            };

            logistica.agregar(envio);
            actualizarTabla();
            limpiar();
            JOptionPane.showMessageDialog(this,
                "✅ Envío " + codigo + " guardado.\nCosto: " + FMT.format(envio.calcularTarifa()),
                "Guardado", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            mostrarError("Peso y distancia deben ser valores numéricos.");
        }
    }

    private void retirar() {
        String codigo = txtNumero.getText().trim();
        if (codigo.isEmpty()) {
            mostrarError("Ingrese el código del envío a retirar.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Retirar el envío con código: " + codigo + "?",
            "Confirmar retiro", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (logistica.retirar(codigo)) {
                actualizarTabla();
                limpiar();
                JOptionPane.showMessageDialog(this, "✅ Envío retirado correctamente.");
            } else {
                mostrarError("No se encontró ningún envío con código: " + codigo);
            }
        }
    }

    private void limpiar() {
        txtNumero.setText("");
        txtCliente.setText("");
        txtPeso.setText("");
        txtDistancia.setText("");
        cmbTipo.setSelectedIndex(0);
        tabla.clearSelection();
        txtNumero.requestFocus();
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for (Envio e : logistica.listar()) {
            modeloTabla.addRow(new Object[]{
                e.getTipo(),
                e.getCodigo(),
                e.getCliente(),
                e.getPeso(),
                e.getDistancia(),
                FMT.format(e.calcularTarifa())
            });
        }
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Datos de demostración (como en la imagen del examen) ──────────
    private void cargarDatosDemostracion() {
        logistica.agregar(new Terrestre("10001", "Polimeros Col.", 1200, 400));
        logistica.agregar(new Terrestre("10002", "Textiles Pepalfa", 500, 600));
        logistica.agregar(new Aereo("10003",    "Flores Colombia", 1500, 2000));
        actualizarTabla();
    }

    // ── Main ──────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new VentanaLogistica().setVisible(true);
        });
    }
}
