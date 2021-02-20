package de.chess.ui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import de.chess.game.Board;
import de.chess.game.Move;
import de.chess.game.MoveFlag;
import de.chess.game.MoveGenerator;
import de.chess.game.MoveList;
import de.chess.game.PieceCode;
import de.chess.game.Winner;
import de.chess.main.Constants;
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
	
	private static Move[] pawnPromotions; 
	
	private static int winner = Winner.NONE;
	
	private static Point mouseClick;
	
	public static void update(Board board) {
		if(lastAIMove != null) {
			
			if(lastAIMoveState < 1) {
				lastAIMoveState += 1/32f;
			} else {
				lastAIMoveState = 1;
			}
		}
		
		boolean noWinner = winner == Winner.NONE;
		
		if(noWinner && board.getSide() == PieceCode.BLACK) {
			lastAIMove = board.makeAIMove();
			lastAIMoveState = 0;
			
			checkForWinner(board);
		}
		
		if(mouseClick != null) {
			
			if(pawnPromotions != null) {
				int i = PromotionUI.isHoveringBox(mouseClick.x, mouseClick.y, UIManager.getWidth(), UIManager.getHeight());
				
				if(i != -1) {
					clearLastAIMove();
					
					board.makeMove(pawnPromotions[i]);
					
					checkForWinner(board);
				}
				
				pawnPromotions = null;
				
				mouseClick = null;
				
				return;
			}
			
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
							MoveList list = new MoveList();
							
							MoveGenerator.generateAllMoves(board, list);
							
							ArrayList<Move> moves = MoveGenerator.getMovesForIndex(index, board, list, true);
							
							if(moves.size() != 0) {
								fillHand(p, boardX, boardY, index, mx, my, moves);
							}
						}
					} else {
						Move[] moves = new Move[4];
						int l = 0;
						
						for(Move check : handMoves) {
							if(index == check.getTo()) {
								moves[l] = check;
								
								l++;
							}
						}
						
						clearHand();
						
						if(l != 0) {
							if(l == 1) {
								clearLastAIMove();
								
								board.makeMove(moves[0]);
								
								checkForWinner(board);
							} else {
								PromotionUI.setSide(board.getSide());
								
								PromotionUI.setOffset(moves[0].getTo() % 8);
								
								pawnPromotions = moves;
							}
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
		int offsetX = (UIManager.getWidth() - 512) / 2;
		int offsetY = (UIManager.getHeight() - 512) / 2;
		
		if(lastAIMove != null) {
			int fromX = offsetX + lastAIMove.getFrom() % 8 * 64 + 32;
			int fromY = offsetY + lastAIMove.getFrom() / 8 * 64 + 32;
			int toX = offsetX + lastAIMove.getTo() % 8 * 64 + 32;
			int toY = offsetY + lastAIMove.getTo() / 8 * 64 + 32;
			
			double d = MathUtil.sigmoid(lastAIMoveState);
			
			graphics.setColor(Constants.COLOR_BLUE);
			graphics.setStroke(Constants.LINE_STROKE);
			
			graphics.drawLine(fromX, fromY, (int) (fromX + (toX - fromX) * d), (int) (fromY + (toY - fromY) * d));
		}
		
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
		
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		graphics.drawImage(sprite, trans, null);
		
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}
	
	private static void drawPiece(Graphics2D graphics, int p, int x, int y, int offX, int offY) {
		int pos1X = x;
		int pos1Y = y;
		
		int pos2X = -1;
		int pos2Y = -1;
		
		int index = y*8 + x;
		
		if(pawnPromotions != null) {
			Move m = pawnPromotions[0];
			
			if(index == m.getFrom()) {
				pos1X = m.getTo() % 8;
				pos1Y = m.getTo() / 8;
			}
		} else if(lastAIMove != null && lastAIMoveState != 1) {
			
			if(lastAIMove.getTo() == index) {
				
				pos1X = lastAIMove.getFrom() % 8;
				pos1Y = lastAIMove.getFrom() / 8;
				pos2X = x;
				pos2Y = y;
				
			} else if(y == lastAIMove.getTo() / 8 && ((lastAIMove.getFlag() == MoveFlag.CASTLING_QUEEN_SIDE && x == 3) || (lastAIMove.getFlag() == MoveFlag.CASTLING_KING_SIDE && x == 5))) {
				
				int fromX = 0;
				
				if(lastAIMove.getFlag() == MoveFlag.CASTLING_KING_SIDE) fromX = 7;
				
				pos1X = fromX;
				pos1Y = lastAIMove.getFrom() / 8;
				pos2X = x;
				pos2Y = y;
			}
		}
		
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
			
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			
			graphics.drawImage(sprite, trans, null);
			
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}
	}
	
	public static void onMouseClick(Point p) {
		mouseClick = p;
	}
	
	public static void clearLastAIMove() {
		lastAIMove = null;
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
	
	public static double getLastAIMoveState() {
		return lastAIMoveState;
	}
	
	public static int getHand() {
		return hand;
	}
	
	public static ArrayList<Move> getHandMoves() {
		return handMoves;
	}
	
	public static Move[] getPawnPromotions() {
		return pawnPromotions;
	}
	
}
