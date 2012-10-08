package urdf.arm;

import java.util.ArrayList;
import java.util.HashSet;

import urdf.ilp.Literal;
import urdf.ilp.Relation;

public class AssociationRuleNode {
	private static AssociationRuleNode root;
	private static HashSet<AssociationRuleNode> existentItems;
	private static Relation numericalProperty;
	private static Histogram histogram;
	
	private float kldivRoot;
	
	private ArrayList<AssociationRuleNode> children;
	private ArrayList<AssociationRuleNode> parents;
	private ArrayList<AssociationRuleNode> constants;
	
	private HashSet<Relation> items;
	private HashSet<Literal> literals;
	
	private boolean isGood;

	private int[] distribution;
	private float entropy;
	private int totalCount;
	private int support;
	private float mean;
	
	private int nextVariable;
	
	public AssociationRuleNode(Relation numericalProperty) {
		isGood = false;
		this.numericalProperty = numericalProperty;
	}
	
	public void addParent(AssociationRuleNode node) {
		if (existentItems.contains(node)) {
			if (node.items.size()==(this.items.size()-1)) {
				if (this.items.containsAll(node.items)) {
					this.parents.add(node);
					return;
				} else {
					throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");
				}
			} else {
				throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.items.size() + " and should be " + this.items.size());
			}
		} else {
			throw new IllegalArgumentException("Parent node should already existent");
		}
		

	}
	
	public void addChild(AssociationRuleNode node){
		if (node.items.size()==(this.items.size()+1)) {
			if (node.items.containsAll(this.items)) {
				this.children.add(node);
				this.existentItems.add(node);	
				return;
			} else {
				throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");
			}
		} else {
			throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.items.size() + " and should be " + this.items.size());
		}
		
	}
	
	public HashSet<Relation> getItems() {
		return items;
	}

	public void setItems(HashSet<Relation> items) {
		this.items = items;
	}
	
	public boolean addItem(Relation item) {
		return this.items.add(item);
	}
	
	
	
	@Override
	public boolean equals(Object o) {
		try {
			AssociationRuleNode node = (AssociationRuleNode) o;
			return this.items.equals(node.getItems());
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	@Override 
	public AssociationRuleNode clone() {
		AssociationRuleNode newNode = new AssociationRuleNode(this.numericalProperty);
		newNode.items = (HashSet<Relation>) this.items.clone();
		newNode.literals = (HashSet<Literal>) this.literals.clone();
		
		return newNode;
		
	}
	
}
