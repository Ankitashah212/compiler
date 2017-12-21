/*
 * Created by Ankita Shah on Sep 10, 2005
 */
package cs.ashah.tl05;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This class provides regular expression values for the fixed charsets like
 * symbols, operators, in-built functions etc.
 * 
 * @author Ankita Shah
 */
public class Patterns {

	//-------------------------------------------------------------------------
	// singleton
	private static Patterns singleton = new Patterns();
	/**
	 * Returns the single instace of the class.
	 */
	public static Patterns getInstance() {
		return singleton;
	}
	//-------------------------------------------------------------------------
	private Properties config = new Properties();
	// default constructor, loads the patters from property file
	protected Patterns() {
		try {
			this.config.load(new FileInputStream("Format.properties"));
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	//-------------------------------------------------------------------------
	/**
	 * Returns the regex patter for the given fixed symbol. Return null
	 * if the regex is not configured.
	 */
	public String getPattern(String symbol) {
		return this.config.getProperty(symbol, symbol).trim();
	}
	//-------------------------------------------------------------------------
	/**
	 * Checks whether the given value matches to the given symbol.
	 */
	public boolean matchPattern(String symbol, String value) {
		Pattern pattern = Pattern.compile(
								this.getPattern(symbol), 
								Pattern.COMMENTS);
		CharSequence sequence = value.subSequence(0, value.length());
		
		return pattern.matcher(sequence).matches();
	}
}
