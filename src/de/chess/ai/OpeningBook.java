package de.chess.ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

public class OpeningBook {
	
	private static final HashMap<Long, OpeningPosition> POSITIONS = new HashMap<Long, OpeningPosition>();
	
	public static void load() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(OpeningBook.class.getClassLoader().getResourceAsStream("assets/opening_book.txt")));
			
			String s;
			
			while((s = reader.readLine()) != null) {
				String[] split = s.split(" ");
				
				long key = Long.parseLong(split[0]);
				
				int l = split.length - 1;
				
				int[] moves = new int[l];
				int[] counts = new int[l];
				
				for(int i=1; i<split.length; i++) {
					String[] move = split[i].split("\\(");
					
					String countString = move[1];
					
					countString = countString.substring(0, countString.length() - 1);
					
					int hash = Integer.parseInt(move[0]);
					int count = Integer.parseInt(countString);
					
					int index = i - 1;
					
					moves[index] = hash;
					counts[index] = count;
				}
				
				OpeningPosition p = new OpeningPosition(moves, counts);
				
				p.calcWeights();
				
				POSITIONS.put(key, p);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			
			System.exit(1);
		}
	}
	
	public static OpeningPosition getOpeningPosition(long key) {
		return POSITIONS.get(key);
	}
	
//	private static final String RANKS = "87654321";
//	private static final String FILES = "abcdefgh";
//	private static final String TYPES = "NBRQK";
//	
//	public static void load() {
//		try {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(OpeningBook.class.getClassLoader().getResourceAsStream("assets/games.txt")));
//			
//			String s;
//			
//			while((s = reader.readLine()) != null) {
//				parseGame(s);
//			}
//			
//			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("opening_book.txt")));
//			
//			for(long key : POSITIONS.keySet()) {
//				OpeningPosition p = POSITIONS.get(key);
//				
//				String line = key + "";
//				
//				boolean hasAnyMoves = false;
//				
//				for(int move : p.getRecordedMoves().keySet()) {
//					int amount = p.getRecordedMoves().get(move);
//					
//					if(amount >= 10) {
//						hasAnyMoves = true;
//						
//						line += " " + move + "("+amount+")";
//					}
//				}
//				
//				if(hasAnyMoves) {
//					writer.write(line);
//					writer.newLine();
//				}
//			}
//			
//			writer.close();
//		    
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			
//			System.exit(1);
//		}
//	}
//	
//	private static void addBookMove(long key, Move m) {
//		OpeningPosition p = POSITIONS.get(key);
//		
//		if(p == null) {
//			p = new OpeningPosition();
//			
//			POSITIONS.put(key, p);
//		}
//		
//		p.addMove(m.getHash());
//	}
//	
//	private static void parseGame(String gameString) {
//		Board b = new Board();
//		
//		String[] split = gameString.split(" ");
//		
//		for(String s : split) {
//			if(s.equals("1/2-1/2") || s.equals("0-1") || s.equals("1-0")) {
//				break;
//			}
//			
//			s = s.replace("+", "").replace("#", "").replace("x", "").replace("-", "");
//			
//			MoveList list = new MoveList();
//			
//			MoveGenerator.generateAllMoves(b, list);
//			
//			Move move = null;
//			
//			for(int i=0; i<list.getCount(); i++) {
//				Move m = list.getMove(i);
//				
//				boolean legal = true;
//				
//				b.makeMove(m);
//				
//				if(b.isOpponentInCheck()) {
//					legal = false;
//				}
//				
//				b.undoMove(m);
//				
//				if(legal && isCorrectMove(s, m, b)) {
//					move = m;
//					break;
//				}
//			}
//			
//			if(move == null) {
//				System.out.println("No move found: "+s);
//				
//				return;
//			}
//			
//			addBookMove(b.getPositionKey(), move);
//			
//			b.makeMove(move);
//		}
//	}
//	
//	private static boolean isCorrectMove(String s, Move m, Board b) {
//		if(s.equals("OO")) {
//			return m.getFlag() == MoveFlag.CASTLING_KING_SIDE;
//		} else if(s.equals("OOO")) {
//			return m.getFlag() == MoveFlag.CASTLING_QUEEN_SIDE;
//		}
//		
//		int type = TYPES.indexOf(s.charAt(0));
//		
//		if(type == -1) {
//			type = PieceCode.PAWN;
//			
//			if(s.contains("=")) {
//				int promoted = TYPES.indexOf(s.charAt(s.length() - 1)) + PieceCode.KNIGHT;
//				
//				s = s.substring(0, s.length() - 2);
//				
//				if(m.getPromoted() != promoted) return false;
//			}
//		} else {
//			type += PieceCode.KNIGHT;
//			
//			s = s.substring(1);
//		}
//		
//		int toX = FILES.indexOf(s.charAt(s.length() - 2));
//		int toY = RANKS.indexOf(s.charAt(s.length() - 1));
//		
//		int to = toY * 8 + toX;
//		
//		s = s.substring(0, s.length() - 2);
//		
//		int fromX = -1;
//		int fromY = -1;
//		
//		if(s.length() == 1) {
//			fromX = FILES.indexOf(s.charAt(0));
//			
//			if(fromX == -1) fromY = RANKS.indexOf(s.charAt(0));
//		} else if(s.length() == 2) {
//			fromX = FILES.indexOf(s.charAt(0));
//			fromY = FILES.indexOf(s.charAt(1));
//		}
//		
//		boolean correctFrom = true;
//		
//		if(fromX != -1 && fromX != m.getFrom() % 8) correctFrom = false;
//		if(fromY != -1 && fromY != m.getFrom() / 8) correctFrom = false;
//		
////		if(type == b.getPieceType(m.getFrom()) && to == m.getTo() && correctFrom) {
////			System.out.println("---");
////			System.out.println(original);
////			System.out.println(m);
////		}
//		
//		return type == b.getPieceType(m.getFrom()) && to == m.getTo() && correctFrom;
//	}
	
}
