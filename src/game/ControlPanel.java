package game;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;

import game.GameBoard.GameState;

public class ControlPanel extends JPanel {
	private int currentDifficulty = 0;
	private JLabel minesLabel;
	private JLabel timerLabel;
	private JButton newGameButton;
	private JComboBox<String> choosenDifficulty;
	
	private GameBoard board;
	private Timer gameTimer;
	private int elapsedTime = 0;
	
	public interface ControlPanelListener {
		void onNewGame(int width, int height, int mines);
		void onShowStatistics();
	}
	
	private ControlPanelListener listener;
	
	public ControlPanel(GameBoard board, ControlPanelListener listener, int initialDifficulty) {
		this.board = board;
		this.listener = listener;
		this.currentDifficulty = initialDifficulty;
		
		setLayout(new FlowLayout());
		initializeComponents();
		initializeTimer();
		
		addComponentListener(new ComponentAdapter() {
		        @Override
		        public void componentResized(ComponentEvent e) {
		            updateLayout();
		        }
		    });
	}
	
	public int getElapsedTime() {
        return elapsedTime;
    }
	
	public int getCurrentDifficulty() {
        return currentDifficulty;
    }
	
	private void initializeComponents() {
		minesLabel = new JLabel("Мин: " + board.getRemainingMines());
		minesLabel.setFont(new Font("Arial", Font.BOLD, 16));
		
		timerLabel = new JLabel("Время : 0");
		timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
		
		newGameButton = new JButton("Новая игра");
		newGameButton.addActionListener(e -> startNewGame());
		
		choosenDifficulty = new JComboBox<>(new String[] {
			"Новичок (9х9, 10 мин)",
			"Любитель (16х16, 40 мин)",
			"Эксперт (16х30, 99 мин)"
		});
		choosenDifficulty.setSelectedIndex(currentDifficulty);
		choosenDifficulty.addActionListener(e -> changeDifficulty());
		
		updateLayout();
	}
	
	private void updateLayout() {
	    removeAll();
	    
	    if (getWidth() < 600) {
	        JButton menuButton = new JButton("☰");
	        menuButton.addActionListener(e -> showMenu());
	        add(minesLabel);
	        add(timerLabel);
	        add(menuButton);
	    } else {
	    	add(minesLabel);
	        add(Box.createHorizontalStrut(20));
	        add(timerLabel);
	        add(Box.createHorizontalStrut(20));
	        add(newGameButton);
	        add(Box.createHorizontalStrut(20));
	        add(choosenDifficulty);
	        add(Box.createHorizontalStrut(20));
	        JButton statsButton = new JButton("Статистика");
	        statsButton.addActionListener(e -> listener.onShowStatistics());
	        add(statsButton);
	    }
	    
	    revalidate();
	    repaint();
	}

	private void showMenu() {
	    JPopupMenu menu = new JPopupMenu();
	    
	    JMenuItem newGame = new JMenuItem("Новая игра");
	    newGame.addActionListener(e -> startNewGame());
	    menu.add(newGame);
	    
	    JMenuItem statsItem = new JMenuItem("Статистика");
	    statsItem.addActionListener(e -> listener.onShowStatistics());
	    menu.add(statsItem);
	    
	    JMenu difficulty = new JMenu("Сложность");
	    String[] levels = {"Новичок", "Любитель", "Эксперт"};
	    for (int i = 0; i < levels.length; i++) {
	        final int index = i;
	        JMenuItem item = new JMenuItem(levels[i]);
	        item.addActionListener(e -> choosenDifficulty.setSelectedIndex(index));
	        difficulty.add(item);
	    }
	    menu.add(difficulty);
	    
	    menu.show(this, 0, getHeight());
	}
	
	private void initializeTimer() {
		gameTimer = new Timer(1000, e -> {
			if(board.getGameState() == GameState.IN_PROGRESS) {
				elapsedTime++;
				updateTimer();
			}
		});
	}
	
	private void startNewGame() {
		gameTimer.stop();
		elapsedTime = 0;
		
		int width, height, mines;
		
		int selectedDiff = choosenDifficulty.getSelectedIndex();
	    currentDifficulty = selectedDiff;
		switch (selectedDiff) {
		case 0: width = 9; height = 9; mines = 10; break;
		case 1: width = 16; height = 16; mines = 40; break;
		case 2: width = 30; height = 16; mines = 99; break;
		default: width = 9; height = 9; mines = 10;
		}
		
		listener.onNewGame(width, height, mines);
	}
	
	private void changeDifficulty() {
		int newDifficulty = choosenDifficulty.getSelectedIndex();
		if(board.getGameState() == GameState.IN_PROGRESS || board.getGameState() == GameState.NOT_STARTED) {
			int result = JOptionPane.showConfirmDialog(this,
					"Сменить сложнось? Текущая игра будет сброшена.",
					"Подтверждение", JOptionPane.YES_NO_OPTION);
			
			if(result == JOptionPane.YES_OPTION) {
				currentDifficulty = newDifficulty;
				startNewGame();
			} else {
				choosenDifficulty.setSelectedIndex(currentDifficulty);
			}
		} else {
			currentDifficulty = newDifficulty;
		}
	}
	
	public void startTimer() {
		elapsedTime = 0;
		gameTimer.start();
	}
	
	public void stopTimer() {
		gameTimer.stop();
	}
	
	public void updateDisplay() {
		minesLabel.setText("Мин: " + board.getRemainingMines());
		updateTimer();
	}
	
	private void updateTimer() {
		timerLabel.setText("Время: " + elapsedTime);
	}
	
	public void onGameStarted() {
		startTimer();
	}
	
	public void onGameFinished() {
		stopTimer();
	}
}