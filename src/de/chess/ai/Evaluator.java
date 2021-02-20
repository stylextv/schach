package de.chess.ai;

import de.chess.game.Board;
import de.chess.game.PieceCode;

public class Evaluator {
	
	private static final int PAWN_VALUE = 30;
	private static final int KNIGHT_VALUE = 96;
	private static final int BISHOP_VALUE = 99;
	private static final int ROOK_VALUE = 153;
	private static final int QUEEN_VALUE = 264;
	private static final int KING_VALUE = 120;
	
	private static final int[] PAWN_TABLE = new int[] {
			 0,   0,   0,   0,   0,   0,   0,   0, 
			 5,  10,  10, -20, -20,  10,  10,   5, 
			 5,  -5, -10,   0,   0, -10,  -5,   5, 
			 0,   0,   0,  20,  20,   0,   0,   0, 
			 5,   5,  10,  25,  25,  10,   5,   5, 
			10,  10,  20,  30,  30,  20,  10,  10, 
			50,  50,  50,  50,  50,  50,  50,  50, 
			 0,   0,   0,   0,   0,   0,   0,   0
	};
	
	private static final int[] KNIGHT_TABLE = new int[] {
			-50, -40, -30, -30, -30, -30, -40, -50, 
			-40, -20,   0,   5,   5,   0, -20, -40, 
			-30,   5,  10,  15,  15,  10,   5, -30, 
			-30,   0,  15,  20,  20,  15,   0, -30, 
			-30,   5,  15,  20,  20,  15,   5, -30, 
			-30,   0,  10,  15,  15,  10,   0, -30, 
			-40, -20,   0,   0,   0,   0, -20, -40, 
			-50, -40, -30, -30, -30, -30, -40, -50
	};
	
	private static final int[] BISHOP_TABLE = new int[]{
			-20, -10, -10, -10, -10, -10, -10, -20, 
			-10,   5,   0,   0,   0,   0,   5, -10, 
			-10,  10,  10,  10,  10,  10,  10, -10, 
			-10,   0,  10,  10,  10,  10,   0, -10, 
			-10,   5,   5,  10,  10,   5,   5, -10, 
			-10,   0,   5,  10,  10,   5,   0, -10, 
			-10,   0,   0,   0,   0,   0,   0, -10, 
			-20, -10, -10, -10, -10, -10, -10, -20
	};
	
	private static final int[] ROOK_TABLE = new int[]{
			 0,   0,   0,   5,   5,   0,   0,   0, 
			-5,   0,   0,   0,   0,   0,   0,  -5, 
			-5,   0,   0,   0,   0,   0,   0,  -5, 
			-5,   0,   0,   0,   0,   0,   0,  -5, 
			-5,   0,   0,   0,   0,   0,   0,  -5, 
			-5,   0,   0,   0,   0,   0,   0,  -5, 
			 5,  10,  10,  10,  10,  10,  10,   5, 
			 0,   0,   0,   0,   0,   0,   0,   0
	};
	
	private static final int[] QUEEN_TABLE = new int[]{
			-20, -10, -10,  -5,  -5,   -10, -10, -20, 
			-10,   0,   5,   0,   0,   0,   0, -10, 
			-10,   5,   5,   5,   5,   5,   0, -10, 
			  0,   0,   5,   5,   5,   5,   0,  -5, 
			 -5,   0,   5,   5,   5,   5,   0,  -5, 
			-10,   0,   5,   5,   5,   5,   0, -10, 
			-10,   0,   0,   0,   0,   0,   0, -10, 
			-20, -10, -10,  -5,  -5, -10, -10, -20
	};
	
	private static final int[] KING_TABLE = new int[]{
			 20,  30,  10,   0,   0,  10,  30,  20, 
			 20,  20,   0,   0,   0,   0,  20,  20, 
			-10, -20, -20, -20, -20, -20, -20, -10, 
			-20, -30, -30, -40, -40, -30, -30, -20, 
			-30, -40, -40, -50, -50, -40, -40, -30, 
			-30, -40, -40, -50, -50, -40, -40, -30, 
			-30, -40, -40, -50, -50, -40, -40, -30, 
			-30, -40, -40, -50, -50, -40, -40, -30
	};
	
	private static final int[] KING_TABLE_ENDGAME = new int[]{
			-50, -30,-30, -30, -30, -30, -30, -50,
			-30, -30,  0,   0,   0,   0, -30, -30,
			-30, -10, 20,  30,  30,  20, -10, -30,
			-30, -10, 30,  40,  40,  30, -10, -30,
			-30, -10, 30,  40,  40,  30, -10, -30,
			-30, -10, 20,  30,  30,  20, -10, -30,
			-30, -20,-10,   0,   0, -10, -20, -30,
			-50, -40,-30, -20, -20, -30, -40, -50
	};
	
	private static final int[][] TABLES = new int[][] {
			null,
			null,
			PAWN_TABLE,
			KNIGHT_TABLE,
			BISHOP_TABLE,
			ROOK_TABLE,
			QUEEN_TABLE,
			KING_TABLE,
			KING_TABLE_ENDGAME
	};
	
	private static final int[] VALUES = new int[] {
			0,
			0,
			PAWN_VALUE,
			KNIGHT_VALUE,
			BISHOP_VALUE,
			ROOK_VALUE,
			QUEEN_VALUE,
			KING_VALUE
	};
    
    private static final int[] MIRROR_TABLE = {
    		56,  57,  58,  59,  60,	 61,  62,  63,
    		48,	 49,  50,  51,  52,	 53,  54,  55,
    		40,	 41,  42,  43,  44,	 45,  46,  47,
    		32,	 33,  34,  35,  36,	 37,  38,  39,
    		24,	 25,  26,  27,  28,	 29,  30,  31,
    		16,  17,  18,  19,  20,	 21,  22,  23,
    		 8,   9,  10,  11,  12,  13,  14,  15,
    		 0,   1,   2,   3,   4,   5,   6,	7
    };
    
	static {
		for(int i=PieceCode.PAWN; i<=PieceCode.LAST; i++) {
			int[] table = TABLES[i];
			
			int base = VALUES[i];
			
			for(int j=0; j<64; j++) {
				int m = table[j];
				
				table[j] = Math.round(base + base * m / 300f);
			}
		}
	}
	
	public static int eval(Board b) {
		b.countPieces();
		
		boolean isEndgame = b.isEndgame();
		
		int score = 0;
		
		for(int i=0; i<12; i++) {
			int color = PieceCode.getColorFromSpriteCode(i);
			int type = PieceCode.getTypeFromSpriteCode(i);
			
			for(int j=0; j<b.getPieceAmount(i); j++) {
				
				if(isEndgame && type == PieceCode.KING) type++;
				
				int index = b.getPieceIndex(i, j);
				
				if(color == PieceCode.WHITE) {
					index = MIRROR_TABLE[index];
					
					score += TABLES[type][index];
				} else {
					score -= TABLES[type][index];
				}
			}
		}
		
		if(b.getSide() == PieceCode.WHITE) return score;
		return -score;
	}
	
	public static int getPieceValue(int type) {
		return VALUES[type];
	}
	
}
