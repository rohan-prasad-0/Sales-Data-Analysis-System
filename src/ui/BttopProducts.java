/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import db.db;
import org.jfree.chart.plot.PlotOrientation;
import java.io.*;
import java.awt.Desktop;
import javax.swing.JFileChooser;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 *
 * @author anom
 */
public class BttopProducts extends javax.swing.JPanel {

    /**
     * Creates new form BttopProducts
     */
    public BttopProducts() {
        initComponents();
        loadRegions();

        cmbPeriod.setSelectedItem("All Time");

        // Disable custom date pickers initially
        dchFrom.setEnabled(false);
        dchTo.setEnabled(false);

        // Period filter listener
        cmbPeriod.addActionListener(e -> {

            boolean isCustom = cmbPeriod.getSelectedItem()
                    .toString().equals("Custom Range");

            dchFrom.setEnabled(isCustom);
            dchTo.setEnabled(isCustom);

            loadChart();
        });

        // Region filter listener
        cmbRegion.addActionListener(e -> loadChart());
        cmbChartType.addActionListener(e -> loadChart());

        // 🔥 IMPORTANT: Reload when dates change
        dchFrom.getDateEditor().addPropertyChangeListener("date", e -> {
            if (cmbPeriod.getSelectedItem().toString().equals("Custom Range")) {
                loadChart();
            }
        });

        dchTo.getDateEditor().addPropertyChangeListener("date", e -> {
            if (cmbPeriod.getSelectedItem().toString().equals("Custom Range")) {
                loadChart();
            }
        });

        loadChart();
    }

    // Load Top 10 Products Chart + Table
    private void loadChart() {

        // Dataset for JFreeChart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Get table model and clear existing rows
        DefaultTableModel model
                = (DefaultTableModel) tbltopsellingproduct.getModel();
        model.setRowCount(0);

        try (Connection con = db.getConnection()) {

            // Get selected filter values from UI
            String selectedPeriod = cmbPeriod.getSelectedItem().toString();
            String selectedRegion = cmbRegion.getSelectedItem().toString();
            String chartType = cmbChartType.getSelectedItem().toString();

            // Base SQL query
            StringBuilder sql = new StringBuilder("""
        SELECT product_id, product_name,
               SUM(quantity) AS total_quantity,
               SUM(total_price) AS total_revenue
        FROM sales
        WHERE 1=1
        """);

            // Apply region filter if not "All"
            if (!selectedRegion.equals("All")) {
                sql.append(" AND region = ? ");
            }

            // Apply date filter based on selected period
            switch (selectedPeriod) {

                case "Today" ->
                    sql.append(" AND DATE(date)=CURDATE() ");

                case "This Week" ->
                    sql.append(" AND YEARWEEK(date,1)=YEARWEEK(CURDATE(),1) ");

                case "This Month" -> {
                    sql.append(" AND MONTH(date)=MONTH(CURDATE()) ");
                    sql.append(" AND YEAR(date)=YEAR(CURDATE()) ");
                }

                case "This Year" ->
                    sql.append(" AND YEAR(date)=YEAR(CURDATE()) ");

                case "Custom Range" ->
                    sql.append(" AND DATE(date) BETWEEN ? AND ? ");
            }

            // Group, sort, and limit results to top 10 products
            sql.append("""
        GROUP BY product_id, product_name
        ORDER BY total_quantity DESC
        LIMIT 10
        """);

            // Prepare statement
            PreparedStatement pst = con.prepareStatement(sql.toString());

            int paramIndex = 1;

            // Set region parameter if needed
            if (!selectedRegion.equals("All")) {
                pst.setString(paramIndex++, selectedRegion);
            }

            // Handle custom date range parameters
            if (selectedPeriod.equals("Custom Range")) {

                java.util.Date fromDate = dchFrom.getDate();
                java.util.Date toDate = dchTo.getDate();

                // Validate dates
                if (fromDate == null || toDate == null) {
                    return;
                }

                if (fromDate.after(toDate)) {
                    JOptionPane.showMessageDialog(this,
                            "From date cannot be after To date");
                    return;
                }

                // Set date parameters
                pst.setDate(paramIndex++, new java.sql.Date(fromDate.getTime()));
                pst.setDate(paramIndex++, new java.sql.Date(toDate.getTime()));
            }

            // Execute query
            ResultSet rs = pst.executeQuery();

            int rank = 1;

            // Process result set
            while (rs.next()) {

                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                int totalQty = rs.getInt("total_quantity");
                double totalRevenue = rs.getDouble("total_revenue");

                // Add row to table
                model.addRow(new Object[]{
                    rank++,
                    productId,
                    productName,
                    totalQty,
                    totalRevenue
                });

                // Add data to chart dataset
                dataset.addValue(totalQty, "Quantity", productName);
            }

            JFreeChart chart;

            // Create chart based on selected type
            if (chartType.equals("Bar Chart")) {

                chart = ChartFactory.createBarChart(
                        "Top 10 Products",
                        "Product",
                        "Quantity Sold",
                        dataset,
                        PlotOrientation.VERTICAL,
                        false,
                        true,
                        false
                );

            } else {

                chart = ChartFactory.createLineChart(
                        "Top 10 Products",
                        "Product",
                        "Quantity Sold",
                        dataset
                );
            }

            // Create chart panel and enable zooming
            ChartPanel cp = new ChartPanel(chart);
            cp.setMouseWheelEnabled(true);

            // Refresh chart panel UI
            chartPanel.removeAll();
            chartPanel.add(cp, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();

        } catch (Exception e) {
            // Handle errors
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading data: " + e.getMessage());
        }
    }

    // get region from the database
    private void loadRegions() {

        try (Connection con = db.getConnection(); PreparedStatement pst = con.prepareStatement(
                "SELECT DISTINCT region FROM sales"); ResultSet rs = pst.executeQuery()) {

            cmbRegion.removeAllItems();
            cmbRegion.addItem("All");

            while (rs.next()) {
                cmbRegion.addItem(rs.getString("region"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading regions");
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
            html.append(".info{margin:10px 0;font-size:14px;color:#333;}");
            html.append("</style>");
            html.append("</head><body>");

            html.append("<h1>Top Selling Products Report</h1>");

            // Filter Details
            html.append("<h2>Selected Filters</h2>");
            html.append("<p class='info'><b>Period:</b> ")
                    .append(cmbPeriod.getSelectedItem().toString())
                    .append("</p>");

            html.append("<p class='info'><b>Region:</b> ")
                    .append(cmbRegion.getSelectedItem().toString())
                    .append("</p>");

            html.append("<p class='info'><b>Chart Type:</b> ")
                    .append(cmbChartType.getSelectedItem().toString())
                    .append("</p>");

            // Table Data
            html.append("<h2>Top 10 Product Details</h2>");
            html.append("<table>");

            DefaultTableModel model
                    = (DefaultTableModel) tbltopsellingproduct.getModel();

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
            chooser.setSelectedFile(new File("Top_Selling_Products_Report.pdf"));

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

        centerPanel = new javax.swing.JPanel();
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
        tablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbltopsellingproduct = new javax.swing.JTable();
        chartPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(940, 700));

        centerPanel.setLayout(new java.awt.BorderLayout());

        topContainer.setLayout(new javax.swing.BoxLayout(topContainer, javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setBackground(new java.awt.Color(15, 23, 42));

        jLabel1.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(229, 231, 235));
        jLabel1.setText("Top Selling Products ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(391, 391, 391)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        jLabel4.setText("Region:");
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

        centerPanel.add(topContainer, java.awt.BorderLayout.NORTH);

        tablePanel.setLayout(new java.awt.BorderLayout());

        tbltopsellingproduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Rank", "Product ID", "Product Name", "Total Quantity Sold", "Total Revenue"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tbltopsellingproduct);

        tablePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        centerPanel.add(tablePanel, java.awt.BorderLayout.CENTER);

        chartPanel.setPreferredSize(new java.awt.Dimension(0, 250));
        chartPanel.setLayout(new java.awt.BorderLayout());
        centerPanel.add(chartPanel, java.awt.BorderLayout.SOUTH);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(centerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(centerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        // TODO add your handling code here:

        generatePDFReport();

    }//GEN-LAST:event_btnPrintActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPrint;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JComboBox<String> cmbChartType;
    private javax.swing.JComboBox<String> cmbPeriod;
    private javax.swing.JComboBox<String> cmbRegion;
    private com.toedter.calendar.JDateChooser dchFrom;
    private com.toedter.calendar.JDateChooser dchTo;
    private javax.swing.JPanel filtersPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tbltopsellingproduct;
    private javax.swing.JPanel topContainer;
    // End of variables declaration//GEN-END:variables
}
