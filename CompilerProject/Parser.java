/*
 * Created by Ankita Shah on Sep 13, 2005
 */
package cs.ashah.tl05;

import java.util.ArrayList;
import java.util.List;


/**
 * Basic parser, syntax validator for the TL05.
 * 
 * @author Ankita Shah
 */
public class Parser {
	//-------------------------------------------------------------------------
	private static Patterns patterns = Patterns.getInstance();
	private Scanner scanner = null;
	private SyntaxNode programTree = null;
	//-------------------------------------------------------------------------
	public Parser(Scanner scanner) {
		this.scanner = scanner;
		this.programTree = new SyntaxNode(null, null);
	}
	//-------------------------------------------------------------------------
	public void parse() throws Exception {
		Parser.parseProgram(this.scanner, this.programTree);
		this.programTree.mergeSingleChild();
	}
	
	public SyntaxNode getProgramTree() {
		return this.programTree;
	}
	//-------------------------------------------------------------------------
	// helper methods to check various programming constructs
	
	// verifies that the next token is exactly the given keyword. if not, 
	// throws an exception.
	private static void checkValidKeyword(
							Scanner scanner, String keyword) throws Exception {
		if(!scanner.moveToNext().equals(keyword))
			throwInvalidSymbol(scanner.getCurrentToken());		
	}
	
	// verifies that the next token complies to the given pattern. if not,
	// throws an exception
	private static void checkValidPattern(
							Scanner scanner, String ident) throws Exception {
		if(!patterns.matchPattern(ident, scanner.moveToNext()))
			throwInvalidSymbol(scanner.getCurrentToken());		
	}
	
	// common code to throw exception with invalid keyword message
	private static void throwInvalidSymbol(String token) throws Exception {
		throw new Exception("Invalid Symbol/Keyword/Identifier: " + token);
	}
	
	// verifies that the current token in the scanner is the same as given.
	// throws a 'required' type of message
	private static void checkRequiredSymbol(
							Scanner scanner, String symbol) throws Exception {
		if(!scanner.getCurrentToken().equals(symbol))
			throwSymbolRequired(symbol, scanner.getCurrentToken());		
	}
	
	// commond code to throw 'required' type of exception
	private static void throwSymbolRequired(
			String required, String present) throws Exception {
		throw new Exception("Missing Symbol/Keyword/Identifier [" 
								+ required + "] near " + present);
	}
	
	// helper method to find out sub sequence, considering the given 
	// keywork/pattern nas a delimeter
	private static String getSubSequence(
								Scanner scanner, String delimeter,
								String[] subDelims) throws Exception {
		List subDelimList = new ArrayList();
		
		if(subDelims != null) {
			for(int i=0;i<subDelims.length;i++) 
				subDelimList.add(subDelims[i]);
		}
		
		StringBuffer subsequence = new StringBuffer();
		int expectedDelims = 1;
		int encounterdDelims = 0;
		
		while(scanner.hasNext() && expectedDelims != encounterdDelims) {
			scanner.moveToNext();

			// manage the internal keywords, which also needs the same delim
			if(scanner.getCurrentToken().equals(delimeter))
				encounterdDelims++;
			else if(subDelimList.contains(scanner.getCurrentToken()))
					expectedDelims++;
			
			if(encounterdDelims != expectedDelims) {
				subsequence.append(scanner.getCurrentToken());
				subsequence.append(' ');
			}			
		}
		
		if(expectedDelims != encounterdDelims) 
			throw new Exception("Keyword Required: " + delimeter 
								+ " at " + scanner.getCurrentToken());
		
		return subsequence.toString();
		
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE THE PROGRAM
	private static void parseProgram(
			Scanner scanner, SyntaxNode parentNode) throws Exception {

		// PROGRAM ident <declarations> BEGIN <statementSequence> END
		String declrations = null;
		String statementSequence = null;
		
		// check the fixed parts
		checkValidKeyword(scanner, "PROGRAM");
		parentNode.addChild(new SyntaxNode("PROGRAM", parentNode));
		
		checkValidPattern(scanner, "ident");
		parentNode.addChild(
				new SyntaxNode(scanner.getCurrentToken(), parentNode));
			
		// go upto begin, and fetch the declarations		
		declrations = getSubSequence(scanner, "BEGIN", null);
		
		SyntaxNode declarationNode = new SyntaxNode(null, parentNode);
		parentNode.addChild(declarationNode);
		Parser.parseDeclarations(
				new Scanner(declrations.toString()), declarationNode);

		parentNode.addChild(new SyntaxNode("BEGIN", parentNode));

		// go upto end, and fetch the statementSeq
		String[] internalSeqs = {"IF", "WHILE"};
		statementSequence = getSubSequence(scanner, "END", 
											internalSeqs);
		//---------------------------------------------------------------------
		// parse subsequences		
		if(statementSequence == null || statementSequence.length() == 0)
			throw new Exception("Program Body Cannot be Empty.");
		
		SyntaxNode statementSeqNode = new SyntaxNode(null, parentNode);
		parentNode.addChild(statementSeqNode);
		Parser.parseStatementSequence(
				new Scanner(statementSequence), statementSeqNode);
		//---------------------------------------------------------------------
		// check if the proper end exists, and its the last token
		checkRequiredSymbol(scanner, "END");
		
		if(scanner.hasNext())
			throwSymbolRequired("END", scanner.getCurrentToken());
		parentNode.addChild(new SyntaxNode("END", parentNode));
		//---------------------------------------------------------------------
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE DECLARATIONS
	private static void parseDeclarations(
			Scanner scanner, SyntaxNode parentNode) throws Exception {
		// VAR ident AS <type> SC <declarations> | EPS

		// its ok if the declarations is empty
		if(!scanner.hasNext()) return;
		//---------------------------------------------------------------------
		String type = "";
		
		// check the fixed parts
		checkValidKeyword(scanner,"VAR");
		parentNode.addChild(new SyntaxNode("VAR", parentNode));
		
		checkValidPattern(scanner, "ident");
		parentNode.addChild(new SyntaxNode(
				scanner.getCurrentToken(), parentNode));
		
		checkValidKeyword(scanner, "AS");
		parentNode.addChild(new SyntaxNode("AS", parentNode));
		
		// get the type subseq
		type = getSubSequence(scanner, ";", null);
		
		SyntaxNode typeNode = new SyntaxNode(null, parentNode);
		parentNode.addChild(typeNode);		
		Parser.parseType(new Scanner(type), typeNode);
		
		// check that it ends if with a valid SC
		checkRequiredSymbol(scanner, ";");
		parentNode.addChild(new SyntaxNode(";", parentNode));		
		//---------------------------------------------------------------------		
		// everything else is the next decl
		Scanner subScanner = scanner.getSubScanner();
		if(subScanner.hasNext())  {
			SyntaxNode declarationNode = 
				new SyntaxNode(null, parentNode.getParent());
			parentNode.addSibling(declarationNode);
			parseDeclarations(subScanner, declarationNode);
		}
		//---------------------------------------------------------------------		
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE TYPE
	private static void parseType(
			Scanner scanner, SyntaxNode parentNode) throws Exception {
		// <type> = ARRAY num OF <type> | INT | BOOL

		// check the valid value as whole
		if(scanner.input.equals("INT")) {
			parentNode.addChild(new SyntaxNode("INT", parentNode));
			return;
		}
		if(scanner.input.equals("BOOL")) {
			parentNode.addChild(new SyntaxNode("BOOL", parentNode));			
			return;
		}
		
		// check the formats
		checkValidKeyword(scanner, "ARRAY");
		parentNode.addChild(new SyntaxNode("ARRAY", parentNode));			
		
		checkValidPattern(scanner, "num");
		parentNode.addChild(new SyntaxNode(
				scanner.getCurrentToken(), parentNode));			
		
		checkValidKeyword(scanner, "OF");
		parentNode.addChild(new SyntaxNode("OF", parentNode));			
		
		// everything else is a type again
		SyntaxNode typeNode = new SyntaxNode(null, parentNode);
		parentNode.addChild(typeNode);
		Parser.parseType(scanner.getSubScanner(), typeNode);
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE STATEMENT/S
	private static void parseStatementSequence(
						Scanner scanner, SyntaxNode parentNode) throws Exception {
		// <statement> = (<assignment> | <ifStatement> | 
		//					<whileStatement> | <writeInt> | <writeLn>)
		//                  SC OneMoreStatement
		
		
		// its OK if its empty
		if(scanner.input.length() == 0) return;
		
		// folowing are the valid constrcuts, however, they should not
		// check for SC to end, that would be done here.
		//---------------------------------------------------------------------
		if(scanner.input.startsWith("IF")) {
			Parser.parseIfStatement(scanner, parentNode);
		}
		//---------------------------------------------------------------------
		else if(scanner.input.startsWith("WHILE")) {
			Parser.parseWhileStatement(scanner, parentNode);
		}
		//---------------------------------------------------------------------
		else if(scanner.input.startsWith("WRITEINT")) {
			// move the writeint part, and get the expr
			scanner.moveToNext();
			parentNode.addChild(new SyntaxNode("WRITEINT", parentNode));
			
			String expression = getSubSequence(scanner, ";", null);
			
			SyntaxNode expressionNode = new SyntaxNode(null, parentNode);
			parentNode.addChild(expressionNode);
			Parser.parseExpression(new Scanner(expression), expressionNode);
			
			// add ;
			parentNode.addChild(new SyntaxNode(";", parentNode));
		}
		//---------------------------------------------------------------------
		else if(scanner.input.startsWith("WRITELN")) {
			// check sanity
			if(!scanner.moveToNext().equals("WRITELN") ||
				!scanner.moveToNext().equals(";"))
				throw new Exception("Invalid Syntax: WRITELN");
			else {
				// Add to parser tree;
				parentNode.addChild(new SyntaxNode("WRITELN", parentNode));
				parentNode.addChild(new SyntaxNode(";", parentNode));
			}
		}
		//---------------------------------------------------------------------
		else {
			String assignment = getSubSequence(scanner, ";", null);
			
			Parser.parseAssignment(new Scanner(assignment), parentNode);
			
			// add ;
			parentNode.addChild(new SyntaxNode(";", parentNode));
		}
		//---------------------------------------------------------------------
		// if there is something more remained, there are more statements
		if(scanner.hasNext()) {
			SyntaxNode statementSeqNode = 
				new SyntaxNode(null, parentNode.getParent());
			parentNode.addSibling(statementSeqNode);
			Parser.parseStatementSequence(
					scanner.getSubScanner(), statementSeqNode);
		}
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE IF STATEMENT
	private static void parseIfStatement(
			Scanner scanner, SyntaxNode parentNode) throws Exception {
		// IF <expression> THEN <statementSequence> ELSE <statementSequence> END
		// IF <expression> THEN <statementSequence> END

		// check the fix part
		checkValidKeyword(scanner, "IF");
		parentNode.addChild(new SyntaxNode("IF", parentNode));
		
		String expression = getSubSequence(scanner, "THEN", null);
		
		SyntaxNode expressionNode = new SyntaxNode(null, parentNode);
		parentNode.addChild(expressionNode);
		Parser.parseExpression(new Scanner(expression), expressionNode);
		
		// check for the required keyword
		checkRequiredSymbol(scanner, "THEN");
		parentNode.addChild(new SyntaxNode("THEN", parentNode));
		
		// get the total IF body
		String[] internals = {"IF", "WHILE"};
		String ifBody = getSubSequence(scanner, "END", internals);
		//---------------------------------------------------------------------
		// get the THEN and ELSE seqences (else is optional)
		String ifSequence = null;
		String elseSequence = null;
		
		int elsePos = ifBody.indexOf("ELSE");
		if(elsePos != -1) {
			// there exists an else sequence
			ifSequence = ifBody.substring(0, elsePos);
			elseSequence = ifBody.substring(elsePos + 4);
		} else ifSequence = ifBody;
		//---------------------------------------------------------------------
		SyntaxNode ifStatements = new SyntaxNode(null, parentNode);
		parentNode.addChild(ifStatements);
		
		Parser.parseStatementSequence(
				new Scanner(ifSequence), ifStatements);
		if(elseSequence != null) {
			parentNode.addChild(new SyntaxNode("ELSE", parentNode));
			SyntaxNode elseStatements = new SyntaxNode(null, parentNode);
			parentNode.addChild(elseStatements);
			
			Parser.parseStatementSequence(
					new Scanner(elseSequence), elseStatements);
		}
		//---------------------------------------------------------------------
		// make sure it ENDs properly
		checkRequiredSymbol(scanner, "END");
		parentNode.addChild(new SyntaxNode("END", parentNode));		
		//---------------------------------------------------------------------		
	} 
	//-------------------------------------------------------------------------
	// METHOD TO PARSE WHILE STATEMENT
	private static void parseWhileStatement(
					Scanner scanner, SyntaxNode parentNode) throws Exception {
		// <whileStatement> = WHILE <expression> DO <statementSequence> ENDWHILE
		
		// check the keyword
		checkValidKeyword(scanner, "WHILE");
		parentNode.addChild(new SyntaxNode("WHILE", parentNode));
		
		String expression = getSubSequence(scanner, "DO", null);
		
		SyntaxNode expressionNode = new SyntaxNode(null, parentNode);
		parentNode.addChild(expressionNode);		
		Parser.parseExpression(new Scanner(expression), expressionNode);

		// add DO to the tree
		parentNode.addChild(new SyntaxNode("DO", parentNode));
		
		
		String[] internals = {"IF", "WHILE"};
		String statementSequence = getSubSequence(scanner, "END", internals);
		
		//parse and add to tree
		SyntaxNode statementSeqNode = new SyntaxNode(null, parentNode);
		parentNode.addChild(statementSeqNode);
		parseStatementSequence(
				new Scanner(statementSequence), statementSeqNode);
		
		// add END to the tree
		parentNode.addChild(new SyntaxNode("END", parentNode));		
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE EXPRESSION
	private static void parseExpression(
				Scanner scanner, SyntaxNode parentNode) throws Exception {
		// <simpleExpression> | <simpleExpression> OP4 <simpleExpression>
		// OP4 = EQ|NE|LT|GT|LTE|GTE 
		
		// find out whether its one simpelExp or two simpleExps
		StringBuffer simpleExp = new StringBuffer();
		StringBuffer simpleExp2 = new StringBuffer();
		
		boolean exp1 = true;
		String token = null;
		String operation = null;
		while(scanner.hasNext()) {
			token = scanner.moveToNext();
			
			if(patterns.matchPattern("OP4", token) && exp1) {
				operation = token;
				exp1 = false;
			} else {
				if(exp1) simpleExp.append(token + ' ');
				else simpleExp2.append(token + ' ');
			}
		}
		
		//parse simple expressions
		SyntaxNode simplExpNode1 = new SyntaxNode(null, parentNode);
		parentNode.addChild(simplExpNode1);
		Parser.parseSimpleExpression(
				new Scanner(simpleExp.toString()), simplExpNode1);
		
		if(simpleExp2.length() != 0) {
			parentNode.addChild(new SyntaxNode(operation, parentNode));
			//parse tree for simple exp
			SyntaxNode simplExpNode2 = new SyntaxNode(null, parentNode);
			parentNode.addChild(simplExpNode2);
			Parser.parseSimpleExpression(
					new Scanner(simpleExp2.toString()), simplExpNode2);
		}
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE SIMPLE EXPRESSIONS
	private static void parseSimpleExpression(
					Scanner scanner, SyntaxNode parentNode) throws Exception {
		// <simpleExpression> = <term> OP3 <term> | <term>
		// OP3 = PLUS|MINUS 

		// find out whether its one term or two terms
		StringBuffer term1 = new StringBuffer();
		StringBuffer term2 = new StringBuffer();
		
		boolean isTerm1 = true;
		String token = null;
		String operation = null;
		while(scanner.hasNext()) {
			token = scanner.moveToNext();
			
			if(patterns.matchPattern("OP3", token) && isTerm1) {
				isTerm1 = false;
				operation = token;
			} else {
				if(isTerm1) term1.append(token + ' ');
				else term2.append(token + ' ');
			}
		}
		
		// parse terms 1
		SyntaxNode term1Node = new SyntaxNode(null, parentNode);
		parentNode.addChild(term1Node);
		Parser.parseTerm(new Scanner(term1.toString()), term1Node);
		
		// parse terms 2
		if(term2.length() != 0) {
			parentNode.addChild(new SyntaxNode(operation, parentNode));
			
			SyntaxNode term2Node = new SyntaxNode(null, parentNode);
			parentNode.addChild(term2Node);
			Parser.parseTerm(new Scanner(term2.toString()), term2Node);
		}
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE TERMS
	private static void parseTerm(
			Scanner scanner, SyntaxNode parentNode) throws Exception {
		// <factor> OP2 <factor> | <factor>
		// OP2 = MUL|DIV|MOD 

		
		// find out whether its one factor or two factors
		StringBuffer factor1 = new StringBuffer();
		StringBuffer factor2 = new StringBuffer();
		
		boolean isFactor1 = true;
		String token = null;
		String operation = null;
		
		while(scanner.hasNext()) {
			token = scanner.moveToNext();
			
			if(patterns.matchPattern("OP2", token) && isFactor1) {
				isFactor1 = false;
				operation = token;
			} else {
				if(isFactor1) factor1.append(token + ' ');
				else factor2.append(token + ' ');
			}
		}
		
		// parse factor 1
		SyntaxNode factor1Node = new SyntaxNode(null, parentNode);
		parentNode.addChild(factor1Node);
		Parser.parseFactor(new Scanner(factor1.toString()), factor1Node);
		
		// parse factor 2
		if(factor2.length() != 0) {
			parentNode.addChild(new SyntaxNode(operation, parentNode));
		
			SyntaxNode factor2Node = new SyntaxNode(null, parentNode);
			parentNode.addChild(factor2Node);
			Parser.parseFactor(new Scanner(factor2.toString()), factor2Node);
		}
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE FACTORS
	private static void parseFactor(
					Scanner scanner, SyntaxNode parentNode) throws Exception {
		// <factor> = <memCell> | lit | ( <expression> )
		
		// check if its just a literal
		if(patterns.matchPattern("lit", scanner.input)) {
			parentNode.addChild(new SyntaxNode(scanner.input, parentNode));
			return;
		} 
		// check if its the paranthases
		else if(scanner.input.startsWith("(") && scanner.input.endsWith(")")) {
			
			String expression = 
				scanner.input.substring(1, scanner.input.length() - 1);
			
			parentNode.addChild(new SyntaxNode("(", parentNode));
			
			SyntaxNode expressionNode = new SyntaxNode(null, parentNode);
			parentNode.addChild(expressionNode);
			Parser.parseExpression(new Scanner(expression), expressionNode);
			
			parentNode.addChild(new SyntaxNode(")", parentNode));
			
		} else {
			SyntaxNode memNode = new SyntaxNode(null, parentNode);
			parentNode.addChild(memNode);
			Parser.parseMemCell(scanner, memNode);
		}
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE MEMCELL
	private static void parseMemCell(
				Scanner scanner, SyntaxNode parentNode) throws Exception {
		// ident | ident LB <expression> RB
		String ident = scanner.input;
		boolean paranth = false;
		
		if(ident.startsWith("(") && ident.endsWith(")")) {
			ident = ident.substring(1, ident.length());
			paranth = true;
		}
		
		if(!patterns.matchPattern("ident", ident))
			throwInvalidSymbol(ident);
		
		// add syntax nodes
		if(paranth) parentNode.addChild(new SyntaxNode("(", parentNode));
		parentNode.addChild(new SyntaxNode(ident, parentNode));
		if(paranth) parentNode.addChild(new SyntaxNode(")", parentNode));
	}
	//-------------------------------------------------------------------------
	// METHOD TO PARSE ASSIGNMENT
	private static void parseAssignment(
					Scanner scanner, SyntaxNode parentNode) throws Exception {
		// <memCell> := <expression> | <memCell> := READINT
		
		// find the memcell and LHS
		String memCell = getSubSequence(scanner, ":=", null);
		
		//parser tree for memCell
		SyntaxNode memNode = new SyntaxNode(null, parentNode);
		parentNode.addChild(memNode);
		Parser.parseMemCell(new Scanner(memCell), memNode);
		
		// add ASSG to tree
		parentNode.addChild(new SyntaxNode(":=", parentNode));
		
		Scanner subScanner = scanner.getSubScanner();
		// check if its just the readint
		if(subScanner.input.equals("READINT")) {
			parentNode.addChild(new SyntaxNode("READINT", parentNode));
			return;
		} else {
			SyntaxNode expressionNode = new SyntaxNode(null, parentNode);
			parentNode.addChild(expressionNode);
			Parser.parseExpression(subScanner, expressionNode);
		}
	}
	//-------------------------------------------------------------------------
}
