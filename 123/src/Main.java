// Main.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Pattern;

public class Main extends JFrame {
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JTextField fieldN, fieldTime, fieldResult;

    // Регулярное выражение для проверки положительного числа (с плавающей точкой)
    private static final Pattern POSITIVE_NUMBER = Pattern.compile("^\\d*\\.?\\d+$");

    public Main() {
        initUI();
    }

    private void initUI() {
        setTitle("Трекер производительности (Arduino + LCD)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === Поля ввода ===
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        inputPanel.add(new JLabel("Число N:"));
        fieldN = new JTextField();
        inputPanel.add(fieldN);

        inputPanel.add(new JLabel("Время (сек):"));
        fieldTime = new JTextField();
        inputPanel.add(fieldTime);

        inputPanel.add(new JLabel("Получившееся число:"));
        fieldResult = new JTextField();
        inputPanel.add(fieldResult);

        // === Кнопки ===
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAdd = new JButton("Добавить");
        JButton btnDelete = new JButton("Удалить");
        JButton btnClear = new JButton("Очистить");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        inputPanel.add(new JLabel());
        inputPanel.add(buttonPanel);

        add(inputPanel, BorderLayout.NORTH);

        // === Таблица ===
        String[] columns = {"Число N", "Время (сек)", "Получившееся число"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // запрет редактирования
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setGridColor(Color.LIGHT_GRAY);
        dataTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dataTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);

        // === Стилизация ===
        getContentPane().setBackground(Color.WHITE);
        inputPanel.setBackground(Color.WHITE);
        buttonPanel.setBackground(Color.WHITE);

        btnAdd.setBackground(new Color(46, 125, 50));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);

        btnDelete.setBackground(new Color(211, 47, 47));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);

        btnClear.setBackground(new Color(255, 143, 0));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);

        // === Обработчики ===
        btnAdd.addActionListener(e -> addRow());
        btnDelete.addActionListener(e -> deleteRow());
        btnClear.addActionListener(e -> clearFields());

        // === Горячие клавиши ===
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl N"), "add");
        getRootPane().getActionMap().put("add", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { addRow(); }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        getRootPane().getActionMap().put("delete", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { deleteRow(); }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "clear");
        getRootPane().getActionMap().put("clear", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { clearFields(); }
        });

        // === Валидация времени ===
        fieldTime.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = fieldTime.getText();
                if (!text.isEmpty() && !POSITIVE_NUMBER.matcher(text).matches()) {
                    JOptionPane.showMessageDialog(null,
                            "Разрешены только положительные числа (например: 0.00142).",
                            "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
                    fieldTime.setText(text.replaceAll("[^\\d.]", ""));
                }
            }
        });
    }

    private void addRow() {
        String nStr = fieldN.getText().trim();
        String timeStr = fieldTime.getText().trim();
        String resultStr = fieldResult.getText().trim();

        if (nStr.isEmpty() || timeStr.isEmpty() || resultStr.isEmpty()) {
            showError("Все поля должны быть заполнены.");
            return;
        }

        if (!POSITIVE_NUMBER.matcher(timeStr).matches()) {
            showError("Время должно быть положительным числом.");
            return;
        }

        try {
            int n = Integer.parseInt(nStr);
            double time = Double.parseDouble(timeStr);
            long result = Long.parseLong(resultStr);

            tableModel.addRow(new Object[]{
                    n,
                    String.format("%.5f", time),
                    result
            });

            clearFields();
            JOptionPane.showMessageDialog(this, "Строка добавлена!", "Успех", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            showError("Числовые поля содержат некорректные значения.");
        }
    }

    private void deleteRow() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Выберите строку для удаления.");
            return;
        }

        tableModel.removeRow(selectedRow);
        JOptionPane.showMessageDialog(this, "Строка удалена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearFields() {
        fieldN.setText("");
        fieldTime.setText("");
        fieldResult.setText("");
        fieldN.requestFocus();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            } catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}
