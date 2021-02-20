package de.chess.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import de.chess.game.Winner;
import de.chess.main.Constants;
import de.chess.util.ImageUtil;
import de.chess.util.MathUtil;

public class PopupUI {
	
	private static final int SHADOW_MARGIN = 80;
	
	private static final BufferedImage BUFFER = new BufferedImage(320 + SHADOW_MARGIN, 238 + SHADOW_MARGIN, BufferedImage.TYPE_INT_ARGB);
	private static final Graphics2D BUFFER_GRAPHICS = (Graphics2D) BUFFER.getGraphics();
	
	private static final int BUTTON_WIDTH = 180;
	private static final int BUTTON_HEIGHT = 39;
	
	private static float state;
	
	private static int displayWinner = Winner.NONE;
	
	static {
		BUFFER_GRAPHICS.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		BUFFER_GRAPHICS.setBackground(new Color(0, 0, 0, 0));
	}
	
	private static float buttonState;
	
	public static BufferedImage generate(String title, float alpha, boolean hoveringButton) {
		buttonState = MathUtil.lerp(buttonState, hoveringButton ? 1 : 0, 0.3f);
		
		BUFFER_GRAPHICS.clearRect(0, 0, BUFFER.getWidth(), BUFFER.getHeight());
		
		int width = BUFFER.getWidth() - SHADOW_MARGIN;
		int height = BUFFER.getHeight() - SHADOW_MARGIN;
		
		int off = SHADOW_MARGIN / 2;
		
		BUFFER_GRAPHICS.drawImage(ImageUtil.POPUP_SHADOW, 0, 0, null);
		
		BUFFER_GRAPHICS.setColor(Constants.COLOR_WHITE);
		BUFFER_GRAPHICS.fillRoundRect(off, off, width, height, 20, 20);
		
		BUFFER_GRAPHICS.setColor(Constants.COLOR_BLACK);
		BUFFER_GRAPHICS.setFont(Constants.FONT_BOLD);
		
		BUFFER_GRAPHICS.drawString(title, off + width/2 - BUFFER_GRAPHICS.getFontMetrics().stringWidth(title)/2, off + height/2 - 23);
		
		int j = Constants.BRIGHTNESS_BLACK;
		
		BUFFER_GRAPHICS.setColor(new Color(j, j, j, 255 - (int) (32 * buttonState)));
		BUFFER_GRAPHICS.fillRoundRect(off + width/2 - BUTTON_WIDTH/2, off + height/2 + 10, BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_HEIGHT, BUTTON_HEIGHT);
		
		BUFFER_GRAPHICS.setColor(Constants.COLOR_WHITE);
		BUFFER_GRAPHICS.setFont(Constants.FONT_REGULAR);
		
		String text = "Nochmal";
		
		BUFFER_GRAPHICS.drawString(text, off + width/2 - BUFFER_GRAPHICS.getFontMetrics().stringWidth(text)/2, off + height/2 + 10 + BUTTON_HEIGHT/2 + BUFFER_GRAPHICS.getFontMetrics().getHeight()/5 + 1);
		
		DataBuffer data = BUFFER.getRaster().getDataBuffer();
		
		for(int i=0; i<data.getSize(); i++) {
			int rgb = data.getElem(i);
			
			int a = (rgb >>> 24) & 0xFF;
			a = Math.round(alpha * a);
			
			rgb = (rgb & 0x00FFFFFF) + (a << 24);
			
			data.setElem(i, rgb);
		}
		
		return BUFFER;
	}
	
	public static void updatePopup(Graphics2D graphics) {
		boolean show = BoardUI.getWinner() != Winner.NONE && BoardUI.getLastAIMoveState() == 1;
		
		state = MathUtil.lerp(state, show ? 1 : 0, 0.2f);
		
		if(state > 0.005f) {
			String s;
			
			if(displayWinner == Winner.DRAW) s = "Unentschieden";
			else s = displayWinner == Winner.WHITE ? "Sieg" : "Niederlage";
			
			drawPopup(graphics, s);
		} else {
			buttonState = 0;
		}
	}
	
	private static void drawPopup(Graphics2D graphics, String title) {
		BufferedImage image = generate(title, state, isHoveringButton(UIManager.getMouseX(), UIManager.getMouseY(), UIManager.getWidth(), UIManager.getHeight()));
		
		float x = (UIManager.getWidth() - image.getWidth()) / 2;
		float y = (UIManager.getHeight() - image.getHeight()) / 2;
		
		x = x - (1 - state) * 40;
		
		AffineTransform trans = new AffineTransform();
		
		trans.translate(x, y);
		
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		graphics.drawImage(image, trans, null);
		
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}
	
	public static boolean isHoveringButton(int mx, int my, int width, int height) {
		int x = width / 2 - BUTTON_WIDTH/2;
		int y = height / 2 + 13;
		
		x = x - (int) ((1 - state) * 40);
		
		x = mx - x;
		y = my - y;
		
		return x >= 0 && y>= 0 && x < BUTTON_WIDTH && y < BUTTON_HEIGHT;
	}
	
	public static void setDisplayedWinner(int w) {
		displayWinner = w;
	}
	
	public static float getState() {
		return state;
	}
	
}
