# Sales Data Analysis System

https://camo.githubusercontent.com/c1c2ed30dfeaeececda64dd5279cdacc0ab942b3c421dd1bc96b723298acb3b0/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f4a6176612d31372d6f72616e67653f6c6f676f3d6a617661


The Sales Data Analysis System is a comprehensive Java Swing desktop application designed for visualizing and analyzing supermarket sales data. It provides an administrative dashboard with interactive charts, summary cards, and detailed tables to derive insights from sales transactions. The application supports data loading from CSV files, user management, and generation of PDF reports for various analytical views.

## Features

*   **Secure Authentication:** A login system to control access to the application.
*   **Interactive Dashboard:** A central dashboard providing a high-level overview of key metrics like total revenue, total orders, top-selling product, and best-performing region.
*   **Data Loading:** Easily load sales data from a `.csv` file directly into the system's database.
*   **Top Products Analysis:** View and analyze top-selling products based on quantity sold and revenue, complete with sortable tables and graphical representations (Bar/Line charts).
*   **Product Sales Trends:** Analyze the sales performance of individual products over time with monthly trend charts.
*   **Customer Insights:** Gain insights into customer behavior, including total customers, average spending per customer, and a breakdown of new vs. repeat customers.
*   **Regional Sales Performance:** Compare sales performance across different regions, analyzing revenue, order counts, and growth trends.
*   **Dynamic Filtering:** Filter data across all dashboards by time period (Today, This Week, Month, Year, Custom Range) and region.
*   **User Management:** An admin-only panel to add, update, and delete user accounts (Admin/Staff roles).
*   **PDF Report Generation:** Print or save detailed analytical reports for each module as a PDF file.

## Screenshots

| Login Screen | Main Dashboard |
| :---: | :---: |
| <img alt="Login Screen" src="https://github.com/user-attachments/assets/e618f460-0697-4928-ba1e-fa14346fff29" /> | <img alt="Main Dashboard" src="https://github.com/user-attachments/assets/035feb88-3c61-4e39-9e6e-33a142d4b721" /> |

| Load Data from CSV | Top Selling Products |
| :---: | :---: |
| <img alt="Load Data from CSV" src="https://github.com/user-attachments/assets/9e2cad57-69d1-448d-b8e3-ebd3ce2463bb" /> | <img alt="Top Selling Products" src="https://github.com/user-attachments/assets/83e7a013-2f0d-45d3-a52e-8333cafbe085" /> |

| Product Sales Trend | Customer Behavior Analysis |
| :---: | :---: |
| <img alt="Product Sales Trend" src="https://github.com/user-attachments/assets/45966f0a-0a95-464b-a6c1-193473c32df4" /> | <img alt="Customer Behavior Analysis" src="https://github.com/user-attachments/assets/f9f249d5-aeb4-408d-bc26-0c26021c8043" /> |

| Regional Sales Analysis |
| :---: |
| <img alt="Regional Sales Analysis" src="https://github.com/user-attachments/assets/36a1b099-bc79-4a8a-967f-a64709b500c4" /> |

## Technologies Used

*   **Language:** Java
*   **UI Framework:** Java Swing
*   **IDE:** Apache NetBeans
*   **Database:** MySQL
*   **Charting Library:** JFreeChart
*   **UI Look & Feel:** FlatLaf
*   **PDF Generation:** iText, Flying Saucer
*   **Build Tool:** Apache Ant

## Setup and Installation

### Prerequisites

*   JDK 8 or higher
*   MySQL Server
*   Apache NetBeans IDE (or any other Java IDE)

### Database Setup

1.  Start your MySQL server.
2.  Create a new database named `Sampath_Food_City`.

    ```sql
    CREATE DATABASE Sampath_Food_City;
    ```
3.  Use the new database.

    ```sql
    USE Sampath_Food_City;
    ```
4.  Create the necessary tables.

    *   **`users` table:** For storing login credentials.
        ```sql
        CREATE TABLE users (
          username VARCHAR(50) PRIMARY KEY,
          password VARCHAR(255) NOT NULL,
          role VARCHAR(10) NOT NULL -- 'Admin' or 'Staff'
        );
        ```

    *   **`sales` table:** For storing the transaction data.
        ```sql
        CREATE TABLE sales (
          transaction_id INT PRIMARY KEY,
          customer_id INT,
          product_id INT,
          product_name VARCHAR(255),
          quantity INT,
          priceper_unit DECIMAL(10, 2),
          date DATE,
          total_price DECIMAL(10, 2),
          region VARCHAR(255)
        );
        ```
5.  The database connection details are configured in `src/db/db.java`. You can modify the `JDBC_URL`, `USERNAME`, and `PASSWORD` if your setup is different.

### Running the Application

1.  Clone the repository:
    ```bash
    git clone https://github.com/rohan-prasad-0/Sales-Data-Analysis-System.git
    ```
2.  Open the project in Apache NetBeans IDE.
3.  Ensure all the library `.jar` files included in the repository are correctly referenced in the project's properties.
4.  The main entry point for the application is `src/ui/login.java`. Run this file to start the application.
5.  A default `supermarket_sales.csv` file is included in the root directory. Use the "Load Data" feature in the application to import this data into the `sales` table.
