package urdf.arm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.rdf3x.ResultSet;

public class AssociationRuleNode {
	private static AssociationRuleNode root;
	private static HashSet<AssociationRuleNode> existentItems = new HashSet<AssociationRuleNode>();
	private static HashSet<Relation> candidates;
	private static Relation rootRelation;
	private static Literal rootLiteral;	
	private static final int numOfBuckets =  100;
	
	private float kldivRoot;
	
	private Histogram histogram = null;
	
	private ArrayList<AssociationRuleNode> children;
	private ArrayList<AssociationRuleNode> parents;
	private ArrayList<AssociationRuleNode> constants;
	
	private HashMap<Relation,Literal> relationLiteralMap;
	
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
		this.relationLiteralMap = new HashMap<Relation, Literal>();
		this.children = new ArrayList<AssociationRuleNode>();
		this.parents = new ArrayList<AssociationRuleNode>();
		this.constants = new ArrayList<AssociationRuleNode>();
		
		if (rootRelation==null || !rootRelation.equals(numericalProperty)) 
			this.rootRelation = numericalProperty;
		if (rootLiteral==null || !rootLiteral.getRelation().equals(numericalProperty)) 
			this.rootLiteral = new Literal(rootRelation, nextVariable++, nextVariable++);
		if (root==null && this.level==0)
			root = this;
		
		existentItems.add(this);

	}
	
	public void addParent(AssociationRuleNode node) {
		if (existentItems.contains(node)) {
			if (node.getRelations().size()==(this.getRelations().size()-1) && node.level==(this.level-1)) {
				if (this.getRelations().containsAll(node.getRelations())) {
					this.parents.add(node);
					return;
				} else {
					throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");
				}
			} else 
				throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.getRelations().size() + " and should be " + this.getRelations().size());
			
		} else 
			throw new IllegalArgumentException("Parent node should already existent");
	}
	
	public void addChild(AssociationRuleNode node){
		if (node.getRelations().size()==(this.getRelations().size()+1) && node.level==(this.level+1)) {
			if (node.getRelations().containsAll(this.getRelations())) {
				this.children.add(node);
				this.existentItems.add(node);	
				return;
			} else 
				throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");
			
		} else 
			throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.getRelations().size() + " and should be " + this.getRelations().size());		
	}
	
	public void addConstant(AssociationRuleNode node){
		if (node.getRelations().size()==this.getRelations().size() && node.level==this.level) {
			if (node.getRelations().containsAll(this.getRelations())) {
				this.constants.add(node);
				this.existentItems.add(node);	
				return;
			} else 
				throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");
			
		} else 
			throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.getRelations().size() + " and should be " + this.getRelations().size() + "\t Level is "+node.level+" and should be "+this.level);
			
	}
	
	public int[] getDistribution() {
		return this.distribution;
	}
	
	public float[] getNormalizedDistribution() {
		return ArrayTools.normalize(distribution);
	}
	
	public Collection<Literal> getLiterals() {
		return relationLiteralMap.values();
	}
	
	public Literal getLiteral(Relation r) {
		return relationLiteralMap.get(r);
	}
	
	public Collection<Relation> getRelations() {
		return relationLiteralMap.keySet();
	}
	
	public ArrayList<AssociationRuleNode> getConstants() {
		return this.constants;
	}
	
	public void addItem(Relation item) {
		if (!relationLiteralMap.containsKey(item)) {
			level++;
			Literal newLiteral = new Literal(item, rootLiteral.getFirstArgument(), nextVariable++);
			relationLiteralMap.put(item, newLiteral);
			return;
		} else 
			throw new IllegalArgumentException("Relation "+item.getName()+" could not be added  (already existent)");
	}
	
	public void addItem(Relation item, String constant) {
		if (!relationLiteralMap.containsKey(item)) {
			level++;
			Literal newLiteral = new Literal(item, rootLiteral.getFirstArgument(), 1/*old variable*/, -1, 0/*new variable*/, constant);
			relationLiteralMap.put(item, newLiteral);
			return;
		} else 
			throw new IllegalArgumentException("Relation "+item.getName()+" could not be added  (already existent)");
	}
	
	public void setConstant(Relation item, String constant) {
		if (relationLiteralMap.containsKey(item)) {
			/*Literal literal = relationLiteralMap.get(item).clone();
			literal.setSecondArgument(-1);
			literal.setConstant(constant);
			relationLiteralMap.put(item, literal);*/
			relationLiteralMap.put(item, new Literal(item, rootLiteral.getFirstArgument(), 1, -1, 1, constant));		
		} else
			throw new IllegalArgumentException("Relation "+item.getName()+" is not contained in node");
	}

	public void queryNodeGroupProperties(QueryHandler qh) throws SQLException {
		if (level==1 && this.getRelations().size()==1) {
			Literal groupLiteral = (Literal) relationLiteralMap.values().toArray()[0];
			ResultSet rs = qh.retrieveGroupDistribution(rootLiteral, groupLiteral);
			if (level>0 && histogram!=null) {
				String lastGroup = "";
				AssociationRuleNode newConst = null;
				while (rs.next()) {
					String groupConst = rs.getString(1).replaceAll("\"", "");
					float x = rs.getFloat(2);		
					int count = rs.getInt(3);
					
					if (!lastGroup.equals(groupConst)) {
						if (newConst!=null) newConst.extractHistogramInformation();
						newConst = this.clone();
						newConst.setConstant(groupLiteral.getRelation(), groupConst);
						for (AssociationRuleNode parent: parents)
							newConst.addParent(parent);
						this.addConstant(newConst);
					}
					lastGroup = groupConst;
					
					this.histogram.addDataPoint(x, count);
					newConst.histogram.addDataPoint(x, count);
				}
				newConst.extractHistogramInformation();
				this.extractHistogramInformation();
			}
		}

	}
	
	
	public void queryNodeProperties(QueryHandler qh) throws SQLException {
		ResultSet rs = qh.retrieveDistribution(rootLiteral, relationLiteralMap.values());
		if (level==0 && histogram==null) {
			rs.first();
			float min = rs.getFloat(1);
			rs.last();
			float max = rs.getFloat(1);
			histogram = new Histogram(min, max, numOfBuckets);
		}
		histogram.reset();
		rs.beforeFirst();
		while (rs.next()) {
			float x = rs.getFloat(1);		
			int count = rs.getInt(2);
			histogram.addDataPoint(x, count);
		}
		extractHistogramInformation();
	}
	
	private void extractHistogramInformation(){ 
		this.distribution = histogram.getDistribution();
		this.entropy = ArrayTools.entropy(this.distribution);
		this.kldivRoot = ArrayTools.klDivergence(ArrayTools.laplaceSmooth(this.distribution), ArrayTools.laplaceSmooth(root.distribution));
		this.support = ArrayTools.sum(this.distribution);			
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			AssociationRuleNode node = (AssociationRuleNode) o;
			for (Relation key: relationLiteralMap.keySet()) {
				if (!key.equals(node.getLiteral(key).getRelation()) || !relationLiteralMap.get(key).getConstant().equals(node.getLiteral(key).getConstant()))
					return false;
			}
			return true;
			
		} catch (ClassCastException e) {
		}
		return false;
	}
	
	@Override 
	public AssociationRuleNode clone() {
		AssociationRuleNode newNode = new AssociationRuleNode(this.rootRelation);
		newNode.histogram = this.histogram.clone();
		newNode.histogram.reset();
		newNode.nextVariable = this.nextVariable;
		newNode.relationLiteralMap = new HashMap<Relation, Literal>();
		newNode.level = this.level;
		for (Relation key: this.relationLiteralMap.keySet()) {
			newNode.relationLiteralMap.put(key, this.relationLiteralMap.get(key));
		}
		return newNode;
	}
	
	@Override
	public String toString() {
		String output = rootLiteral.getSparqlPatternWithConstant();
		for (Literal l: relationLiteralMap.values())
			output += l.getSparqlPatternWithConstant();
		output += "\n\tEntropy="+this.entropy+" Support="+this.support + " KLdiv="+this.kldivRoot;  
		return output;
	}
	
}
