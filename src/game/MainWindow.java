package game;

import javax.swing.*;

import game.GameBoard.GameState;

import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame implements ControlPanel.ControlPanelListener {
    private GameBoard board;
    private ControlPanel controlPanel;
    private JButton[][] buttons;
    private JPanel gamePanel;
    private int cellSize = 0; 
    private Statistics statistics = new Statistics();
    
    public MainWindow() {
        setTitle("Сапёр");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(300, 550));
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (buttons == null) return;
                
                int cols = buttons[0].length;
                int rows = buttons.length;
                
                int cell = Math.max(25, (getWidth() - 16) / cols);
                
                int controlHeight = controlPanel.getHeight();
                int targetHeight = cell * rows + controlHeight + 39;
                
                if (Math.abs(getHeight() - targetHeight) > 5) {
                    setSize(getWidth(), targetHeight);
                }
                
                resizeButtons();
                updateButtons();
            }
        });
        
        initializeGame(9, 9, 10);
        setVisible(true);
    }
    
    private void initializeGame(int width, int height, int mines) {
        if(gamePanel != null) {
            getContentPane().removeAll();
        }

        board = new GameBoard(width, height, mines);

        int difficultyToUse = 0;
        if (controlPanel != null) {
            difficultyToUse = controlPanel.getCurrentDifficulty();
        }

        controlPanel = new ControlPanel(board, this, difficultyToUse);
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);

        gamePanel = new JPanel(new GridLayout(height, width, 1, 1));
        gamePanel.setBackground(Color.DARK_GRAY);
        buttons = new JButton[height][width];
        initializeButtons();
        add(gamePanel, BorderLayout.CENTER);

        setResizable(true);
        setLocationRelativeTo(null);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int maxCellW = (int)((screen.width * 0.7) / width);
        int maxCellH = (int)((screen.height * 0.7) / height);
        int startCell = Math.min(40, Math.min(maxCellW, maxCellH));
        int minCell = 30;

        int minW = minCell * width + 16;
        int minH = minCell * height + controlPanel.getHeight() + 39;
        setMinimumSize(new Dimension(minW, minH));
        setSize(startCell * width + 16, startCell * height + controlPanel.getHeight() + 39);

        revalidate();
        repaint();
    }

    private void initializeButtons() {
        gamePanel.removeAll();
        
        for (int y = 0; y < buttons.length; y++) {
            for (int x = 0; x < buttons[0].length; x++) {
                JButton button = new JButton();
                button.setFocusPainted(false);
                button.setMargin(new Insets(0, 0, 0, 0));
                button.putClientProperty("row", y);
                button.putClientProperty("col", x);
                button.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        buttonClick(e, button);
                    }
                });
                gamePanel.add(button);
                buttons[y][x] = button;
            }
        }
        SwingUtilities.invokeLater(() -> {
            resizeButtons();
            updateButtons();
        });
    }

    private void resizeButtons() {
        Dimension panelSize = gamePanel.getSize();
        if (panelSize.width == 0 || panelSize.height == 0) return;

        int cols = buttons[0].length;
        int rows = buttons.length;

        int cellW = (panelSize.width - cols) / cols;
        int cellH = (panelSize.height - rows) / rows;
        cellSize = Math.min(cellW, cellH);

        int fontSize = Math.max(4, (int)(cellSize * 0.35));
        Font font = new Font("Arial", Font.BOLD, fontSize);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                JButton btn = buttons[y][x];
                btn.setFont(font);
                btn.setPreferredSize(new Dimension(cellSize, cellSize));
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setBorderPainted(false);
            }
        }
        revalidate();
        repaint();
    }
    
    private void buttonClick(MouseEvent e, JButton button) {
        if (board.isGameOver()) return;
        
        if(board.getGameState() == GameState.NOT_STARTED) {
        	controlPanel.onGameStarted();
        }
        
        int row = (int) button.getClientProperty("row");
        int col = (int) button.getClientProperty("col");
        
        if(SwingUtilities.isLeftMouseButton(e)) {
            board.leftClick(col, row);
        } else if(SwingUtilities.isRightMouseButton(e)) {
            board.rightClick(col, row);
        }
        
        updateButtons();
        controlPanel.updateDisplay();
        
        if (board.isGameOver()) {
        	controlPanel.onGameFinished();
            showGameResults();
        }
    }
    
    private void updateButtons() {
        for (int y = 0; y < buttons.length; y++) {
            for (int x = 0; x < buttons[0].length; x++) {
                updateButton(x, y);
            }
        }
    }
    
    private void updateButton(int x, int y) {
        JButton button = buttons[y][x];
        Cell cell = board.getCell(x, y);
        
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        
        int fontSize = Math.max(12, (int)(cellSize * 0.5));
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        
        if(cell.isOpen()) {
            if(cell.isMine()) {
                button.setText("💣");
                button.setBackground(Color.RED);
                button.setForeground(Color.BLACK);
            } else {
                int minesAround = cell.getMinesAround();
                if(minesAround > 0) {
                    button.setText(String.valueOf(minesAround));
                    setNumberColor(button, minesAround);
                } else {
                    button.setText("");
                }
                button.setBackground(Color.LIGHT_GRAY);
            }
        } else if (cell.isFlag()) {
            button.setText("⚑");
            button.setBackground(Color.YELLOW);
            button.setForeground(Color.RED);
            button.setFont(new Font("Arial", Font.BOLD, Math.max(14, fontSize)));
        } else {
            button.setText("");
            button.setBackground(new Color(200, 220, 255));
            button.setEnabled(true);
        }
    }

    private void setNumberColor(JButton button, int number) {
        Color color;
        switch (number) {
            case 1:
                color = new Color(0, 0, 255);
                break;
            case 2:
                color = new Color(0, 128, 0);
                break;
            case 3:
                color = new Color(255, 0, 0);
                break;
            case 4:
                color = new Color(0, 0, 128);
                break;
            case 5:
                color = new Color(128, 0, 0);
                break;
            case 6:
                color = new Color(0, 128, 128);
                break;
            case 7:
                color = Color.BLACK;
                break;
            case 8:
                color = Color.GRAY;
                break;
            default:
                color = Color.BLACK;
                break;
        }

        button.setForeground(color);
        button.setOpaque(true);
        button.setBorderPainted(false);
    }
    
    private void showGameResults() {
    	int difficulty = controlPanel.getCurrentDifficulty();
        int time = controlPanel.getElapsedTime();
        boolean win = board.isVictory();
    	statistics.recordGame(difficulty, win, time);
    	
        if (board.isVictory()) {
        	JOptionPane.showMessageDialog(this, 
                    "🎉 Поздравляем! Вы победили! 🎉\nВремя:" + controlPanel.getElapsedTime() + " сек",
                    "Победа!", 
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
        	JOptionPane.showMessageDialog(this, 
                    "💥 Вы наступили на мину! 💥\nПопробуйте еще раз!",
                    "Поражение", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showStatistics() {
        JDialog dialog = new JDialog(this, "Статистика", true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 4, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        panel.add(new JLabel(""));
        panel.add(bold("Новичок"));
        panel.add(bold("Любитель"));
        panel.add(bold("Эксперт"));

        panel.add(bold("Игр сыграно:"));
        panel.add(new JLabel(String.valueOf(statistics.getGames(0))));
        panel.add(new JLabel(String.valueOf(statistics.getGames(1))));
        panel.add(new JLabel(String.valueOf(statistics.getGames(2))));

        panel.add(bold("Процент побед:"));
        panel.add(new JLabel(statistics.getWinRate(0)));
        panel.add(new JLabel(statistics.getWinRate(1)));
        panel.add(new JLabel(statistics.getWinRate(2)));

        panel.add(bold("Лучшее время:"));
        panel.add(new JLabel(statistics.getBestTime(0)));
        panel.add(new JLabel(statistics.getBestTime(1)));
        panel.add(new JLabel(statistics.getBestTime(2)));

        dialog.add(panel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel bottom = new JPanel();
        bottom.add(closeButton);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private JLabel bold(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        return label;
    }
    
    @Override
    public void onShowStatistics() {
        showStatistics();
    }
    
    @Override
    public void onNewGame(int width, int height, int mines) { 
    	initializeGame(width, height, mines);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow();
        });
    }
}