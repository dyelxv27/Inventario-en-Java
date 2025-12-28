import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


//PRODUCTO

class Producto {
    String id;
    String nombre;
    String categoria;
    double precio;

    public Producto(String id, String nombre, String categoria, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
    }

    @Override
    public String toString() {
        return String.format("ID: %-8s | %-15s | %-10s | $%.2f", id, nombre, categoria, precio);
    }
}


//CLASE TABLA HASH

class TablaHash {
    private LinkedList<Producto>[] tabla;
    private int capacidad;
    private int numElementos;
    private long colisionesTotales;

    @SuppressWarnings("unchecked")
    public TablaHash(int maxItemsEsperados) {
        int tamanoCalculado = (int) (maxItemsEsperados * 1.33);
        this.capacidad = siguientePrimo(tamanoCalculado);
        this.tabla = new LinkedList[capacidad];
        this.numElementos = 0;
        this.colisionesTotales = 0;
        for (int i = 0; i < capacidad; i++) tabla[i] = new LinkedList<>();
    }

    private boolean esPrimo(int n) {
        if (n <= 1) return false;
        for (int i = 2; i * i <= n; i++) if (n % i == 0) return false;
        return true;
    }

    private int siguientePrimo(int n) {
        while (!esPrimo(n)) n++;
        return n;
    }

    private int funcionHash(String clave) {
        int hash = 0;
        int p = 31;
        for (int i = 0; i < clave.length(); i++) hash = (p * hash + clave.charAt(i)) % capacidad;
        return Math.abs(hash);
    }

    public void insertar(Producto p) {
        int indice = funcionHash(p.id);
        for (Producto prod : tabla[indice]) {
            if (prod.id.equals(p.id)) {
                prod.nombre = p.nombre;
                prod.categoria = p.categoria;
                prod.precio = p.precio;
                return;
            }
        }
        if (!tabla[indice].isEmpty()) colisionesTotales++;
        tabla[indice].add(p);
        numElementos++;
    }

    public Producto buscar(String id) {
        int indice = funcionHash(id);
        for (Producto p : tabla[indice]) if (p.id.equals(id)) return p;
        return null;
    }

    public boolean eliminar(String id) {
        int indice = funcionHash(id);
        for (Producto p : tabla[indice]) {
            if (p.id.equals(id)) {
                tabla[indice].remove(p);
                numElementos--;
                return true;
            }
        }
        return false;
    }

    public float obtenerFactorCarga() { return (float) numElementos / capacidad; }
    public long getColisiones() { return colisionesTotales; }
    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        for (LinkedList<Producto> bucket : tabla) lista.addAll(bucket);
        return lista;
    }
}


//CLASE PARA GRAFICAS

class PanelGrafica extends JPanel {
    private List<Integer> ejeX;
    private List<Double> ejeY1;
    private List<Double> ejeY2;
    private String tituloEjeX;
    private String tituloEjeY1;
    private String tituloEjeY2;
    private String tituloGrafica;


    public PanelGrafica(List<Integer> x, List<Double> y1, List<Double> y2, String tX, String tY1, String tY2, String titulo) {
        this.ejeX = x;
        this.ejeY1 = y1;
        this.ejeY2 = y2;
        this.tituloEjeX = tX;
        this.tituloEjeY1 = tY1;
        this.tituloEjeY2 = tY2;
        this.tituloGrafica = titulo;
        this.setPreferredSize(new Dimension(600, 400));
        this.setBackground(Color.WHITE);
    }


    public PanelGrafica(List<Integer> x, List<Double> y1, String tX, String tY1, String titulo) {
        this(x, y1, null, tX, tY1, null, titulo);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 60;
        int width = getWidth();
        int height = getHeight();


        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.BLACK);


        g2.drawLine(padding, height - padding, width - padding, height - padding);

        g2.setColor(Color.BLUE);
        g2.drawLine(padding, height - padding, padding, padding);

        if (ejeY2 != null) {
            g2.setColor(Color.RED);
            g2.drawLine(width - padding, height - padding, width - padding, padding);
        }


        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString(tituloGrafica, width / 2 - g2.getFontMetrics().stringWidth(tituloGrafica) / 2, padding / 2);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString(tituloEjeX, width / 2, height - 15);


        AffineTransform orig = g2.getTransform();
        g2.rotate(-Math.PI / 2);
        g2.setColor(Color.BLUE);
        g2.drawString(tituloEjeY1, -height / 2 - 30, padding - 40);

        if (ejeY2 != null) {
            g2.setColor(Color.RED);
            g2.drawString(tituloEjeY2, -height / 2 - 30, width - padding + 40);
        }
        g2.setTransform(orig);

        if (ejeX.isEmpty() || ejeY1.isEmpty()) return;


        double minX = ejeX.get(0);
        double maxX = ejeX.get(ejeX.size() - 1);
        double xScale = (double) (width - 2 * padding) / (maxX - minX + 1);


        double maxY1 = 0;
        for (double val : ejeY1) maxY1 = Math.max(maxY1, val);
        maxY1 = (maxY1 == 0) ? 1 : maxY1 * 1.1; // Evitar div/0
        double yScale1 = (double) (height - 2 * padding) / maxY1;


        double maxY2 = 0;
        double yScale2 = 0;
        if (ejeY2 != null) {
            for (double val : ejeY2) maxY2 = Math.max(maxY2, val);
            maxY2 = (maxY2 == 0) ? 1 : maxY2 * 1.1;
            yScale2 = (double) (height - 2 * padding) / maxY2;
        }


        drawSerie(g2, ejeX, ejeY1, minX, xScale, yScale1, padding, height, Color.BLUE);


        if (ejeY2 != null) {
            drawSerie(g2, ejeX, ejeY2, minX, xScale, yScale2, padding, height, Color.RED);
        }


        g2.setColor(Color.BLACK);
        g2.drawRect(width - 150, padding, 120, 50);
        g2.setColor(Color.BLUE);
        g2.drawString("■ " + tituloEjeY1, width - 140, padding + 20);
        if (ejeY2 != null) {
            g2.setColor(Color.RED);
            g2.drawString("■ " + tituloEjeY2, width - 140, padding + 40);
        }
    }

    private void drawSerie(Graphics2D g2, List<Integer> xData, List<Double> yData, double minX, double xScale, double yScale, int padding, int height, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));

        List<Point> points = new ArrayList<>();
        for (int i = 0; i < xData.size(); i++) {
            int x = (int) ((xData.get(i) - minX) * xScale + padding);
            int y = (int) ((height - padding) - (yData.get(i) * yScale));
            points.add(new Point(x, y));
        }

        for (int i = 0; i < points.size() - 1; i++) {
            g2.drawLine(points.get(i).x, points.get(i).y, points.get(i+1).x, points.get(i+1).y);
        }

        for (int i = 0; i < points.size(); i++) {
            g2.fillOval(points.get(i).x - 3, points.get(i).y - 3, 6, 6);
            g2.drawString(String.format("%.0f", yData.get(i)), points.get(i).x - 10, points.get(i).y - 10);
        }
    }
}


//CLASE PRINCIPAL

public class SistemaInventarioGUI extends JFrame {

    private TablaHash inventario;
    private JTextField txtId, txtNombre, txtCategoria, txtPrecio, txtBuscar;
    private JTextArea areaLog;
    private JTable tablaVisual;
    private DefaultTableModel modeloTabla;

    public SistemaInventarioGUI() {
        inventario = new TablaHash(100);

        setTitle("Sistema de Inventarios");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        JPanel panelNorte = new JPanel(new GridLayout(2, 1));
        JPanel panelCampos = new JPanel(new FlowLayout());

        txtId = new JTextField(8); setBorde(txtId, "ID");
        txtNombre = new JTextField(10); setBorde(txtNombre, "Nombre");
        txtCategoria = new JTextField(10); setBorde(txtCategoria, "Categoría");
        txtPrecio = new JTextField(8); setBorde(txtPrecio, "Precio");
        JButton btnInsertar = new JButton("Guardar / Actualizar");

        panelCampos.add(txtId); panelCampos.add(txtNombre);
        panelCampos.add(txtCategoria); panelCampos.add(txtPrecio);
        panelCampos.add(btnInsertar);

        JPanel panelAcciones = new JPanel(new FlowLayout());
        JButton btnCargar = new JButton("📂 Cargar Archivo");
        JButton btnTest = new JButton("📊 EJECUTAR");
        btnTest.setBackground(new Color(200, 255, 200));

        panelAcciones.add(btnCargar);
        panelAcciones.add(btnTest);

        panelNorte.add(panelCampos);
        panelNorte.add(panelAcciones);
        add(panelNorte, BorderLayout.NORTH);


        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("ID"); modeloTabla.addColumn("Nombre");
        modeloTabla.addColumn("Categoría"); modeloTabla.addColumn("Precio");
        tablaVisual = new JTable(modeloTabla);

        areaLog = new JTextArea();
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Consola de Resultados"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tablaVisual), scrollLog);
        split.setDividerLocation(350);
        add(split, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new FlowLayout());
        txtBuscar = new JTextField(12); setBorde(txtBuscar, "ID Operación");
        JButton btnBuscar = new JButton("🔍 Buscar");
        JButton btnEliminar = new JButton("🗑 Eliminar");

        panelSur.add(txtBuscar);
        panelSur.add(btnBuscar);
        panelSur.add(btnEliminar);
        add(panelSur, BorderLayout.SOUTH);


        btnInsertar.addActionListener(e -> accionInsertar());
        btnBuscar.addActionListener(e -> accionBuscar());
        btnEliminar.addActionListener(e -> accionEliminar());
        btnCargar.addActionListener(e -> accionCargarArchivo());
        btnTest.addActionListener(e -> new Thread(this::correrExperimentoOficial).start());
    }

    private void correrExperimentoOficial() {
        areaLog.setText("");
        log("INICIANDO");
        log("Calculando para N = {100, 200, 300, 500, 800, 1000}...");

        int[] cargas = {100, 200, 300, 500, 800, 1000};
        Random rand = new Random();

        List<Integer> listN = new ArrayList<>();
        List<Double> listColisiones = new ArrayList<>();
        List<Double> listTiempoIns = new ArrayList<>();
        List<Double> listTiempoBusq = new ArrayList<>();

        for (int n : cargas) {
            long tInsert = 0, tBusqOK = 0, colisiones = 0;

            for (int r = 0; r < 3; r++) {
                TablaHash tablaTemp = new TablaHash(n);
                String[] ids = new String[n];
                for(int i=0; i<n; i++) ids[i] = "K" + i + "_" + rand.nextInt(99999);

                // Insertar
                long t1 = System.nanoTime();
                for(int i=0; i<n; i++) tablaTemp.insertar(new Producto(ids[i], "P", "C", 1.0));
                tInsert += (System.nanoTime() - t1);
                colisiones += tablaTemp.getColisiones();

                // Buscar (25%)
                int cant25 = (int)(n * 0.25);
                t1 = System.nanoTime();
                for(int i=0; i<cant25; i++) tablaTemp.buscar(ids[i]);
                tBusqOK += (System.nanoTime() - t1);
            }

            // Guardar promedios
            listN.add(n);
            listColisiones.add((double)colisiones/3);
            listTiempoIns.add((double)tInsert/3 / 1000); // Convertir a microsegundos
            listTiempoBusq.add((double)tBusqOK/3 / 1000);

            log("N=" + n + " procesado.");
        }

        log("FINALIZADO. Abriendo graficas...");

        SwingUtilities.invokeLater(() -> {
            JFrame frameGraficas = new JFrame("Resultados: Colisiones y Tiempos");
            frameGraficas.setSize(950, 650);
            JTabbedPane tabs = new JTabbedPane();

            tabs.addTab("Colisiones vs Tiempo",
                    new PanelGrafica(listN, listColisiones, listTiempoIns,
                            "Cantidad de Elementos (N)", "Colisiones (Azul)", "Tiempo Inserción(Rojo)",
                            "Relación Carga: Colisiones vs Tiempo"));

            tabs.addTab("Tiempo de Búsqueda",
                    new PanelGrafica(listN, listTiempoBusq, "Cantidad de Elementos (N)", "Tiempo", "Tiempo de Búsqueda (25%)"));

            frameGraficas.add(tabs);
            frameGraficas.setVisible(true);
        });
    }

    private void accionInsertar() {
        try {
            String id = txtId.getText();
            if(id.isEmpty()) throw new Exception("ID vacío");
            inventario.insertar(new Producto(id, txtNombre.getText(), txtCategoria.getText(), Double.parseDouble(txtPrecio.getText())));
            actualizarTabla();
            limpiarCampos();
        } catch(Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }
    private void accionBuscar() {
        Producto p = inventario.buscar(txtBuscar.getText());
        if(p != null) JOptionPane.showMessageDialog(this, p.toString());
        else JOptionPane.showMessageDialog(this, "No encontrado");
    }
    private void accionEliminar() {
        if(inventario.eliminar(txtBuscar.getText())) actualizarTabla();
        else JOptionPane.showMessageDialog(this, "No encontrado");
    }
    private void accionCargarArchivo() {
        JFileChooser fc = new JFileChooser();
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
                String l;
                while((l = br.readLine()) != null) {
                    String[] d = l.split(",");
                    if(d.length >= 4) inventario.insertar(new Producto(d[0].trim(), d[1].trim(), d[2].trim(), Double.parseDouble(d[3].trim())));
                }
                actualizarTabla();
            } catch(IOException e) { JOptionPane.showMessageDialog(this, "Error archivo"); }
        }
    }
    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for(Producto p : inventario.obtenerTodos()) modeloTabla.addRow(new Object[]{p.id, p.nombre, p.categoria, p.precio});
    }
    private void limpiarCampos() { txtId.setText(""); txtNombre.setText(""); txtCategoria.setText(""); txtPrecio.setText(""); }
    private void setBorde(JTextField tf, String t) { tf.setBorder(BorderFactory.createTitledBorder(t)); }
    private void log(String m) { areaLog.append(m + "\n"); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SistemaInventarioGUI().setVisible(true));
    }
}