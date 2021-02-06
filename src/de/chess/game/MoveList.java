package de.chess.game;

public class MoveList {
	
	private Move[] moves;
	
	private int count;
	
	public MoveList() {
		moves = new Move[BoardConstants.MAX_POSSIBLE_MOVES];
	}
	
	public Move[] getMoves() {
		return moves;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setMoves(Move[] moves) {
		this.moves = moves;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void addMove(int from, int to, int captured, int promoted, int flag) {
		moves[count] = new Move(from, to, captured, promoted, flag);
		count++;
	}
	
	public Move getMove(int i) {
		return moves[i];
	}
	
	public void setMove(Move move, int i) {
		moves[i] = move;
	}
	
}
