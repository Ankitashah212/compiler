/*
 * Created by Ankita Shah on Sep 13, 2005
 */
package cs.ashah.tl05;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class Compiler {

	public static void main(String[] args) {
		boolean success = false;
		try {
			//-----------------------------------------------------------------
			// read the input file
			InputStream input = new FileInputStream("ex1.txt");
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			input.close();
			
			String program = new String(buffer);
			//-----------------------------------------------------------------
			// scan and parse the program
			Parser parser = new Parser(new Scanner(program));
			parser.parse();
			
			printTree(parser.getProgramTree(), "|-");
			success = true;
			//-----------------------------------------------------------------
		} catch (Exception e) {
			System.out.println("Error:\n" + e.getMessage());
			success = false;
		}
		
		if(success)
			System.out.println("Program compiled successfully.");
	}
	//-------------------------------------------------------------------------
	private static void printTree(SyntaxNode node, String prefix) {
		System.out.println(prefix + node.getLabel());
		
		List childs = node.getChilds();
		for(int i=0;i<childs.size();i++) {
			printTree((SyntaxNode) childs.get(i), "  " + prefix);
		}
		
	}
	//-------------------------------------------------------------------------	

}
