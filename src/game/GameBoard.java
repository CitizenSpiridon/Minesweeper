package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameBoard {

	private Cell[][] grid;
	private int width;
	private int height;
	private int totalMines;
	private boolean minesPlaced = false;
	private GameState gameState = GameState.NOT_STARTED;
	private int correctFlags = 0;
	private int placedFlags = 0;
	
	public GameBoard(int width, int height, int mines) {
		this.width = width;
		this.height = height;
		totalMines = mines;
		grid = new Cell[width][height];
	
	
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				grid[i][j] = new Cell();
			}
		}
	}
	
	public Cell getCell(int x, int y) {
        return grid[x][y];
    }
	
	public GameState getGameState() {
	    return gameState;
	}
	
	public boolean isVictory() {
        return checkVictory();
    }
	
	public boolean isGameOver() {
        return gameState == GameState.VICTORY || gameState == GameState.DEFEAT;
    }
	
	public int getRemainingMines() {
    	return totalMines - placedFlags;
    }

	public void placeMines (int firstClickX, int firstClickY) {
		if(minesPlaced) {
			throw new IllegalStateException("Мины уже расставлены!");
		}
		
		List<int[]> availablePositions = new ArrayList<>();
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if(!(x >= firstClickX - 1 && x <= firstClickX + 1 &&
					      y >= firstClickY - 1 && y <= firstClickY + 1)) {
//				if(!((x >= firstClickX - 1 && x <= firstClickX + 1) && 
//				   (y >= firstClickY - 1 && y <= firstClickY + 1))) {
					availablePositions.add(new int[]{x, y});
				}
			}
		}
		
		Collections.shuffle(availablePositions);
		
		for(int i  = 0; i < totalMines; i++) {
			int[] position = availablePositions.get(i);
			grid[position[0]][position[1]].setMine(true);
			
		}
		
		minesPlaced = true;
		
		setMines();	
	}
	
	private void setMines() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int mineCount = countMinesAround(x, y);
				grid[x][y].setMinesAround(mineCount);
			}
		}
	}
	
	
	private int countMinesAround(int x, int y) {
		int count = 0;
		
		if(!grid[x][y].isMine()) {
			for(int dx = -1; dx <= 1; dx++) {
				for(int dy = -1; dy <= 1; dy++) {
					if(dx == 0 && dy == 0) continue;
					
					int adjacentX = x + dx;
					int adjacentY = y + dy;
					
					if(adjacentX >=0 && adjacentX < width && adjacentY >= 0 && adjacentY < height) {
						if (grid[adjacentX][adjacentY].isMine()) {
							count++;
						}
					}
				}
			}
		}
		return count;
	}
	
	public enum GameState {
		NOT_STARTED,
		IN_PROGRESS,
		VICTORY,
		DEFEAT
	}
	
	private void revealCell(int x, int y) {
		if(grid[x][y].isFlag() || grid[x][y].isOpen()) {
			return;
		}
		
		grid[x][y].setOpen(true);
		
		if(grid[x][y].isMine()) {
			gameState = GameState.DEFEAT;
			return;
		}
		
		if(grid[x][y].getMinesAround() == 0) {
			for(int dx = -1; dx <= 1; dx++) {
				for(int dy = -1; dy <= 1; dy++) {
					
					int adjacentX = x + dx;
					int adjacentY = y + dy;
					
					if(adjacentX >=0 && adjacentX < width && adjacentY >= 0 && adjacentY < height) {
						revealCell(adjacentX, adjacentY);
					}				
				}
			}
		}
	}
	
	private void toggleFlag(int x, int y) {
		boolean wasFlagged = grid[x][y].isFlag();	
		grid[x][y].setFlag(!wasFlagged);
			
		if(wasFlagged) {
			placedFlags --;
			if(grid[x][y].isMine()) correctFlags--;
		} else {
			placedFlags++;
			if(grid[x][y].isMine()) correctFlags++;
		}
	}
	
	private boolean checkVictory() {
	    for (int x = 0; x < width; x++)
	        for (int y = 0; y < height; y++)
	            if (!grid[x][y].isMine() && !grid[x][y].isOpen())
	                return false;
	    return true;
	}
	
	public void leftClick(int x, int y) {
	    if(gameState == GameState.NOT_STARTED) {
	        placeMines(x, y);
	        gameState = GameState.IN_PROGRESS;
	    }
	    
	    if(gameState == GameState.IN_PROGRESS) {
	        revealCell(x, y);
	        if(checkVictory()) {      
	            gameState = GameState.VICTORY;
	        }
	    }
	}
	
	public void rightClick(int x, int y) {
		if(gameState == GameState.IN_PROGRESS) {
			toggleFlag(x, y);
			if(checkVictory()) {
				gameState = GameState.VICTORY;
			}
		}
	}
}