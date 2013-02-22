package urdf.cl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.emory.mathcs.backport.java.util.Collections;

import urdf.ilp.LearningManager;
import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;


public class CorrelationLattice implements Serializable {
	
	private static final long serialVersionUID = 4782549761623555466L;
	
	private static Logger logger = Logger.getLogger(LearningManager.loggerName);
	
	private CorrelationLatticeNode root;
	//private HashMap<Long,CorrelationLatticeNode> idNodesMap = new HashMap<Long, CorrelationLatticeNode>();
	private ArrayList<CorrelationLatticeNode> existentNodes = new ArrayList<CorrelationLatticeNode>();
	private Collection<Relation> candidates;
	private Relation rootRelation;
	
	private float divergenceThreshold = (float) 0.0;
	private float independenceThreshold = (float) 0.0;
	private float confidenceThreshold = (float) 0.75;
	private int supportThreshold = 25;
	
	private Histogram rootHistogram;
	
	private int numberOfBuckets = 25;
	
	private int maxNodesPerLevel = Integer.MAX_VALUE;
	
	private int maxConstantsPerRelation = 50000;
	
	public boolean histogramBySupport = true;
	private Collection<NumericalRule> resultRules = new ArrayList<NumericalRule>();
	
	int maxLevels;
	
	ArrayList<Relation> candidateRelations = new ArrayList<Relation>();
	
	public CorrelationLattice(Relation rootRelation, ArrayList<Relation> cadidates, int levels) {
		this.maxLevels = levels;
		this.candidates = cadidates;
		this.rootRelation = rootRelation;
		this.root = new CorrelationLatticeNode(this,rootRelation);
		this.root.setNumberOfBuckets(numberOfBuckets);
		//this.idNodesMap.put(root.getId(),root);
		this.existentNodes.add(root);
		this.resultRules = new ArrayList<NumericalRule>();
	}
	
	
	public void buildLattice(QueryHandler queryHandler) throws SQLException {
		logger.log(Level.INFO,"Building Lattice for relation "+rootRelation.getSimpleName());

		root.queryNodeProperties(queryHandler);	
		
		ArrayTools.print(root.getDistribution());
		ArrayTools.print(rootHistogram.getBoundaries());
		
		//rootHistogram = root.getHistogram();
		
		// First level with the candidate relations
		LinkedList<CorrelationLatticeNode> nextLevel = new LinkedList<CorrelationLatticeNode>(); 
		for (Relation r : candidates) {
			if (r!=null) {
				logger.log(Level.INFO,r.getSimpleName());
				CorrelationLatticeNode newNode = root.clone();
				newNode.addLiteral(r);
				newNode.addParent(root);
				newNode.queryNodeGroupProperties(queryHandler);
				
				if (!r.isDangerous()) {
					nextLevel.add(newNode);
					logger.log(Level.DEBUG,"\n"+newNode);
					logger.log(Level.DEBUG,"\t"+newNode.getInfo());
				}
				
				int i=0;
				//for (long l: newNode.getConstants()) {
				//	CorrelationLatticeNode n = getExistentNode(l);
				for (CorrelationLatticeNode n: newNode.getConstants()) {
					logger.log(Level.DEBUG,"\n"+n);
					logger.log(Level.DEBUG,"\t"+n.getInfo());
					if (i++<maxConstantsPerRelation && n.getSupport()>=supportThreshold) {
						nextLevel.add(n);
						//System.out.println(n);
					}else 
						break;
				}
			}
		}
		root.analizeNode();
		
		// Joins the nodes
		LinkedList<CorrelationLatticeNode> level = nextLevel;
		nextLevel = new LinkedList<CorrelationLatticeNode>();
		for (int depth=1; depth<maxLevels; depth++) {	
			logger.log(Level.INFO,"\n\nLEVEL"+(depth+1)+" #="+level.size()+"!!!");
			Collections.sort(level);
			Object[] leaves = level.toArray();
			//logger.log(Level.DEBUG,"Top-"+maxNodesPerLevel);for (int i=0; i<leaves.length && i<maxNodesPerLevel; i++) {logger.log(Level.DEBUG, ((CorrelationLatticeNode)leaves[i]).maxKldivParents + "\t" + ((CorrelationLatticeNode)leaves[i]).toString());} logger.log(Level.DEBUG,);
			int joinedpairs = 0;
			
			for (int i=1; i<leaves.length; i++) {
				if ((i%100)==0) System.out.println(i);
				CorrelationLatticeNode iNode = (CorrelationLatticeNode) leaves[i];				
				for (int j=0; !iNode.isPruned() && j<i; j++) {
					CorrelationLatticeNode jNode = (CorrelationLatticeNode) leaves[j];
					if (iNode.getSupport()>=supportThreshold && jNode.getSupport()>=supportThreshold)
						if (!jNode.isPruned() && !iNode.isPruned()) {
							if (joinedpairs<maxNodesPerLevel)
								try {
									CorrelationLatticeNode newNode = joinNodes(iNode, jNode, queryHandler);	
									if (newNode.getSupport()>=supportThreshold) {
										//System.out.println(newNode);
										iNode.addChild(newNode);
										jNode.addChild(newNode);
										nextLevel.add(newNode);										
	
										joinedpairs++;
										logger.log(Level.DEBUG,"\n"+newNode);
										logger.log(Level.DEBUG,"\t"+newNode.getInfo());
									}

								} catch (IllegalArgumentException e) {
								}
						}
				}
			}
			for (CorrelationLatticeNode node : nextLevel)
				node.analizeNode();

			level = nextLevel;
			nextLevel = new LinkedList<CorrelationLatticeNode>();
		}
	}
	
	//public CorrelationLatticeNode getExistentNode(long n) {
	//	return idNodesMap.get(n);
	//}
	
	public CorrelationLatticeNode getExistentNode(CorrelationLatticeNode n) {
		return existentNodes.get(existentNodes.indexOf(n));
	}
	
	public boolean addNodeToExistent(CorrelationLatticeNode n) {
		//idNodesMap.put(n.getId(),n);
		return existentNodes.add(n);
	}
	
	public boolean nodeExistis(CorrelationLatticeNode n) {
		return existentNodes.contains(n);
	}
	
	public void addRule(NumericalRule rule) {
		for (NumericalRule r: resultRules) {
			if (rule.isSpecializationOf(r)) {
				rule.generalizations.add(r);								
			}		
		}	
		if (rule.hasGainComparedToGeneralizations(confidenceThreshold, supportThreshold) && !resultRules.contains(rule)) 
			resultRules.add(rule);
	}
	
	public CorrelationLatticeNode joinNodes(CorrelationLatticeNode node1,CorrelationLatticeNode node2, QueryHandler qh) throws SQLException {		
		CorrelationLatticeNode newNode = node1.clone();

		if (node1.getLevel() != node2.getLevel())
			throw new IllegalArgumentException("Nodes should be of same level");
		//if (node1.getConstants().contains(node2.getId()) || node2.getConstants().contains(node1.getId()))
		if (node1.getConstants().contains(node2) || node2.getConstants().contains(node1))
			throw new IllegalArgumentException("One node is constant of the other");
		

		Literal diff1 = null;
		Literal diff2 = null;

		// Checks if nodes are joinable
		for (Literal l2 : node2.getLiterals()) {
			Literal l1;
			if ((l1 = newNode.getLiteral(l2.getRelation())) != null) {
				if (!equalLiterals(l1, l2)) {
					checkIfConstantNode(node1,node2);
					throw new IllegalArgumentException("Nodes contain same relation but with distinct constants\n\t"+ node1 + "\n\t" + node2);
				}
			} else {
				newNode.addLiteral(l2);
				Literal newLit = newNode.getLiteral(l2.getRelation());
				
				diff2 = l2;
				if (newNode.getLevel() > (node1.getLevel() + 1))
					throw new IllegalArgumentException("Nodes cannot join, there's more than one diffent literal\n\t"+ node1 + "\n\t" + node2);
			}
		}
		// Search for the other differential literal
		for (Literal l : node1.getLiterals()) 
			if (node2.getLiteral(l.getRelation()) == null) 
				diff1 = l;
			
		if (diff1==null || diff2==null) 
			throw new IllegalArgumentException("Nodes cannot join, there's not 2 differential literals \n\t"+ node1 + "\n\t" + node2);


		//if (!idNodesMap.containsKey(newNode)) {
		if (!existentNodes.contains(newNode)) {

			newNode.queryNodeProperties(qh);

			
			//idNodesMap.put(newNode.getId(),newNode);
			
			//existentNodes.add(newNode);
			
			//if (!node.isPruned()) {
			if (newNode.getSupport()>=supportThreshold) {		
				newNode.addParent(node1);
				newNode.addParent(node2);
				newNode.analizeNode();
				existentNodes.add(newNode);
				int[] indepHipotheses = independentJoinDitribution(node1, node2);
				float indepMeasure = ArrayTools.chiSquare(ArrayTools.laplaceSmooth(indepHipotheses), ArrayTools.laplaceSmooth(newNode.getDistribution()));
				if (indepMeasure >= independenceThreshold) {
					node1.addChild(newNode);
					node2.addChild(newNode);		
					node1.addSuggestion(newNode,diff2);
					node2.addSuggestion(newNode,diff1);
					node1.addHeadNewLiteral(diff1, diff2, newNode, node2);
					node2.addHeadNewLiteral(diff2, diff1, newNode, node1);
				}
			} else {
				throw new IllegalArgumentException("Node "+newNode+" doesnt satisfy support threshold");
			}
		} else {
			CorrelationLatticeNode existent = getExistentNode(newNode);
			int[] indepHipotheses = independentJoinDitribution(node1, node2);
			if (existent.getDistribution()==null)
				existent.queryNodeProperties(qh);
			float indepMeasure = ArrayTools.chiSquare(ArrayTools.laplaceSmooth(indepHipotheses), ArrayTools.laplaceSmooth(existent.getDistribution()));
			existent.addParent(node1);
			existent.addParent(node2);
			if (indepMeasure >= independenceThreshold && existent.getSupport()>=supportThreshold) {
				node1.addChild(existent);
				node2.addChild(existent);
				node1.addSuggestion(existent,diff2);
				node2.addSuggestion(existent,diff1);
				existent.analizeNode();
				node1.addHeadNewLiteral(diff1, diff2, existent, node2);
				node2.addHeadNewLiteral(diff2, diff1, existent, node1);
			} else {
				throw new IllegalArgumentException("New node is independent from parent nodes "+newNode);
			}
			throw new IllegalArgumentException("Join of nodes result in an already existant node "+newNode);
		}
		
		return newNode;
	}
	
	public void checkNodeForRules(CorrelationLatticeNode node) {
		//for (long p: node.getParents()) 
		//	testRule(node,idNodesMap.get(p));
		for (CorrelationLatticeNode p: node.getParents())
			testRule(node,p);
		
		//for (long c: node.getConstants()) {
		//	CorrelationLatticeNode n = getExistentNode(c);
		for (CorrelationLatticeNode n: node.getConstants()) {
			if (n.getSupport()>=supportThreshold) 
				testRule(n,node);
		}
	}
	
	public Collection<NumericalRule> searchRules(CorrelationLatticeNode root){
		Collection<CorrelationLatticeNode> set = new ArrayList<CorrelationLatticeNode>();
		Collection<CorrelationLatticeNode> next = new ArrayList<CorrelationLatticeNode>();
		
		set.add(root);
		while (!set.isEmpty()) {
			for (CorrelationLatticeNode node: set) {
				//for (long l: node.getChildren()) {
				//	CorrelationLatticeNode child = getExistentNode(l);
				for (CorrelationLatticeNode child: node.getChildren()) {
					if (/*!child.isPruned()*/child.getSupport()>=supportThreshold)
						next.add(child);
				}
				if (/*!node.isPruned()*/node.getSupport()>=supportThreshold)
					checkNodeForRules(node);
			}

			set = next;
			next = new HashSet<CorrelationLatticeNode>();
		}
		return resultRules;
	}
	
	public void checkIfConstantNode(CorrelationLatticeNode node1,CorrelationLatticeNode node2) {
		int node1ConstantOf2 = 0;
		int node2ConstantOf1 = 0;
		
		if (node1.getLevel() != node2.getLevel())
			return;
		//if (node1.getConstants().contains(node2.getId()) || node2.getConstants().contains(node1.getId()))
		if (node1.getConstants().contains(node2) || node2.getConstants().contains(node1))
			return;
		
		for (Literal l2 : node2.getLiterals()) {
			Literal l1 = node1.getLiteral(l2.getRelation());
			if (l1 == null)
				return;
			else {
				if (!equalLiterals(l1, l2)) {
					if (l1.getSecondArgument()==-1 && l2.getSecondArgument()!=-1)
						node1ConstantOf2++;
					if (l1.getSecondArgument()!=-1 && l2.getSecondArgument()==-1)
						node2ConstantOf1++;
					if (l1.getSecondArgument()==-1 && l2.getSecondArgument()==-1)
						return;
					if (l1.getSecondArgument()!=-1 && l2.getSecondArgument()!=-1){
						if (l1.getMin()==Float.NEGATIVE_INFINITY && l1.getMax()==Float.POSITIVE_INFINITY)
							node2ConstantOf1++;
						else if (l2.getMin()==Float.NEGATIVE_INFINITY && l2.getMax()==Float.POSITIVE_INFINITY)
							node1ConstantOf2++;
						else 
							return;
					}
						
				}
			}
		}
		if (node1ConstantOf2==1 && node2ConstantOf1==0)
			node2.addConstant(node1);
		if (node1ConstantOf2==0 && node2ConstantOf1==1)
			node1.addConstant(node2);
	}
	
	public void testRule(CorrelationLatticeNode node, CorrelationLatticeNode parent) {
		float overallAccuracy = ((float)ArrayTools.sum(node.getDistribution()))/((float)ArrayTools.sum(parent.getDistribution()));
		float[] acc = ArrayTools.getAccuraciesWithMinSupport(node.getDistribution(), parent.getDistribution(), supportThreshold);

		boolean isGood = false;
		if (overallAccuracy<confidenceThreshold) 
			if (ArrayTools.max(acc) > confidenceThreshold) 
				isGood = true;
				
		if (ArrayTools.max(acc)>1) {
			logger.log(Level.ERROR,"ACC OVER 1");
			logger.log(Level.ERROR,parent);
			logger.log(Level.ERROR,node);
			ArrayTools.print(acc);
			logger.log(Level.ERROR,node.toString());
			ArrayTools.print(node.getDistribution());
			logger.log(Level.ERROR,parent.toString());
			ArrayTools.print(parent.getDistribution());
		}
		
		if (isGood)  {
			NumericalRule rule;
			Collection<Relation> litNode = node.getRelations();
			Collection<Relation> litParent = parent.getRelations();
			if (litNode.size() == (litParent.size()+1)) {
				try {
					Literal head = null;
					// If node is a child from parent
					if (litNode.size()==(litParent.size()+1)) {
						for (Relation r: litNode) {
							if (!litParent.contains(r)) {
								head = node.getLiteral(r);
								break;
							}
						}
					}
					// If node is a constant from parent
					if (litNode.size()==litParent.size()) {
						for (Relation r: litNode) {
							int arg2Node = node.getLiteral(r).getSecondArgument();
							int arg2Parent = parent.getLiteral(r).getSecondArgument();
							if ((arg2Node == -1 || arg2Parent == -1) && arg2Node != arg2Parent) {
								head = node.getLiteral(r);
							}
						}									
					}
					if (head!=null) {
						rule = new NumericalRule(head, 1);							
						for (Relation bodyLitKey: litParent) 
							rule.addLiteral(node.getLiteral(bodyLitKey));
						
						rule.setSupportAndAccuracyDistribution(node.getDistribution(), parent.getDistribution(), acc);
						
						addRule(rule);
						
						logger.log(Level.DEBUG,rule.getRuleString());
						logger.log(Level.DEBUG, ArrayTools.toString(ArrayTools.divide(node.getDistribution(), parent.getDistribution())));
						logger.log(Level.DEBUG, ArrayTools.toString(acc));
						logger.log(Level.DEBUG,parent.toString());
						logger.log(Level.DEBUG, ArrayTools.toString(parent.getDistribution()));
						logger.log(Level.DEBUG,node.toString());
						logger.log(Level.DEBUG, ArrayTools.toString(node.getDistribution()));
						//try{ArrayTools.plot(acc, ArrayTools.normalize(node.getDistribution()), rule.getRuleString());} catch (IOException e) {}
					} else {
						//logger.log(Level.DEBUG,"Fodeo!!!!!\n" + node.toString() + "\n" + parent.toString());
					}
				} catch (Exception e) {
					logger.log(Level.DEBUG,e.getMessage());
				}	
			}

		}
	}
	
	/**
	 * Checks if literals are equals for the lattice (ignoring different variable names)
	 * @param l1
	 * @param l2
	 * @return
	 */
	public boolean equalLiterals(Literal l1, Literal l2) {
		if (l1.equals(l2))
			return true; 
		else {
			if (l1.getSecondArgument()!=-1 && l2.getSecondArgument()!=-1)
				if (l1.getMax()==l2.getMax() && l1.getMin()==l2.getMin())
					return true;
			return false;
		}
	}
	
	public CorrelationLatticeNode getCommonParent(CorrelationLatticeNode node1,CorrelationLatticeNode node2) {
		//for (long l1: node1.getParents()) {
		//	CorrelationLatticeNode p1 = getExistentNode(l1);
		for (CorrelationLatticeNode p1: node1.getParents()) {
			//if (node2.getParents().contains(p1.getId()))
			if (node2.getParents().contains(p1))
				return p1;
		}
		return null;
	}
	
	public int[] independentJoinDitribution(CorrelationLatticeNode node1,CorrelationLatticeNode node2) {
		CorrelationLatticeNode commonParent = getCommonParent(node1, node2);
		if (commonParent==null)
			throw new IllegalArgumentException("Nodes cannot be joined. There's no common parent");
		
		float[] pNode1GivenParent = ArrayTools.divide(node1.getDistribution(), commonParent.getDistribution());
		float[] pNode2GivenParent = ArrayTools.divide(node2.getDistribution(), commonParent.getDistribution());
		
		return ArrayTools.round(ArrayTools.multiply(ArrayTools.multiply(pNode1GivenParent, pNode2GivenParent),commonParent.getDistribution()));
				
	}
	
	public CorrelationLatticeNode getRoot() {
		return root;
	}
	
	public Histogram getHistogram() {
		return rootHistogram;
	}

	public void setHistogram(Histogram histogram) {
		this.rootHistogram = histogram;
	}
	
	public Histogram copyHistogram() {
		if (rootHistogram==null)
			return null;
		Histogram h = rootHistogram.clone();
		h.reset();
		return h;
	}
	
	public Literal getRootLiteral() {
		return root.getLiteral(rootRelation);
	}
	
	public Relation getRootRelation() {
		return rootRelation;
	}

	public Collection<NumericalRule> getResultRules() {
		return resultRules;
	}
	
	public int getMaxLevels() {
		return maxLevels;
	}
	public void setMaxLevels (int maxLevels) {
		this.maxLevels = maxLevels;
	}
	
	public float getDivergenceThreshold() {
		return divergenceThreshold;
	}
	public void setDivergenceThreshold(float divThreshold) {
		this.divergenceThreshold = divThreshold;
	}
	public int getSupportThreshold() {
		return supportThreshold;
	}
	public void setSupportThreshold(int supportThreshold) {
		this.supportThreshold = supportThreshold;
	}
	public float getIndependenceThreshold() {
		return independenceThreshold;
	}
	public void setIndependenceThreshold(float independenceThreshold) {
		this.independenceThreshold = independenceThreshold;
	}
	public float getConfidenceThreshold() {
		return confidenceThreshold;
	}
	public void setConfidenceThreshold(float confidenceThreshold) {
		this.confidenceThreshold = confidenceThreshold;
	}
	public int getNumberOfBuckets() {
		return numberOfBuckets;
	}
	public void setNumberOfBuckets(int numberOfBuckets) {
		this.numberOfBuckets = numberOfBuckets;
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
	
	public void breadthFirst(){
		root.breadthFirst();
	}
	
	public static CorrelationLattice readFromDisk(String path) {	 
        try{
        	FileInputStream fileIn;
        	fileIn =new FileInputStream(path);
        	CorrelationLattice lattice = (CorrelationLattice) SerializationUtils.deserialize(fileIn);
        	return lattice;
       }
       catch(IOException e){
           //e.printStackTrace();
           return null;
       }
	}
}
