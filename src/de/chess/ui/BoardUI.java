package de.chess.ui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import de.chess.game.Board;
import de.chess.game.Move;
import de.chess.game.MoveGenerator;
import de.chess.game.PieceCode;
import de.chess.game.Winner;
import de.chess.util.ImageUtil;
import de.chess.util.MathUtil;

public class BoardUI {
	
	private static Move lastAIMove;
	private static double lastAIMoveState;
	
	private static int hand = -1;
	private static ArrayList<Move> handMoves;
	
	private static int handFrom;
	
	private static int handRenderX;
	private static int handRenderY;
	private static double handRotation;
	
	private static int winner = Winner.NONE;
	
	private static Point mouseClick;
	
	public static void update(Board board) {
		if(lastAIMove != null) {
			
			if(lastAIMoveState < 1) {
				
				lastAIMoveState += 1/32f;
			} else {
				lastAIMove = null;
			}
		}
		
		boolean noWinner = winner == Winner.NONE;
		
		if(noWinner && board.getSide() == PieceCode.BLACK) {
			lastAIMove = board.makeAIMove();
			lastAIMoveState = 0;
			
			checkForWinner(board);
		}
		
		if(mouseClick != null) {
			
			int mx = mouseClick.x - UIManager.getWidth()/2 + 256;
			int my = mouseClick.y - UIManager.getHeight()/2 + 256;
			
			mouseClick = null;
			
			if(mx >= 0 && my >= 0) {
				int boardX = mx / 64;
				int boardY = my / 64;
				
				if(boardX < 8 && boardY < 8) {
					
					int index = boardY * 8 + boardX;
					
					if(hand == -1) {
						int p = board.getPiece(index);
						
						if(p != -1 && PieceCode.getColorFromSpriteCode(p) == board.getSide()) {
							MoveGenerator.generateAllMoves(board);
							ArrayList<Move> moves = MoveGenerator.getMovesForIndex(index, board, true);
							
							if(moves.size() != 0) {
								fillHand(p, boardX, boardY, index, mx, my, moves);
							}
						}
					} else {
						Move m = null;
						
						for(Move check : handMoves) {
							if(index == check.getTo()) {
								m = check;
								break;
							}
						}
						
						clearHand();
						
						if(m != null) {
							board.makeMove(m);
							
							checkForWinner(board);
						}
					}
					
				} else clearHand();
			} else clearHand();
		}
	}
	
	private static void fillHand(int p, int x, int y, int index, int mx, int my, ArrayList<Move> moves) {
		hand = p;
		handMoves = moves;
		
		handFrom = index;
		
		handRenderX = x*64 - mx;
		handRenderY = y*64 - my;
	}
	
	private static void clearHand() {
		hand = -1;
	}
	
	public static void drawBoard(Graphics2D graphics, Board board) {
		int offsetX = (UIManager.getWidth() - ImageUtil.BOARD.getWidth()) / 2;
		int offsetY = (UIManager.getHeight() - ImageUtil.BOARD.getHeight()) / 2;
		
		graphics.drawImage(ImageUtil.BOARD, offsetX, offsetY, null);
		
		for(int x=0; x<8; x++) {
			for(int y=0; y<8; y++) {
				int i = y * 8 + x;
				
				if(hand != -1 && i == handFrom) continue;
				
				int p = board.getPiece(i);
				
				if(p != -1) drawPiece(graphics, p, x, y, offsetX, offsetY);
			}
		}
	}
	
	public static void drawHand(Graphics2D graphics) {
		if(hand == -1) return;
		
		BufferedImage sprite = PieceCode.getSprite(hand);
		AffineTransform trans = new AffineTransform();
		
		int x = UIManager.getMouseX() + handRenderX;
		int y = UIManager.getMouseY() + handRenderY - 4;
		
		trans.translate(x, y);
		
		int target = UIManager.getMouseXMoved() * 4;
		if(target > 90) target = 90;
		else if(target < -90) target = -90;
		
		handRotation = MathUtil.lerp(handRotation, target, 0.15f);
		
		trans.rotate(Math.toRadians(handRotation), sprite.getWidth()/2, 10);
		
		graphics.drawImage(sprite, trans, null);
	}
	
	private static void drawPiece(Graphics2D graphics, int p, int x, int y, int offsetX, int offsetY) {
		int pos1X = x;
		int pos1Y = y;
		
		int pos2X = -1;
		int pos2Y = -1;
		
		if(lastAIMove != null) {
			int index = y*8 + x;
			
			if(lastAIMove.getTo() == index) {
				
				pos1X = lastAIMove.getFrom() % 8;
				pos1Y = lastAIMove.getFrom() / 8;
				pos2X = x;
				pos2Y = y;
				
			}
		}
		
		int offX = 32 + offsetX;
		int offY = 32 + offsetY;
		
		pos1X = offX + pos1X * 64;
		pos1Y = offY + pos1Y * 64;
		
		BufferedImage sprite = PieceCode.getSprite(p);
		
		if(pos2X == -1) {
			
			graphics.drawImage(sprite, pos1X, pos1Y, null);
			
		} else {
			pos2X = offX + pos2X * 64;
			pos2Y = offY + pos2Y * 64;
			
			double d = MathUtil.sigmoid(lastAIMoveState);
			
			AffineTransform trans = new AffineTransform();
			trans.translate(pos1X+(pos2X-pos1X)*d, pos1Y+(pos2Y-pos1Y)*d);
			
			graphics.drawImage(sprite, trans, null);
		}
	}
	
	public static void onMouseClick(Point p) {
		mouseClick = p;
	}
	
	private static void checkForWinner(Board b) {
		winner = b.findWinner();
		
		if(winner != Winner.NONE) {
			PopupUI.setDisplayedWinner(winner);
		}
	}
	
	public static int getWinner() {
		return winner;
	}
	
	public static void setWinner(int w) {
		winner = w;
	}
	
	public static Move getLastAIMove() {
		return lastAIMove;
	}
	
	public static int getHand() {
		return hand;
	}
	
	public static ArrayList<Move> getHandMoves() {
		return handMoves;
	}
	
}
