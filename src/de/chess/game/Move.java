package de.chess.game;

public class Move {
	
    private int from;
    private int to;
    
    private int captured;
    private int promoted;
    
    private int flag;
    
    private int score;
    
	public Move(int from, int to, int captured, int promoted, int flag) {
    	this.from = from;
    	this.to = to;
    	
    	this.captured = captured;
    	this.promoted = promoted;
    	
    	this.flag = flag;
	}
	
	public int getHash() {
		return from | (to << 7) | (promoted << 14);
	}
    
	public int getFrom() {
		return from;
	}
	
	public int getTo() {
		return to;
	}
	
	public int getCaptured() {
		return captured;
	}
	
	public int getPromoted() {
		return promoted;
	}
	
	public int getFlag() {
		return flag;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	@Override
	public String toString() {
		return "Move[to = "+to+", from = "+from+", captured = "+captured+", promoted = "+promoted+", flag = "+flag+"]";
	}
	
}