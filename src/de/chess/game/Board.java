package de.chess.game;

import de.chess.ai.MinimaxAI;
import de.chess.ai.OpeningBook;
import de.chess.ai.OpeningPosition;

public class Board {
	
	private BitBoard[] bitBoards = new BitBoard[PieceCode.LAST + 1];
	
	private int[] pieces = new int[BoardConstants.BOARD_SIZE_SQ];
	
	private int[][] pieceIndices = new int[12][11];
	
	private int[] pieceCounters = new int[12];
	
	private int side;
	
	private int historyPly;
	
	private int fiftyMoveCounter;
	
	private int castlePerms;
	
    private int enPassant;
    
    private long positionKey;
	
	private UndoStructure[] history = new UndoStructure[BoardConstants.MAX_GAME_MOVES];
	
	public Board() {
		for(int i=0; i<bitBoards.length; i++) {
			bitBoards[i] = new BitBoard();
		}
		
		for(int i=0; i<history.length; i++) {
			history[i] = new UndoStructure();
		}
		
		reset();
	}
	
	public void reset() {
		side = PieceCode.WHITE;
		
		historyPly = 0;
		
		fiftyMoveCounter = 0;
		
		castlePerms = 0;
		
		enPassant = BoardSquare.NONE;
		
		positionKey = 0;
		
		for(int i=0; i<bitBoards.length; i++) {
			bitBoards[i].clear();
		}
		
		for(int i=0; i<pieces.length; i++) {
			pieces[i] = -1;
			
			positionKey ^= PositionKey.getRandomNumber(PositionKey.NOTHING_OFFSET + i);
		}
		
		setPiece(0, PieceCode.BLACK, PieceCode.ROOK);
		setPiece(1, PieceCode.BLACK, PieceCode.KNIGHT);
		setPiece(2, PieceCode.BLACK, PieceCode.BISHOP);
		setPiece(3, PieceCode.BLACK, PieceCode.QUEEN);
		setPiece(4, PieceCode.BLACK, PieceCode.KING);
		setPiece(5, PieceCode.BLACK, PieceCode.BISHOP);
		setPiece(6, PieceCode.BLACK, PieceCode.KNIGHT);
		setPiece(7, PieceCode.BLACK, PieceCode.ROOK);
		
		setPiece(56, PieceCode.WHITE, PieceCode.ROOK);
		setPiece(57, PieceCode.WHITE, PieceCode.KNIGHT);
		setPiece(58, PieceCode.WHITE, PieceCode.BISHOP);
		setPiece(59, PieceCode.WHITE, PieceCode.QUEEN);
		setPiece(60, PieceCode.WHITE, PieceCode.KING);
		setPiece(61, PieceCode.WHITE, PieceCode.BISHOP);
		setPiece(62, PieceCode.WHITE, PieceCode.KNIGHT);
		setPiece(63, PieceCode.WHITE, PieceCode.ROOK);
		
		for(int i=0; i<8; i++) {
			setPiece(8 + i, PieceCode.BLACK, PieceCode.PAWN);
			setPiece(48 + i, PieceCode.WHITE, PieceCode.PAWN);
		}
	}
	
	public int getPiece(int index) {
		return pieces[index];
	}
	
	public int getPieceType(int index) {
		int i = pieces[index];
		
		if(i == -1) return 0;
		
		return PieceCode.getTypeFromSpriteCode(i);
	}
	
	public void clearSquare(int index, int side, int type) {
		long key = BoardConstants.BIT_SET[index];
		
		bitBoards[side].xor(key);
		bitBoards[type].xor(key);
		
		positionKey ^= PositionKey.getRandomNumber(pieces[index] * 64 + index);
		positionKey ^= PositionKey.getRandomNumber(PositionKey.NOTHING_OFFSET + index);
		
		pieces[index] = -1;
	}
	
	public void setPiece(int index, int side, int type) {
		long key = BoardConstants.BIT_SET[index];
		
		bitBoards[side].xor(key);
		bitBoards[type].xor(key);
		
		int code = PieceCode.getSpriteCode(side, type);
		
		pieces[index] = code;
		
		positionKey ^= PositionKey.getRandomNumber(PositionKey.NOTHING_OFFSET + index);
		positionKey ^= PositionKey.getRandomNumber(code * 64 + index);
	}
	
	public void makeMove(Move m) {
		UndoStructure u = history[historyPly];
		
		u.setPositionKey(positionKey);
		
		int opponentSide = (side + 1) % 2;
		
		int originalPiece = getPieceType(m.getFrom());
		int placedPiece = originalPiece;
		
		if(m.getPromoted() != 0) placedPiece = m.getPromoted();
		
		if(m.getCaptured() != 0) clearSquare(m.getTo(), opponentSide, m.getCaptured());
		setPiece(m.getTo(), side, placedPiece);
		clearSquare(m.getFrom(), side, originalPiece);
		
		if(m.getFlag() == MoveFlag.EN_PASSANT) {
			int target = enPassant;
			
			if(side == PieceCode.WHITE) target += 8;
			else target -= 8;
			
			clearSquare(target, opponentSide, PieceCode.PAWN);
		}
		
		u.setFiftyMoveCounter(fiftyMoveCounter);
		u.setCastlePerms(castlePerms);
		u.setEnPassant(enPassant);
		
		historyPly++;
		
		if(originalPiece == PieceCode.PAWN || m.getCaptured() != 0) fiftyMoveCounter = 0;
		else fiftyMoveCounter++;
		
		if(enPassant != BoardSquare.NONE) {
			positionKey ^= PositionKey.getRandomNumber(PositionKey.EN_PASSANT_OFFSET + enPassant % 8);
		}
		
		if(m.getFlag() == MoveFlag.DOUBLE_PAWN_ADVANCE) {
			enPassant = (m.getFrom() + m.getTo()) / 2;
			
			positionKey ^= PositionKey.getRandomNumber(PositionKey.EN_PASSANT_OFFSET + enPassant % 8);
		} else {
			enPassant = BoardSquare.NONE;
		}
		
		if(m.getFlag() == MoveFlag.CASTLING_QUEEN_SIDE || m.getFlag() == MoveFlag.CASTLING_KING_SIDE) {
			
			removeCastlePerms(side);
			
			int y = m.getFrom() / 8;
			
			int rookFrom = y * 8;
			int rookTo = m.getTo();
			
			if(m.getFlag() == MoveFlag.CASTLING_KING_SIDE) {
				rookFrom += 7;
				
				rookTo -= 1;
			} else {
				rookTo += 1;
			}
			
			setPiece(rookTo, side, PieceCode.ROOK);
			clearSquare(rookFrom, side, PieceCode.ROOK);
			
		} else if(castlePerms != Castling.BOTH) {
			
			if(m.getFrom() == MoveGenerator.KING_START_POSITION[side]) {
				removeCastlePerms(side);
			}
			
			updateCastlePerms(PieceCode.WHITE, m.getFrom(), m.getTo());
			updateCastlePerms(PieceCode.BLACK, m.getFrom(), m.getTo());
		}
		
		side = opponentSide;
		positionKey ^= PositionKey.getRandomNumber(PositionKey.SIDE_OFFSET);
	}
	
	public void undoMove(Move m) {
		int opponentSide = side;
		
		side = (side + 1) % 2;
		
		int originalPiece = getPieceType(m.getTo());
		int placedPiece = originalPiece;
		
		if(m.getPromoted() != 0) placedPiece = PieceCode.PAWN;
		
		setPiece(m.getFrom(), side, placedPiece);
		clearSquare(m.getTo(), side, originalPiece);
		
		int captured = m.getCaptured();
		if(captured != 0) setPiece(m.getTo(), opponentSide, captured);
		
		historyPly--;
		
		UndoStructure u = history[historyPly];
		
		fiftyMoveCounter = u.getFiftyMoveCounter();
		
		castlePerms = u.getCastlePerms();
		
		enPassant = u.getEnPassant();
		
		if(m.getFlag() == MoveFlag.EN_PASSANT) {
			int target = enPassant;
			
			if(side == PieceCode.WHITE) target += 8;
			else target -= 8;
			
			setPiece(target, opponentSide, PieceCode.PAWN);
		}
		
		if(m.getFlag() == MoveFlag.CASTLING_QUEEN_SIDE || m.getFlag() == MoveFlag.CASTLING_KING_SIDE) {
			
			int y = m.getFrom() / 8;
			
			int rookFrom = y * 8;
			int rookTo = m.getTo();
			
			if(m.getFlag() == MoveFlag.CASTLING_KING_SIDE) {
				rookFrom += 7;
				
				rookTo -= 1;
			} else {
				rookTo += 1;
			}
			
			setPiece(rookFrom, side, PieceCode.ROOK);
			clearSquare(rookTo, side, PieceCode.ROOK);
		}
		
		positionKey = u.getPositionKey();
	}
	
	public Move makeAIMove() {
		OpeningPosition p = OpeningBook.getOpeningPosition(positionKey);
		
		if(p != null) {
			
			int hash = p.getRandomMove();
			
			Move m = null;
			
			MoveList list = new MoveList();
			
			MoveGenerator.generateAllMoves(this, list);
			
			for(int i=0; i<list.getCount(); i++) {
				Move move = list.getMove(i);
				
				if(move.getHash() == hash) {
					m = move;
					break;
				}
			}
			
			System.out.println("---");
			System.out.println("played move from book: "+m);
			
			makeMove(m);
			
			return m;
		}
		
		Move m = MinimaxAI.findNextMove(this);
		
		makeMove(m);
		
		return m;
	}
	
	public boolean isSideInCheck() {
		return isInCheck(side);
	}
	
	public boolean isOpponentInCheck() {
		return isInCheck((side + 1) % 2);
	}
	
	private boolean isInCheck(int side) {
		long kingSquares = getBitBoard(side).andReturn(getBitBoard(PieceCode.KING));
		
		int index = BitOperations.bitScanForward(kingSquares);
		
		return isUnderAttack(index, side);
	}
	
	public boolean isUnderAttack(int square, int defenderSide) {
		int attackerSide = (defenderSide + 1) % 2;
		
		long knights = getBitBoard(attackerSide).andReturn(getBitBoard(PieceCode.KNIGHT));
		
		if((LookupTable.KNIGHT_MOVES[square] & knights) != 0) {
			return true;
		}
		
		long king = getBitBoard(attackerSide).andReturn(getBitBoard(PieceCode.KING));
		
		if((LookupTable.KING_MOVES[square] & king) != 0) {
			return true;
		}
		
		long pawns = getBitBoard(attackerSide).andReturn(getBitBoard(PieceCode.PAWN));
		
		int dir = -8;
		if(attackerSide == PieceCode.WHITE) dir = 8;
		
		int j = square + dir;
		
		if(j > 7 && j < 56) {
			int squareX = square % 8;
			
			if(squareX > 0 && (BoardConstants.BIT_SET[square - 1 + dir] & pawns) != 0) {
				return true;
			}
			if(squareX < 7 && (BoardConstants.BIT_SET[square + 1 + dir] & pawns) != 0) {
				return true;
			}
		}
		
		long occupiedSquares = getBitBoard(defenderSide).orReturn(getBitBoard(attackerSide));
		
		if(checkSliderMoves(square, attackerSide, occupiedSquares, PieceCode.BISHOP, LookupTable.RELEVANT_BISHOP_MOVES, LookupTable.BISHOP_MAGIC_VALUES, LookupTable.BISHOP_MAGIC_INDEX_BITS, LookupTable.BISHOP_MOVES)) {
			return true;
		}
		
		if(checkSliderMoves(square, attackerSide, occupiedSquares, PieceCode.ROOK, LookupTable.RELEVANT_ROOK_MOVES, LookupTable.ROOK_MAGIC_VALUES, LookupTable.ROOK_MAGIC_INDEX_BITS, LookupTable.ROOK_MOVES)) {
			return true;
		}
		
		return false;
	}
	
	private boolean checkSliderMoves(int square, int side, long occupiedSquares, int type, long[] moveTable, long[] magicValues, int[] magicIndices, long[][] finalMoveTable) {
		long squares = getBitBoard(side).andReturn(getBitBoard(type).orReturn(getBitBoard(PieceCode.QUEEN)));
		
		long moves = MoveGenerator.getSliderMoves(square, occupiedSquares, moveTable, magicValues, magicIndices, finalMoveTable);
		
		return (moves & squares) != 0;
	}
	
	private void removeCastlePerms(int side) {
		if(side == PieceCode.WHITE) {
			if((castlePerms & Castling.WHITE_KING_SIDE) == 0) positionKey ^= PositionKey.getRandomNumber(PositionKey.CASTLING_OFFSET);
			if((castlePerms & Castling.WHITE_QUEEN_SIDE) == 0) positionKey ^= PositionKey.getRandomNumber(PositionKey.CASTLING_OFFSET + 1);
			
			castlePerms |= Castling.WHITE;
		} else {
			if((castlePerms & Castling.BLACK_KING_SIDE) == 0) positionKey ^= PositionKey.getRandomNumber(PositionKey.CASTLING_OFFSET + 2);
			if((castlePerms & Castling.BLACK_QUEEN_SIDE) == 0) positionKey ^= PositionKey.getRandomNumber(PositionKey.CASTLING_OFFSET + 3);
			
			castlePerms |= Castling.BLACK;
		}
	}
	
	private void updateCastlePerms(int side, int from, int to) {
		updateCastlePerms(side, from, to, 0);
		updateCastlePerms(side, from, to, 1);
	}
	private void updateCastlePerms(int side, int from, int to, int rookIndex) {
		int square;
		
		if(rookIndex == 0) square = MoveGenerator.ROOK1_START_POSITION[side];
		else square = MoveGenerator.ROOK2_START_POSITION[side];
		
		if(from == square || to == square) {
			int mask;
			long key;
			
			if(side == PieceCode.WHITE) {
				if(rookIndex == 0) {
					mask = Castling.WHITE_QUEEN_SIDE;
					key = PositionKey.getRandomNumber(PositionKey.CASTLING_OFFSET + 1);
				} else {
					mask = Castling.WHITE_KING_SIDE;
					key = PositionKey.getRandomNumber(PositionKey.CASTLING_OFFSET);
				}
			} else {
				if(rookIndex == 0) {
					mask = Castling.BLACK_QUEEN_SIDE;
					key = PositionKey.getRandomNumber(PositionKey.CASTLING_OFFSET + 3);
				} else {
					mask = Castling.BLACK_KING_SIDE;
					key = PositionKey.getRandomNumber(PositionKey.CASTLING_OFFSET + 2);
				}
			}
			
			if((castlePerms & mask) == 0) {
				positionKey ^= key;
				
				castlePerms |= mask;
			}
		}
	}
	
	public boolean isLegalMove(Move m) {
		boolean legal = true;
		
		makeMove(m);
		
		if(isOpponentInCheck()) legal = false;
		
		undoMove(m);
		
		return legal;
	}
	
	public boolean hasThreefoldRepetition() {
		int start = historyPly - 2;
		
		int count = 0;
		
		for(int i = start; i >= 0; i -= 2) {
			UndoStructure previous = history[i];
			
			if(previous.getPositionKey() == positionKey) {
				count++;
				
				if(count == 2) return true;
			} else if(previous.getCastlePerms() != castlePerms || previous.getFiftyMoveCounter() >= fiftyMoveCounter) {
				break;
			}
		}
		
		return false;
	}
	
	public void countPieces() {
		for(int i=0; i<pieceCounters.length; i++) {
			pieceCounters[i] = 0;
		}
		
		for(int i=0; i<64; i++) {
			int code = pieces[i];
			
			if(code != -1) {
				int l = pieceCounters[code];
				
				pieceIndices[code][l] = i;
				
				pieceCounters[code] = l + 1;
			}
		}
	}
	
	public int getPieceAmount(int code) {
		return pieceCounters[code];
	}
	
	public int getPieceIndex(int code, int i) {
		return pieceIndices[code][i];
	}
	
	public boolean isEndgame() {
		int count = 0;
		
		for(int i=1; i<12; i++) {
			int type = PieceCode.getTypeFromSpriteCode(i);
			
			if(type != PieceCode.PAWN && type != PieceCode.KING) {
				count += pieceCounters[i];
			}
		}
		
		return count <= 4;
	}
	
	public int findWinner() {
		MoveList list = new MoveList();
		
		MoveGenerator.generateAllMoves(this, list);
		
		boolean hasLegalMoves = false;
		
		for(int i=0; i<list.getCount(); i++) {
			Move m = list.getMove(i);
			
			if(isLegalMove(m)) {
				hasLegalMoves = true;
				
				break;
			}
		}
		
		return findWinner(hasLegalMoves);
	}
	
	public int findWinner(boolean hasLegalMoves) {
		if(hasLegalMoves) {
			if(fiftyMoveCounter == 100 || hasThreefoldRepetition()) return Winner.DRAW;
			
			return Winner.NONE;
		}
		
		if(!isSideInCheck()) {
			return Winner.DRAW;
		}
		
		if(side == PieceCode.WHITE) return Winner.BLACK;
		return Winner.WHITE;
	}
	
	public int getSide() {
		return side;
	}
	
	public int getHistoryPly() {
		return historyPly;
	}
	
	public int getFiftyMoveCounter() {
		return fiftyMoveCounter;
	}
	
	public int getCastlePerms() {
		return castlePerms;
	}
	
	public int getEnPassant() {
		return enPassant;
	}
	
	public long getPositionKey() {
		return positionKey;
	}
	
	public BitBoard getBitBoard(int code) {
		return bitBoards[code];
	}
	
	public int[] getPieces() {
		return pieces;
	}
	
}
