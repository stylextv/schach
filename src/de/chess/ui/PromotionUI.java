package de.chess.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import de.chess.game.PieceCode;
import de.chess.main.Constants;
import de.chess.util.ImageUtil;
import de.chess.util.MathUtil;

public class PromotionUI {
	
	private static final int SHADOW_MARGIN = 80;
	
	private static final BufferedImage BUFFER = new BufferedImage(64 + SHADOW_MARGIN, 256 + SHADOW_MARGIN, BufferedImage.TYPE_INT_ARGB);
	private static final Graphics2D BUFFER_GRAPHICS = (Graphics2D) BUFFER.getGraphics();
	
	private static final int[] TYPES = new int[] {
			PieceCode.QUEEN,
			PieceCode.KNIGHT,
			PieceCode.ROOK,
			PieceCode.BISHOP
	};
	
	private static int side;
	private static int offset;
	
	private static float state;
	
	static {
		BUFFER_GRAPHICS.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		BUFFER_GRAPHICS.setBackground(new Color(0, 0, 0, 0));
	}
	
	private static float[] hoverStates = new float[TYPES.length];
	
	public static BufferedImage generate(float alpha, int hoveringBox) {
		for(int i=0; i<hoverStates.length; i++) {
			hoverStates[i] = MathUtil.lerp(hoverStates[i], hoveringBox == i ? 1 : 0, 0.5f);
		}
		
		BUFFER_GRAPHICS.clearRect(0, 0, BUFFER.getWidth(), BUFFER.getHeight());
		
		int width = BUFFER.getWidth() - SHADOW_MARGIN;
		int height = BUFFER.getHeight() - SHADOW_MARGIN;
		
		int off = SHADOW_MARGIN / 2;
		
		BUFFER_GRAPHICS.drawImage(ImageUtil.PROMOTION_SHADOW, 0, 0, null);
		
		BUFFER_GRAPHICS.setColor(Constants.COLOR_WHITE);
		BUFFER_GRAPHICS.fillRect(off, off, width, height);
		
		for(int i=0; i<hoverStates.length; i++) {
			float f = hoverStates[i];
			
			if(f > 0.005f) {
				int j = Constants.BRIGHTNESS_BLACK;
				
				BUFFER_GRAPHICS.setColor(new Color(j, j, j, (int) (32 * f)));
				
				BUFFER_GRAPHICS.fillRect(off, off + i * 64, 64, 64);
			}
		}
		
		for(int i=0; i<TYPES.length; i++) {
			int type = TYPES[i];
			
			BUFFER_GRAPHICS.drawImage(PieceCode.getSprite(PieceCode.getSpriteCode(side, type)), off, off + i * 64, null);
		}
		
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
	
	public static void updateDropDown(Graphics2D graphics) {
		boolean show = BoardUI.getPawnPromotions() != null;
		
		state = MathUtil.lerp(state, show ? 1 : 0, 0.2f);
		
		if(state > 0.005f) {
			drawDropDown(graphics);
		} else for(int i=0; i<hoverStates.length; i++) {
			hoverStates[i] = 0;
		}
	}
	
	private static void drawDropDown(Graphics2D graphics) {
		BufferedImage image = generate(state, isHoveringBox(UIManager.getMouseX(), UIManager.getMouseY(), UIManager.getWidth(), UIManager.getHeight()));
		
		float x = (UIManager.getWidth() - 512 - SHADOW_MARGIN) / 2 + offset * 64;
		float y = (UIManager.getHeight() - 512 - SHADOW_MARGIN) / 2;
		
		x = x - (1 - state) * 20;
		
		AffineTransform trans = new AffineTransform();
		
		trans.translate(x, y);
		
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		graphics.drawImage(image, trans, null);
		
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}
	
	public static int isHoveringBox(int mx, int my, int width, int height) {
		int x = (width - 512) / 2 + offset * 64;
		int y = (height - 512) / 2;
		
		x = x - (int) ((1 - state) * 20);
		
		x = mx - x;
		y = my - y;
		
		if(x >= 0 && y>= 0 && x < 64 && y < 256) {
			return y / 64;
		}
		return -1;
	}
	
	public static void setSide(int i) {
		side = i;
	}
	
	public static void setOffset(int i) {
		offset = i;
	}
	
	public static float getState() {
		return state;
	}
	
}
