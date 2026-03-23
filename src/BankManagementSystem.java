import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// Login: username = manager | password = manager123
public class BankManagementSystem {

    public static void main(String[] args) {
        new LoginFrame();
    }

    // DB connection
    static Connection getConnection() throws SQLException {
        try { Class.forName("oracle.jdbc.driver.OracleDriver"); }
        catch (ClassNotFoundException e) { throw new SQLException("Driver not found"); }
        return DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe", "mayank", "2405587");
    }

    // Login
    static class LoginFrame extends JFrame implements ActionListener {

        JTextField     userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JCheckBox showPassCheckBox = new JCheckBox("Show Password");
        JButton loginBtn = new JButton("Login");

        LoginFrame() {
            setTitle("Bank Management System - Login");
            setSize(320, 260);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(null);
            setLocationRelativeTo(null);
            setResizable(false);

            JLabel title = new JLabel("BANK MANAGEMENT SYSTEM", SwingConstants.CENTER);
            title.setBounds(0, 15, 320, 25);
            title.setFont(new Font("Arial", Font.BOLD, 16));
            add(title);

            JLabel ul = new JLabel("Username:");
            ul.setBounds(40, 60, 80, 25); add(ul);
            userField.setBounds(125, 60, 155, 25); add(userField);

            JLabel pl = new JLabel("Password:");
            pl.setBounds(40, 98, 80, 25); add(pl);
            passField.setBounds(125, 98, 155, 25); add(passField);

            showPassCheckBox.setBounds(125, 128, 155, 25);
            showPassCheckBox.addItemListener(e -> togglePasswordVisibility());
            add(showPassCheckBox);

            loginBtn.setBounds(105, 165, 100, 30);
            loginBtn.addActionListener(this);
            add(loginBtn);

            setVisible(true);
        }

        private void togglePasswordVisibility() {
            if (showPassCheckBox.isSelected()) {
                // Show password - set echo char to null
                passField.setEchoChar((char) 0);
            } else {
                // Hide password - set echo char to default bullet
                passField.setEchoChar('●');
            }
        }

        public void actionPerformed(ActionEvent e) {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();

            if (u.equals("manager") && p.equals("manager123")) {
                new MainPanel();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.");
            }
        }
    }

    static class MainPanel extends JFrame implements ActionListener {

        JTabbedPane tabs = new JTabbedPane();

        // Customers tab
        DefaultTableModel custModel;
        JTable custTable;
        JButton addCustBtn    = new JButton("Add Customer");
        JButton deleteCustBtn = new JButton("Delete Customer");
        JButton createAccBtn  = new JButton("Create Account");

        // Accounts tab
        DefaultTableModel accModel;
        JTable accTable;
        JButton approveLoanBtn = new JButton("Approve Loan");
        JButton repayLoanBtn   = new JButton("Record Repayment");
        JButton viewLoansBtn   = new JButton("View Loans");
        JButton deactivateAccBtn = new JButton("Deactivate Account");
        JButton reactivateAccBtn = new JButton("Reactivate Account");

        // Loans tab
        DefaultTableModel loanModel;
        JTable loanTable;
        JButton closeLoanBtn   = new JButton("Close Loan");

        JButton refreshBtn = new JButton("Refresh");
        JButton logoutBtn  = new JButton("Logout");

        MainPanel() {
            setTitle("Bank Management System");
            setSize(950, 600);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(null);
            setLocationRelativeTo(null);
            setResizable(false);

            JLabel title = new JLabel("Bank Management System");
            title.setBounds(20, 10, 250, 28);
            title.setFont(new Font("Arial", Font.BOLD, 16));
            add(title);

            refreshBtn.setBounds(700, 12, 100, 28); refreshBtn.addActionListener(this); add(refreshBtn);
            logoutBtn.setBounds(810,  12, 100, 28); logoutBtn.addActionListener(this);  add(logoutBtn);

            tabs.setBounds(10, 50, 920, 510);
            add(tabs);

            buildCustomersTab();
            buildAccountsTab();
            buildLoansTab();

            loadAll();
            setVisible(true);
        }

        // Customers tab
        void buildCustomersTab() {
            JPanel p = new JPanel(null);

            addCustBtn.setBounds(10, 8, 140, 28);    addCustBtn.addActionListener(this);    p.add(addCustBtn);
            deleteCustBtn.setBounds(160, 8, 150, 28); deleteCustBtn.addActionListener(this); p.add(deleteCustBtn);
            createAccBtn.setBounds(320, 8, 150, 28); createAccBtn.addActionListener(this); p.add(createAccBtn);

            String[] cols = {"Customer ID", "Full Name", "Email", "Phone", "Gender", "Registered", "No. of Accounts"};
            custModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            custTable = new JTable(custModel);
            JScrollPane sc = new JScrollPane(custTable);
            sc.setBounds(5, 45, 905, 415); p.add(sc);

            tabs.addTab("Customers", p);
        }

        // Accounts tab
        void buildAccountsTab() {
            JPanel p = new JPanel(null);

            approveLoanBtn.setBounds(10,  8, 140, 28); approveLoanBtn.addActionListener(this); p.add(approveLoanBtn);
            repayLoanBtn.setBounds(160,   8, 160, 28); repayLoanBtn.addActionListener(this);   p.add(repayLoanBtn);
            viewLoansBtn.setBounds(330,   8, 130, 28); viewLoansBtn.addActionListener(this);   p.add(viewLoansBtn);
            deactivateAccBtn.setBounds(470, 8, 170, 28); deactivateAccBtn.addActionListener(this); p.add(deactivateAccBtn);
            reactivateAccBtn.setBounds(650, 8, 160, 28); reactivateAccBtn.addActionListener(this); p.add(reactivateAccBtn);

            String[] cols = {"Account No", "Customer ID", "Holder Name", "Type", "Balance", "Loan Balance", "Status"};
            accModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            accTable = new JTable(accModel);
            accTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    String status = (value == null) ? "" : value.toString().trim().toUpperCase();

                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                        c.setForeground(table.getSelectionForeground());
                        return c;
                    }

                    if (status.equals("ACTIVE")) {
                        c.setBackground(new Color(198, 239, 206));
                        c.setForeground(new Color(0, 97, 0));
                    } else if (status.equals("CLOSED") || status.equals("INACTIVE") || status.equals("DEACTIVATED")) {
                        c.setBackground(new Color(255, 199, 206));
                        c.setForeground(new Color(156, 0, 6));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                    return c;
                }
            });
            JScrollPane sc = new JScrollPane(accTable);
            sc.setBounds(5, 45, 905, 415); p.add(sc);

            tabs.addTab("Accounts", p);
        }

        // Loans tab
        void buildLoansTab() {
            JPanel p = new JPanel(null);

            closeLoanBtn.setBounds(10, 8, 130, 28); closeLoanBtn.addActionListener(this); p.add(closeLoanBtn);

            String[] cols = {"Loan ID", "Account No", "Holder Name", "Loan Amount", "Paid", "Remaining", "Status", "Date Issued"};
            loanModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            loanTable = new JTable(loanModel);
            loanTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    String status = (value == null) ? "" : value.toString().trim().toUpperCase();

                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                        c.setForeground(table.getSelectionForeground());
                        return c;
                    }

                    if (status.equals("ACTIVE")) {
                        c.setBackground(new Color(198, 239, 206));
                        c.setForeground(new Color(0, 97, 0));
                    } else if (status.equals("CLOSED")) {
                        c.setBackground(new Color(255, 235, 156));
                        c.setForeground(new Color(102, 60, 0));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                    return c;
                }
            });
            JScrollPane sc = new JScrollPane(loanTable);
            sc.setBounds(5, 45, 905, 415); p.add(sc);

            tabs.addTab("Loans", p);
        }

        // Load all data
        void loadAll() {
            loadCustomers();
            loadAccounts();
            loadLoans();
        }

        void loadCustomers() {
            custModel.setRowCount(0);
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT c.customer_id, c.full_name, c.email, c.phone, c.gender, " +
                                 "TO_CHAR(c.created_date, 'DD-Mon-YYYY'), COUNT(a.account_number) " +
                                 "FROM customers c LEFT JOIN accounts a ON c.customer_id = a.customer_id " +
                                 "GROUP BY c.customer_id, c.full_name, c.email, c.phone, c.gender, c.created_date " +
                                 "ORDER BY c.customer_id");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    custModel.addRow(new Object[]{
                            rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7)});
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        void loadAccounts() {
            accModel.setRowCount(0);
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT a.account_number, a.customer_id, c.full_name, a.account_type, " +
                                 "a.balance, COALESCE(a.loan_balance, 0), a.status " +
                                 "FROM accounts a JOIN customers c ON a.customer_id = c.customer_id " +
                                 "ORDER BY a.account_number");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    accModel.addRow(new Object[]{
                            rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getDouble(5), rs.getDouble(6), rs.getString(7)});
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        void loadLoans() {
            loanModel.setRowCount(0);
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT l.loan_id, l.account_number, c.full_name, l.loan_amount, " +
                                 "l.amount_paid, (l.loan_amount - l.amount_paid), l.status, " +
                                 "TO_CHAR(l.issue_date,'DD-MON-YYYY') " +
                                 "FROM loans l JOIN accounts a ON l.account_number = a.account_number " +
                                 "JOIN customers c ON a.customer_id = c.customer_id " +
                                 "ORDER BY l.issue_date DESC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    loanModel.addRow(new Object[]{
                            rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getDouble(4), rs.getDouble(5), rs.getDouble(6),
                            rs.getString(7), rs.getString(8)});
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        // Button actions
        public void actionPerformed(ActionEvent e) {
            if      (e.getSource() == refreshBtn)    loadAll();
            else if (e.getSource() == logoutBtn)     { new LoginFrame(); dispose(); }
            else if (e.getSource() == addCustBtn)    addCustomer();
            else if (e.getSource() == deleteCustBtn) deleteCustomer();
            else if (e.getSource() == createAccBtn)  createAccount();
            else if (e.getSource() == approveLoanBtn) approveLoan();
            else if (e.getSource() == repayLoanBtn)   recordRepayment();
            else if (e.getSource() == viewLoansBtn)   viewAccountLoans();
            else if (e.getSource() == deactivateAccBtn) deactivateAccount();
            else if (e.getSource() == reactivateAccBtn) reactivateAccount();
            else if (e.getSource() == closeLoanBtn)   closeLoan();
        }

        // Add customer
        void addCustomer() {
            new AddCustomerFrame(this);
        }

        // Create account
        void createAccount() {
            int row = custTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }

            String custId = (String) custModel.getValueAt(row, 0);
            String name = (String) custModel.getValueAt(row, 1);

            String[] types = {"SAVINGS", "CURRENT"};
            String accType = (String) JOptionPane.showInputDialog(
                    this,
                    "Create account for: " + name + " (" + custId + ")\nSelect account type:",
                    "Account Type",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    types,
                    types[0]
            );
            if (accType == null) return;

            String depositInput = JOptionPane.showInputDialog(this,
                    "Opening balance for " + accType + " account (Rs.):", "0");
            if (depositInput == null || depositInput.trim().isEmpty()) return;

            double openingBalance;
            try {
                openingBalance = Double.parseDouble(depositInput.trim());
                if (openingBalance < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid non-negative amount."); return;
            }

            try (Connection con = getConnection()) {
                String nextAccNo;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 'A'||LPAD(TO_CHAR(NVL(MAX(TO_NUMBER(SUBSTR(account_number,2))),0)+1),6,'0') " +
                                "FROM accounts")) {
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    nextAccNo = rs.getString(1);
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO accounts(account_number, customer_id, account_type, balance, loan_balance, status) " +
                                "VALUES(?, ?, ?, ?, 0, 'ACTIVE')")) {
                    ps.setString(1, nextAccNo);
                    ps.setString(2, custId);
                    ps.setString(3, accType);
                    ps.setDouble(4, openingBalance);
                    ps.executeUpdate();
                }

                JOptionPane.showMessageDialog(this,
                        accType + " account " + nextAccNo + " created for " + name + " with opening balance Rs." +
                                String.format("%.2f", openingBalance));
                loadAll();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        // Delete customer
        void deleteCustomer() {
            int row = custTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer first."); return; }

            String custId = (String) custModel.getValueAt(row, 0);
            String name   = (String) custModel.getValueAt(row, 1);

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT COUNT(*) FROM accounts WHERE customer_id = ?")) {
                ps.setString(1, custId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                int accCount = rs.getInt(1);
                if (accCount > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot delete " + name + ".\nThis customer has " + accCount +
                                    " account(s).\nDelete or reassign their accounts first.");
                    return;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete customer: " + name + " (" + custId + ")?\nThis cannot be undone.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "DELETE FROM customers WHERE customer_id = ?")) {
                ps.setString(1, custId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Customer " + name + " deleted.");
                loadCustomers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        // Approve loan
        void approveLoan() {
            int row = accTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an account first."); return; }

            String accNo  = (String) accModel.getValueAt(row, 0);
            String name   = (String) accModel.getValueAt(row, 2);
            String status = ((String) accModel.getValueAt(row, 6)).toUpperCase();
            double existing = (double) accModel.getValueAt(row, 5);

            // Check if account is closed
            if (!status.equals("ACTIVE")) {
                JOptionPane.showMessageDialog(this,
                        "Cannot approve loan. Account " + accNo + " is " + status.toLowerCase() + ".");
                return;
            }

            if (existing > 0) {
                JOptionPane.showMessageDialog(this,
                        name + " already has an active loan of Rs." + String.format("%.2f", existing) +
                                "\nPlease clear it before issuing a new one.");
                return;
            }

            String input = JOptionPane.showInputDialog(this,
                    "Approve loan for: " + name + " (" + accNo + ")\nEnter principal amount (Rs.):");
            if (input == null || input.trim().isEmpty()) return;

            double principal;
            try {
                principal = Double.parseDouble(input.trim());
                if (principal <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount."); return;
            }

            String interestInput = JOptionPane.showInputDialog(this,
                    "Enter annual interest rate (%):", "5");
            if (interestInput == null || interestInput.trim().isEmpty()) return;

            double interestRate;
            try {
                interestRate = Double.parseDouble(interestInput.trim());
                if (interestRate < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid interest rate."); return;
            }

            String durationInput = JOptionPane.showInputDialog(this,
                    "Enter loan duration (years):", "1");
            if (durationInput == null || durationInput.trim().isEmpty()) return;

            double duration;
            try {
                duration = Double.parseDouble(durationInput.trim());
                if (duration <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid duration."); return;
            }

            // Calculate total amount with interest using simple interest formula
            // Total = Principal + (Principal * Rate * Time / 100)
            double totalInterest = (principal * interestRate * duration) / 100;
            double totalAmount = principal + totalInterest;

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Loan Details:\n" +
                    "Principal Amount: Rs." + String.format("%.2f", principal) + "\n" +
                    "Interest Rate: " + String.format("%.2f", interestRate) + "% per annum\n" +
                    "Duration: " + String.format("%.2f", duration) + " years\n" +
                    "Total Interest: Rs." + String.format("%.2f", totalInterest) + "\n" +
                    "Total Amount: Rs." + String.format("%.2f", totalAmount) + "\n\n" +
                    "Approve this loan?",
                    "Confirm Loan Details", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection con = getConnection()) {
                con.setAutoCommit(false);
                String nextLoanId;

                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 'LN'||LPAD(TO_CHAR(NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(loan_id,'[0-9]+'))),0)+1),6,'0') FROM loans")) {
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    nextLoanId = rs.getString(1);
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance = balance + ?, loan_balance = ? WHERE account_number = ?")) {
                    ps.setDouble(1, totalAmount); 
                    ps.setDouble(2, totalAmount); 
                    ps.setString(3, accNo);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO loans(loan_id, account_number, loan_amount, amount_paid, status) " +
                                "VALUES(?, ?, ?, 0, 'ACTIVE')")) {
                    ps.setString(1, nextLoanId);
                    ps.setString(2, accNo);
                    ps.setDouble(3, totalAmount);
                    ps.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(this,
                        "Loan approved!\n\n" +
                        "Loan ID: " + nextLoanId + "\n" +
                        "Holder: " + name + "\n" +
                        "Principal: Rs." + String.format("%.2f", principal) + "\n" +
                        "Interest (" + String.format("%.2f", interestRate) + "% for " + 
                        String.format("%.2f", duration) + " years): Rs." + String.format("%.2f", totalInterest) + "\n" +
                        "Total Payable: Rs." + String.format("%.2f", totalAmount));
                loadAll();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        // Record repayment
        void recordRepayment() {
            int row = accTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an account first."); return; }

            String accNo   = (String) accModel.getValueAt(row, 0);
            String name    = (String) accModel.getValueAt(row, 2);
            double loanBal = (double) accModel.getValueAt(row, 5);

            if (loanBal <= 0) {
                JOptionPane.showMessageDialog(this, name + " has no active loan."); return;
            }

            String input = JOptionPane.showInputDialog(this,
                    "Repayment for: " + name + " (" + accNo + ")\n" +
                            "Remaining loan: Rs." + String.format("%.2f", loanBal) +
                            "\nEnter repayment amount:");
            if (input == null || input.trim().isEmpty()) return;

            double amount;
            try {
                amount = Double.parseDouble(input.trim());
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount."); return;
            }

            if (amount > loanBal) {
                JOptionPane.showMessageDialog(this,
                        "Repayment amount exceeds loan balance!\nMax repayment: Rs." + String.format("%.2f", loanBal));
                return;
            }

            double accountBalance = (double) accModel.getValueAt(row, 4);
            if (amount > accountBalance) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient account balance for repayment!\nAccount balance: Rs." + String.format("%.2f", accountBalance));
                return;
            }

            try (Connection con = getConnection()) {
                con.setAutoCommit(false);

                double newLoanBal = loanBal - amount;
                String loanStatus = (newLoanBal <= 0) ? "CLOSED" : "ACTIVE";

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance = balance - ?, loan_balance = ? WHERE account_number = ?")) {
                    ps.setDouble(1, amount); ps.setDouble(2, newLoanBal); ps.setString(3, accNo);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE loans SET amount_paid = amount_paid + ?, status = ? " +
                                "WHERE account_number = ? AND status = 'ACTIVE'")) {
                    ps.setDouble(1, amount); ps.setString(2, loanStatus); ps.setString(3, accNo);
                    ps.executeUpdate();
                }

                con.commit();
                String msg = "Repayment of Rs." + String.format("%.2f", amount) + " recorded.";
                if (loanStatus.equals("CLOSED")) msg += "\nLoan fully repaid!";
                else msg += "\nRemaining: Rs." + String.format("%.2f", newLoanBal);

                JOptionPane.showMessageDialog(this, msg);
                loadAll();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        // View loans for selected account
        void viewAccountLoans() {
            int row = accTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an account first."); return; }
            String accNo = (String) accModel.getValueAt(row, 0);
            String name  = (String) accModel.getValueAt(row, 2);
            new LoanHistoryFrame(accNo, name);
        }

        // Deactivate account
        void deactivateAccount() {
            int row = accTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an account first."); return; }

            String accNo = (String) accModel.getValueAt(row, 0);
            String name = (String) accModel.getValueAt(row, 2);
            String status = ((String) accModel.getValueAt(row, 6)).toUpperCase();

            // Check status
            if (!status.equals("ACTIVE")) {
                JOptionPane.showMessageDialog(this, "Only active accounts can be deactivated."); return;
            }

            // Check for active loans in database (primary check)
            int activeLoans = 0;
            try (Connection con = getConnection();
                 PreparedStatement chk = con.prepareStatement(
                         "SELECT COUNT(*) FROM loans WHERE account_number = ? AND status = 'ACTIVE'")) {
                chk.setString(1, accNo);
                ResultSet rs = chk.executeQuery();
                rs.next();
                activeLoans = rs.getInt(1);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error checking loans: " + ex.getMessage());
                return;
            }

            if (activeLoans > 0) {
                JOptionPane.showMessageDialog(this, "Cannot deactivate. This account has " + activeLoans + 
                        " active loan(s).\nPlease clear all loans before deactivating.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deactivate account " + accNo + " for " + name + "?",
                    "Confirm Deactivate", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "UPDATE accounts SET status = 'CLOSED' WHERE account_number = ?")) {
                ps.setString(1, accNo);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Account " + accNo + " deactivated successfully.");
                loadAll();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        // Reactivate account
        void reactivateAccount() {
            int row = accTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an account first."); return; }

            String accNo = (String) accModel.getValueAt(row, 0);
            String name = (String) accModel.getValueAt(row, 2);
            String status = ((String) accModel.getValueAt(row, 6)).toUpperCase();

            if (!status.equals("CLOSED")) {
                JOptionPane.showMessageDialog(this, "Only closed accounts can be reactivated."); return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reactivate account " + accNo + " for " + name + "?",
                    "Confirm Reactivate", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "UPDATE accounts SET status = 'ACTIVE' WHERE account_number = ?")) {
                ps.setString(1, accNo);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Account " + accNo + " reactivated.");
                loadAll();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        // Close loan
        void closeLoan() {
            int row = loanTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a loan first."); return; }

            String loanId = (String) loanModel.getValueAt(row, 0);
            String status = (String) loanModel.getValueAt(row, 6);
            if (status.equals("CLOSED")) { JOptionPane.showMessageDialog(this, "Loan is already closed."); return; }

            String name = (String) loanModel.getValueAt(row, 2);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Force-close loan " + loanId + " for " + name + "?\n(Use only for write-offs or corrections)",
                    "Confirm Close", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            String accNo = (String) loanModel.getValueAt(row, 1);
            try (Connection con = getConnection()) {
                con.setAutoCommit(false);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE loans SET status='CLOSED' WHERE loan_id=?")) {
                    ps.setString(1, loanId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET loan_balance=0 WHERE account_number=?")) {
                    ps.setString(1, accNo); ps.executeUpdate();
                }
                con.commit();
                JOptionPane.showMessageDialog(this, "Loan " + loanId + " closed.");
                loadAll();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    // Add customer
    static class AddCustomerFrame extends JFrame implements ActionListener {

        MainPanel panel;
        JTextField nameField   = new JTextField();
        JTextField emailField  = new JTextField();
        JTextField phoneField  = new JTextField();
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JButton saveBtn   = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        AddCustomerFrame(MainPanel panel) {
            this.panel = panel;
            setTitle("Add New Customer");
            setSize(380, 310);
            setLayout(null);
            setLocationRelativeTo(null);
            setResizable(false);

            JLabel title = new JLabel("Add New Customer");
            title.setBounds(100, 10, 200, 22);
            title.setFont(new Font("Arial", Font.BOLD, 13));
            add(title);

            JLabel nl = new JLabel("Full Name *:");  nl.setBounds(20,  50, 110, 25); add(nl);
            nameField.setBounds(135,  50, 210, 25); add(nameField);

            JLabel el = new JLabel("Email *:");      el.setBounds(20,  88, 110, 25); add(el);
            emailField.setBounds(135, 88, 210, 25); add(emailField);

            JLabel pl = new JLabel("Phone:");        pl.setBounds(20, 126, 110, 25); add(pl);
            phoneField.setBounds(135, 126, 210, 25);
            setupPhoneField();
            add(phoneField);

            JLabel gl = new JLabel("Gender:");       gl.setBounds(20, 164, 110, 25); add(gl);
            genderBox.setBounds(135, 164, 210, 25); add(genderBox);

            saveBtn.setBounds(60,   250, 100, 30); saveBtn.addActionListener(this);   add(saveBtn);
            cancelBtn.setBounds(205, 250, 100, 30); cancelBtn.addActionListener(this); add(cancelBtn);

            setVisible(true);
        }

        private void setupPhoneField() {
            phoneField.setDocument(new javax.swing.text.PlainDocument() {
                @Override
                public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
                    if (str == null) return;
                    
                    // Filter to allow only digits
                    String filtered = str.replaceAll("[^0-9]", "");
                    
                    // Check if adding this would exceed 10 digits
                    if (getLength() + filtered.length() > 10) {
                        filtered = filtered.substring(0, Math.max(0, 10 - getLength()));
                    }
                    
                    if (!filtered.isEmpty()) {
                        super.insertString(offset, filtered, attr);
                    }
                }
            });
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == cancelBtn) { dispose(); return; }

            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String gender = (String) genderBox.getSelectedItem();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Email are required."); return;
            }

            // Validate phone number - must be exactly 10 digits
            if (!phone.isEmpty()) {
                if (!phone.matches("\\d{10}")) {
                    JOptionPane.showMessageDialog(this, "Phone must be exactly 10 digits."); return;
                }
            }

            // Validate email format - must be username@gmail.com
            if (!email.matches("^[a-zA-Z0-9._%-]+@gmail\\.com$")) {
                JOptionPane.showMessageDialog(this, "Email must be in format: username@gmail.com"); return;
            }

            try (Connection con = getConnection()) {
                String nextCustId;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 'C'||TO_CHAR(NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(customer_id,'[0-9]+'))),0)+1) FROM customers")) {
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    nextCustId = rs.getString(1);
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO customers(customer_id, full_name, email, phone, gender) VALUES(?, ?, ?, ?, ?)")) {
                    ps.setString(1, nextCustId);
                    ps.setString(2, name);
                    ps.setString(3, email);
                    ps.setString(4, phone.isEmpty() ? null : phone);
                    ps.setString(5, gender);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Customer \"" + name + "\" added successfully.");
                panel.loadAll();
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    // Loan history
    static class LoanHistoryFrame extends JFrame {

        LoanHistoryFrame(String accNo, String name) {
            setTitle("Loan History - " + name);
            setSize(720, 380);
            setLayout(null);
            setLocationRelativeTo(null);
            setResizable(false);

            JLabel tl = new JLabel("Loan History  |  " + name + "  (" + accNo + ")");
            tl.setBounds(15, 10, 600, 22);
            tl.setFont(new Font("Arial", Font.BOLD, 13));
            add(tl);

            String[] cols = {"Loan ID", "Loan Amount", "Amount Paid", "Remaining", "Status", "Date Issued"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT loan_id, loan_amount, amount_paid, " +
                                 "(loan_amount - amount_paid), status, TO_CHAR(issue_date,'DD-MON-YYYY') " +
                                 "FROM loans WHERE account_number = ? ORDER BY issue_date DESC")) {
                ps.setString(1, accNo);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    model.addRow(new Object[]{
                            rs.getString(1), rs.getDouble(2), rs.getDouble(3),
                            rs.getDouble(4), rs.getString(5), rs.getString(6)});
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }

            JTable table = new JTable(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBounds(10, 40, 695, 290);
            add(scroll);

            JButton close = new JButton("Close");
            close.setBounds(310, 338, 100, 28);
            close.addActionListener(ev -> dispose());
            add(close);

            setVisible(true);
        }
    }
}