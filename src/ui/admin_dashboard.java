/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ui;

import db.db;
import java.awt.Cursor;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author anom
 */
public class admin_dashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(admin_dashboard.class.getName());

    private ImageIcon normalIcon;
    private ImageIcon redIcon;
    private ImageIcon logoutIcon;
    private ImageIcon logoutHoverIcon;

    /**
     * Creates new form admin_dashboard
     */
    JpanelLoader jpload = new JpanelLoader();

    public admin_dashboard() {
        initComponents();

        loadSummaryCards();
        loadSummaryPanel();
        loadSummaryChart();

        // exit 
        normalIcon = new ImageIcon(getClass().getResource("../icons/close.png"));
        redIcon = new ImageIcon(getClass().getResource("../icons/close_red.png"));

        lblClose.setIcon(normalIcon);

        //logout
        logoutIcon = new ImageIcon(getClass().getResource("../icons/logout.png"));
        logoutHoverIcon = new ImageIcon(getClass().getResource("../icons/logout_hover.png"));

        lblLogout.setIcon(logoutIcon);
        lblLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

    }

    // summery card
    private void loadSummaryCards() {

        try (Connection con = db.getConnection()) {

            // Total Revenue
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT IFNULL(SUM(total_price),0) FROM sales"
            );
            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {
                lblTotalRevenueValue.setText(
                        "Rs. " + String.format("%,.2f", rs1.getDouble(1))
                );
            }

            // Total Orders
            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT COUNT(*) FROM sales"
            );
            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {
                lblTotalOrderValue.setText(rs2.getString(1) + " Orders");
            }

            // Top Product
            PreparedStatement ps3 = con.prepareStatement(
                    "SELECT product_name FROM sales "
                    + "GROUP BY product_name "
                    + "ORDER BY SUM(quantity) DESC LIMIT 1"
            );
            ResultSet rs3 = ps3.executeQuery();

            if (rs3.next()) {
                lblTopProductValue.setText(rs3.getString(1));
            }

            // Best Branch
            PreparedStatement ps4 = con.prepareStatement(
                    "SELECT region FROM sales "
                    + "GROUP BY region "
                    + "ORDER BY SUM(total_price) DESC LIMIT 1"
            );

            ResultSet rs4 = ps4.executeQuery();

            if (rs4.next()) {
                lblBestBranchValue.setText(rs4.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // summery panal
    
    private void loadSummaryPanel() {

        try (Connection con = db.getConnection()) {

            // Highest Sales Month
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT DATE_FORMAT(date, '%M %Y') AS month_name, "
                    + "SUM(total_price) AS total "
                    + "FROM sales "
                    + "GROUP BY YEAR(date), MONTH(date) "
                    + "ORDER BY total DESC LIMIT 1"
            );

            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {
                lblHighestSalesMonth.setText("Highest Sales Month:");
                lblHighestValue.setText(
                        rs1.getString("month_name") + " (Rs. "
                        + String.format("%,.2f", rs1.getDouble("total")) + ")"
                );
            }

            // Lowest Sales Month
            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT DATE_FORMAT(date, '%M %Y') AS month_name, "
                    + "SUM(total_price) AS total "
                    + "FROM sales "
                    + "GROUP BY YEAR(date), MONTH(date) "
                    + "ORDER BY total ASC LIMIT 1"
            );

            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {
                lblLowestSalesMonth.setText("Lowest Sales Month:");
                lblLowestValue.setText(
                        rs2.getString("month_name") + " (Rs. "
                        + String.format("%,.2f", rs2.getDouble("total")) + ")"
                );
            }

            // Average Monthly Revenue
            PreparedStatement ps3 = con.prepareStatement(
                    "SELECT AVG(month_total) FROM ("
                    + "SELECT SUM(total_price) AS month_total "
                    + "FROM sales "
                    + "GROUP BY YEAR(date), MONTH(date)"
                    + ") x"
            );

            ResultSet rs3 = ps3.executeQuery();

            if (rs3.next()) {
                lblAverageMonthlyRevenue.setText("Average Monthly Revenue:");
                lblAverageValue.setText(
                        "Rs. " + String.format("%,.2f", rs3.getDouble(1))
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // summery chart
    
    private void loadSummaryChart() {
        try (Connection con = db.getConnection()) {

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            String sql = "SELECT MONTHNAME(date) AS month, SUM(total_price) AS total "
                    + "FROM sales "
                    + "GROUP BY MONTH(date), MONTHNAME(date) "
                    + "ORDER BY MONTH(date)";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                dataset.addValue(rs.getDouble("total"), "Sales", rs.getString("month"));
            }

            JFreeChart chart = ChartFactory.createLineChart(
                    "Monthly Revenue Trend",
                    "Month",
                    "Revenue (Rs.)",
                    dataset
            );

            ChartPanel cp = new ChartPanel(chart);
            cp.setPreferredSize(new java.awt.Dimension(600, 300));

            summeryChartPanel.removeAll();
            summeryChartPanel.setLayout(new java.awt.BorderLayout());
            summeryChartPanel.add(cp, java.awt.BorderLayout.CENTER);
            summeryChartPanel.revalidate();
            summeryChartPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
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
        jPanel2 = new javax.swing.JPanel();
        btnHome = new javax.swing.JButton();
        lblHome = new javax.swing.JLabel();
        btnDataload = new javax.swing.JButton();
        lblDataload = new javax.swing.JLabel();
        btnTopproducts = new javax.swing.JButton();
        lblTopproducts = new javax.swing.JLabel();
        btnProducttrends = new javax.swing.JButton();
        lblProducttrends = new javax.swing.JLabel();
        btnCostomer = new javax.swing.JButton();
        lblCostomer = new javax.swing.JLabel();
        btnregional = new javax.swing.JButton();
        lblRegional = new javax.swing.JLabel();
        lblClose = new javax.swing.JLabel();
        lblLogout = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnmanageusers = new javax.swing.JButton();
        lblRegional1 = new javax.swing.JLabel();
        controlPanel = new javax.swing.JPanel();
        HomePanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        mainViewPanal = new javax.swing.JPanel();
        homePanel = new javax.swing.JPanel();
        summaryCardsPanel = new javax.swing.JPanel();
        cardTotalRevenue = new javax.swing.JPanel();
        lblTotalRevenueTitle = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblTotalRevenueValue = new javax.swing.JLabel();
        cardTotalOrders = new javax.swing.JPanel();
        lblTotalOrder = new javax.swing.JLabel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblTotalOrderValue = new javax.swing.JLabel();
        cardTopProduct = new javax.swing.JPanel();
        lblTopProduct = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblTopProductValue = new javax.swing.JLabel();
        cardBestBranch = new javax.swing.JPanel();
        lblBestBranch = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 8), new java.awt.Dimension(0, 8), new java.awt.Dimension(32767, 8));
        lblBestBranchValue = new javax.swing.JLabel();
        contentPanel = new javax.swing.JPanel();
        summaryPanel = new javax.swing.JPanel();
        summaryGridPanel = new javax.swing.JPanel();
        lblHighestSalesMonth = new javax.swing.JLabel();
        lblHighestValue = new javax.swing.JLabel();
        lblLowestSalesMonth = new javax.swing.JLabel();
        lblLowestValue = new javax.swing.JLabel();
        lblAverageMonthlyRevenue = new javax.swing.JLabel();
        lblAverageValue = new javax.swing.JLabel();
        summaryTitlePanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        summeryChartPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(248, 250, 252));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(30, 41, 53));

        btnHome.setFont(new java.awt.Font("JetBrainsMono NF", 0, 18)); // NOI18N
        btnHome.setText("Home");
        btnHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHomeActionPerformed(evt);
            }
        });

        lblHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/home.png"))); // NOI18N

        btnDataload.setFont(new java.awt.Font("JetBrainsMono NF", 0, 18)); // NOI18N
        btnDataload.setText("Load Data");
        btnDataload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDataloadActionPerformed(evt);
            }
        });

        lblDataload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/folder.png"))); // NOI18N

        btnTopproducts.setFont(new java.awt.Font("JetBrainsMono NF", 0, 18)); // NOI18N
        btnTopproducts.setText("Top Products");
        btnTopproducts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTopproductsActionPerformed(evt);
            }
        });

        lblTopproducts.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/star.png"))); // NOI18N

        btnProducttrends.setFont(new java.awt.Font("JetBrainsMono NF", 0, 18)); // NOI18N
        btnProducttrends.setText("Product Trends");
        btnProducttrends.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProducttrendsActionPerformed(evt);
            }
        });

        lblProducttrends.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/trending.png"))); // NOI18N

        btnCostomer.setFont(new java.awt.Font("JetBrainsMono NF", 0, 18)); // NOI18N
        btnCostomer.setText("Customer Insights");
        btnCostomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCostomerActionPerformed(evt);
            }
        });

        lblCostomer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/groups.png"))); // NOI18N

        btnregional.setFont(new java.awt.Font("JetBrainsMono NF", 0, 18)); // NOI18N
        btnregional.setText("Regional Sales");
        btnregional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnregionalActionPerformed(evt);
            }
        });

        lblRegional.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/map_search.png"))); // NOI18N

        lblClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/close.png"))); // NOI18N
        lblClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCloseMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblCloseMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblCloseMouseExited(evt);
            }
        });

        lblLogout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logout.png"))); // NOI18N
        lblLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblLogoutMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblLogoutMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblLogoutMouseExited(evt);
            }
        });

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo.png"))); // NOI18N

        jPanel4.setBackground(new java.awt.Color(30, 41, 53));

        jLabel12.setFont(new java.awt.Font("sansserif", 1, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(51, 51, 255));
        jLabel12.setText("Sampath Food City");

        jLabel11.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(102, 153, 255));
        jLabel11.setText("Sales Dashboard");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addComponent(jLabel11)
                .addContainerGap(52, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(13, 13, 13)
                    .addComponent(jLabel12)
                    .addContainerGap(17, Short.MAX_VALUE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 34, Short.MAX_VALUE)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(7, 7, 7)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(27, Short.MAX_VALUE)))
        );

        btnmanageusers.setFont(new java.awt.Font("JetBrainsMono NF", 0, 18)); // NOI18N
        btnmanageusers.setText("Manage Users");
        btnmanageusers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnmanageusersActionPerformed(evt);
            }
        });

        lblRegional1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/key.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblDataload)
                                .addGap(6, 6, 6)
                                .addComponent(btnDataload, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblTopproducts)
                                .addGap(6, 6, 6)
                                .addComponent(btnTopproducts, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblProducttrends)
                                .addGap(6, 6, 6)
                                .addComponent(btnProducttrends, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblCostomer)
                                .addGap(6, 6, 6)
                                .addComponent(btnCostomer, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblHome)
                                .addGap(6, 6, 6)
                                .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblRegional)
                                    .addComponent(lblRegional1))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnmanageusers, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addComponent(lblClose)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lblLogout)
                                            .addGap(26, 26, 26))
                                        .addComponent(btnregional, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblHome, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDataload, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDataload, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTopproducts, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTopproducts, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProducttrends, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProducttrends, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCostomer, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCostomer, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRegional, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnregional, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnmanageusers, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRegional1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(108, 108, 108)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblClose, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

        controlPanel.setLayout(new java.awt.BorderLayout());

        HomePanel.setLayout(new java.awt.BorderLayout());

        jPanel3.setBackground(new java.awt.Color(15, 23, 42));

        jLabel1.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(229, 231, 235));
        jLabel1.setText("Dashboard Overview");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(391, 391, 391)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(394, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        HomePanel.add(jPanel3, java.awt.BorderLayout.NORTH);

        mainViewPanal.setBackground(new java.awt.Color(248, 250, 252));
        mainViewPanal.setLayout(new java.awt.BorderLayout());

        homePanel.setForeground(new java.awt.Color(248, 250, 252));
        homePanel.setLayout(new java.awt.BorderLayout());

        summaryCardsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        summaryCardsPanel.setLayout(new java.awt.GridLayout(1, 4, 16, 0));

        cardTotalRevenue.setBackground(new java.awt.Color(59, 130, 246));
        cardTotalRevenue.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTotalRevenue.setLayout(new javax.swing.BoxLayout(cardTotalRevenue, javax.swing.BoxLayout.Y_AXIS));

        lblTotalRevenueTitle.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTotalRevenueTitle.setForeground(new java.awt.Color(102, 102, 102));
        lblTotalRevenueTitle.setText("Total Revenue");
        cardTotalRevenue.add(lblTotalRevenueTitle);
        cardTotalRevenue.add(filler1);

        lblTotalRevenueValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblTotalRevenueValue.setForeground(new java.awt.Color(15, 23, 42));
        lblTotalRevenueValue.setText("Rs. 0");
        cardTotalRevenue.add(lblTotalRevenueValue);

        summaryCardsPanel.add(cardTotalRevenue);

        cardTotalOrders.setBackground(new java.awt.Color(16, 185, 129));
        cardTotalOrders.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTotalOrders.setLayout(new javax.swing.BoxLayout(cardTotalOrders, javax.swing.BoxLayout.Y_AXIS));

        lblTotalOrder.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTotalOrder.setForeground(new java.awt.Color(102, 102, 102));
        lblTotalOrder.setText("Total Orders ");
        cardTotalOrders.add(lblTotalOrder);
        cardTotalOrders.add(filler2);

        lblTotalOrderValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblTotalOrderValue.setForeground(new java.awt.Color(15, 23, 42));
        lblTotalOrderValue.setText("0 Orders ");
        cardTotalOrders.add(lblTotalOrderValue);

        summaryCardsPanel.add(cardTotalOrders);

        cardTopProduct.setBackground(new java.awt.Color(139, 92, 246));
        cardTopProduct.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardTopProduct.setLayout(new javax.swing.BoxLayout(cardTopProduct, javax.swing.BoxLayout.Y_AXIS));

        lblTopProduct.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTopProduct.setForeground(new java.awt.Color(102, 102, 102));
        lblTopProduct.setText("Top Product");
        cardTopProduct.add(lblTopProduct);
        cardTopProduct.add(filler3);

        lblTopProductValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblTopProductValue.setForeground(new java.awt.Color(15, 23, 42));
        lblTopProductValue.setText("Milk Powder");
        cardTopProduct.add(lblTopProductValue);

        summaryCardsPanel.add(cardTopProduct);

        cardBestBranch.setBackground(new java.awt.Color(245, 158, 11));
        cardBestBranch.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        cardBestBranch.setLayout(new javax.swing.BoxLayout(cardBestBranch, javax.swing.BoxLayout.Y_AXIS));

        lblBestBranch.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblBestBranch.setForeground(new java.awt.Color(102, 102, 102));
        lblBestBranch.setText("Best Branch");
        cardBestBranch.add(lblBestBranch);
        cardBestBranch.add(filler4);

        lblBestBranchValue.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        lblBestBranchValue.setForeground(new java.awt.Color(15, 23, 42));
        lblBestBranchValue.setText("Matugama");
        cardBestBranch.add(lblBestBranchValue);

        summaryCardsPanel.add(cardBestBranch);

        homePanel.add(summaryCardsPanel, java.awt.BorderLayout.NORTH);

        contentPanel.setLayout(new java.awt.GridLayout(2, 1, 10, 10));

        summaryPanel.setBackground(new java.awt.Color(255, 255, 255));
        summaryPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        summaryPanel.setPreferredSize(new java.awt.Dimension(940, 200));
        summaryPanel.setLayout(new java.awt.BorderLayout());

        summaryGridPanel.setBackground(new java.awt.Color(255, 255, 255));
        summaryGridPanel.setPreferredSize(new java.awt.Dimension(751, 100));
        summaryGridPanel.setLayout(new java.awt.GridLayout(3, 2, 10, 8));

        lblHighestSalesMonth.setForeground(new java.awt.Color(15, 23, 42));
        lblHighestSalesMonth.setText("Highest Sales Month: -");
        summaryGridPanel.add(lblHighestSalesMonth);
        summaryGridPanel.add(lblHighestValue);

        lblLowestSalesMonth.setForeground(new java.awt.Color(15, 23, 42));
        lblLowestSalesMonth.setText("Lowest Sales Month: -");
        summaryGridPanel.add(lblLowestSalesMonth);
        summaryGridPanel.add(lblLowestValue);

        lblAverageMonthlyRevenue.setForeground(new java.awt.Color(15, 23, 42));
        lblAverageMonthlyRevenue.setText("Average Monthly Revenue: -");
        summaryGridPanel.add(lblAverageMonthlyRevenue);
        summaryGridPanel.add(lblAverageValue);

        summaryPanel.add(summaryGridPanel, java.awt.BorderLayout.CENTER);

        summaryTitlePanel.setBackground(new java.awt.Color(255, 255, 255));
        summaryTitlePanel.setLayout(new java.awt.BorderLayout());

        jLabel2.setFont(new java.awt.Font("sansserif", 1, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(15, 23, 42));
        jLabel2.setText("Monthly Summary");
        summaryTitlePanel.add(jLabel2, java.awt.BorderLayout.CENTER);

        summaryPanel.add(summaryTitlePanel, java.awt.BorderLayout.NORTH);

        contentPanel.add(summaryPanel);

        summeryChartPanel.setBackground(new java.awt.Color(60, 120, 200));

        javax.swing.GroupLayout summeryChartPanelLayout = new javax.swing.GroupLayout(summeryChartPanel);
        summeryChartPanel.setLayout(summeryChartPanelLayout);
        summeryChartPanelLayout.setHorizontalGroup(
            summeryChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        summeryChartPanelLayout.setVerticalGroup(
            summeryChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        contentPanel.add(summeryChartPanel);

        homePanel.add(contentPanel, java.awt.BorderLayout.CENTER);

        mainViewPanal.add(homePanel, java.awt.BorderLayout.CENTER);

        HomePanel.add(mainViewPanal, java.awt.BorderLayout.CENTER);

        controlPanel.add(HomePanel, java.awt.BorderLayout.CENTER);

        jPanel1.add(controlPanel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1210, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnDataloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataloadActionPerformed
        // TODO add your handling code here:

        BtloadData btloaddata = new BtloadData();
        jpload.jPanelLoader(controlPanel, btloaddata);


    }//GEN-LAST:event_btnDataloadActionPerformed

    private void btnTopproductsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTopproductsActionPerformed
        // TODO add your handling code here:

        BttopProducts bttopproducts = new BttopProducts();
        jpload.jPanelLoader(controlPanel, bttopproducts);

    }//GEN-LAST:event_btnTopproductsActionPerformed

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        // TODO add your handling code here:

        jpload.jPanelLoader(controlPanel, HomePanel);


    }//GEN-LAST:event_btnHomeActionPerformed

    private void btnProducttrendsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProducttrendsActionPerformed
        // TODO add your handling code here:

        BtProductTrends trends = new BtProductTrends();
        jpload.jPanelLoader(controlPanel, trends);

    }//GEN-LAST:event_btnProducttrendsActionPerformed

    private void btnCostomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCostomerActionPerformed
        // TODO add your handling code here:

        BtCustomerInsights customer = new BtCustomerInsights();
        jpload.jPanelLoader(controlPanel, customer);

    }//GEN-LAST:event_btnCostomerActionPerformed

    private void btnregionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnregionalActionPerformed
        // TODO add your handling code here:

        BtRegionalSales regional = new BtRegionalSales();
        jpload.jPanelLoader(controlPanel, regional);

    }//GEN-LAST:event_btnregionalActionPerformed

    private void lblCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCloseMouseClicked
        // TODO add your handling code here:

        System.exit(0);


    }//GEN-LAST:event_lblCloseMouseClicked

    private void lblCloseMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCloseMouseEntered
        // TODO add your handling code here:

        lblClose.setIcon(redIcon);


    }//GEN-LAST:event_lblCloseMouseEntered

    private void lblCloseMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCloseMouseExited
        // TODO add your handling code here:

        lblClose.setIcon(normalIcon);

    }//GEN-LAST:event_lblCloseMouseExited

    private void lblLogoutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLogoutMouseEntered
        // TODO add your handling code here:

        lblLogout.setIcon(logoutHoverIcon);

    }//GEN-LAST:event_lblLogoutMouseEntered

    private void lblLogoutMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLogoutMouseExited
        // TODO add your handling code here:

        lblLogout.setIcon(logoutIcon);

    }//GEN-LAST:event_lblLogoutMouseExited

    private void lblLogoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLogoutMouseClicked
        // TODO add your handling code here:

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            login lg = new login();
            lg.setVisible(true);
            this.dispose();
        }


    }//GEN-LAST:event_lblLogoutMouseClicked

    private void btnmanageusersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnmanageusersActionPerformed
        // TODO add your handling code here:

        BtManageUsers btmanageusers = new BtManageUsers();
        jpload.jPanelLoader(controlPanel, btmanageusers);
    }//GEN-LAST:event_btnmanageusersActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new admin_dashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel HomePanel;
    private javax.swing.JButton btnCostomer;
    private javax.swing.JButton btnDataload;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnProducttrends;
    private javax.swing.JButton btnTopproducts;
    private javax.swing.JButton btnmanageusers;
    private javax.swing.JButton btnregional;
    private javax.swing.JPanel cardBestBranch;
    private javax.swing.JPanel cardTopProduct;
    private javax.swing.JPanel cardTotalOrders;
    private javax.swing.JPanel cardTotalRevenue;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JPanel homePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblAverageMonthlyRevenue;
    private javax.swing.JLabel lblAverageValue;
    private javax.swing.JLabel lblBestBranch;
    private javax.swing.JLabel lblBestBranchValue;
    private javax.swing.JLabel lblClose;
    private javax.swing.JLabel lblCostomer;
    private javax.swing.JLabel lblDataload;
    private javax.swing.JLabel lblHighestSalesMonth;
    private javax.swing.JLabel lblHighestValue;
    private javax.swing.JLabel lblHome;
    private javax.swing.JLabel lblLogout;
    private javax.swing.JLabel lblLowestSalesMonth;
    private javax.swing.JLabel lblLowestValue;
    private javax.swing.JLabel lblProducttrends;
    private javax.swing.JLabel lblRegional;
    private javax.swing.JLabel lblRegional1;
    private javax.swing.JLabel lblTopProduct;
    private javax.swing.JLabel lblTopProductValue;
    private javax.swing.JLabel lblTopproducts;
    private javax.swing.JLabel lblTotalOrder;
    private javax.swing.JLabel lblTotalOrderValue;
    private javax.swing.JLabel lblTotalRevenueTitle;
    private javax.swing.JLabel lblTotalRevenueValue;
    private javax.swing.JPanel mainViewPanal;
    private javax.swing.JPanel summaryCardsPanel;
    private javax.swing.JPanel summaryGridPanel;
    private javax.swing.JPanel summaryPanel;
    private javax.swing.JPanel summaryTitlePanel;
    private javax.swing.JPanel summeryChartPanel;
    // End of variables declaration//GEN-END:variables
}
