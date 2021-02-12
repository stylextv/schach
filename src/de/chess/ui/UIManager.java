package de.chess.ui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import de.chess.game.PieceCode;
import de.chess.game.Winner;
import de.chess.io.Window;
import de.chess.main.Constants;
import de.chess.main.Main;
import de.chess.util.ImageUtil;

public class UIManager {
	
	private static Window window;
	
	private static int width;
	private static int height;
	
	private static int mouseX;
	private static int mouseY;
	
	private static int mouseXMoved;
	
	public static void createWindow() {
		window = new Window(Constants.WINDOW_DEFAULT_WIDTH + 16, Constants.WINDOW_DEFAULT_HEIGHT + 39);
		window.create();
	}
	
	public static void update() {
		BoardUI.update(Main.getBoard());
	}
	
	public static void drawFrame(Graphics2D graphics) {
		width = window.getWidth();
		height = window.getHeight();
		
		Point p = window.getMousePosition();
		if(p != null) {
			mouseXMoved = p.x - mouseX;
			mouseX = p.x;
			mouseY = p.y;
		} else mouseXMoved = 0;
		
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		graphics.drawImage(ImageUtil.BACKGROUND, (width - ImageUtil.BACKGROUND.getWidth()) / 2, (height - ImageUtil.BACKGROUND.getHeight()) / 2, null);
		
		BoardUI.drawBoard(graphics, Main.getBoard());
		
		MoveIndicatorUI.drawMoves(graphics);
		
		BoardUI.drawHand(graphics);
		
		PopupUI.updatePopup(graphics);
	}
	
	public static void onMousePress(Point p) {
		if(BoardUI.getWinner() == Winner.NONE) {
			
			if(Main.getBoard().getSide() == PieceCode.WHITE) {
				BoardUI.onMouseClick(p);
			}
			
		} else if(PopupUI.isHoveringButton(p.x, p.y, width, height)) {
			
			BoardUI.clearLastAIMove();
			
			Main.getBoard().reset();
			
			BoardUI.setWinner(Winner.NONE);
		}
	}
	
	public static void drawSync() {
		window.drawSync();
	}
	
	public static int getWidth() {
		return width;
	}
	
	public static int getHeight() {
		return height;
	}
	
	public static int getMouseX() {
		return mouseX;
	}
	
	public static int getMouseY() {
		return mouseY;
	}
	
	public static int getMouseXMoved() {
		return mouseXMoved;
	}
	
}
