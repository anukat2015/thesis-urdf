package urdf.arm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.rdf3x.ResultSet;

public class AssociationRuleNode {
	private static AssociationRuleNode root;
	private static HashSet<AssociationRuleNode> existentItems;
	private static HashSet<Relation> candidates;
	private static Relation rootRelation;
	private static Literal rootLiteral;
	private static Histogram histogram = null;
	private static final int numOfBuckets =  100;
	
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
	private int level;
	
	public AssociationRuleNode(Relation numericalProperty) {
		this.isGood = false;
		this.level = 0;
		this.nextVariable = (int) 'A';
		this.support = 0;
		this.totalCount = 0;
		this.mean = Float.NaN;
		this.kldivRoot = Float.NaN;
		this.entropy = Float.NaN;
		this.rootRelation = numericalProperty;
		this.rootLiteral = new Literal(rootRelation, nextVariable++, nextVariable++);
		this.literals = new HashSet<Literal>();
		this.items = new HashSet<Relation>();

	}
	
	public void addParent(AssociationRuleNode node) {
		if (existentItems.contains(node)) {
			if (node.items.size()==(this.items.size()-1) && node.level==(this.level-1)) {
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
		if (node.items.size()==(this.items.size()+1) && node.level==(this.level+1)) {
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
		boolean success = this.items.add(item);
		if (success) {
			level++;
			return success;
		} else 
			throw new IllegalArgumentException("Relation "+item.getName()+" could not be added");
	}

	
	public void queryNodeProperties(QueryHandler qh) {
		try {
			ResultSet rs = qh.retrieveDistribution(rootLiteral, literals);
			if (level==0 && histogram==null) {
				rs.first();
				float min = rs.getFloat(1);
				rs.last();
				float max = rs.getFloat(1);
				histogram = new Histogram(min, max, numOfBuckets);
			}
			while (rs.next()) {
				
			}
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		AssociationRuleNode newNode = new AssociationRuleNode(this.rootRelation);
		newNode.items = (HashSet<Relation>) this.items.clone();
		newNode.literals = (HashSet<Literal>) this.literals.clone();
		newNode.nextVariable = this.nextVariable;
		
		return newNode;
		
	}
	
}
