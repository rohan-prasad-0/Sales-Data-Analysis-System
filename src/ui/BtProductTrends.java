/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui;

import db.db;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PlotOrientation;
import java.io.*;
import java.awt.Desktop;
import javax.swing.JFileChooser;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 *
 * @author anom
 */
public class BtProductTrends extends javax.swing.JPanel {

    /**
     * Creates new form BtProductTrends
     */
    public BtProductTrends() {
        initComponents();
        setupTable();

        loadProductsToCombo();
        cmbRegion.addActionListener(e -> refreshData());
        cmbChartType.addActionListener(e -> loadChart());

        // Disable date pickers by default
        dchFrom.setEnabled(false);
        dchTo.setEnabled(false);

        // ComboBox listener
        cmbPeriod.addActionListener(e -> handlePeriodChange());

        // Date change listeners (only active for Custom Range)
        dchFrom.addPropertyChangeListener("date", evt -> {
            if ("Custom Range".equals(cmbPeriod.getSelectedItem().toString())) {
                refreshData();
            }
        });

        dchTo.addPropertyChangeListener("date", evt -> {
            if ("Custom Range".equals(cmbPeriod.getSelectedItem().toString())) {
                refreshData();
            }
        });

        refreshData();
    }

    /**
     * Handles period selection changes.
     */
    private void handlePeriodChange() {

        boolean custom = "Custom Range"
                .equals(cmbPeriod.getSelectedItem().toString());

        dchFrom.setEnabled(custom);
        dchTo.setEnabled(custom);

        refreshData();
    }

    /**
     * Refreshes table, summary cards and chart.
     */
    private void refreshData() {
        loadTopProducts();
        loadChart();
    }

    /**
     * Initializes non-editable table model.
     */
    private void setupTable() {

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Product Name", "Total Sold", "Revenue"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblProductTrends.setModel(model);
    }

    /**
     * Loads top products, calculates totals and growth.
     */
    /**
     * Loads top products, calculates totals and growth.
     */
    private void loadTopProducts() {

        String period = cmbPeriod.getSelectedItem().toString();
        String selectedProduct = cmbRegion.getSelectedItem().toString();

        StringBuilder query = new StringBuilder(
                "SELECT product_name, "
                + "SUM(quantity) AS total_sold, "
                + "SUM(total_price) AS revenue "
                + "FROM sales "
        );

        query.append(buildDateCondition(period));

        if (!selectedProduct.equals("All")) {

            if (period.equals("All Time")) {
                query.append(" WHERE product_name = ? ");
            } else {
                query.append(" AND product_name = ? ");
            }
        }

        query.append(" GROUP BY product_name ")
                .append(" ORDER BY total_sold DESC ")
                .append(" LIMIT 10");

        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(query.toString())) {

            if (!applyDateParameters(ps, period)) {
                return;
            }

            int paramIndex = period.equals("Custom Range") ? 3 : 1;

            if (!selectedProduct.equals("All")) {
                ps.setString(paramIndex, selectedProduct);
            }

            ResultSet rs = ps.executeQuery();

            DefaultTableModel model
                    = (DefaultTableModel) tblProductTrends.getModel();
            model.setRowCount(0);

            int totalSales = 0;
            double totalRevenue = 0;
            String topProduct = "-";
            boolean first = true;

            while (rs.next()) {

                String product = rs.getString("product_name");
                int sold = rs.getInt("total_sold");
                double revenue = rs.getDouble("revenue");

                model.addRow(new Object[]{product, sold, revenue});

                totalSales += sold;
                totalRevenue += revenue;

                if (first) {
                    topProduct = product;
                    first = false;
                }
            }

            lblTotalSaleValue.setText(String.valueOf(totalSales));
            lblRevenueValue.setText("Rs. " + String.format("%.2f", totalRevenue));
            lblTopProductValue.setText(topProduct);

            calculateGrowth(con, period, totalSales);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds WHERE condition based on selected period.
     */
    private String buildDateCondition(String period) {

        switch (period) {

            case "Today":
                return "WHERE DATE(date) = CURDATE() ";

            case "This Week":
                return "WHERE YEARWEEK(date,1)=YEARWEEK(CURDATE(),1) ";

            case "This Month":
                return "WHERE MONTH(date)=MONTH(CURDATE()) "
                        + "AND YEAR(date)=YEAR(CURDATE()) ";

            case "This Year":
                return "WHERE YEAR(date)=YEAR(CURDATE()) ";

            case "Custom Range":
                return "WHERE date BETWEEN ? AND ? ";

            default: // All Time
                return "";
        }
    }

    /**
     * Applies date parameters for Custom Range.
     */
    private boolean applyDateParameters(PreparedStatement ps, String period)
            throws Exception {

        if ("Custom Range".equals(period)) {

            if (dchFrom.getDate() == null || dchTo.getDate() == null) {
                return false; // dates not ready yet
            }

            ps.setDate(1, new java.sql.Date(dchFrom.getDate().getTime()));
            ps.setDate(2, new java.sql.Date(dchTo.getDate().getTime()));
        }

        return true;
    }

    /**
     * Calculates percentage growth compared to previous period.
     */
    private void calculateGrowth(Connection con, String period, int currentTotal) {

        double previousTotal = 0;
        String growthQuery = "";

        java.sql.Date prevFrom = null;
        java.sql.Date prevTo = null;

        switch (period) {

            case "Today":
                growthQuery = "SELECT SUM(quantity) FROM sales "
                        + "WHERE DATE(date)=CURDATE()-INTERVAL 1 DAY";
                break;

            case "This Week":
                growthQuery = "SELECT SUM(quantity) FROM sales "
                        + "WHERE YEARWEEK(date,1)=YEARWEEK(CURDATE(),1)-1";
                break;

            case "This Month":
                growthQuery = "SELECT SUM(quantity) FROM sales "
                        + "WHERE MONTH(date)=MONTH(CURDATE()-INTERVAL 1 MONTH) "
                        + "AND YEAR(date)=YEAR(CURDATE()-INTERVAL 1 MONTH)";
                break;

            case "This Year":
                growthQuery = "SELECT SUM(quantity) FROM sales "
                        + "WHERE YEAR(date)=YEAR(CURDATE())-1";
                break;

            case "Custom Range":

                if (dchFrom.getDate() == null || dchTo.getDate() == null) {
                    lblGrowthValue.setText("N/A");
                    lblGrowthValue.setForeground(new java.awt.Color(102, 102, 102));
                    return;
                }

                java.util.Date from = dchFrom.getDate();
                java.util.Date to = dchTo.getDate();

                long diff = to.getTime() - from.getTime();
                long days = (diff / (1000 * 60 * 60 * 24)) + 1;

                prevFrom = new java.sql.Date(from.getTime() - (days * 86400000));
                prevTo = new java.sql.Date(from.getTime() - 86400000);

                growthQuery = "SELECT SUM(quantity) FROM sales "
                        + "WHERE date BETWEEN ? AND ?";
                break;

            default:
                lblGrowthValue.setText("N/A");
                lblGrowthValue.setForeground(new java.awt.Color(102, 102, 102));
                return;
        }

        try (
                PreparedStatement ps = con.prepareStatement(growthQuery)) {

            if (period.equals("Custom Range")) {
                ps.setDate(1, prevFrom);
                ps.setDate(2, prevTo);
            }

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                previousTotal = rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        double growth = 0;

        if (previousTotal > 0) {
            growth = ((currentTotal - previousTotal) / previousTotal) * 100;
        }

        lblGrowthValue.setText(String.format("%.1f%%", growth));

        if (growth > 0) {
            lblGrowthValue.setForeground(new java.awt.Color(16, 185, 129));
        } else if (growth < 0) {
            lblGrowthValue.setForeground(new java.awt.Color(220, 38, 38));
        } else {
            lblGrowthValue.setForeground(new java.awt.Color(102, 102, 102));
        }
    }

    /**
     * Loads bar chart.
     */
    private void loadChart() {

        String period = cmbPeriod.getSelectedItem().toString();
        String selectedProduct = cmbRegion.getSelectedItem().toString();
        String chartType = cmbChartType.getSelectedItem().toString();

        try (Connection con = db.getConnection()) {

            JFreeChart chart = null;
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // ==================================================
            // ALL PRODUCTS
            // ==================================================
            if (selectedProduct.equals("All")) {

                String query
                        = "SELECT product_name, SUM(quantity) AS total_sold "
                        + "FROM sales "
                        + buildDateCondition(period)
                        + " GROUP BY product_name "
                        + " ORDER BY total_sold DESC LIMIT 5";

                PreparedStatement ps = con.prepareStatement(query);

                if (!applyDateParameters(ps, period)) {
                    return;
                }

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    dataset.addValue(
                            rs.getInt("total_sold"),
                            "Sales",
                            rs.getString("product_name")
                    );
                }

                if (chartType.equals("Bar Chart")) {

                    chart = ChartFactory.createBarChart(
                            "Top 5 Products",
                            "Product",
                            "Sales",
                            dataset,
                            PlotOrientation.VERTICAL,
                            false,
                            true,
                            false
                    );

                } else {

                    chart = ChartFactory.createLineChart(
                            "Top 5 Products",
                            "Product",
                            "Sales",
                            dataset
                    );
                }

            } // ==================================================
            // SINGLE PRODUCT MONTHLY
            // ==================================================
            else {

                String query
                        = "SELECT MONTH(date) AS month, SUM(quantity) AS total_sold "
                        + "FROM sales "
                        + "WHERE product_name = ? "
                        + "GROUP BY MONTH(date) "
                        + "ORDER BY MONTH(date)";

                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, selectedProduct);

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {

                    int month = rs.getInt("month");

                    String monthName = java.time.Month.of(month)
                            .name()
                            .substring(0, 3);

                    monthName = monthName.substring(0, 1)
                            + monthName.substring(1).toLowerCase();

                    dataset.addValue(
                            rs.getInt("total_sold"),
                            "Sales",
                            monthName
                    );
                }

                if (chartType.equals("Bar Chart")) {

                    chart = ChartFactory.createBarChart(
                            selectedProduct + " Monthly Trend",
                            "Month",
                            "Sales",
                            dataset
                    );

                } else {

                    chart = ChartFactory.createLineChart(
                            selectedProduct + " Monthly Trend",
                            "Month",
                            "Sales",
                            dataset
                    );
                }
            }

            ChartPanel cp = new ChartPanel(chart);

            chartPanel.removeAll();
            chartPanel.add(cp, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Product combo box
    private void loadProductsToCombo() {

        cmbRegion.removeAllItems();
        cmbRegion.addItem("All");

        try (Connection con = db.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT DISTINCT product_name FROM sales ORDER BY product_name"); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cmbRegion.addItem(rs.getString("product_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // gen report
    
    private void generatePDFReport() {
        OutputStream os = null;

        try {
            StringBuilder html = new StringBuilder();

            html.append("<html><head>");
            html.append("<style>");
            html.append("body{font-family:Arial;padding:20px;}");
            html.append("h1{color:#1e3a8a;text-align:center;}");
            html.append("h2{color:#1e3a8a;border-bottom:2px solid #3b82f6;padding-bottom:5px;}");
            html.append("table{width:100%;border-collapse:collapse;margin-top:15px;}");
            html.append("th,td{border:1px solid #ccc;padding:8px;text-align:left;}");
            html.append("th{background:#3b82f6;color:white;}");
            html.append(".card{border:1px solid #ccc;background:#f8f9fa;text-align:center;padding:15px;}");
            html.append(".title{font-weight:bold;color:#555;}");
            html.append(".value{font-size:20px;color:green;font-weight:bold;margin-top:8px;}");
            html.append("</style>");
            html.append("</head><body>");

            html.append("<h1>Product Trends Report</h1>");

            // Summary Cards
            html.append("<h2>Key Insights</h2>");
            html.append("<table>");
            html.append("<tr>");

            html.append("<td width='25%'><div class='card'>");
            html.append("<div class='title'>Total Sales</div>");
            html.append("<div class='value'>").append(lblTotalSaleValue.getText()).append("</div>");
            html.append("</div></td>");

            html.append("<td width='25%'><div class='card'>");
            html.append("<div class='title'>Total Revenue</div>");
            html.append("<div class='value'>").append(lblRevenueValue.getText()).append("</div>");
            html.append("</div></td>");

            html.append("<td width='25%'><div class='card'>");
            html.append("<div class='title'>Growth</div>");
            html.append("<div class='value'>").append(lblGrowthValue.getText()).append("</div>");
            html.append("</div></td>");

            html.append("<td width='25%'><div class='card'>");
            html.append("<div class='title'>Top Product</div>");
            html.append("<div class='value'>").append(lblTopProductValue.getText()).append("</div>");
            html.append("</div></td>");

            html.append("</tr>");
            html.append("</table>");

            // Data Table
            html.append("<h2>Detailed Analysis Data</h2>");
            html.append("<table>");

            DefaultTableModel model = (DefaultTableModel) tblProductTrends.getModel();

            html.append("<tr>");
            for (int i = 0; i < model.getColumnCount(); i++) {
                html.append("<th>").append(model.getColumnName(i)).append("</th>");
            }
            html.append("</tr>");

            for (int row = 0; row < model.getRowCount(); row++) {
                html.append("<tr>");
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    html.append("<td>")
                            .append(val == null ? "" : val.toString())
                            .append("</td>");
                }
                html.append("</tr>");
            }

            html.append("</table>");
            html.append("</body></html>");

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Product_Trends_Report.pdf"));

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                File file = chooser.getSelectedFile();

                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }

                os = new FileOutputStream(file);

                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(html.toString());
                renderer.layout();
                renderer.createPDF(os);

                os.close();

                JOptionPane.showMessageDialog(this, "PDF Report Generated!");
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        topContainer = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        filtersPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cmbPeriod = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        dchFrom = new com.toedter.calendar.JDateChooser();
        jLabel7 = new javax.swing.JLabel();
        dchTo = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        cmbRegion = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        cmbChartType = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        btnPrint = new javax.swing.JButton();
        summaryCardsPanel = new javax.swing.JPanel();
        cardTotalRevenue = new javax.swing.JPanel();
        lblTotalRevenueTitle = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblTotalSaleValue = new javax.swing.JLabel();
        cardTotalOrders = new javax.swing.JPanel();
        lblTotalRevenueTitle1 = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblRevenueValue = new javax.swing.JLabel();
        cardTopProduct = new javax.swing.JPanel();
        lblTotalRevenueTitle2 = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblGrowthValue = new javax.swing.JLabel();
        cardBestBranch = new javax.swing.JPanel();
        lblTopProduct = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblTopProductValue = new javax.swing.JLabel();
        chartPanel = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductTrends = new javax.swing.JTable();

        setPreferredSize(new java.awt.Dimension(940, 700));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(940, 700));
        jPanel1.setLayout(new java.awt.BorderLayout());

        topContainer.setLayout(new javax.swing.BoxLayout(topContainer, javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setBackground(new java.awt.Color(15, 23, 42));

        jLabel1.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(229, 231, 235));
        jLabel1.setText("Product Trends");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(391, 391, 391)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        topContainer.add(jPanel3);

        filtersPanel.setBackground(new java.awt.Color(102, 102, 102));
        filtersPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/filter.png"))); // NOI18N
        filtersPanel.add(jLabel2);

        jLabel3.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel3.setText("Period:");
        filtersPanel.add(jLabel3);

        cmbPeriod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Today", "This Week", "This Month", "This Year", "All Time", "Custom Range" }));
        filtersPanel.add(cmbPeriod);

        jLabel6.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel6.setText("From:");
        filtersPanel.add(jLabel6);
        filtersPanel.add(dchFrom);

        jLabel7.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel7.setText("To:");
        filtersPanel.add(jLabel7);
        filtersPanel.add(dchTo);

        jLabel4.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel4.setText("Product:");
        filtersPanel.add(jLabel4);

        cmbRegion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All" }));
        filtersPanel.add(cmbRegion);

        jLabel8.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel8.setText("Chart Type");
        filtersPanel.add(jLabel8);

        cmbChartType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Bar Chart", "Line Chart" }));
        filtersPanel.add(cmbChartType);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/print.png"))); // NOI18N
        filtersPanel.add(jLabel5);

        btnPrint.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        btnPrint.setText("Print");
        btnPrint.addActionListener(this::btnPrintActionPerformed);
        filtersPanel.add(btnPrint);

        topContainer.add(filtersPanel);

        summaryCardsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        summaryCardsPanel.setLayout(new java.awt.GridLayout(1, 4, 16, 0));

        cardTotalRevenue.setBackground(new java.awt.Color(59, 130, 246));
        cardTotalRevenue.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTotalRevenue.setLayout(new javax.swing.BoxLayout(cardTotalRevenue, javax.swing.BoxLayout.Y_AXIS));

        lblTotalRevenueTitle.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTotalRevenueTitle.setForeground(new java.awt.Color(102, 102, 102));
        lblTotalRevenueTitle.setText("Total Sales");
        cardTotalRevenue.add(lblTotalRevenueTitle);
        cardTotalRevenue.add(filler1);

        lblTotalSaleValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblTotalSaleValue.setForeground(new java.awt.Color(15, 23, 42));
        lblTotalSaleValue.setText("0");
        cardTotalRevenue.add(lblTotalSaleValue);

        summaryCardsPanel.add(cardTotalRevenue);

        cardTotalOrders.setBackground(new java.awt.Color(16, 185, 129));
        cardTotalOrders.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTotalOrders.setLayout(new javax.swing.BoxLayout(cardTotalOrders, javax.swing.BoxLayout.Y_AXIS));

        lblTotalRevenueTitle1.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTotalRevenueTitle1.setForeground(new java.awt.Color(102, 102, 102));
        lblTotalRevenueTitle1.setText("Total Revenue");
        cardTotalOrders.add(lblTotalRevenueTitle1);
        cardTotalOrders.add(filler2);

        lblRevenueValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblRevenueValue.setForeground(new java.awt.Color(15, 23, 42));
        lblRevenueValue.setText("Rs. 0");
        cardTotalOrders.add(lblRevenueValue);

        summaryCardsPanel.add(cardTotalOrders);

        cardTopProduct.setBackground(new java.awt.Color(139, 92, 246));
        cardTopProduct.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTopProduct.setLayout(new javax.swing.BoxLayout(cardTopProduct, javax.swing.BoxLayout.Y_AXIS));

        lblTotalRevenueTitle2.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTotalRevenueTitle2.setForeground(new java.awt.Color(102, 102, 102));
        lblTotalRevenueTitle2.setText("Growth");
        cardTopProduct.add(lblTotalRevenueTitle2);
        cardTopProduct.add(filler3);

        lblGrowthValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblGrowthValue.setForeground(new java.awt.Color(15, 23, 42));
        lblGrowthValue.setText("0%");
        cardTopProduct.add(lblGrowthValue);

        summaryCardsPanel.add(cardTopProduct);

        cardBestBranch.setBackground(new java.awt.Color(245, 158, 11));
        cardBestBranch.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardBestBranch.setLayout(new javax.swing.BoxLayout(cardBestBranch, javax.swing.BoxLayout.Y_AXIS));

        lblTopProduct.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTopProduct.setForeground(new java.awt.Color(102, 102, 102));
        lblTopProduct.setText("Top Product");
        cardBestBranch.add(lblTopProduct);
        cardBestBranch.add(filler4);

        lblTopProductValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblTopProductValue.setForeground(new java.awt.Color(15, 23, 42));
        lblTopProductValue.setText("Bread");
        cardBestBranch.add(lblTopProductValue);

        summaryCardsPanel.add(cardBestBranch);

        topContainer.add(summaryCardsPanel);

        jPanel1.add(topContainer, java.awt.BorderLayout.NORTH);

        chartPanel.setBackground(new java.awt.Color(255, 255, 255));
        chartPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setLayout(new java.awt.BorderLayout());
        jPanel1.add(chartPanel, java.awt.BorderLayout.CENTER);

        tablePanel.setPreferredSize(new java.awt.Dimension(940, 200));
        tablePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(456, 200));

        tblProductTrends.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Month", "Sales", "Revenue"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblProductTrends);

        tablePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(tablePanel, java.awt.BorderLayout.SOUTH);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed

        // PRINT BUTTON ACTION
        generatePDFReport();
    }//GEN-LAST:event_btnPrintActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPrint;
    private javax.swing.JPanel cardBestBranch;
    private javax.swing.JPanel cardTopProduct;
    private javax.swing.JPanel cardTotalOrders;
    private javax.swing.JPanel cardTotalRevenue;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JComboBox<String> cmbChartType;
    private javax.swing.JComboBox<String> cmbPeriod;
    private javax.swing.JComboBox<String> cmbRegion;
    private com.toedter.calendar.JDateChooser dchFrom;
    private com.toedter.calendar.JDateChooser dchTo;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JPanel filtersPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblGrowthValue;
    private javax.swing.JLabel lblRevenueValue;
    private javax.swing.JLabel lblTopProduct;
    private javax.swing.JLabel lblTopProductValue;
    private javax.swing.JLabel lblTotalRevenueTitle;
    private javax.swing.JLabel lblTotalRevenueTitle1;
    private javax.swing.JLabel lblTotalRevenueTitle2;
    private javax.swing.JLabel lblTotalSaleValue;
    private javax.swing.JPanel summaryCardsPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblProductTrends;
    private javax.swing.JPanel topContainer;
    // End of variables declaration//GEN-END:variables
}
