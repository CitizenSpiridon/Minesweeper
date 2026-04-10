package game;

public class Cell {
	
	private boolean mine;
	private boolean flag;
	private boolean open;
	private int minesAround;

	public Cell() {
		mine = false; 
		flag = false;
		open = false;
		minesAround = 0;
	}
	
	
	
	public boolean isMine() {
		return mine;
	}
	
	public boolean isFlag() {
		return flag;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public int getMinesAround() {
		return minesAround;
	}
	
	public void setMine(boolean value) {
		mine = value;
	}
	
	public void setFlag(boolean value) {
		flag = value;
	}
	
	public void setOpen(boolean value) {
		open = value;
	}
	
	public void setMinesAround(int number) {
		minesAround = number;
	}
}
