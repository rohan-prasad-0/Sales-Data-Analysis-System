/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import db.db;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.awt.BorderLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import java.io.*;
import java.awt.Desktop;
import javax.swing.JFileChooser;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 *
 * @author anom
 */
public class BtRegionalSales extends javax.swing.JPanel {

    /**
     * Creates new form BtRegionalSales
     */
    public BtRegionalSales() {
        initComponents();

        loadRegions();
        setupFilters();
        
        dchFrom.setEnabled(false);
        dchTo.setEnabled(false);
        
        loadRegionalSalesData();

    }
    
    // chart
    
    private void loadChart() {

        try (Connection con = db.getConnection()) {

            String selectedRegion = cmbRegion.getSelectedItem().toString();
            String selectedPeriod = cmbPeriod.getSelectedItem().toString();

            LocalDate today = LocalDate.now();
            LocalDate startDate;
            LocalDate endDate;

            switch (selectedPeriod) {

                case "Today" -> {
                    startDate = today;
                    endDate = today;
                }

                case "This Week" -> {
                    startDate = today.with(java.time.DayOfWeek.MONDAY);
                    endDate = today;
                }

                case "This Month" -> {
                    startDate = today.withDayOfMonth(1);
                    endDate = today;
                }

                case "This Year" -> {
                    startDate = today.withDayOfYear(1);
                    endDate = today;
                }

                case "All Time" -> {
                    startDate = LocalDate.of(2000, 1, 1);
                    endDate = today;
                }

                case "Custom Range" -> {

                    Date fromDate = dchFrom.getDate();
                    Date toDate = dchTo.getDate();

                    if (fromDate == null || toDate == null) {
                        return;
                    }

                    startDate = fromDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    endDate = toDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }

                default -> {
                    return;
                }
            }

            String sql = """
            SELECT region, SUM(total_price) AS revenue
            FROM sales
            WHERE DATE(date) BETWEEN ? AND ?
        """;

            if (!selectedRegion.equals("All")) {
                sql += " AND region = ?";
            }

            sql += " GROUP BY region ORDER BY revenue DESC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());

            if (!selectedRegion.equals("All")) {
                ps.setString(3, selectedRegion);
            }

            ResultSet rs = ps.executeQuery();

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            while (rs.next()) {
                dataset.addValue(
                        rs.getDouble("revenue"),
                        "Revenue",
                        rs.getString("region")
                );
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Regional Revenue",
                    "Region",
                    "Revenue",
                    dataset
            );

            ChartPanel cp = new ChartPanel(chart);

            chartPanel.removeAll();
            chartPanel.setLayout(new BorderLayout());
            chartPanel.add(cp, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates summary metrics such as total revenue and overall growth.
     */
    private void updateSummaryCards() {

        try (Connection con = db.getConnection()) {

            String selectedRegion = cmbRegion.getSelectedItem().toString();

            String regionCondition
                    = selectedRegion.equals("All")
                    ? ""
                    : "WHERE region = '" + selectedRegion + "'";

            String sql = """
                SELECT 
                    region,
                    SUM(total_price) AS revenue
                FROM sales
                %s
                GROUP BY region
                ORDER BY revenue DESC
            """.formatted(regionCondition);

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            String topRegion = "-";
            String lowestRegion = "-";
            double highestRevenue = 0;
            double lowestRevenue = Double.MAX_VALUE;
            int regionCount = 0;

            while (rs.next()) {

                String region = rs.getString("region");
                double revenue = rs.getDouble("revenue");

                if (regionCount == 0) {
                    topRegion = region;
                    highestRevenue = revenue;
                }

                if (revenue < lowestRevenue) {
                    lowestRevenue = revenue;
                    lowestRegion = region;
                }

                regionCount++;
            }

            // Update UI labels
            lblTopRegionValue.setText(topRegion);
            lblHighestRevenueValue.setText(String.format("Rs. %.2f", highestRevenue));
            lblActiveRegionsValue.setText(String.valueOf(regionCount));
            lblLowestPerformingRegionValue.setText(lowestRegion);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads distinct regions into the region filter combo box.
     */
    private void loadRegions() {

        try (Connection con = db.getConnection()) {

            cmbRegion.removeAllItems();
            cmbRegion.addItem("All");

            String sql = "SELECT DISTINCT region FROM sales ORDER BY region";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cmbRegion.addItem(rs.getString("region"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Attaches listeners to filters to reload data dynamically.
     */
    private void setupFilters() {

        cmbPeriod.addActionListener(e -> {

            boolean custom = cmbPeriod.getSelectedItem()
                    .toString()
                    .equals("Custom Range");

            dchFrom.setEnabled(custom);
            dchTo.setEnabled(custom);

            loadRegionalSalesData();
        });

        cmbRegion.addActionListener(e -> loadRegionalSalesData());

        dchFrom.getDateEditor().addPropertyChangeListener("date", e -> {
            if (cmbPeriod.getSelectedItem().toString().equals("Custom Range")) {
                loadRegionalSalesData();
            }
        });

        dchTo.getDateEditor().addPropertyChangeListener("date", e -> {
            if (cmbPeriod.getSelectedItem().toString().equals("Custom Range")) {
                loadRegionalSalesData();
            }
        });
    }

    /**
     * Loads regional revenue data and calculates period-over-period growth.
     *
     * Growth Logic: - Predefined periods compare with previous equivalent
     * period. - Custom Range compares with previous equal-length date range.
     */
    private void loadRegionalSalesData() {

        String selectedPeriod = cmbPeriod.getSelectedItem().toString();
        String selectedRegion = cmbRegion.getSelectedItem().toString();

        DefaultTableModel model = (DefaultTableModel) tblRegionalSales.getModel();
        model.setRowCount(0);

        try (Connection con = db.getConnection()) {

            LocalDate today = LocalDate.now();

            LocalDate currentStart;
            LocalDate currentEnd;
            LocalDate previousStart;
            LocalDate previousEnd;

            switch (selectedPeriod) {

                case "Today" -> {
                    currentStart = today;
                    currentEnd = today;

                    previousStart = today.minusDays(1);
                    previousEnd = today.minusDays(1);
                }

                case "This Week" -> {
                    currentStart = today.with(java.time.DayOfWeek.MONDAY);
                    currentEnd = today;

                    previousStart = currentStart.minusWeeks(1);
                    previousEnd = currentEnd.minusWeeks(1);
                }

                case "This Month" -> {
                    currentStart = today.withDayOfMonth(1);
                    currentEnd = today;

                    previousStart = currentStart.minusMonths(1);
                    previousEnd = previousStart.withDayOfMonth(previousStart.lengthOfMonth());
                }

                case "This Year" -> {
                    currentStart = today.withDayOfYear(1);
                    currentEnd = today;

                    previousStart = currentStart.minusYears(1);
                    previousEnd = previousStart.withDayOfYear(previousStart.lengthOfYear());
                }

                case "All Time" -> {
                    currentStart = LocalDate.of(2000, 1, 1);
                    currentEnd = today;

                    previousStart = LocalDate.of(1900, 1, 1);
                    previousEnd = LocalDate.of(1999, 12, 31);
                }

                case "Custom Range" -> {

                    Date fromDate = dchFrom.getDate();
                    Date toDate = dchTo.getDate();

                    if (fromDate == null || toDate == null) {
                        return;
                    }

                    currentStart = fromDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    currentEnd = toDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    long days = ChronoUnit.DAYS.between(currentStart, currentEnd) + 1;

                    previousEnd = currentStart.minusDays(1);
                    previousStart = previousEnd.minusDays(days - 1);
                }

                default -> {
                    return;
                }
            }

            String currentCondition
                    = "DATE(date) BETWEEN '" + currentStart + "' AND '" + currentEnd + "'";

            String previousCondition
                    = "DATE(date) BETWEEN '" + previousStart + "' AND '" + previousEnd + "'";

            String regionCondition
                    = selectedRegion.equals("All")
                    ? ""
                    : " AND region = '" + selectedRegion + "'";

            String sql = """
                SELECT region,
                       SUM(CASE WHEN %s THEN total_price ELSE 0 END) AS current_revenue,
                       SUM(CASE WHEN %s THEN total_price ELSE 0 END) AS previous_revenue
                FROM sales
                WHERE 1=1 %s
                GROUP BY region
                ORDER BY current_revenue DESC
            """.formatted(currentCondition, previousCondition, regionCondition);

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String region = rs.getString("region");
                double currentRevenue = rs.getDouble("current_revenue");
                double previousRevenue = rs.getDouble("previous_revenue");

                double growth;

                if (previousRevenue == 0 && currentRevenue > 0) {
                    growth = 100.0;
                } else if (previousRevenue == 0) {
                    growth = 0.0;
                } else {
                    growth = ((currentRevenue - previousRevenue) / previousRevenue) * 100.0;
                }

                model.addRow(new Object[]{
                    region,
                    currentRevenue,
                    previousRevenue,
                    String.format("%.2f %%", growth)
                });
            }

            updateSummaryCards();
            loadChart();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load regional sales data.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
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

            html.append("<h1>Regional Sales Report</h1>");

            // Summary Cards
            html.append("<h2>Key Insights</h2>");
            html.append("<table>");
            html.append("<tr>");

            html.append("<td width='25%'><div class='card'>");
            html.append("<div class='title'>Top Region</div>");
            html.append("<div class='value'>")
                    .append(lblTopRegionValue.getText())
                    .append("</div>");
            html.append("</div></td>");

            html.append("<td width='25%'><div class='card'>");
            html.append("<div class='title'>Highest Revenue</div>");
            html.append("<div class='value'>")
                    .append(lblHighestRevenueValue.getText())
                    .append("</div>");
            html.append("</div></td>");

            html.append("<td width='25%'><div class='card'>");
            html.append("<div class='title'>Active Regions</div>");
            html.append("<div class='value'>")
                    .append(lblActiveRegionsValue.getText())
                    .append("</div>");
            html.append("</div></td>");

            html.append("<td width='25%'><div class='card'>");
            html.append("<div class='title'>Lowest Region</div>");
            html.append("<div class='value'>")
                    .append(lblLowestPerformingRegionValue.getText())
                    .append("</div>");
            html.append("</div></td>");

            html.append("</tr>");
            html.append("</table>");

            // Data Table
            html.append("<h2>Detailed Regional Sales Data</h2>");
            html.append("<table>");

            DefaultTableModel model
                    = (DefaultTableModel) tblRegionalSales.getModel();

            html.append("<tr>");
            for (int i = 0; i < model.getColumnCount(); i++) {
                html.append("<th>")
                        .append(model.getColumnName(i))
                        .append("</th>");
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
            chooser.setSelectedFile(new File("Regional_Sales_Report.pdf"));

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

                JOptionPane.showMessageDialog(this,
                        "PDF Report Generated!");

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
        jLabel5 = new javax.swing.JLabel();
        btnPrint = new javax.swing.JButton();
        summaryCardsPanel = new javax.swing.JPanel();
        cardTotalRevenue = new javax.swing.JPanel();
        lblTopRegion = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblTopRegionValue = new javax.swing.JLabel();
        cardTotalOrders = new javax.swing.JPanel();
        lblHighestRevenue = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblHighestRevenueValue = new javax.swing.JLabel();
        cardTopProduct = new javax.swing.JPanel();
        lblActiveRegions = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblActiveRegionsValue = new javax.swing.JLabel();
        cardBestBranch = new javax.swing.JPanel();
        lblLowestPerformingRegion = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblLowestPerformingRegionValue = new javax.swing.JLabel();
        chartPanel = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRegionalSales = new javax.swing.JTable();

        setPreferredSize(new java.awt.Dimension(940, 700));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(940, 700));
        jPanel1.setLayout(new java.awt.BorderLayout());

        topContainer.setLayout(new javax.swing.BoxLayout(topContainer, javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setBackground(new java.awt.Color(15, 23, 42));

        jLabel1.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(229, 231, 235));
        jLabel1.setText("Regional Sales");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(391, 391, 391)
                .addComponent(jLabel1)
                .addContainerGap(451, Short.MAX_VALUE))
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
        jLabel4.setText("Region:");
        filtersPanel.add(jLabel4);

        cmbRegion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All" }));
        filtersPanel.add(cmbRegion);

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

        lblTopRegion.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTopRegion.setForeground(new java.awt.Color(102, 102, 102));
        lblTopRegion.setText("Top Region");
        cardTotalRevenue.add(lblTopRegion);
        cardTotalRevenue.add(filler1);

        lblTopRegionValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblTopRegionValue.setForeground(new java.awt.Color(15, 23, 42));
        lblTopRegionValue.setText("0");
        cardTotalRevenue.add(lblTopRegionValue);

        summaryCardsPanel.add(cardTotalRevenue);

        cardTotalOrders.setBackground(new java.awt.Color(16, 185, 129));
        cardTotalOrders.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTotalOrders.setLayout(new javax.swing.BoxLayout(cardTotalOrders, javax.swing.BoxLayout.Y_AXIS));

        lblHighestRevenue.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblHighestRevenue.setForeground(new java.awt.Color(102, 102, 102));
        lblHighestRevenue.setText("Highest Revenue");
        cardTotalOrders.add(lblHighestRevenue);
        cardTotalOrders.add(filler2);

        lblHighestRevenueValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblHighestRevenueValue.setForeground(new java.awt.Color(15, 23, 42));
        lblHighestRevenueValue.setText("Rs. 0");
        cardTotalOrders.add(lblHighestRevenueValue);

        summaryCardsPanel.add(cardTotalOrders);

        cardTopProduct.setBackground(new java.awt.Color(139, 92, 246));
        cardTopProduct.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTopProduct.setLayout(new javax.swing.BoxLayout(cardTopProduct, javax.swing.BoxLayout.Y_AXIS));

        lblActiveRegions.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblActiveRegions.setForeground(new java.awt.Color(102, 102, 102));
        lblActiveRegions.setText("Active Regions");
        cardTopProduct.add(lblActiveRegions);
        cardTopProduct.add(filler3);

        lblActiveRegionsValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblActiveRegionsValue.setForeground(new java.awt.Color(15, 23, 42));
        lblActiveRegionsValue.setText("5");
        cardTopProduct.add(lblActiveRegionsValue);

        summaryCardsPanel.add(cardTopProduct);

        cardBestBranch.setBackground(new java.awt.Color(245, 158, 11));
        cardBestBranch.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardBestBranch.setLayout(new javax.swing.BoxLayout(cardBestBranch, javax.swing.BoxLayout.Y_AXIS));

        lblLowestPerformingRegion.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblLowestPerformingRegion.setForeground(new java.awt.Color(102, 102, 102));
        lblLowestPerformingRegion.setText("Lowest Performing Region");
        cardBestBranch.add(lblLowestPerformingRegion);
        cardBestBranch.add(filler4);

        lblLowestPerformingRegionValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblLowestPerformingRegionValue.setForeground(new java.awt.Color(15, 23, 42));
        lblLowestPerformingRegionValue.setText("North");
        cardBestBranch.add(lblLowestPerformingRegionValue);

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

        tblRegionalSales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Region", "Current Revenue", "Previous Revenue", "Growth"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblRegionalSales);

        tablePanel.add(jScrollPane1, java.awt.BorderLayout.SOUTH);

        jPanel1.add(tablePanel, java.awt.BorderLayout.SOUTH);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        // TODO add your handling code here:

        generatePDFReport();
    }//GEN-LAST:event_btnPrintActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPrint;
    private javax.swing.JPanel cardBestBranch;
    private javax.swing.JPanel cardTopProduct;
    private javax.swing.JPanel cardTotalOrders;
    private javax.swing.JPanel cardTotalRevenue;
    private javax.swing.JPanel chartPanel;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblActiveRegions;
    private javax.swing.JLabel lblActiveRegionsValue;
    private javax.swing.JLabel lblHighestRevenue;
    private javax.swing.JLabel lblHighestRevenueValue;
    private javax.swing.JLabel lblLowestPerformingRegion;
    private javax.swing.JLabel lblLowestPerformingRegionValue;
    private javax.swing.JLabel lblTopRegion;
    private javax.swing.JLabel lblTopRegionValue;
    private javax.swing.JPanel summaryCardsPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblRegionalSales;
    private javax.swing.JPanel topContainer;
    // End of variables declaration//GEN-END:variables
}
