package urdf.arm;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import rcaller.RCaller;

import urdf.ilp.HeadPredicate;
import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.ilp.Rule;
import urdf.rdf3x.ResultSet;

public class AssociationRuleNode implements Comparable<AssociationRuleNode>{

	private static AssociationRuleNode root;
	private static ArrayList<AssociationRuleNode> existentItems = new ArrayList<AssociationRuleNode>();
	private static HashSet<Relation> candidates;
	private static Relation rootRelation;
	private static Literal rootLiteral;
	private static final int numOfBuckets = 25;

	private float kldivRoot;
	private float chisqdivRoot;

	private Histogram histogram = null;

	private TreeSet<AssociationRuleNode> children;
	private TreeSet<AssociationRuleNode> parents;
	private TreeSet<AssociationRuleNode> constants;

	private HashMap<AssociationRuleNode, Float> kldivParents;
	private HashMap<AssociationRuleNode, Float> kldivChildren;
	private HashMap<AssociationRuleNode, Float> kldivConstants;
	private float maxKldivParents;
	private float maxKldivChildren;
	private float maxKldivConstants;

	private HashMap<Relation, Literal> relationLiteralMap;

	private boolean isGood;
	private boolean pruned;

	private int[] distribution;
	private float entropy;
	private int totalCount;
	private int support;
	private float mean;

	private int nextVariable;
	private int level;
	
	private static RCaller caller = new RCaller();
	
	private static Collection<String> resultRules = new HashSet<String>();

	// Thresholds
	private static final float kldivThreshold = (float) 0.15;
	private static final float chisqThreshold = (float) 0.3;
	private static final float indepThreshold = (float) 0.05;
	private static final int supportThreshold = 100;
	private static final int bucketSupportThreshold = 5;

	public AssociationRuleNode(Relation numericalProperty) {


		
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
		this.children = new TreeSet<AssociationRuleNode>();
		this.parents = new TreeSet<AssociationRuleNode>();
		this.constants = new TreeSet<AssociationRuleNode>();
		this.kldivChildren = new HashMap<AssociationRuleNode, Float>();
		this.kldivParents = new HashMap<AssociationRuleNode, Float>();
		this.kldivConstants = new HashMap<AssociationRuleNode, Float>();
		this.maxKldivChildren = Float.NEGATIVE_INFINITY;
		this.maxKldivParents = Float.NEGATIVE_INFINITY;
		this.maxKldivConstants = Float.NEGATIVE_INFINITY;

		if (rootRelation == null || !rootRelation.equals(numericalProperty))
			this.rootRelation = numericalProperty;
		if (rootLiteral == null || !rootLiteral.getRelation().equals(numericalProperty))
			this.rootLiteral = new Literal(rootRelation, nextVariable++, nextVariable++);
		if (root == null && this.level == 0) {
			root = this;
			existentItems = new ArrayList<AssociationRuleNode>();
			existentItems.add(this);
			resultRules = new HashSet<String>();
		}

	}

	public void addParent(AssociationRuleNode node) {
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
			throw new IllegalArgumentException("Parent node should already existent");
	}

	public void addChild(AssociationRuleNode node) {
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

	public void addConstant(AssociationRuleNode node) {
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

	public Collection<AssociationRuleNode> getConstants() {
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
		if (constant == null)
			addItem(item);
		if (!this.relationLiteralMap.containsKey(item)) {
			this.level++;
			Literal newLiteral = new Literal(item, rootLiteral.getFirstArgument(), 1, -1, 1, constant, true);
			this.relationLiteralMap.put(item, newLiteral);
			return;
		} else
			throw new IllegalArgumentException("Relation " + item.getName() + " could not be added in " + this.toString() + "\n" + this.getInfo());
	}

	public void setConstant(Relation item, String constant) {
		if (relationLiteralMap.containsKey(item)) {
			relationLiteralMap.put(item,new Literal(item, rootLiteral.getFirstArgument(), 1, -1, 1, constant, true));
		} else
			throw new IllegalArgumentException("Relation " + item.getName() + " is not contained in node");
	}

	public void queryNodeGroupProperties(QueryHandler qh) throws SQLException {
		if (level == 1 && this.getRelations().size() == 1) {
			Literal groupLiteral = (Literal) relationLiteralMap.values().toArray()[0];
			ResultSet rs = qh.retrieveGroupDistribution(rootLiteral,groupLiteral);
			if (level > 0 && histogram != null) {
				String lastGroup = "";
				AssociationRuleNode newConst = null;
				while (rs.next()) {
					String groupConst = rs.getString(1).replaceAll("\"", "");
					float x = rs.getFloat(2);
					int count = rs.getInt(3);

					if (!lastGroup.equals(groupConst)) {
						if (newConst != null) {
							newConst.extractHistogramInformation();
							newConst.analizeNode();
						}
						newConst = this.clone();
						newConst.setConstant(groupLiteral.getRelation(), groupConst);
						for (AssociationRuleNode parent : parents)
							newConst.addParent(parent);
						this.addConstant(newConst);
					}
					lastGroup = groupConst;

					this.histogram.addDataPoint(x, count);
					newConst.histogram.addDataPoint(x, count);
				}
				newConst.extractHistogramInformation();
				newConst.analizeNode();
				this.extractHistogramInformation();
				this.analizeNode();
			}
		}

	}

	public void queryNodeProperties(QueryHandler qh) throws SQLException {
		ResultSet rs = qh.retrieveDistribution(rootLiteral,relationLiteralMap.values());
		if (level == 0 && histogram == null) {
			rs.first();
			float min = rs.getFloat(1);
			rs.last();
			float max = rs.getFloat(1);
			//histogram = new Histogram(min, max, numOfBuckets);
			histogram = new VHistogram(rs, min, max, numOfBuckets);
		} 
		else {
			histogram.reset();
			rs.beforeFirst();
			while (rs.next()) {
				float x = rs.getFloat(1);
				int count = rs.getInt(2);
				histogram.addDataPoint(x, count);
			}
		}
		/*histogram.reset();
		rs.beforeFirst();
		while (rs.next()) {
			float x = rs.getFloat(1);
			int count = rs.getInt(2);
			histogram.addDataPoint(x, count);
		}*/
		
		extractHistogramInformation();
	}
	
	public void extractHistogramInformation() {
		this.distribution = histogram.getDistribution();
		this.mean = histogram.getMean();
		this.entropy = ArrayTools.entropy(this.distribution);
		this.kldivRoot = ArrayTools.klDivergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(root.distribution));
		this.chisqdivRoot = ArrayTools.chisqDivergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(root.distribution));
		this.support = ArrayTools.sum(this.distribution);
	}
	
	public void analizeNode() {
		for (AssociationRuleNode parent : parents) {
			float kldiv = ArrayTools.klDivergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(parent.distribution));
			kldivParents.put(parent, kldiv);
			if (kldiv > this.maxKldivParents)
				this.maxKldivParents = kldiv;
		}
		for (AssociationRuleNode constant : constants) {
			if (constant.distribution != null) {
				float kldiv = ArrayTools.klDivergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(constant.distribution));
				kldivConstants.put(constant, kldiv);
				if (kldiv > this.maxKldivConstants)
					this.maxKldivConstants = kldiv;
			}
		}
		for (AssociationRuleNode child : children) {
			if (child.distribution != null) {
				float kldiv = ArrayTools.klDivergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(child.distribution));
				kldivChildren.put(child, kldiv);
				if (kldiv > this.maxKldivChildren)
					this.maxKldivChildren = kldiv;
			}
		}

		if (this.maxKldivParents < kldivThreshold) {
			if (constants.isEmpty())
				this.pruned = true;
			else if (maxKldivConstants < kldivThreshold)
				this.pruned = true;
		}
		if (this.support < supportThreshold) {
			this.pruned = true;
			for (AssociationRuleNode c : this.constants)
				c.pruned = true;
		}
	}

	public boolean canJoin(Object o) {
		// To join, nodes should have all literals similar but one, which should
		// have different relation
		try {
			AssociationRuleNode node = (AssociationRuleNode) o;
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
			AssociationRuleNode node = (AssociationRuleNode) o;
			// Nodes are equal if they have same relations with same constants
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
			return true;

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
		return code;
	}
	
	@Override
	public AssociationRuleNode clone() {
		AssociationRuleNode newNode = new AssociationRuleNode(this.rootRelation);
		newNode.histogram = this.histogram.clone();
		newNode.histogram.reset();
		newNode.nextVariable = this.nextVariable;
		newNode.relationLiteralMap = new HashMap<Relation, Literal>();
		newNode.level = this.level;
		for (Relation key : this.relationLiteralMap.keySet()) {
			newNode.relationLiteralMap.put(key, this.relationLiteralMap
					.get(key).clone());
		}
		return newNode;
	}
	
	@Override
	public String toString() {
		String output = rootLiteral.getSparqlPatternWithConstant();
		for (Literal l : relationLiteralMap.values())
			output += l.getSparqlPatternWithConstant();
		return output;
	}

	@Override
	public int compareTo(AssociationRuleNode o) {
		// TODO Auto-generated method stub
		return Float.compare(this.kldivRoot, o.kldivRoot);
	}
	
	public static Collection<String> searchDistRules(AssociationRuleNode node, boolean high, boolean low) throws IOException { 
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
					double factor = 1.5;
					double minPercentage = 0.90;
					double diffLow = ((double)(constant.support - count))/ ((double)constant.support) ;
					double diffHigh = ((double)(constant.support - count  - constant.distribution[bucket]))/ ((double)constant.support) ;
					if ((mean <= rootMean/2 && diffHigh <= (1-minPercentage)) || (mean <= rootMean*2 && diffLow >= minPercentage)) {
						
						if (mean <= rootMean/factor && diffHigh <= (1-minPercentage) && low==false) {
							rule = "hasLowIncome(A) <- " + rule;
							low = true;
							high = false;
							resultRules.add(rule);
							ArrayTools.plot(ArrayTools.normalize(constant.distribution), ArrayTools.normalize(root.distribution), rule);
						}						
						if (mean >= rootMean*factor && diffLow >= minPercentage && high==false) {					
							rule = "hasHighIncome(A) <- " + rule;
							high = true;
							low = false;			
							resultRules.add(rule);
							ArrayTools.plot(ArrayTools.normalize(constant.distribution), ArrayTools.normalize(root.distribution), rule);
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
	}

	public static Collection<String> searchRules(AssociationRuleNode node) throws IOException {
		
		
		for (AssociationRuleNode child: node.children) {

			for (AssociationRuleNode constant: child.getConstants()) {
				if (!constant.isPruned()) {
					//System.out.println("\t\t"+constant+"\n\t\t\t"+constant.getInfo());
					for (AssociationRuleNode p: constant.parents) {
						if (!p.isPruned()) {
							
							float[] acc = ArrayTools.divide(constant.distribution, p.distribution);
							float kldiv = ArrayTools.klDivergence(ArrayTools.laplaceSmooth(constant.distribution), ArrayTools.laplaceSmooth(p.distribution));
							
							boolean isGood = false;
							if (kldiv > kldivThreshold) {
								for (int i=0; i<acc.length; i++) {
									if (acc[i]>0.5 && constant.distribution[i]>bucketSupportThreshold) {
										isGood = true;
										break;
									}
								}
							}
							//float maxacc = ArrayTools.max(acc);
							
							if (isGood)  {
							//if (maxacc > 0.1 && kldiv > kldivThreshold/4)  {
								String rule = "";
								Literal head;
								for (Relation r: constant.getRelations()) {
									if (p.getLiteral(r)==null) {
										rule = constant.getLiteral(r).getRuleLiteralString() + " <- " + rule;
										head = constant.getLiteral(r); 
									} else
										rule +=  constant.getLiteral(r).getRuleLiteralString();
								}
								resultRules.add(rule);
								System.out.println(rule);
								System.out.println(constant.getInfo());
								ArrayTools.print(acc);
								ArrayTools.print(constant.distribution);
								/*if (constant.parents.size()>=2) {
									for (AssociationRuleNode p2: constant.parents) {
										if (!p.equals(p2)) {
											try {
												int[] prediction = AssociationRuleNode.independentJoinDitribution(p, p2); 
												ArrayTools.print(prediction);
												float kldivIndep = ArrayTools.klDivergence(ArrayTools.laplaceSmooth(constant.distribution), ArrayTools.laplaceSmooth(prediction));
												float cvIndep = ArrayTools.chisqDivergence(ArrayTools.laplaceSmooth(constant.distribution), ArrayTools.laplaceSmooth(prediction));
												float indepMeasure = ArrayTools.indepMeasure(ArrayTools.laplaceSmooth(constant.distribution), ArrayTools.laplaceSmooth(prediction));
												System.out.println("kldiv="+kldivIndep+" chisq="+cvIndep+" indep="+indepMeasure);
											} catch (IllegalArgumentException e) {}
										}
									}
								}*/
								
								ArrayTools.plot(acc, ArrayTools.normalize(constant.distribution), rule);
								
							}
						}
					}
				}
			}
			searchRules(child);
		}

		return resultRules;
	}
	
	public static AssociationRuleNode joinNodes(AssociationRuleNode node1,AssociationRuleNode node2, QueryHandler qh) throws SQLException {
		AssociationRuleNode newNode = node1.clone();

		if (node1.level != node2.level)
			throw new IllegalArgumentException("Nodes should be of same level");

		for (Literal l : node2.getLiterals()) {
			Literal sameRelation;
			if ((sameRelation = newNode.getLiteral(l.getRelation())) != null) {
				if (sameRelation.getSecondArgument()==-1 && l.getSecondArgument()==-1 && !l.getConstant().equals(sameRelation.getConstant()))
					throw new IllegalArgumentException("Nodes contain same relation but with distinct constants\n\t"+ node1 + "\n\t" + node2);
			} else {
				try {
					newNode.addItem(l.getRelation(), l.getConstant());
					if (newNode.level > (node1.level + 1))
						throw new IllegalArgumentException("Nodes cannot join, there's more than one diffent literal\n\t"+ node1 + "\n\t" + node2);
				} catch (IllegalArgumentException e) {
				}
			}
		}

		if (newNode.level == node1.level)
			throw new IllegalArgumentException("Nodes cannot join, they are equal\n\t" + node1 + "\n\t"+ node2);

		
		if (!existentItems.contains(newNode)) {
			//existentItems.add(newNode);
			return newNode;
			/*newNode.queryNodeProperties(qh);
			indepMeasure = ArrayTools.indepMeasure(ArrayTools.laplaceSmooth(indepHipotheses), ArrayTools.laplaceSmooth(newNode.distribution));
			if (indepMeasure < indepThreshold) 
				throw new IllegalArgumentException("New node is independent from parent nodes "+newNode);
			else {
				return newNode;
			}*/
		} else {
			AssociationRuleNode existent = existentItems.get(existentItems.indexOf(newNode));
			int[] indepHipotheses = independentJoinDitribution(node1, node2);
			float indepMeasure = ArrayTools.indepMeasure(ArrayTools.laplaceSmooth(indepHipotheses), ArrayTools.laplaceSmooth(existent.distribution));
			if (indepMeasure < indepThreshold) 
				throw new IllegalArgumentException("New node is independent from parent nodes "+newNode);
			
			existent.addParent(node1);
			existent.addParent(node2);
			//node1.addChild(existent);
			//node2.addChild(existent);
			existent.analizeNode();
			throw new IllegalArgumentException("Join of nodes result in an already existant node "+newNode);
		}
	}
	
	public static AssociationRuleNode getCommonParent(AssociationRuleNode node1,AssociationRuleNode node2) {
		for (AssociationRuleNode p1: node1.parents) {
			if (node2.parents.contains(p1))
				return p1;
		}
		return null;
	}
	
	public static int[] independentJoinDitribution(AssociationRuleNode node1,AssociationRuleNode node2) {
		AssociationRuleNode commonParent = AssociationRuleNode.getCommonParent(node1, node2);
		if (commonParent==null)
			throw new IllegalArgumentException("Nodes cannot be joined. There's no common parent");
		
		float[] pNode1GivenParent = ArrayTools.divide(node1.distribution, commonParent.distribution);
		float[] pNode2GivenParent = ArrayTools.divide(node2.distribution, commonParent.distribution);
		
		return ArrayTools.round(ArrayTools.multiply(ArrayTools.multiply(pNode1GivenParent, pNode2GivenParent),commonParent.distribution));
				
	}

}
