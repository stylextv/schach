package de.chess.ui;

import java.awt.Graphics2D;

import de.chess.game.Board;
import de.chess.game.Move;
import de.chess.main.Constants;

public class MoveIndicatorUI {
	
	public static void drawMoves(Graphics2D graphics, Board b) {
		if(BoardUI.getHand() != -1) {
			int ox = UIManager.getWidth() / 2 - 256;
			int oy = UIManager.getHeight() / 2 - 256;
			
			for(Move m : BoardUI.getHandMoves()) {
				int to = m.getTo();
				int toY = to / 8;
				int toX = to % 8;
				
				if(b.getPiece(to) == -1) {
					if((toX + toY) % 2 == 0) graphics.setColor(Constants.COLOR_WHITE);
					else graphics.setColor(Constants.COLOR_BLACK);
					
					graphics.fillArc(ox+toX*64+32-13, oy+toY*64+32-13, 26, 26, 0, 360);
				}
				
				graphics.setColor(Constants.COLOR_BLUE);
				graphics.fillArc(ox+toX*64+32-10, oy+toY*64+32-10, 20, 20, 0, 360);
			}
		}
	}
	
}
