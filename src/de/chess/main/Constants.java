package de.chess.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

public class Constants {
	
	public static final String NAME = "Schach";
	
	public static final int WINDOW_DEFAULT_WIDTH = 1100;
	public static final int WINDOW_DEFAULT_HEIGHT = 950;
	
	public static final int BRIGHTNESS_BLACK = 0;
	public static final Color COLOR_BLACK = new Color(BRIGHTNESS_BLACK, BRIGHTNESS_BLACK, BRIGHTNESS_BLACK);
	public static final Color COLOR_WHITE = new Color(0xFFFFFF);
	public static final Color COLOR_BLUE = new Color(0x2C74FE);
	
	public static final Font FONT_REGULAR = new Font("Gilroy-Medium", 0, 16);
	public static final Font FONT_BOLD = new Font("Gilroy-ExtraBold", 0, 36);
	
	public static final BasicStroke LINE_STROKE = new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	public static final boolean PRINT_FPS = false;
	
}
