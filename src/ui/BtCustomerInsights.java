/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui;

import javax.swing.JOptionPane;
import db.db;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.io.*;
import java.awt.Desktop;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.extend.UserAgentCallback;

/**
 *
 * @author anom
 */
public class BtCustomerInsights extends javax.swing.JPanel {

    /**
     * Creates new form BtCustomerInsights
     */
    public BtCustomerInsights() {
        initComponents();
        setupTable();
        loadRegions();
        addEvents();
        refreshData();
    }

    // table
    private void setupTable() {

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Customer ID", "Orders", "Revenue", "Avg Order"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCustomerInsights.setModel(model);
    }

    private void refreshData() {
        loadSummary();
        loadTable();
        loadChart();
    }

    // day choose
    private String getDateFilter() {

        String period = cmbPeriod.getSelectedItem().toString();

        switch (period) {

            case "Today":
                return "WHERE date = CURDATE()";

            case "This Week":
                return "WHERE YEARWEEK(date,1)=YEARWEEK(CURDATE(),1)";

            case "This Month":
                return "WHERE MONTH(date)=MONTH(CURDATE()) AND YEAR(date)=YEAR(CURDATE())";

            case "This Year":
                return "WHERE YEAR(date)=YEAR(CURDATE())";

            case "Custom Range":

                if (jDateChooser1.getDate() != null && jDateChooser2.getDate() != null) {

                    java.text.SimpleDateFormat sdf
                            = new java.text.SimpleDateFormat("yyyy-MM-dd");

                    String from = sdf.format(jDateChooser1.getDate());
                    String to = sdf.format(jDateChooser2.getDate());

                    return "WHERE date BETWEEN '" + from + "' AND '" + to + "'";
                }

                return "";

            default:
                return "";
        }
    }

    // day chooser event
    private void addEvents() {

        toggleDateFields();

        cmbPeriod.addActionListener(e -> {
            toggleDateFields();
            refreshData();
        });

        cmbRegion.addActionListener(e -> refreshData());
        cmbChartType.addActionListener(e -> loadChart());

        jDateChooser1.addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                refreshData();
            }
        });

        jDateChooser2.addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                refreshData();
            }
        });
    }

    // desable jchooser
    private void toggleDateFields() {

        boolean custom = cmbPeriod.getSelectedItem()
                .toString()
                .equals("Custom Range");

        jDateChooser1.setEnabled(custom);
        jDateChooser2.setEnabled(custom);

        if (!custom) {
            jDateChooser1.setDate(null);
            jDateChooser2.setDate(null);
        }
    }

    // load regions
    private void loadRegions() {

        cmbRegion.removeAllItems();
        cmbRegion.addItem("All");

        try (Connection con = db.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT DISTINCT region FROM sales"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                cmbRegion.addItem(rs.getString("region"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // filters
    private String getWhereClause() {

        String where = " WHERE 1=1 ";

        String period = cmbPeriod.getSelectedItem().toString();
        String region = cmbRegion.getSelectedItem().toString();

        if (period.equals("Today")) {
            where += " AND date = CURDATE() ";
        } else if (period.equals("This Week")) {
            where += " AND YEARWEEK(date,1)=YEARWEEK(CURDATE(),1) ";
        } else if (period.equals("This Month")) {
            where += " AND MONTH(date)=MONTH(CURDATE()) AND YEAR(date)=YEAR(CURDATE()) ";
        } else if (period.equals("This Year")) {
            where += " AND YEAR(date)=YEAR(CURDATE()) ";
        } else if (period.equals("Custom Range")) {

            if (jDateChooser1.getDate() != null && jDateChooser2.getDate() != null) {

                java.text.SimpleDateFormat sdf
                        = new java.text.SimpleDateFormat("yyyy-MM-dd");

                String from = sdf.format(jDateChooser1.getDate());
                String to = sdf.format(jDateChooser2.getDate());

                where += " AND date BETWEEN '" + from + "' AND '" + to + "' ";
            }
        }

        if (!region.equals("All")) {
            where += " AND region='" + region + "' ";
        }

        return where;
    }

    // summery cards
    private void loadSummary() {

        try (Connection con = db.getConnection()) {

            String where = getWhereClause();

            // Total Customers
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT COUNT(DISTINCT customer_id) FROM sales " + where
            );
            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {
                lblTotalCustomersValue.setText(rs1.getString(1));
            }

            // Avg Spending
            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT AVG(total_spend) FROM ("
                    + "SELECT SUM(total_price) total_spend FROM sales "
                    + where
                    + " GROUP BY customer_id) x"
            );
            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {
                lblAvgSpendingValue.setText(
                        "Rs. " + String.format("%.2f", rs2.getDouble(1))
                );
            }

            // New Customers
            PreparedStatement ps3 = con.prepareStatement(
                    "SELECT COUNT(*) FROM ("
                    + "SELECT customer_id FROM sales "
                    + where
                    + " GROUP BY customer_id HAVING COUNT(*) = 1) x"
            );
            ResultSet rs3 = ps3.executeQuery();

            if (rs3.next()) {
                lblNewCustomersValue.setText(rs3.getString(1));
            }

            // Repeat Customers
            PreparedStatement ps4 = con.prepareStatement(
                    "SELECT COUNT(*) FROM ("
                    + "SELECT customer_id FROM sales "
                    + where
                    + " GROUP BY customer_id HAVING COUNT(*) > 1) x"
            );
            ResultSet rs4 = ps4.executeQuery();

            if (rs4.next()) {
                lblRepeatCustomersValue.setText(rs4.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // table data
    private void loadTable() {

        DefaultTableModel model
                = (DefaultTableModel) tblCustomerInsights.getModel();

        model.setRowCount(0);

        try (Connection con = db.getConnection()) {

            String where = getWhereClause();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT customer_id, "
                    + "COUNT(*) AS orders_count, "
                    + "SUM(total_price) AS revenue, "
                    + "AVG(total_price) AS avg_order "
                    + "FROM sales "
                    + where
                    + " GROUP BY customer_id "
                    + "ORDER BY revenue DESC"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                model.addRow(new Object[]{
                    rs.getInt("customer_id"),
                    rs.getInt("orders_count"),
                    rs.getDouble("revenue"),
                    rs.getDouble("avg_order")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // chart
    private void loadChart() {

        String chartType = cmbChartType.getSelectedItem().toString();

        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        DefaultPieDataset pieDataset = new DefaultPieDataset();

        try (Connection con = db.getConnection()) {

            String where = getWhereClause();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT customer_id, SUM(total_price) AS revenue "
                    + "FROM sales "
                    + where
                    + " GROUP BY customer_id "
                    + " ORDER BY revenue DESC LIMIT 5"
            );

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String customer = "C" + rs.getInt("customer_id");
                double revenue = rs.getDouble("revenue");

                categoryDataset.addValue(revenue, "Revenue", customer);
                pieDataset.setValue(customer, revenue);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        JFreeChart chart = null;

        if (chartType.equals("Pie Chart")) {

            chart = ChartFactory.createPieChart(
                    "Top 5 Customers",
                    pieDataset,
                    true,
                    true,
                    false
            );

        } else {

            chart = ChartFactory.createBarChart(
                    "Top 5 Customers",
                    "Customer",
                    "Revenue",
                    categoryDataset
            );
        }

        ChartPanel cp = new ChartPanel(chart);

        chartPanel.removeAll();
        chartPanel.add(cp, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    // gen report
    
    private void generatePDFReport() {
        OutputStream os = null;

        try {
            StringBuilder html = new StringBuilder();

            html.append("<?xml version='1.0' encoding='UTF-8'?>");
            html.append("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' ");
            html.append("'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");

            html.append("<html xmlns='http://www.w3.org/1999/xhtml'>");
            html.append("<head>");
            html.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />");

            html.append("<style>");
            html.append("@page { size:A4 portrait; margin:25px; }");
            html.append("body { font-family:Arial; font-size:11px; color:#222; }");
            html.append("h1 { text-align:center; font-size:24px; color:#1e3a5f; margin-bottom:25px; }");
            html.append("h2 { color:#1e3a5f; border-bottom:2px solid #3498db; padding-bottom:5px; margin-top:20px; }");

            html.append("table { width:100%; border-collapse:collapse; }");
            html.append("th { background:#3498db; color:white; padding:8px; border:1px solid #ccc; }");
            html.append("td { padding:7px; border:1px solid #ccc; }");

            html.append(".box { border:1px solid #ccc; background:#f8f9fa; padding:12px; text-align:center; }");
            html.append(".label { font-weight:bold; color:#555; }");
            html.append(".value { font-size:18px; color:green; font-weight:bold; margin-top:6px; }");
            html.append("</style>");

            html.append("</head>");
            html.append("<body>");

            // Title
            html.append("<h1>Customer Behavior Analysis Report</h1>");

            // Summary
            html.append("<h2>Key Insights</h2>");

            html.append("<table style='margin-bottom:20px;'>");
            html.append("<tr>");

            html.append("<td width='25%'><div class='box'><div class='label'>Total Customers</div><div class='value'>")
                    .append(lblTotalCustomersValue.getText())
                    .append("</div></div></td>");

            html.append("<td width='25%'><div class='box'><div class='label'>Avg Spending</div><div class='value'>")
                    .append(lblAvgSpendingValue.getText())
                    .append("</div></div></td>");

            html.append("<td width='25%'><div class='box'><div class='label'>New Customers</div><div class='value'>")
                    .append(lblNewCustomersValue.getText())
                    .append("</div></div></td>");

            html.append("<td width='25%'><div class='box'><div class='label'>Repeat Customers</div><div class='value'>")
                    .append(lblRepeatCustomersValue.getText())
                    .append("</div></div></td>");

            html.append("</tr>");
            html.append("</table>");

            // Table Data
            html.append("<h2>Detailed Analysis Data</h2>");
            html.append("<table>");

            DefaultTableModel model = (DefaultTableModel) tblCustomerInsights.getModel();

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
            chooser.setSelectedFile(new File("Customer_Behavior_Report.pdf"));

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

                JOptionPane.showMessageDialog(this, "PDF Generated Successfully!");

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
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel7 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        cmbRegion = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        cmbChartType = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        btnPrint = new javax.swing.JButton();
        summaryCardsPanel = new javax.swing.JPanel();
        cardTotalRevenue = new javax.swing.JPanel();
        lblTotalCustomers = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblTotalCustomersValue = new javax.swing.JLabel();
        cardTotalOrders = new javax.swing.JPanel();
        lblAvgSpending = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblAvgSpendingValue = new javax.swing.JLabel();
        cardTopProduct = new javax.swing.JPanel();
        lblNewCustomers = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblNewCustomersValue = new javax.swing.JLabel();
        cardBestBranch = new javax.swing.JPanel();
        lblRepeatCustomers = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblRepeatCustomersValue = new javax.swing.JLabel();
        chartPanel = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCustomerInsights = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(940, 700));
        jPanel1.setLayout(new java.awt.BorderLayout());

        topContainer.setLayout(new javax.swing.BoxLayout(topContainer, javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setBackground(new java.awt.Color(15, 23, 42));

        jLabel1.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(229, 231, 235));
        jLabel1.setText("Customer Insight");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(391, 391, 391)
                .addComponent(jLabel1)
                .addContainerGap(432, Short.MAX_VALUE))
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
        filtersPanel.add(jDateChooser1);

        jLabel7.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel7.setText("To:");
        filtersPanel.add(jLabel7);
        filtersPanel.add(jDateChooser2);

        jLabel4.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel4.setText("Region");
        filtersPanel.add(jLabel4);

        cmbRegion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All" }));
        filtersPanel.add(cmbRegion);

        jLabel8.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel8.setText("Chart Type");
        filtersPanel.add(jLabel8);

        cmbChartType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Bar Chart", "Pie Chart" }));
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

        lblTotalCustomers.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTotalCustomers.setForeground(new java.awt.Color(102, 102, 102));
        lblTotalCustomers.setText("Total Customers");
        cardTotalRevenue.add(lblTotalCustomers);
        cardTotalRevenue.add(filler1);

        lblTotalCustomersValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblTotalCustomersValue.setForeground(new java.awt.Color(15, 23, 42));
        lblTotalCustomersValue.setText("0");
        cardTotalRevenue.add(lblTotalCustomersValue);

        summaryCardsPanel.add(cardTotalRevenue);

        cardTotalOrders.setBackground(new java.awt.Color(16, 185, 129));
        cardTotalOrders.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTotalOrders.setLayout(new javax.swing.BoxLayout(cardTotalOrders, javax.swing.BoxLayout.Y_AXIS));

        lblAvgSpending.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblAvgSpending.setForeground(new java.awt.Color(102, 102, 102));
        lblAvgSpending.setText("Avg Spending");
        cardTotalOrders.add(lblAvgSpending);
        cardTotalOrders.add(filler2);

        lblAvgSpendingValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblAvgSpendingValue.setForeground(new java.awt.Color(15, 23, 42));
        lblAvgSpendingValue.setText("Rs. 0");
        cardTotalOrders.add(lblAvgSpendingValue);

        summaryCardsPanel.add(cardTotalOrders);

        cardTopProduct.setBackground(new java.awt.Color(139, 92, 246));
        cardTopProduct.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTopProduct.setLayout(new javax.swing.BoxLayout(cardTopProduct, javax.swing.BoxLayout.Y_AXIS));

        lblNewCustomers.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblNewCustomers.setForeground(new java.awt.Color(102, 102, 102));
        lblNewCustomers.setText("New Customers");
        cardTopProduct.add(lblNewCustomers);
        cardTopProduct.add(filler3);

        lblNewCustomersValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblNewCustomersValue.setForeground(new java.awt.Color(15, 23, 42));
        lblNewCustomersValue.setText("Nick");
        cardTopProduct.add(lblNewCustomersValue);

        summaryCardsPanel.add(cardTopProduct);

        cardBestBranch.setBackground(new java.awt.Color(245, 158, 11));
        cardBestBranch.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardBestBranch.setLayout(new javax.swing.BoxLayout(cardBestBranch, javax.swing.BoxLayout.Y_AXIS));

        lblRepeatCustomers.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblRepeatCustomers.setForeground(new java.awt.Color(102, 102, 102));
        lblRepeatCustomers.setText("Repeat Customers");
        cardBestBranch.add(lblRepeatCustomers);
        cardBestBranch.add(filler4);

        lblRepeatCustomersValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblRepeatCustomersValue.setForeground(new java.awt.Color(15, 23, 42));
        lblRepeatCustomersValue.setText("Alex");
        cardBestBranch.add(lblRepeatCustomersValue);

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

        tblCustomerInsights.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Region", "Orders", "Revenue", "Avg Order Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblCustomerInsights);

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
    private javax.swing.JComboBox<String> cmbChartType;
    private javax.swing.JComboBox<String> cmbPeriod;
    private javax.swing.JComboBox<String> cmbRegion;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JPanel filtersPanel;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
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
    private javax.swing.JLabel lblAvgSpending;
    private javax.swing.JLabel lblAvgSpendingValue;
    private javax.swing.JLabel lblNewCustomers;
    private javax.swing.JLabel lblNewCustomersValue;
    private javax.swing.JLabel lblRepeatCustomers;
    private javax.swing.JLabel lblRepeatCustomersValue;
    private javax.swing.JLabel lblTotalCustomers;
    private javax.swing.JLabel lblTotalCustomersValue;
    private javax.swing.JPanel summaryCardsPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblCustomerInsights;
    private javax.swing.JPanel topContainer;
    // End of variables declaration//GEN-END:variables
}
