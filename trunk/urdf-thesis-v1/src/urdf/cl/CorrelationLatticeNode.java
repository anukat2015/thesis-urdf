package urdf.cl;

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
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.bcel.generic.CPInstruction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import rcaller.RCaller;
import urdf.ilp.LearningManager;
import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.ilp.RelationsInfo;
import urdf.ilp.Rule;
import urdf.rdf3x.ResultSet;

public class CorrelationLatticeNode implements Comparable<CorrelationLatticeNode>, Serializable{

	private static final long serialVersionUID = 4782549761623555466L;
	
	private static long idCount = 0;
	private final long id;
	
	private static Logger logger = Logger.getLogger(LearningManager.loggerName);
	
	private CorrelationLattice lattice;
	//private long root;
	private CorrelationLatticeNode root;
	private Relation rootRelation;
	private Literal rootLiteral;
	
	public static boolean histogramBySupport = true;
	public static boolean nonNegativeAttribute = true;
	public static boolean allowNumericalBipartition = false;

	private float kldivRoot;

	//private TreeSet<Long> children;
	//private TreeSet<Long> parents;
	//private TreeSet<Long> constants;
	private TreeSet<CorrelationLatticeNode> children;
	private TreeSet<CorrelationLatticeNode> parents;
	private TreeSet<CorrelationLatticeNode> constants;

	
	//private TreeMap<Float,Long> sortedChildren;
	private TreeMap<Float,CorrelationLatticeNode> sortedChildren;
	//private TreeMap<Float,Long> sortedConstants;
	private TreeMap<Float,CorrelationLatticeNode> sortedConstants;
	
	//private HashMap<String,Long> literalChildMap;
	private HashMap<String,CorrelationLatticeNode> literalChildMap;
	private HashMap<String, TreeMap<Float,Literal>> headSuggestionsMap;
	
	//private TreeMap<Float,Long> kldivParents;
	//private TreeMap<Float,CorrelationLatticeNode> interestingnessParents;
	private TreeMap<Float,Literal> suggestions;

	public float maxInterestingnessParents;
	private float maxInterestingnessChildren;


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

	// Thresholds
	private float divThreshold = (float) 0.0;
	private float indepThreshold = (float) 0;
	private int supportThreshold = 25;
	public int bucketSupportThreshold = 4;
	public float confidenceThreshold = (float) 0.7;
	
	private int numOfBuckets = 25;

	public CorrelationLatticeNode(CorrelationLattice lattice, Relation numericalProperty) {
		this.id = idCount++;
		this.lattice = lattice;
		this.nextVariable = (int) 'A';
		this.isGood = false;
		this.pruned = false;
		this.support = 0;
		this.totalCount = 0;
		this.mean = Float.NaN;
		this.kldivRoot = Float.NaN;
		this.entropy = Float.NaN;
		//this.relationLiteralMap = new HashMap<Relation, Literal>();
		//this.literalChildMap = new HashMap<String, Long>();
		//this.children = new TreeSet<Long>();
		//this.parents = new TreeSet<Long>();
		//this.constants = new TreeSet<Long>();
		//this.suggestions = new TreeMap<Float, Literal>();
		//this.kldivParents = new TreeMap<Float, Long>();
		//this.sortedChildren = new TreeMap<Float, Long>();
		//this.sortedConstants = new TreeMap<Float, Long>();
		this.relationLiteralMap = new HashMap<Relation, Literal>();
		this.literalChildMap = new HashMap<String, CorrelationLatticeNode>();
		this.children = new TreeSet<CorrelationLatticeNode>();
		this.parents = new TreeSet<CorrelationLatticeNode>();
		this.constants = new TreeSet<CorrelationLatticeNode>();
		this.suggestions = new TreeMap<Float, Literal>();
		//this.interestingnessParents = new TreeMap<Float, CorrelationLatticeNode>();
		this.sortedChildren = new TreeMap<Float, CorrelationLatticeNode>();
		this.sortedConstants = new TreeMap<Float, CorrelationLatticeNode>();
		this.headSuggestionsMap = new HashMap<String, TreeMap<Float,Literal>>();
		this.maxInterestingnessChildren = Float.NEGATIVE_INFINITY;
		this.maxInterestingnessParents = Float.NEGATIVE_INFINITY;
		
		if (lattice.getRoot()!=null) {
			//this.root = lattice.getRoot().getId();
			this.root = root;
			this.rootRelation = lattice.getRootRelation();
			this.rootLiteral = lattice.getRootLiteral();	
		}else {
			//this.root = id;
			this.root = this;
			this.rootRelation = numericalProperty;
			this.rootLiteral = new Literal(rootRelation, nextVariable++, nextVariable++);
			//this.relationLiteralMap.put(rootRelation, rootLiteral);
			this.level = 0;
		}
		
		

		
		//lattice.addNodeToExistent(this);

	}
	
	//public void addParent(long node) {
	//	addParent(lattice.getExistentNode(node));
	//}
	

	public void addParent(CorrelationLatticeNode node) {
		if (lattice.nodeExistis(node)) {
			if (node.getRelations().size() == (this.getRelations().size() - 1) && node.level == (this.level - 1)) {
				if (this.getRelations().containsAll(node.getRelations())) {
					//this.parents.add(node.getId());
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
	
	//public void addChild(long node) {
	//	addChild(lattice.getExistentNode(node));
	//}

	public void addChild(CorrelationLatticeNode node) {
		if (node.getRelations().size() == (this.getRelations().size() + 1) && node.level == (this.level + 1)) {
			if (node.getRelations().containsAll(this.getRelations()) && node.getSupport()>=supportThreshold) {
				//this.children.add(node.getId());
				this.children.add(node);
				lattice.addNodeToExistent(node);
				Literal difference = node.getDifferenceLiteralFromParent(this);
				String key = (difference.toString()).replaceAll("\\?([A-Z])", "\\?");
				this.addSuggestion(node, difference);
				//this.literalChildMap.put(key, node.getId());
				this.literalChildMap.put(key, node);
				return;
			} else
				throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");

		} else
			throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.getRelations().size() + " and should be "+ this.getRelations().size());
	}
	
	//public void addConstant(long node) {
	//	addConstant(lattice.getExistentNode(node));
	//}

	public void addConstant(CorrelationLatticeNode node) {
		if (node.getRelations().size() == this.getRelations().size() && node.level == this.level) {
			if (node.getRelations().containsAll(this.getRelations())) {
				//this.constants.add(node.getId()); 
				this.constants.add(node);
				lattice.addNodeToExistent(node);
				return;
			} else
				throw new IllegalArgumentException("Child itemset is not valid, it should be equals to this set plus an extra relation");

		} else
			throw new IllegalArgumentException("Child itemset size doesn't match! It's " + node.getRelations().size() + " and should be " + this.getRelations().size() + "\t Level is " + node.level + " and should be " + this.level);

	}
	
	//public long getId() {
	//	return id;
	//}

	public boolean isPruned() {
		return pruned;
	}

	public int[] getDistribution() {
		return this.distribution;
	}

	public int getLevel() {
		return level;
	}
	
	public float[] getNormalizedDistribution() {
		return ArrayTools.normalize(distribution);
	}

	
	public int getSupport() {
		return support;
	}
	
	public CorrelationLatticeNode getChild(Literal l) {
		String key = (l.toString()).replaceAll("\\?([A-Z])", "\\?");
		//Long id = literalChildMap.get(key);
		//if (id==null) 
		//	return null;
		//else
		//	return lattice.getExistentNode(id);
		//System.out.println("getChild("+key+")");
		return literalChildMap.get(key);
	}
	
	public Literal getRootLiteral() {
		return rootLiteral;
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

	//public Collection<Long> getConstants() {
	public Collection<CorrelationLatticeNode> getConstants() {
		return this.constants;
	}
	
	//public Collection<Long> getParents() {
	public Collection<CorrelationLatticeNode> getParents() {
		return this.parents;
	}
	
	//public Collection<Long> getChildren() {
	public Collection<CorrelationLatticeNode> getChildren() {
		return this.children;
	}
	
	public Collection<String> getChildrenKeys() {
		return this.literalChildMap.keySet();
	}
	
	public int getNumberOfBuckets() {
		return numOfBuckets;
	}
	public void setNumberOfBuckets(int numOfBuckets) {
		this.numOfBuckets = numOfBuckets;
	}
	
	public void addLiteral(Relation r) {
		if (!relationLiteralMap.containsKey(r)) {
			level++;
			Literal newLiteral = new Literal(r,rootLiteral.getFirstArgument(), nextVariable++);
			this.relationLiteralMap.put(r, newLiteral);
			return;
		} else
			throw new IllegalArgumentException("Relation " + r.getName() + " could not be added in " + this.toString() + "\n" + this.getInfo());
	}
	
	public void addLiteral(Literal l) {
		if (!relationLiteralMap.containsKey(l.getRelation())) {
			level++;
			Literal newLiteral;
			if (l.getSecondArgument()==-1) 
				newLiteral = new Literal(l.getRelation(), rootLiteral.getFirstArgument(), 1, -1, 1, l.getConstant(), true);
			else 
				newLiteral = new Literal(l.getRelation(),rootLiteral.getFirstArgument(), nextVariable++);
			
			newLiteral.setMinMax(l.getMin(), l.getMax());
			this.relationLiteralMap.put(l.getRelation(), newLiteral);
			return;
		} else
			throw new IllegalArgumentException("Relation " + l.getRelationName() + " could not be added in " + this.toString() + "\n" + this.getInfo());
	}

	public void setConstant(Relation item, String constant) {
		if (relationLiteralMap.containsKey(item)) {
			relationLiteralMap.put(item,new Literal(item, rootLiteral.getFirstArgument(), 1, -1, 1, constant, true));
		} else
			throw new IllegalArgumentException("Relation " + item.getName() + " is not contained in node");
	}
	
	private void extractNodeAndConstants(ResultSet rs, Literal groupLiteral, float mean) throws SQLException {
		logger.log(Level.DEBUG,"Extracting "+groupLiteral.getRelationName()+" with mean="+mean);
		
		Histogram histogram = lattice.copyHistogram();

		CorrelationLatticeNode greaterConst = this.clone();
		CorrelationLatticeNode smallerConst = this.clone();
		greaterConst.relationLiteralMap.get(groupLiteral.getRelation()).setMin(mean);
		smallerConst.relationLiteralMap.get(groupLiteral.getRelation()).setMax(mean);
		Histogram greaterConsthistogram = lattice.copyHistogram();
		Histogram smallerConsthistogram = lattice.copyHistogram();
		
		while (rs.next()) {
			float numConst = rs.getFloat("C");
			float x = rs.getFloat("B");
			if (nonNegativeAttribute) x = Math.abs(x);
			
			int count = 1;
			try { 
				count = rs.getInt("count");
			} catch (SQLException e){}
			
			histogram.addDataPoint(x, count);
			if (numConst>mean)
				greaterConsthistogram.addDataPoint(x, count);
			else
				smallerConsthistogram.addDataPoint(x, count);
		}
		
		
		
		this.extractHistogramInformation(histogram);
		this.analizeNode();
		greaterConst.extractHistogramInformation(greaterConsthistogram);
		if (greaterConst.getSupport()>=supportThreshold) {
			//this.constants.add(smallerConst.getId());
			this.constants.add(greaterConst);
			greaterConst.addParent(root);
			greaterConst.analizeNode();			
			lattice.addNodeToExistent(greaterConst);
		}
		smallerConst.extractHistogramInformation(smallerConsthistogram);
		if (smallerConst.getSupport()>=supportThreshold) {
			this.constants.add(smallerConst);
			//this.constants.add(greaterConst.getId());			
			smallerConst.addParent(root);		
			smallerConst.analizeNode();
			lattice.addNodeToExistent(smallerConst);
		}

		
		//for (long id: constants) {
		//	CorrelationLatticeNode constNode = lattice.getExistentNode(id);
		for (CorrelationLatticeNode constNode: constants) {
			float div = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(constNode.distribution));
			//sortedConstants.put(div, constNode.getId());
			sortedConstants.put(div, constNode);
		}
		
	}
	
	private void extractNodeAndConstants(ResultSet rs, Literal groupLiteral) throws SQLException {
		Histogram histogram = lattice.copyHistogram();
		
		if (level > 0 && histogram != null) {
			String lastGroup = "";
			HashSet<String> entitiesSet = new HashSet<String>();
			CorrelationLatticeNode newConst = this.clone();
			Histogram newConsthistogram = lattice.copyHistogram();
			while (rs.next()) {
				String groupConst = rs.getString("C").replaceAll("\"", "");
				float x = rs.getFloat("B");
				if (nonNegativeAttribute) x = Math.abs(x);
				
				int count = 1;
				try { 
					count = rs.getInt("count");
				} catch (SQLException e){}
				
				String entity = null;
				try { 
					entity = rs.getString("A");
				} catch (SQLException e){}

				if (!lastGroup.equals(groupConst)) {
					newConst.extractHistogramInformation(newConsthistogram);
					if (newConst.support>=lattice.getSupportThreshold()) {
						newConst.setConstant(groupLiteral.getRelation(), lastGroup);
						this.addConstant(newConst);
						newConst.addParent(root);
						newConst.analizeNode();
						lattice.addNodeToExistent(newConst);
						newConst = this.clone();
					}
					newConsthistogram.reset();
				}
				
				if (entity==null || !entitiesSet.contains(entity)) 
					histogram.addDataPoint(x, count);				
				newConsthistogram.addDataPoint(x, count);
				
				if (entity!=null)
					entitiesSet.add(entity);
				lastGroup = groupConst;
			}
			newConst.extractHistogramInformation(newConsthistogram);
			if (newConst.support>=lattice.getSupportThreshold()) {
				newConst.setConstant(groupLiteral.getRelation(), lastGroup);
				this.addConstant(newConst);
				newConst.addParent(root);
				newConst.analizeNode();
				lattice.addNodeToExistent(newConst);
			} //else logger.log(Level.DEBUG,"FailedSupportTest[="+newConst.support+"]:"+lastGroup);
			this.extractHistogramInformation(histogram);
			this.analizeNode();
			
			//for (long id: constants) {
			//	CorrelationLatticeNode constNode = lattice.getExistentNode(id);
			for (CorrelationLatticeNode constNode: constants) {
				float div = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(constNode.distribution));
				//sortedConstants.put(div, constNode.getId());
				sortedConstants.put(div, constNode);
			}
		}
	}


	public void queryNodeGroupProperties(QueryHandler qh) throws SQLException {
		logger.log(Level.DEBUG, "Querying Node Group Properties: "+this);
		
		Histogram histogram = lattice.copyHistogram();
		
		if (level == 1 && this.getRelations().size() == 1) {
			logger.log(Level.DEBUG, "Now actually Querying Node Group Properties: "+this);
			Literal groupLiteral = (Literal) relationLiteralMap.values().toArray()[0];
			Relation groupRelation = groupLiteral.getRelation();
			
			ResultSet rs = null;
			if (groupRelation.isFunction() || groupRelation.isDangerous())
				rs = qh.retrieveCategoryDistribution(rootLiteral,groupLiteral);
			else
				rs = qh.retrieveNonFuctionGroupDistribution(rootLiteral, groupLiteral);
			
			float mean = QueryHandler.getMean(rs);
			if (allowNumericalBipartition && !Float.isNaN(mean)) {
				extractNodeAndConstants(rs, groupLiteral, mean);
			} else {
				extractNodeAndConstants(rs, groupLiteral);
			}
			
			CorrelationLatticeNode rootNode = lattice.getRoot();
			//if (!groupRelation.isDangerous()) 
			//	rootNode.addChild(this);

			//for (long n: constants) {
			for (CorrelationLatticeNode n: constants) {
				//rootNode.addChild(lattice.getExistentNode(n));
				rootNode.addChild(n);
			}
			
			lattice.addNodeToExistent(this);
		}
	}

	public void queryNodeProperties(QueryHandler qh) throws SQLException {

		ResultSet rs = qh.retrieveDistribution(rootLiteral,relationLiteralMap.values(),this.getFilter());

		Histogram histogram = lattice.copyHistogram();
		
		if (histogramBySupport) {
			if (level == 0 && histogram == null) { 
				histogram = new HistogramEqualFrequencies(rs, numOfBuckets);
				lattice.setHistogram(histogram);
			}
			else {
				histogram.reset();
				rs.beforeFirst();
				while (rs.next()) {
					float x = rs.getFloat("B");
					if (nonNegativeAttribute) x = Math.abs(x);
					
					int count = 1;
					try { 
						count = rs.getInt("count");
					} catch (SQLException e){}
					histogram.addDataPoint(x, count);
				}
			}
		} 
		else {
			if (level == 0 && histogram == null) {
				float min = Float.POSITIVE_INFINITY;
				float max = Float.NEGATIVE_INFINITY;
				rs.beforeFirst();
				while (rs.next()) {
					float n = rs.getFloat("B");
					if (nonNegativeAttribute) n = Math.abs(n);
					
					if (n<min) min = n;
					if (n>max) max = n;
				}
				min = (float) 0;
				max = (float) 7550;
				histogram = new HistogramEqualWidth(min, max, numOfBuckets);
				histogram.setMax(Float.POSITIVE_INFINITY);
				//histogram.setMin(Float.NEGATIVE_INFINITY);
				lattice.setHistogram(histogram);
			}			
			histogram.reset();
			rs.beforeFirst();
			while (rs.next()) {
				float x = rs.getFloat("B");
				if (nonNegativeAttribute) x = Math.abs(x);
				
				int count = 1;
				try { 
					count = rs.getInt("count");
				} catch (SQLException e){}
				
				histogram.addDataPoint(x, count);
			}
		}
		extractHistogramInformation(histogram);		

		logger.log(Level.DEBUG, ArrayTools.toString(distribution));
		logger.log(Level.DEBUG, ArrayTools.toString(histogram.getBoundaries()));
	}
	
	public void extractHistogramInformation(Histogram histogram) {
		this.distribution = histogram.getDistribution();
		this.mean = histogram.getMean();
		this.entropy = ArrayTools.entropy(this.distribution);
		this.kldivRoot = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(lattice.getRoot().distribution));
		this.support = ArrayTools.sum(this.distribution);
	}
	
	public void analizeNode() {
		// Maximum KLDIV
		//for (long l : parents) {
		//	CorrelationLatticeNode parent = lattice.getExistentNode(l);
		for (CorrelationLatticeNode parent: parents) {
			float kldiv = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(parent.distribution));
			//kldivParents.put(kldiv,parent.getId());
			//interestingnessParents.put(kldiv,parent);
			if (kldiv > this.maxInterestingnessParents)
				this.maxInterestingnessParents = kldiv;
		}
		
		//for (long l : children) {
		//	CorrelationLatticeNode child = lattice.getExistentNode(l);
		for (CorrelationLatticeNode child: children) {
			if (child.distribution != null) {
				float interest = ArrayTools.divergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(child.distribution));
				//sortedChildren.put(kldiv, child.getId());
				sortedChildren.put(interest, child);
				//sortedChildren.put(ArrayTools.kullbackLeiblerDivergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(child.distribution)), child.getId());
				sortedChildren.put(ArrayTools.kullbackLeiblerDivergence(ArrayTools.laplaceSmooth(this.distribution),ArrayTools.laplaceSmooth(child.distribution)), child);
				if (interest > this.maxInterestingnessChildren)
					this.maxInterestingnessChildren = interest;
			}
		}

		if (/*this.maxKldivParents < kldivThreshold || */this.support < lattice.getSupportThreshold()) {
			this.pruned = true;
		}
		//for (long l : parents) {
		//	CorrelationLatticeNode parent = lattice.getExistentNode(l);
		for (CorrelationLatticeNode parent: parents) {
			float[] acc = ArrayTools.getAccuraciesWithMinSupport(this.getDistribution(), parent.getDistribution(), supportThreshold);
			if (ArrayTools.max(acc) >= 0.75 && ArrayTools.max(acc)<=1){
				//System.out.println(this + "\t<==\t" + parent);
				//ArrayTools.print(acc);
				//ArrayTools.print(ArrayTools.divide(this.getDistribution(), parent.getDistribution()));
				//System.out.println(ArrayTools.entropy(acc));
			}
		}
		
	}

	public String getInfo() {
		return "Level=" + this.level + " Pruned=" + this.pruned + " #Parents="+ this.parents.size() +" Entropy="+ this.entropy + " Support=" + this.support + " KLdivRoot="+ this.kldivRoot + " MaxKLdivParents=" + this.maxInterestingnessParents;
	}
	
	private Literal getDifferenceLiteral(CorrelationLatticeNode node) {
		if (node.getLevel()<(level-1))
			throw new IllegalArgumentException("Nodes should be in the same level");
		
		Literal diff = null;
		int diffLiterals = 0;
		for (Relation r: this.getRelations()) {
			if (node.getRelations().contains(r)) {
				if (lattice.equalLiterals(this.getLiteral(r), node.getLiteral(r))) 
					continue;
			}		
			diffLiterals++;
			diff = this.getLiteral(r);
		}
		if (diff==null) {
			throw new IllegalArgumentException("All literals from "+this+" are contained in "+node);
		}
		else {
			if (diffLiterals==1)
				return diff;
			else 
				throw new IllegalArgumentException("More than one different literal");
		}				
		
	}
	
	public Literal getDifferenceLiteralFromChild(CorrelationLatticeNode node) {
		return node.getDifferenceLiteralFromParent(this);
	}
	
	public Literal getDifferenceLiteralFromParent(CorrelationLatticeNode node) {
		if ((node.getLevel()+1)!=level)
			throw new IllegalArgumentException("Nodes should be in the same level");
		
		return getDifferenceLiteral(node);
	}

	public Literal getDifferenceLiteralFromJoiningNodes(CorrelationLatticeNode node) {
		if (node.getLevel()!=level)
			throw new IllegalArgumentException("Nodes should be in the same level");
		
		return getDifferenceLiteral(node);
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
					if (!lattice.equalLiterals(lit1, lit2))
						return false;
				}
				return true;
			}
		} catch (ClassCastException e) {
		}
		return false;
	}

	@Override
	public int hashCode() {
		int code = 0;
		if (relationLiteralMap!=null)
			for (Literal l : relationLiteralMap.values()) {
				code += l.getRelationName().hashCode();
				if (l.getSecondArgument()==-1 && l.getConstant() != null)
					code += l.getConstant().hashCode();
				if (l.getMax()!=Float.POSITIVE_INFINITY)
					code += (int) l.getMax();
				if (l.getMin()!=Float.NEGATIVE_INFINITY)
					code -= (int) l.getMin();
			}
		return code;
	}
	
	@Override
	public CorrelationLatticeNode clone() {
		CorrelationLatticeNode newNode = new CorrelationLatticeNode(this.lattice,this.rootRelation);
		//newNode.histogram = this.histogram.clone();
		//newNode.histogram.reset();
		newNode.nextVariable = this.nextVariable;
		newNode.level = this.level;
		newNode.root = this.root;
		newNode.rootLiteral = this.rootLiteral;
		newNode.rootRelation = this.rootRelation;
		
		newNode.relationLiteralMap = new HashMap<Relation, Literal>();
		for (Relation key : this.relationLiteralMap.keySet()) 
			newNode.relationLiteralMap.put(key, this.relationLiteralMap.get(key).clone());
		
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
			return -Float.compare(this.maxInterestingnessParents, o.maxInterestingnessParents);
	}
	
	public void addSuggestion(CorrelationLatticeNode node, Literal newLiteral) {
		if (ArrayTools.sum(node.getDistribution())>=supportThreshold) {
			float div = ArrayTools.divergence(ArrayTools.laplaceSmooth(node.getDistribution()),ArrayTools.laplaceSmooth(this.getDistribution()));
			suggestions.put(div, newLiteral);
		}
	}
	
	public void addHeadNewLiteral(Literal head, Literal newLiteral, CorrelationLatticeNode joinNode, CorrelationLatticeNode bodyNode) {
		float div = ArrayTools.divergence(ArrayTools.laplaceSmooth(joinNode.distribution), ArrayTools.laplaceSmooth(bodyNode.distribution));
		
		String key = (head.toString()).replaceAll("\\?([A-Z])", "\\?");
		
		TreeMap<Float,Literal> map = null;
		if (!headSuggestionsMap.containsKey(head)) {
			map = new TreeMap<Float, Literal>();
			headSuggestionsMap.put(key, map);
		} else {
			map = headSuggestionsMap.get(head);
		}
		
		map.put(div, newLiteral);
	}
	

	
	public void breadthFirst() {
		Collection<CorrelationLatticeNode> set = new ArrayList<CorrelationLatticeNode>();
		Collection<CorrelationLatticeNode> next = new ArrayList<CorrelationLatticeNode>();
		
		int i =0;
		int nodes = 0;
		int nodesAtLevel = 0;
		set.add(this);
		while (!set.isEmpty()) {
			System.out.println("Level "+i+":");
			for (CorrelationLatticeNode node: set) {
				System.out.println(node);
				//ArrayTools.print(node.getDistribution());
				nodesAtLevel++;
				nodes++;
				//for (long child: node.children) 
				//	next.add(lattice.getExistentNode(child));
				for (CorrelationLatticeNode child: node.getChildren()) {
					next.add(child);
				}
			}
			set = next;
			next = new HashSet<CorrelationLatticeNode>();
			i++;
			System.out.println("$$$$$ "+i+")"+nodesAtLevel+" $$$$$");
			nodesAtLevel = 0;
		}
		System.out.println("$$$$$ "+nodes+" $$$$$");
	}
	
	public TreeMap<Float,Literal> getSuggestions(Literal head) {
		String key = (head.toString()).replaceAll("\\?([A-Z])", "\\?");
		return getSuggestions(key);
	}
	
	public TreeMap<Float,Literal> getSuggestions(String head) {
		TreeMap<Float,Literal> suggestionsHead = headSuggestionsMap.get(head);
		if (suggestionsHead!=null && !suggestionsHead.isEmpty())
			return suggestionsHead;
		else {
			return suggestions;
		}
	}
	
	public CorrelationLatticeNode search(Literal literal) {
		CorrelationLatticeNode node = getChild(literal);
		return (node==null)?this:node;
	}
	
	public int breadthFirstWithSuggestions(Literal head) {
		Collection<CorrelationLatticeNode> set = new ArrayList<CorrelationLatticeNode>();
		Collection<CorrelationLatticeNode> next = new ArrayList<CorrelationLatticeNode>();
		
		int i =0;
		set.add(this);
		while (!set.isEmpty()) {
			for (CorrelationLatticeNode node: set) {
				
				//for (long child: node.children) 
				//	next.add(lattice.getExistentNode(child));
				for (CorrelationLatticeNode child: node.children) 
					next.add(child);
				
				if (node.headSuggestionsMap.size()>0) {
					logger.log(Level.DEBUG,node);
				}
				for (String l: node.headSuggestionsMap.keySet()) {
					logger.log(Level.DEBUG,"\t"+l.toString());
					for (Float f: node.headSuggestionsMap.get(l).descendingKeySet()) {
						logger.log(Level.DEBUG,"\t\t"+f+"\t:\t"+node.headSuggestionsMap.get(l).get(f).toString());
					}
				}
			}
			set = next;
			next = new HashSet<CorrelationLatticeNode>();
		}
		return i;
	}
	
	public static void reset() {
	}
	
	public String getFilter() {
		String filter = "";
		String literalFilter = "";
		for (Literal l: relationLiteralMap.values()) {
			if ((literalFilter=l.getFilter()).length()>0)
				filter += literalFilter + " && ";
		}
		
		if (filter.length()==0)
			return filter;
		else {
			filter += ")";
			return filter.replace(" && )", "");
		}
	}

	public float getDivThreshold() {
		return divThreshold;
	}

	public void setDivThreshold(float divThreshold) {
		this.divThreshold = divThreshold;
	}

	public float getIndepThreshold() {
		return indepThreshold;
	}

	public void setIndepThreshold(float indepThreshold) {
		this.indepThreshold = indepThreshold;
	}

	/*public void changeInterestingnessMeasuresTo(int measure) {
		ArrayTools.measure = measure;
		for (Entry<Float,CorrelationLatticeNode> e: sortedChildren.entrySet()) {
			sortedChildren.remove(key)
		}
		
		
		private TreeMap<Float,CorrelationLatticeNode> sortedChildren;
		private TreeMap<Float,CorrelationLatticeNode> sortedConstants;
		private HashMap<String, TreeMap<Float,Literal>> headSuggestionsMap;
		private TreeMap<Float,CorrelationLatticeNode> interestingnessParents;
		private TreeMap<Float,Literal> suggestions;

		public float maxInterestingnessParents;
		private float maxInterestingnessChildren;
	}*/
	

}
