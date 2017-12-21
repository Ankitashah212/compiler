/*
 * Created by Ankita Shah on Sep 14, 2005
 */
package cs.ashah.tl05;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Encapsulates a node in the syntax tree. As such there is no spearate tree
 * class, as the node itself can provide a recursive view of its childs. 
 * 
 * @author Ankita Shah
 */
public class SyntaxNode {
	//-------------------------------------------------------------------------
	private static List allNodes = new Vector();
	//-------------------------------------------------------------------------
	private SyntaxNode parent = null;
	private List childs = new ArrayList();
	private String label = null;
	//-------------------------------------------------------------------------
	public SyntaxNode(String label, SyntaxNode parent) {
		this.label = label;
		if(this.label == null) this.label = "+";
		this.parent = parent;
		
		SyntaxNode.allNodes.add(this);
	}
	//-------------------------------------------------------------------------
	public SyntaxNode getParent() {
		return this.parent;
	}
	public boolean hasChilds() {
		return (this.childs.size() > 0);
	}
	public String getLabel() {
		return this.label;
	}
	public void addChild(SyntaxNode child) {
		this.childs.add(child);
	}
	public List getChilds() {
		return this.childs;
	}
	public void addSibling(SyntaxNode node) {
		if(this.parent != null) {
			this.parent.addChild(node);
		}
	}
	//-------------------------------------------------------------------------
	public int getNodeIndex() {
		return SyntaxNode.allNodes.indexOf(this);
	}
	public static SyntaxNode[] getAllNodes() {
		SyntaxNode[] all = new SyntaxNode[allNodes.size()];
		
		for(int i=0;i<all.length;i++)
			all[i] = (SyntaxNode) allNodes.get(i);
		
		return all;
	}
	//-------------------------------------------------------------------------
	// UTILTIY METHODS
	public void mergeSingleChild() {
		if(this.childs.size() == 1) {
			SyntaxNode singleChild = (SyntaxNode) this.childs.get(0);
			singleChild.mergeSingleChild();
			
			this.label = singleChild.label;
			this.parent = singleChild.parent;
			this.childs = singleChild.childs;
		} else {
			for(int i=0;i<this.childs.size();i++) {
				SyntaxNode aChild = (SyntaxNode) this.childs.get(i);
				aChild.mergeSingleChild();
			}
		}
	}
	//-------------------------------------------------------------------------
}
