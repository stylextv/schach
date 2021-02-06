package de.chess.game;

import java.util.ArrayList;

public class LookupTableGenerator {
	
	public static void main(String[] args) {
		generateForQueen();
	}
	
	private static void generateForKnight() {
		for(int i=0; i<64; i++) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			
			generateForKnight(i%8, i/8, list);
			
			generateKey(list);
		}
	}
	private static void generateForKnight(int x, int y, ArrayList<Integer> list) {
		addMove(x+1, y-2, list);
		addMove(x+2, y-1, list);
		addMove(x+2, y+1, list);
		addMove(x+1, y+2, list);
		addMove(x-1, y-2, list);
		addMove(x-2, y-1, list);
		addMove(x-2, y+1, list);
		addMove(x-1, y+2, list);
	}
	
	private static void generateForKing() {
		for(int i=0; i<64; i++) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			
			generateForKing(i%8, i/8, list);
			
			generateKey(list);
		}
	}
	private static void generateForKing(int x, int y, ArrayList<Integer> list) {
		addMove(x+1, y+1, list);
		addMove(x-1, y+1, list);
		addMove(x+1, y-1, list);
		addMove(x-1, y-1, list);
		addMove(x+1, y, list);
		addMove(x-1, y, list);
		addMove(x, y+1, list);
		addMove(x, y-1, list);
	}
	
	private static void generateForQueen() {
		for(int i=0; i<64; i++) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			
			generateForQueen(i%8, i/8, list);
			
			generateKey(list);
		}
	}
	private static void generateForQueen(int x, int y, ArrayList<Integer> list) {
		for(int i=0; i<64; i++) {
			int toX = i % 8;
			int toY = i / 8;
			
			if(toX == x && toY == y) continue;
//			if(toX == 0 || toY == 0 || toX == 7 || toY == 7) continue;
			
			if((toX == x && toY != 0 && toY != 7) || (toY == y && toX != 0 && toX != 7)) {
				addMove(toX, toY, list);
			}
//			if(Math.abs(x - toX) == Math.abs(y - toY)) {
//				addMove(toX, toY, list);
//			}
		}
	}
	
	private static void addMove(int x, int y, ArrayList<Integer> list) {
		if(x < 0 || y < 0 || x > 7 || y > 7) return;
		
		list.add(y * 8 + x);
	}
	
	private static void generateKey(ArrayList<Integer> list) {
		long l = 0;
		
		for(int index : list) {
			l = l | BoardConstants.BIT_SET[index];
		}
		
//		BitOperations.print(l);
		
		String s = l+"";
		if(Math.abs(l) > Integer.MAX_VALUE) s = s+"l";
		
		System.out.println("			"+s+",");
	}
	
}
