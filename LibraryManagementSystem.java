package lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class LibraryManagementSystem extends JFrame {

    // Database connection details
    private static final String DATABASE_URL = "jdbc:ucanaccess://C:/Users/USER/Desktop/Library.accdb";

    // GUI components
    private JTextField bookIDField, titleField, authorField, yearField;
    private JButton addButton, deleteButton, refreshButton;
    private JTable booksTable;
    private DefaultTableModel tableModel;

    public LibraryManagementSystem() {
        // Create and set up the window
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2));
        formPanel.add(new JLabel("Book ID:"));
        bookIDField = new JTextField();
        formPanel.add(bookIDField);

        formPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        formPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        formPanel.add(authorField);

        formPanel.add(new JLabel("Year:"));
        yearField = new JTextField();
        formPanel.add(yearField);

        addButton = new JButton("Add Book");
        formPanel.add(addButton);

        // Add action listener to the add button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBookToDatabase();
            }
        });

        deleteButton = new JButton("Delete Book");
        formPanel.add(deleteButton);

        // Add action listener to the delete button
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedBook();
            }
        });

        refreshButton = new JButton("Refresh List");
        formPanel.add(refreshButton);

        // Add action listener to the refresh button
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchBooksFromDatabase();
            }
        });

        // Create the table model and JTable
        tableModel = new DefaultTableModel(new Object[] { "Book ID", "Title", "Author", "Year" }, 0);
        booksTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(booksTable);

        // Add components to the frame
        add(formPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        // Fetch and display books from the database
        fetchBooksFromDatabase();

        // Display the window
        setVisible(true);
    }

    private void addBookToDatabase() {
        String bookID = bookIDField.getText();
        String title = titleField.getText();
        String author = authorField.getText();
        String year = yearField.getText();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String query = "INSERT INTO Books (BookID, Title, Author, Year) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, bookID);
            statement.setString(2, title);
            statement.setString(3, author);
            statement.setInt(4, Integer.parseInt(year));

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Book added successfully!");
                fetchBooksFromDatabase(); // Refresh the table
            }

            // Clear fields after insertion
            bookIDField.setText("");
            titleField.setText("");
            authorField.setText("");
            yearField.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding book to the database.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow >= 0) {
            String bookID = (String) booksTable.getValueAt(selectedRow, 0);

            try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
                String query = "DELETE FROM Books WHERE BookID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, bookID);

                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                    fetchBooksFromDatabase(); // Refresh the table
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting book from the database.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void fetchBooksFromDatabase() {
        // Clear existing rows
        tableModel.setRowCount(0);

        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            String query = "SELECT * FROM Books";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String bookID = resultSet.getString("BookID");
                String title = resultSet.getString("Title");
                String author = resultSet.getString("Author");
                int year = resultSet.getInt("Year");

                tableModel.addRow(new Object[] { bookID, title, author, year });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching books from the database.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Schedule a job for the event dispatch thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LibraryManagementSystem();
            }
        });
    }
}