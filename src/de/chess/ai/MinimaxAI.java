package de.chess.ai;

import de.chess.game.Board;
import de.chess.game.Move;
import de.chess.game.MoveGenerator;
import de.chess.game.MoveList;
import de.chess.game.Winner;
import de.chess.util.MathUtil;

public class MinimaxAI {
	
	private static final int INFINITY = 100000;
	
	private static final int MAX_DEPTH = 5;
	
	private static Move responseMove;
	
	private static long visitedNodes;
	
	public static void clearCache() {
		
	}
	
	public static Move findNextMove(Board b) {
		long before = System.currentTimeMillis();
		
		visitedNodes = 0;
		
		int score = negamax(b, -INFINITY, INFINITY, 0);
		
		long after = System.currentTimeMillis();
		
		float time = (after - before) / 1000f;
		
		System.out.println("---");
		System.out.println("time: "+MathUtil.DECIMAL_FORMAT.format(time)+"s");
		System.out.println("prediction: "+MathUtil.DECIMAL_FORMAT.format(score));
		System.out.println("visited_nodes: "+MathUtil.DECIMAL_FORMAT.format(visitedNodes));
		System.out.println("nodes_per_second: "+MathUtil.DECIMAL_FORMAT.format(visitedNodes / time));
		System.out.println("move: "+responseMove);
		
		return responseMove;
	}
	
	private static int negamax(Board b, int alpha, int beta, int depth) {
		visitedNodes++;
		
		if(b.getFiftyMoveCounter() == 50) return 0;
		
		if(depth == MAX_DEPTH) {
			int score = Evaluator.eval(b);
			
			return score;
		}
		
		MoveGenerator.generateAllMoves(b);
		
		MoveList list = MoveGenerator.getList();
		
		int count = list.getCount();
		
		Move[] moves = list.getMoves().clone();
		
		Move bestMove = null;
		
		for(int i=0; i<count; i++) {
			Move m = moves[i];
			
			b.makeMove(m);
			
			if(!b.isOpponentInCheck()) {
				int score = -negamax(b, -beta, -alpha, depth+1);
				
				if(score > alpha || bestMove == null) {
					bestMove = m;
					alpha = score;
				}
			}
			
			b.undoMove(m);
			
			if(depth != 0 && alpha >= beta) {
				return alpha;
			}
		}
		
		boolean hasLegalMove = bestMove != null;
		
		if(!hasLegalMove) {
			int winner = b.findWinner(false);
			
			if(winner == Winner.DRAW) return 0;
			
			int score = INFINITY - depth;
			
			return b.getSide() == winner ? score : -score;
		}
		
		if(depth == 0) responseMove = bestMove;
		
		return alpha;
	}
	
}
