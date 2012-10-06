package arm;

import java.util.ArrayList;

public class AssociationRuleNode {
	AssociationRuleNode root;
	float kldivRoot;
	
	ArrayList<AssociationRuleNode> children;
	ArrayList<AssociationRuleNode> constants;
	
	Histogram histogram;
	
	int[] distribution;
	float entropy;
	int totalCount;
	int support;
	float mean;
	
	public AssociationRuleNode() {
		
	}
	
	public void addParent() {
		
	}
	
	public void addChild(){ 
		
	}
	
}
