package de.chess.ai;

import de.chess.game.Board;
import de.chess.game.Move;
import de.chess.game.MoveGenerator;
import de.chess.game.MoveList;
import de.chess.game.TranspositionEntry;
import de.chess.game.TranspositionTable;
import de.chess.game.Winner;
import de.chess.util.MathUtil;

public class MinimaxAI {
	
	private static final int INFINITY = 100000;
	
	private static final int MAX_DEPTH = 5;
	private static final int MAX_QUIESCE_DEPTH = 15;
	
	private static Move responseMove;
	
	private static long visitedNormalNodes;
	private static long visitedQuiesceNodes;
	private static int maxDepth;
	private static int transpositionUses;
	
	public static Move findNextMove(Board b) {
		long before = System.currentTimeMillis();
		
		visitedNormalNodes = 0;
		visitedQuiesceNodes = 0;
		maxDepth = 0;
		transpositionUses = 0;
		
		int score = runNegamax(b, -INFINITY, INFINITY);
		
		float time = (System.currentTimeMillis() - before) / 1000f;
		
		long visitedNodes = visitedNormalNodes + visitedQuiesceNodes;
		
		System.out.println("---");
		System.out.println("time: "+MathUtil.DECIMAL_FORMAT.format(time)+"s");
		System.out.println("max_depth: "+MathUtil.DECIMAL_FORMAT.format(maxDepth));
		System.out.println("prediction: "+MathUtil.DECIMAL_FORMAT.format(score));
		System.out.println("visited_nodes: "+MathUtil.DECIMAL_FORMAT.format(visitedNodes));
		System.out.println("nodes_per_second: "+MathUtil.DECIMAL_FORMAT.format(visitedNodes / time));
		System.out.println("visited_normal_nodes: "+MathUtil.DECIMAL_FORMAT.format(visitedNormalNodes));
		System.out.println("visited_quiesce_nodes: "+MathUtil.DECIMAL_FORMAT.format(visitedQuiesceNodes));
		System.out.println("transposition_uses: "+MathUtil.DECIMAL_FORMAT.format(transpositionUses));
		
		return responseMove;
	}
	
	private static int runNegamax(Board b, int alpha, int beta) {
		visitedNormalNodes++;
		
//		TranspositionEntry entry = TranspositionTable.getEntry(b.getPositionKey());
//		
//		if(entry != null) {
//			transpositionUses++;
//			
//			if(entry.getType() == TranspositionEntry.TYPE_EXACT) {
//				responseMove = entry.getMove();
//				
//				return entry.getScore();
//			} else if(entry.getType() == TranspositionEntry.TYPE_LOWER_BOUND) {
//				alpha = entry.getScore();
//			} else {
//				beta = entry.getScore();
//			}
//		}
		
		MoveList list = new MoveList();
		
		MoveGenerator.generateAllMoves(b, list);
		
		MoveEvaluator.eval(list, b);
		
		Move bestMove = null;
		
		while(list.hasMovesLeft()) {
			Move m = list.next();
			
			b.makeMove(m);
			
			if(!b.isOpponentInCheck()) {
				int score = -negamax(b, -beta, -alpha, 1);
				
				if(score > alpha) {
					bestMove = m;
					alpha = score;
				}
			}
			
			b.undoMove(m);
		}
		
//		TranspositionTable.putEntry(b.getPositionKey(), 0, bestMove, type, alpha, b.getHistoryPly());
		
		responseMove = bestMove;
		
		return alpha;
	}
	
	private static int negamax(Board b, int alpha, int beta, int depth) {
		visitedNormalNodes++;
		if(depth > maxDepth) maxDepth = depth;
		
		if(depth == MAX_DEPTH) {
			visitedNormalNodes--;
			
			return quiesce(b, alpha, beta, depth);
		}
		
		if(b.getFiftyMoveCounter() == 100 || b.hasThreefoldRepetition()) return 0;
		
		MoveList list = new MoveList();
		
		MoveGenerator.generateAllMoves(b, list);
		
		MoveEvaluator.eval(list, b);
		
		boolean hasLegalMove = false;

		while(list.hasMovesLeft()) {
			Move m = list.next();
			
			b.makeMove(m);
			
			if(!b.isOpponentInCheck()) {
				hasLegalMove = true;
				
				int score = -negamax(b, -beta, -alpha, depth + 1);
				
				if(score > alpha) {
					alpha = score;
				}
			}
			
			b.undoMove(m);
			
			if(alpha >= beta) {
				return alpha;
			}
		}
		
		if(!hasLegalMove) {
			int winner = b.findWinner(false);
			
			if(winner == Winner.DRAW) return 0;
			
			int score = INFINITY - depth;
			
			return b.getSide() == winner ? score : -score;
		}
		
		return alpha;
	}
	
	private static int quiesce(Board b, int alpha, int beta, int depth) {
		visitedQuiesceNodes++;
		if(depth > maxDepth) maxDepth = depth;
		
		if(b.getFiftyMoveCounter() == 100 || b.hasThreefoldRepetition()) return 0;
		
		int evalScore = Evaluator.eval(b);
		
	    if(evalScore >= beta) return beta;
	    
	    if(depth == MAX_QUIESCE_DEPTH) {
			return evalScore;
		}
	    
	    if(evalScore > alpha) alpha = evalScore;
		
		MoveList list = new MoveList();
		
		MoveGenerator.generateAllMoves(b, list);
		
		MoveEvaluator.eval(list, b);
		
		boolean hasLegalMove = false;
		
		while(list.hasMovesLeft()) {
			Move m = list.next();
			
			b.makeMove(m);
			
			int score = 0;
			boolean isCapture = false;
			
			if(!b.isOpponentInCheck()) {
				hasLegalMove = true;
				
				if(m.getCaptured() != 0) {
					isCapture = true;
					
					score = -quiesce(b, -beta, -alpha, depth+1);
					
					if(score > alpha) {
						alpha = score;
					}
				}
			}
			
			b.undoMove(m);
			
			if(isCapture && score >= beta) {
				return beta;
			}
		}
		
		if(!hasLegalMove) {
			int winner = b.findWinner(false);
			
			if(winner == Winner.DRAW) return 0;
			
			int score = INFINITY - depth;
			
			return b.getSide() == winner ? score : -score;
		}
		
		return alpha;
	}
	
}
