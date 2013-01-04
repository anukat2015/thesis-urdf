package urdf.arm;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.bcel.generic.CPInstruction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;

import rcaller.RCaller;
import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.ilp.RelationsInfo;
import urdf.ilp.Rule;
import urdf.rdf3x.ResultSet;

public class CorrelationLatticeNode implements Comparable<CorrelationLatticeNode>, Serializable{

	private static final long serialVersionUID = 4782549761623555466L;
	
	private static CorrelationLatticeNode root;
	private static ArrayList<CorrelationLatticeNode> existentItems = new ArrayList<CorrelationLatticeNode>();
	private static Collection<Relation> candidates;
	private static Relation rootRelation;
	private static Literal rootLiteral;
	
	private static RCaller caller = new RCaller();
	private static Collection<NumericalRule> resultRules = new ArrayList<NumericalRule>();

	private float kldivRoot;

	private Histogram histogram = null;

	private HashSet<CorrelationLatticeNode> children;
	private HashSet<CorrelationLatticeNode> parents;
	private HashSet<CorrelationLatticeNode> constants;

	private TreeMap<Float,CorrelationLatticeNode> sortedChildren;
	
	private HashMap<Literal/*Head*/, TreeMap<Float,Literal/*NewLiteral*/>> headNewLiteralMap;
	
	private HashMap<CorrelationLatticeNode, Float> kldivParents;
	private HashMap<CorrelationLatticeNode, Float> kldivChildren;
	private HashMap<CorrelationLatticeNode, Float> kldivConstants;
	public float maxKldivParents;
	private float maxKldivChildren;
	private float maxKldivConstants;

	private HashMap<Relation, Literal> relationLiteralMap;
	private HashMap<Literal, Float> literalMaxBoundary;
	private HashMap<Literal, Float> literalMinBoundary;

	private boolean isGood;
	private boolean pruned;

	private int[] distribution;
	private float entropy;
	private int totalCount;
	private int support;
	private float mean;

	private int nextVariable;
	private int level;
	

	// Thresholds
	public static  float kldivThreshold = (float) 0.0;
	public static  float jsdivThreshold = (float) 0.0;
	public static  float chisqThreshold = (float) 0.0;
	public static  float indepThreshold = (float) 0;
	public static  int supportThreshold = 25;
	public static  int bucketSupportThreshold = 4;
	public static  float confidenceThreshold = (float) 0.7;
	
	public static int numOfBuckets = 25;

	public CorrelationLatticeNode(Relation numericalProperty) {
		this.isGood = false;
		this.pruned = false;
		this.level = 0;
		this.nextVariable = (int) 'A';
		this.support = 0;
		this.totalCount = 0;
		this.mean = Float.NaN;
		this.kldivRoot = Float.NaN;
		this.entropy = Float.NaN;
		
		this.relationLiteralMap = new HashMap<Relation, Literal>();
		this.literalMaxBoundary = new HashMap<Literal, Float>();
		this.literalMinBoundary = new HashMap<Literal, Float>();
		
		this.children = new HashSet<CorrelationLatticeNode>();
		this.parents = new HashSet<CorrelationLatticeNode>();
		this.constants = new HashSet<CorrelationLatticeNode>();
		this.kldivChildren = new HashMap<CorrelationLatticeNode, Float>();
		this.kldivParents = new HashMap<CorrelationLatticeNode, Float>();
		this.kldivConstants = new HashMap<CorrelationLatticeNode, Float>();
		this.sortedChildren = new TreeMap<Float, CorrelationLatticeNode>();
		this.headNewLiteralMap = new HashMap<Literal, TreeMap<Float,Literal>>();
		//this.sortedKLdivChildren = new TreeMap<Float, CorrelationLatticeNode>();
		this.maxKldivChildren = Float.NEGATIVE_INFINITY;
		this.maxKldivParents = Float.NEGATIVE_INFINITY;
		this.maxKldivConstants = Float.NEGATIVE_INFINITY;

		if (rootRelation == null || !rootRelation.equals(numericalProperty))
			this.rootRelation = numericalProperty;
		if (rootLiteral == null || !rootLiteral.getRelation().equals(numericalProperty))
			this.rootLiteral = new Literal(rootRelation, nextVariable++, nextVariable++);
		if (root == null && this.level == 0) {
			root = this;
			existentItems = new ArrayList<CorrelationLatticeNode>();
			existentItems.add(this);
			resultRules = new ArrayList<NumericalRule>();
		}

	}

	public void addParent(CorrelationLatticeNode node) {
		if (existentItems.contains(node)) {
			if (node.getRelations().size() == (this.getRelations().size() - 1) && node.level == (this.level - 1)) {
				if (this.getRelations().containsAll(node.getRelations())) {
					this.parents.add(node);
					return;
				} else {
					throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");
				}
			} else
				throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.getRelations().size() + " and should be " + this.getRelations().size());

		} else
			throw new IllegalArgumentException("Parent node should already be existent:\n"+node.toString());
	}

	public void addChild(CorrelationLatticeNode node) {
		if (node.getRelations().size() == (this.getRelations().size() + 1) && node.level == (this.level + 1)) {
			if (node.getRelations().containsAll(this.getRelations())) {
				this.children.add(node);
				this.existentItems.add(node);
				return;
			} else
				throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");

		} else
			throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.getRelations().size() + " and should be "+ this.getRelations().size());
	}

	public void addConstant(CorrelationLatticeNode node) {
		if (node.getRelations().size() == this.getRelations().size() && node.level == this.level) {
			if (node.getRelations().containsAll(this.getRelations())) {
				this.constants.add(node);
				this.existentItems.add(node);
				return;
			} else
				throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");

		} else
			throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.getRelations().size() + " and should be " + this.getRelations().size() + "\t Level is " + node.level + " and should be " + this.level);

	}

	public boolean isPruned() {
		return pruned;
	}

	public int[] getDistribution() {
		return this.distribution;
	}
	
	public float[] getBoundaries() {
		return this.histogram.boundaries;
	}


	public float[] getNormalizedDistribution() {
		return ArrayTools.normalize(distribution);
	}
	
	public Literal getRootLiteral() {
		return rootLiteral;
	}
	
	public Histogram getHistogram() {
		return histogram;
	}
	
	public int getSupport() {
		return support;
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

	public Collection<CorrelationLatticeNode> getConstants() {
		return this.constants;
	}

	public void addItem(Relation item) {
		if (!relationLiteralMap.containsKey(item)) {
			level++;
			Literal newLiteral = new Literal(item,rootLiteral.getFirstArgument(), nextVariable++);
			relationLiteralMap.put(item, newLiteral);
			return;
		} else
			throw new IllegalArgumentException("Relation " + item.getName() + " could not be added in " + this.toString() + "\n" + this.getInfo());
	}

	public void addItem(Relation item, String constant) {
		if (constant == null) {
			addItem(item);
		} else {
			if (!this.relationLiteralMap.containsKey(item)) {
				this.level++;
				Literal newLiteral = new Literal(item, rootLiteral.getFirstArgument(), 1, -1, 1, constant, true);
				this.relationLiteralMap.put(item, newLiteral);
				return;
			} else
				throw new IllegalArgumentException("Relation " + item.getName() + " could not be added in " + this.toString() + "\n" + this.getInfo());
		}
	}

	public void setConstant(Relation item, String constant) {
		if (relationLiteralMap.containsKey(item)) {
			relationLiteralMap.put(item,new Literal(item, rootLiteral.getFirstArgument(), 1, -1, 1, constant, true));
		} else
			throw new IllegalArgumentException("Relation " + item.getName() + " is not contained in node");
	}
	
	public void setHistogram(Histogram hs) {
		this.histogram = hs;
	}
	
	public void addNumericalLiteralMax(Literal l, float max) {
		literalMaxBoundary.put(l, max);
	}
	public void addNumericalLiteralMin(Literal l, float min) {
		literalMinBoundary.put(l, min);
	}
	
	private void extractNodeAndConstants(ResultSet rs, Literal groupLiteral, float mean) throws SQLException {
		System.out.println("Extracting "+groupLiteral.getRelationName()+" with mean="+mean);
		CorrelationLatticeNode greaterConst = this.clone();
		CorrelationLatticeNode smallerConst = this.clone();
		greaterConst.addNumericalLiteralMin(groupLiteral, mean);
		smallerConst.addNumericalLiteralMax(groupLiteral, mean);
		greaterConst.relationLiteralMap.get(groupLiteral.getRelation()).setMin(mean);
		smallerConst.relationLiteralMap.get(groupLiteral.getRelation()).setMax(mean);
		greaterConst.histogram.reset();
		smallerConst.histogram.reset();
		
		while (rs.next()) {
			float numConst = rs.getFloat(1);
			float x = rs.getFloat(2);
			
			int count = 1;
			try { 
				count = rs.getInt("count");
			} catch (SQLException e){}
			
			this.histogram.addDataPoint(x, count);
			if (numConst>mean)
				greaterConst.histogram.addDataPoint(x, count);
			else
				smallerConst.histogram.addDataPoint(x, count);
		}
		existentItems.add(greaterConst);
		existentItems.add(smallerConst);
		
		this.constants.add(smallerConst);
		this.constants.add(greaterConst);

		for (CorrelationLatticeNode parent : parents) {
			greaterConst.addParent(parent);
			smallerConst.addParent(parent);
		}
		
		this.extractHistogramInformation();
		this.analizeNode();
		greaterConst.extractHistogramInformation();
		greaterConst.analizeNode();
		smallerConst.extractHistogramInformation();
		smallerConst.analizeNode();
		
		
	}
	
	private void extractNodeAndConstants(ResultSet rs, Literal groupLiteral) throws SQLException {
		if (level > 0 && histogram != null) {
			String lastGroup = "";
			CorrelationLatticeNode newConst = this.clone();
			newConst.histogram.reset();
			while (rs.next()) {
				String groupConst = rs.getString(1).replaceAll("\"", "");
				float x = rs.getFloat(2);
				
				int count = 1;
				try { 
					count = rs.getInt("count");
				} catch (SQLException e){}

				if (!lastGroup.equals(groupConst)) {
					newConst.extractHistogramInformation();
					if (newConst.support>=supportThreshold) {
						newConst.setConstant(groupLiteral.getRelation(), lastGroup);
						for (CorrelationLatticeNode parent : parents)
							newConst.addParent(parent);
						this.addConstant(newConst);
						newConst.analizeNode();
						existentItems.add(newConst);
						
						newConst = this.clone();
					} //else System.out.println("FailedSupportTest[="+newConst.support+"]:"+lastGroup);
					newConst.histogram.reset();
				}
				lastGroup = groupConst;

				this.histogram.addDataPoint(x, count);
				newConst.histogram.addDataPoint(x, count);
			}
			newConst.extractHistogramInformation();
			if (newConst.support>=supportThreshold) {
				newConst.setConstant(groupLiteral.getRelation(), lastGroup);
				for (CorrelationLatticeNode parent : parents)
					newConst.addParent(parent);
				this.addConstant(newConst);
				newConst.analizeNode();
				existentItems.add(newConst);
				
				newConst = this.clone();
				newConst.histogram.reset();
			} //else System.out.println("FailedSupportTest[="+newConst.support+"]:"+lastGroup);
			this.extractHistogramInformation();
			this.analizeNode();
		}
	}


	public void queryNodeGroupProperties(QueryHandler qh) throws SQLException {
		if (level == 1 && this.getRelations().size() == 1) {
			Literal groupLiteral = (Literal) relationLiteralMap.values().toArray()[0];
			ResultSet rs = qh.retrieveGroupDistribution(rootLiteral,groupLiteral);
			float mean = QueryHandler.getMean(rs);
			if (!Float.isNaN(mean)) {
				extractNodeAndConstants(rs, groupLiteral, mean);
			} else {
				extractNodeAndConstants(rs, groupLiteral);
			}
			
		}
		existentItems.add(this);

	}

	public void queryNodeProperties(QueryHandler qh) throws SQLException {
		ResultSet rs = qh.retrieveDistribution(rootLiteral,relationLiteralMap.values(),this.getFilter());

		if (level == 0 && histogram == null) {
			float min = Float.POSITIVE_INFINITY;
			float max = Float.NEGATIVE_INFINITY;
			while (rs.next()) {
				float n = rs.getFloat(1);
				if (n<min) min = n;
				if (n>max) max = n;
			}
			 
			//System.out.println("["+min+","+max+"]");
			
			//histogram = new Histogram(min, max, numOfBuckets);
			histogram = new VHistogram(rs, min, max, numOfBuckets);
		} 
		else {
			histogram.reset();
			
			rs.beforeFirst();
			while (rs.next()) {
				float x = rs.getFloat(1);
				
				int count = 1;
				try { 
					count = rs.getInt("count");
				} catch (SQLException e){}
				
				histogram.addDataPoint(x, count);
			}
		}
		/*histogram.reset();
		rs.beforeFirst();
		while (rs.next()) {
			float x = rs.getFloat(1);
			int count = 1;
			try { 
				count = rs.getInt("count");
			} catch (SQLException e){}
			
			histogram.addDataPoint(x, count);
		}*/
		
		extractHistogramInformation();
		ArrayTools.print(distribution);
		ArrayTools.print(histogram.boundaries);
	}
	
	public void extractHistogramInformation() {
		this.distribution = histogram.getDistribution();
		this.mean = histogram.getMean();
		this.entropy = ArrayTools.entropy(this.distribution);
		this.kldivRoot = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(root.distribution));
		this.support = ArrayTools.sum(this.distribution);
	}
	
	public void analizeNode() {
		
		// Maximum KLDIV
		for (CorrelationLatticeNode parent : parents) {
			float kldiv = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(parent.distribution));
			kldivParents.put(parent, kldiv);
			if (kldiv > this.maxKldivParents)
				this.maxKldivParents = kldiv;
		}
		// Average KLDIV
		/*maxKldivParents = 0;
		for (CorrelationLatticeNode parent : parents) {
			float kldiv = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(parent.distribution));
			kldivParents.put(parent, kldiv);
			this.maxKldivParents += kldiv/((float)parents.size());
		}*/
		
		for (CorrelationLatticeNode child : children) {
			if (child.distribution != null) {
				float kldiv = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(child.distribution));
				kldivChildren.put(child, kldiv);
				sortedChildren.put(kldiv, child);
				sortedChildren.put(ArrayTools.kullbackLeiblerDivergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(child.distribution)), child);
				if (kldiv > this.maxKldivChildren)
					this.maxKldivChildren = kldiv;
			}
		}

		if (this.maxKldivParents < kldivThreshold || this.support < supportThreshold) {
			this.pruned = true;
		}
	}

	public boolean canJoin(Object o) {
		// To join, nodes should have all literals similar but one, which should
		// have different relation
		try {
			CorrelationLatticeNode node = (CorrelationLatticeNode) o;
			int countDifferentLiterals = 0;
			for (Relation key : relationLiteralMap.keySet()) {
				if (!key.equals(node.getLiteral(key).getRelation())
						|| !relationLiteralMap.get(key).getConstant()
								.equals(node.getLiteral(key).getConstant())) {
					if (++countDifferentLiterals > 1)
						return false;
					else if (key.equals(node.getLiteral(key).getRelation()))
						return false;
				}
			}
			if (countDifferentLiterals == 1)
				return true;
			else
				return false;

		} catch (ClassCastException e) {
		}
		return false;
	}


	public String getRelationSetNames() {
		String output = "";
		for (Relation r : this.getRelations()) {
			output += r.getSimpleName() + " ";
		}
		return output;
	}

	public String getInfo() {
		return "Level=" + this.level + " Pruned=" + this.pruned + " #Parents="+ this.parents.size() +" Entropy="+ this.entropy + " Support=" + this.support + " KLdivRoot="+ this.kldivRoot + " MaxKLdivParents=" + this.maxKldivParents;
	}

	
	@Override
	public boolean equals(Object o) {
		try {
			CorrelationLatticeNode node = (CorrelationLatticeNode) o;
			// Nodes are equal if they have same relations with same constants
			if (relationLiteralMap!=null && node.relationLiteralMap!=null) {
				if (relationLiteralMap.size()!=node.relationLiteralMap.size())
					return false;
				for (Relation key : relationLiteralMap.keySet()) {
					Literal lit1 = this.getLiteral(key);
					Literal lit2 = node.getLiteral(key);
					if (lit2==null || lit1==null)
						return false;
					int secondVar1 = lit1.getSecondArgument();
					int secondVar2 = lit2.getSecondArgument();
					if (!lit1.getRelation().equals(lit2.getRelation()))
						return false;
					else {
						if ((secondVar1 == -1 && secondVar2 >= 0) || (secondVar1>=0 && secondVar2 == -1))
							return false;
						if (secondVar1 == -1 && secondVar2 == -1 && !lit1.getConstant().equals(lit2.getConstant()))
							return false;
					}
				}
				if (literalMaxBoundary.size()==node.literalMaxBoundary.size() && literalMaxBoundary.keySet().containsAll(node.literalMaxBoundary.keySet())) {
					if (literalMinBoundary.size()==node.literalMinBoundary.size() && literalMinBoundary.keySet().containsAll(node.literalMinBoundary.keySet())) {
						return true;
					}
				}
			}
		} catch (ClassCastException e) {
		}
		return false;
	}

	@Override
	public int hashCode() {
		int code = 0;
		for (Literal l : relationLiteralMap.values()) {
			code += l.getRelationName().hashCode();
			if (l.getSecondArgument()==-1 && l.getConstant() != null)
				code += l.getConstant().hashCode();
		}
		for (Literal l : literalMaxBoundary.keySet()) 
			code += l.hashCode()*literalMaxBoundary.get(l);
		for (Literal l : literalMinBoundary.keySet()) 
			code += l.hashCode()/literalMinBoundary.get(l);
		
		return code;
	}
	
	@Override
	public CorrelationLatticeNode clone() {
		CorrelationLatticeNode newNode = new CorrelationLatticeNode(this.rootRelation);
		newNode.histogram = this.histogram.clone();
		newNode.histogram.reset();
		newNode.nextVariable = this.nextVariable;
		newNode.level = this.level;
		
		newNode.relationLiteralMap = new HashMap<Relation, Literal>();
		for (Relation key : this.relationLiteralMap.keySet()) 
			newNode.relationLiteralMap.put(key, this.relationLiteralMap.get(key).clone());
		
		
		newNode.literalMaxBoundary = new HashMap<Literal, Float>();
		for (Literal key : this.literalMaxBoundary.keySet()) 
			newNode.literalMaxBoundary.put(key.clone(), (float)this.literalMaxBoundary.get(key));
		newNode.literalMinBoundary = new HashMap<Literal, Float>();
		for (Literal key : this.literalMinBoundary.keySet()) 
			newNode.literalMinBoundary.put(key.clone(), (float)this.literalMinBoundary.get(key));
		
		return newNode;
	}
	
	@Override
	public String toString() {
		String output = rootLiteral.toString();
		for (Literal l : relationLiteralMap.values())
			output += l.toString();
		return output;
	}

	@Override
	public int compareTo(CorrelationLatticeNode o) {
		// TODO Auto-generated method stub
		if (this.equals(o)) 
			return 0;
		else
			return -Float.compare(this.maxKldivParents, o.maxKldivParents);
	}
	
	
	public void addHeadNewLiteral(Literal head, Literal newLiteral, CorrelationLatticeNode joinNode, CorrelationLatticeNode bodyNode) {
		
		float div = ArrayTools.divergence(ArrayTools.laplaceSmooth(joinNode.distribution), ArrayTools.laplaceSmooth(bodyNode.distribution));
		
		TreeMap<Float,Literal> map = null;
		if (!headNewLiteralMap.containsKey(head)) {
			map = new TreeMap<Float, Literal>();
			headNewLiteralMap.put(head, map);
		} else {
			map = headNewLiteralMap.get(head);
		}
		
		map.put(div, newLiteral);
	}
	
	public static void addRule(NumericalRule rule) {
		boolean isSpecialization = false;
		for (NumericalRule r: resultRules) {
			if (rule.isSpecializationOf(r)) {
				isSpecialization = true;
				rule.generalizations.add(r);								
			}		
		}	
		if (rule.hasGainComparedToGeneralizations(confidenceThreshold, supportThreshold) && !resultRules.contains(rule)) 
			resultRules.add(rule);
	}
	
	public static void testRule(CorrelationLatticeNode node, CorrelationLatticeNode parent) {
		float overallAccuracy = ((float)ArrayTools.sum(node.distribution))/((float)ArrayTools.sum(parent.distribution));
		float[] acc = ArrayTools.getAccuraciesWithMinSupport(node.distribution, parent.distribution, supportThreshold);

		boolean isGood = false;
		if (overallAccuracy<confidenceThreshold) 
			if (ArrayTools.max(acc) > confidenceThreshold)
				isGood = true;
		
		if (isGood)  {
			NumericalRule rule;
			Collection<Relation> litNode = node.relationLiteralMap.keySet();
			Collection<Relation> litParent = parent.relationLiteralMap.keySet();
			if (litNode.size() == (litParent.size()+1)) {
				try {
					Literal head = null;
					// If node is a child from parent
					if (litNode.size()==(litParent.size()+1)) {
						for (Relation r: litNode) {
							if (!litParent.contains(r)) {
								head = node.relationLiteralMap.get(r);
								break;
							}
						}
					}
					// If node is a constant from parent
					if (litNode.size()==litParent.size()) {
						for (Relation r: litNode) {
							int arg2Node = node.relationLiteralMap.get(r).getSecondArgument();
							int arg2Parent = parent.relationLiteralMap.get(r).getSecondArgument();
							if ((arg2Node == -1 || arg2Parent == -1) && arg2Node != arg2Parent) {
								head = node.relationLiteralMap.get(r);
							}
						}									
					}
					if (head!=null) {
						rule = new NumericalRule(head, 1);							
						for (Relation bodyLitKey: litParent) 
							rule.addLiteral(node.relationLiteralMap.get(bodyLitKey));
						rule.headFilter = node.getFilter(head);
						rule.bodyFilter = node.getFilter();
						
						rule.setSupportAndAccuracyDistribution(node.distribution, parent.distribution, acc);
						
						addRule(rule);
						
						System.out.println(rule.getRuleString());
						ArrayTools.print(ArrayTools.divide(node.distribution, parent.distribution));
						ArrayTools.print(acc);
						System.out.println(parent.toString());
						ArrayTools.print(parent.distribution);
						System.out.println(node.toString());
						ArrayTools.print(node.distribution);
						//try{ArrayTools.plot(acc, ArrayTools.normalize(node.distribution), rule.getRuleString());} catch (IOException e) {}
					} else {
						//System.out.println("Fodeo!!!!!\n" + node.toString() + "\n" + parent.toString());
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}	
			}

		}
	}
	
	public static void checkNodeForRules(CorrelationLatticeNode node) {
		for (CorrelationLatticeNode p: node.parents) {
			testRule(node,p);
		}
		for (CorrelationLatticeNode c: node.constants) {
			if (c.getSupport()>=supportThreshold)
				testRule(c,node);
		}
	}
	
	public static Collection<NumericalRule> searchRules(CorrelationLatticeNode root){
		Collection<CorrelationLatticeNode> set = new ArrayList<CorrelationLatticeNode>();
		Collection<CorrelationLatticeNode> next = new ArrayList<CorrelationLatticeNode>();
		
		set.add(root);
		while (!set.isEmpty()) {
			for (CorrelationLatticeNode node: set) {
				for (CorrelationLatticeNode child: node.children) {
					if (/*!child.isPruned()*/child.getSupport()>=supportThreshold)
						next.add(child);
				}
				if (/*!node.isPruned()*/node.getSupport()>=supportThreshold)
					checkNodeForRules(node);
				
				/*for (CorrelationLatticeNode c: node.constants) {
					float[] acc = ArrayTools.getAccuraciesWithMinSupport(c.distribution, node.distribution, supportThreshold);
					if (ArrayTools.max(acc) > confidenceThreshold) {
						System.out.println("WEEEEE!!!! NEW RULE TYPE: \n" + c.toString() + " ---- " + node.toString());
						ArrayTools.print(acc);
						ArrayTools.print(c.distribution);
					}
				}*/
			}

			set = next;
			next = new HashSet<CorrelationLatticeNode>();
		}
		return resultRules;
	}
	
	public static CorrelationLatticeNode joinNodes(CorrelationLatticeNode node1,CorrelationLatticeNode node2, QueryHandler qh) throws SQLException {
		CorrelationLatticeNode newNode = node1.clone();

		if (node1.level != node2.level)
			throw new IllegalArgumentException("Nodes should be of same level");
		if (node1.constants.contains(node2) || node2.constants.contains(node1))
			throw new IllegalArgumentException("One node is constant of the other");

		Literal diff1 = null;
		Literal diff2 = null;
		int node1ConstantOf2 = 0;
		int node2ConstantOf1 = 0;
		for (Literal l : node2.getLiterals()) {
			Literal sameRelation;
			if ((sameRelation = newNode.getLiteral(l.getRelation())) != null) {
				if (sameRelation.getSecondArgument()==-1 && l.getSecondArgument()==-1 && !l.getConstant().equals(sameRelation.getConstant()))
					throw new IllegalArgumentException("Nodes contain same relation but with distinct constants\n\t"+ node1 + "\n\t" + node2);
				else {
					if (sameRelation.getSecondArgument()==-1 && l.getSecondArgument()!=-1) 
						node1ConstantOf2++;		
					if (sameRelation.getSecondArgument()!=-1 && l.getSecondArgument()==-1) 
						node2ConstantOf1++;
					if (sameRelation.getSecondArgument()!=-1 && l.getSecondArgument()!=-1) {
						if ((newNode.literalMaxBoundary.containsKey(l) && node2.literalMinBoundary.containsKey(l)) ||
							(newNode.literalMinBoundary.containsKey(l) && node2.literalMaxBoundary.containsKey(l))) {
							throw new IllegalArgumentException("Nodes contain different numeric ranges\n\t"+ node1 + "\n\t" + node2);
						} else {
							if ((newNode.literalMaxBoundary.containsKey(l) && !node2.literalMaxBoundary.containsKey(l)) ||
								(newNode.literalMinBoundary.containsKey(l) && !node2.literalMinBoundary.containsKey(l))) {
								node1ConstantOf2++;
							}
							if ((!newNode.literalMaxBoundary.containsKey(l) && node2.literalMaxBoundary.containsKey(l)) ||
								(!newNode.literalMinBoundary.containsKey(l) && node2.literalMinBoundary.containsKey(l))) {
								node2ConstantOf1++;
							}
						}
					}
				}
			} else {
				newNode.addItem(l.getRelation(), l.getConstant());
				Literal newLit = newNode.getLiteral(l.getRelation());
				if (node2.literalMaxBoundary.containsKey(l)) 
					newNode.addNumericalLiteralMax(newLit, node2.literalMaxBoundary.get(l));
				if (node2.literalMinBoundary.containsKey(l)) 
					newNode.addNumericalLiteralMin(newLit, node2.literalMinBoundary.get(l));
				
				diff2 = l;
				if (newNode.level > (node1.level + 1))
					throw new IllegalArgumentException("Nodes cannot join, there's more than one diffent literal\n\t"+ node1 + "\n\t" + node2);
			}
		}
		if (newNode.level != (node1.level+1)) {
			// If one node is constant of the other, add to constant list
			if (newNode.level==node1.level && (node1ConstantOf2 > 0 || node2ConstantOf1 > 0)) {
				if (node1ConstantOf2 == 1 && node2ConstantOf1 == 0) {
					node2.addConstant(node1);
					System.out.println(node1.toString()+ "   is const of   " + node2.toString());
				}
				if (node2ConstantOf1 == 1 && node1ConstantOf2 == 0) {
					node1.addConstant(node2);
					System.out.println(node1.toString()+ "   is const of   " + node1.toString());
				}
			}
			throw new IllegalArgumentException("Nodes cannot join, they are equal\n\t" + node1 + "\n\t"+ node2);
		}
		
		for (Literal l : node1.getLiterals()) {
			if (node2.getLiteral(l.getRelation()) == null) 
				diff1 = l;
		}

		if (!existentItems.contains(newNode)) {
			newNode.addParent(node1);
			newNode.addParent(node2);
			newNode.queryNodeProperties(qh);
			newNode.analizeNode();
			existentItems.add(newNode);
			
			//if (!node.isPruned()) {
			if (newNode.getSupport()>=supportThreshold) {
				int[] indepHipotheses = independentJoinDitribution(node1, node2);
				float indepMeasure = ArrayTools.chiSquare(ArrayTools.laplaceSmooth(indepHipotheses), ArrayTools.laplaceSmooth(newNode.distribution));
				if (indepMeasure >= indepThreshold) {
					node1.addChild(newNode);
					node2.addChild(newNode);		
					node1.addHeadNewLiteral(diff1, diff2, newNode, node2);
					node2.addHeadNewLiteral(diff2, diff1, newNode, node1);
				}
			}
			
			return newNode;
		} else {
			CorrelationLatticeNode existent = existentItems.get(existentItems.indexOf(newNode));
			int[] indepHipotheses = independentJoinDitribution(node1, node2);
			float indepMeasure = ArrayTools.chiSquare(ArrayTools.laplaceSmooth(indepHipotheses), ArrayTools.laplaceSmooth(existent.distribution));
				
			if (indepMeasure >= indepThreshold && existent.support>=supportThreshold) {
				existent.addParent(node1);
				existent.addParent(node2);
				node1.addChild(existent);
				node2.addChild(existent);
				existent.analizeNode();
				node1.addHeadNewLiteral(diff1, diff2, existent, node2);
				node2.addHeadNewLiteral(diff2, diff1, existent, node1);
			} else {
				throw new IllegalArgumentException("New node is independent from parent nodes "+newNode);
			}
			throw new IllegalArgumentException("Join of nodes result in an already existant node "+newNode);
		}
	}
	
	public static CorrelationLatticeNode getCommonParent(CorrelationLatticeNode node1,CorrelationLatticeNode node2) {
		for (CorrelationLatticeNode p1: node1.parents) {
			if (node2.parents.contains(p1))
				return p1;
		}
		return null;
	}
	
	public static int[] independentJoinDitribution(CorrelationLatticeNode node1,CorrelationLatticeNode node2) {
		CorrelationLatticeNode commonParent = CorrelationLatticeNode.getCommonParent(node1, node2);
		if (commonParent==null)
			throw new IllegalArgumentException("Nodes cannot be joined. There's no common parent");
		
		float[] pNode1GivenParent = ArrayTools.divide(node1.distribution, commonParent.distribution);
		float[] pNode2GivenParent = ArrayTools.divide(node2.distribution, commonParent.distribution);
		
		return ArrayTools.round(ArrayTools.multiply(ArrayTools.multiply(pNode1GivenParent, pNode2GivenParent),commonParent.distribution));
				
	}
	
	public void persist(String path) {
      try  {
    	  FileOutputStream fileOut;
		  fileOut = new FileOutputStream(path);  	  
          SerializationUtils.serialize(this, fileOut);
      }
      catch(IOException i){
          i.printStackTrace();
      }
	}
	
	public static CorrelationLatticeNode readFromDisk(String path) {	 
        try{
        	FileInputStream fileIn;
        	fileIn =new FileInputStream(path);
        	CorrelationLatticeNode root = (CorrelationLatticeNode) SerializationUtils.deserialize(fileIn);
        	return root;
       }
       catch(IOException e){
           e.printStackTrace();
           return null;
       }
	}
	
	public void breadthFirst() {
		Collection<CorrelationLatticeNode> set = new ArrayList<CorrelationLatticeNode>();
		Collection<CorrelationLatticeNode> next = new ArrayList<CorrelationLatticeNode>();
		
		int i =0;
		set.add(this);
		while (!set.isEmpty()) {
			
			System.out.println("Level "+i+":");
			
			for (CorrelationLatticeNode node: set) {
				
				System.out.println(node);
				
				for (CorrelationLatticeNode child: node.children) {
					next.add(child);
				}
			}
			set = next;
			next = new HashSet<CorrelationLatticeNode>();
			i++;
		}
	}
	
	public int printSuggestions(Literal head) {
		Collection<CorrelationLatticeNode> set = new ArrayList<CorrelationLatticeNode>();
		Collection<CorrelationLatticeNode> next = new ArrayList<CorrelationLatticeNode>();
		
		int i =0;
		set.add(this);
		while (!set.isEmpty()) {
			for (CorrelationLatticeNode node: set) {
				
				for (CorrelationLatticeNode child: node.children) {
					next.add(child);
				}
				if (node.headNewLiteralMap.size()>0) {
					System.out.println(node);
				}
				for (Literal l: node.headNewLiteralMap.keySet()) {
					System.out.println("\t"+l.getSparqlPatternWithConstant());
					for (Float f: node.headNewLiteralMap.get(l).descendingKeySet()) {
						System.out.println("\t\t"+f+"\t:\t"+node.headNewLiteralMap.get(l).get(f).getSparqlPatternWithConstant());
					}
				}
			}
			set = next;
			next = new HashSet<CorrelationLatticeNode>();
		}
		return i;
	}
	
	public static void reset() {
		root = null;
		existentItems = new ArrayList<CorrelationLatticeNode>();
		candidates = null;
		rootRelation = null;
		rootLiteral = null;
		resultRules = new HashSet<NumericalRule>();
	}
	
	public String getMaxFilter(Literal l) {
		if (literalMaxBoundary.containsKey(l)) 
			return "!(?"+(char)l.getSecondArgument()+">\""+literalMaxBoundary.get(l)+"\")";
		else 
			return "";
	}
	
	public String getMinFilter(Literal l) {
		if (literalMinBoundary.containsKey(l)) 
			return "?"+(char)l.getSecondArgument()+">\""+literalMinBoundary.get(l)+"\"";
		else 
			return "";
	}
	
	public String getFilter(Literal l) {
		String filter = "";
		String max = getMaxFilter(l);
		String min = getMinFilter(l);
		if (min.length()>0) {
			if (max.length()>0) 
				filter += max + " && ";
			return filter += min;
	
		}
		return filter;
	}
	
	public String getFilter() {
		String filter = "";
		for (Literal l: literalMinBoundary.keySet()) 
			filter += "?"+(char)l.getSecondArgument()+">\""+literalMinBoundary.get(l)+"\" && ";
		
		for (Literal l: literalMaxBoundary.keySet()) 
			filter += "!(?"+(char)l.getSecondArgument()+">\""+literalMaxBoundary.get(l)+"\") && ";
		
		if (filter.length()==0)
			return filter;
		else {
			filter += ")";
			return filter.replace(" && )", "");
		}
	}
	
	/*public void queryNodeMultiGroupProperties(QueryHandler qh, CorrelationLatticeNode parent1, CorrelationLatticeNode parent2) throws SQLException {
	ResultSet rs = qh.retrieveMultiGroupDistribution(rootLiteral,relationLiteralMap.values());
	String lastGroup = "";
	CorrelationLatticeNode newConst = null;
	String[] lastConsts = new String[relationLiteralMap.size()];
	String[] consts = new String[relationLiteralMap.size()];
	Histogram hs = this.histogram.clone();
	hs.reset();
	for (String s: lastConsts) s="";
	while (rs.next()) {
		float x = rs.getFloat(1);
		int i=0;
		for (Literal l: relationLiteralMap.values()) {
			consts[i] = rs.getString(i+1).replaceAll("\"", "");
			i++;
		}
		int count = 1;
		try { 
			count = rs.getInt("count");
		} catch (SQLException e){}


		if (!ArrayUtils.isEquals(consts, lastConsts)) {
			if (hs.getSupport() >= supportThreshold) {
				newConst = this.clone();
				newConst.histogram = hs;
				i = 0;
				for (Literal l: relationLiteralMap.values()) {
					newConst.setConstant(l.getRelation(), lastConsts[i]);
					i++;
				}
				newConst.extractHistogramInformation();
				this.addConstant(newConst);			
				existentItems.add(newConst);
			}				
			hs = this.histogram.clone();
			hs.reset();
		}
		lastConsts = consts;

		this.histogram.addDataPoint(x, count);
		hs.addDataPoint(x, count);
	}
	if (hs.getSupport() >= supportThreshold) {
		newConst = this.clone();
		newConst.histogram = hs;
		int i = 0;
		for (Literal l: relationLiteralMap.values()) {
			newConst.setConstant(l.getRelation(), lastConsts[i]);
			i++;
		}
		newConst.extractHistogramInformation();
		this.addConstant(newConst);
		existentItems.add(newConst);
		
	}
	this.extractHistogramInformation();
	
	for (CorrelationLatticeNode n1: parent1.constants) {
		for (CorrelationLatticeNode n2: parent2.constants) {
			try {
				joinNodes(n1, n2, qh);
			} catch (IllegalArgumentException e) {}
		}
	}
	
	for (CorrelationLatticeNode n: this.constants) {
		if (!n.parents.isEmpty()) {
			n.analizeNode();
			if (n.isPruned())
				this.constants.remove(n);
		} else {
			this.constants.remove(n);
		}
	}
	this.analizeNode();

}*/
	
	/*public static Collection<String> searchDistRules(AssociationRuleNode node, boolean high, boolean low){ 
	for (AssociationRuleNode child: node.children) {
		for (AssociationRuleNode constant: child.getConstants()) {
			if (!constant.isPruned()) {
				int bucket = root.histogram.getBucket(root.histogram.getMean());
				int count = 0;
				for (int i=0; i<bucket; i++)
					count += constant.distribution[i];
				
				String rule = "";
				for (Relation r: constant.getRelations()) 
					rule +=  constant.getLiteral(r).getRuleLiteralString();
				
				double rootMean = root.histogram.getMean();
				double mean = node.histogram.getMean();
				double factor = 0.5;
				double minPercentage = 0.75;
				double diffLow = ((double)(count))/ ((double)constant.support) ;
				double diffHigh = ((double)(constant.support - count  - constant.distribution[bucket]))/ ((double)constant.support) ;
				
				
				
				if ((mean <= rootMean*factor && diffLow >= minPercentage) || (mean >= rootMean/factor && diffHigh >= minPercentage)) {
					
					if (mean <= rootMean/factor && diffLow >= minPercentage && low==false) {
						rule = "hasLowIncome(A) <- " + rule + " [acc="+diffLow+"]";
						low = true;
						high = false;
						resultRules.add(rule);
						try {ArrayTools.plot(ArrayTools.normalize(constant.distribution), ArrayTools.normalize(root.distribution), rule);} catch (IOException e) {}
					}						
					if (mean >= rootMean/factor && diffHigh >= minPercentage && high==false) {					
						rule = "hasHighIncome(A) <- " + rule + " [acc="+diffHigh+"]";
						high = true;
						low = false;			
						resultRules.add(rule);
						try {ArrayTools.plot(ArrayTools.normalize(constant.distribution), ArrayTools.normalize(root.distribution), rule);} catch (IOException e) {}
					}
				}		
				else {
					high = false;
					low = false;
				}
			}
		}
		searchDistRules(child,high,low);
	}
	return resultRules;
}*/

}
