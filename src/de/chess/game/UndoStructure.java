package de.chess.game;

public class UndoStructure {
	
    private int enPassant;
    private int castlePerms;
    private int fiftyMoveCounter;
    
    public UndoStructure() {}
    
    public UndoStructure(int enPassant, int castlePerms, int fiftyMoveCounter) {
        this.enPassant = enPassant;
        this.castlePerms = castlePerms;
        this.fiftyMoveCounter = fiftyMoveCounter;
    }
    
	public int getEnPassant() {
		return enPassant;
	}
	
	public void setEnPassant(int enPassant) {
		this.enPassant = enPassant;
	}
	
	public int getCastlePerms() {
		return castlePerms;
	}
	
	public void setCastlePerms(int castlePerms) {
		this.castlePerms = castlePerms;
	}
	
	public int getFiftyMoveCounter() {
		return fiftyMoveCounter;
	}
	
	public void setFiftyMoveCounter(int fiftyMoveCounter) {
		this.fiftyMoveCounter = fiftyMoveCounter;
	}
	
}
