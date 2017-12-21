/*
 * Created by Ankita Shah on Sep 13, 2005
 */
package cs.ashah.tl05;

import java.util.StringTokenizer;

/** 
 * Scans the given input and caches the tokens. The scanner provides two way
 * movement on the stream, where backward movement is done through caching.
 * 
 * @author Ankita Shah
 */
public class Scanner {
	//-------------------------------------------------------------------------
	private String[] tokens = null;
	private int current = -1;
	private int total = 0;
	
	public final String input;
	//-------------------------------------------------------------------------
	/**
	 * Create a new scanner with given input.
	 */
	public Scanner(String input) {
		this.input = input.trim();
		
		StringTokenizer tokenizer = new StringTokenizer(this.input);
		this.total = tokenizer.countTokens();
		
		this.tokens = new String[this.total];
		
		for(int i=0;i<this.total;i++)
			this.tokens[i] = tokenizer.nextToken();
		
		total--;
	}
	//-------------------------------------------------------------------------
	public boolean hasNext() {
		return (current < total);
	}

	public String getCurrentToken() {
		return tokens[current];
	}
	public String moveToNext() {
		if(this.hasNext()) current++;
		return this.getCurrentToken();
	}
	/**
	 * Returns the scanner, which contains tokens fron the current postion of
	 * the scanner upto end of it.
	 * @return
	 */
	public Scanner getSubScanner() {
		StringBuffer remaining = new StringBuffer();
		
		if(this.hasNext()) {			
			for(int i=this.current + 1; i<this.tokens.length;i++) {
				remaining.append(this.tokens[i]);
				remaining.append(' ');
			}
		}
		return new Scanner(remaining.toString());
			
	}
	//-------------------------------------------------------------------------
}
