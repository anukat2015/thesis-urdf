package urdf.ilp;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



/**
 * 	@author Christina Teflioudi
 *	This class tries to learn rules for given predicates  
 */
public class RuleLearner 
{
	private static Logger logger = Logger.getLogger(LearningManager.loggerName);
	
	QueryHandler queryHandler;
	ThresholdChecker tChecker;
	RelationsInfo info;
	
	HeadPredicate head;
	int FactsForHead;
	int depth;
	int inputArg;
	int beamWidth; // if 0 do exhaustive search - no search heuristic 
	
	HashMap<Integer, ArrayList<BodyPredicate>> allowedRelations=new HashMap<Integer, ArrayList<BodyPredicate>>();	
	HashMap<Integer,HashSet<Rule>> rulesCheckedMap;
	RuleTreeNode rulesTree;	
	
	boolean allowFreeVars=false;	//allow final rules with free variables
	boolean tryConstants=false;		//allow rules with constants
	int gainMeasure=0; 	// 0: accuracy gain 1: weighted accuracy gain 2: Information gain 3: weighted information gain
	int numOfTries=20;	// how many constants I am going to try for the type relation
	String[] typeConstantsForArg1;
	String[] typeConstantsForArg2;
	
	// constructor
	public RuleLearner(QueryHandler queryHandler,ThresholdChecker tChecker, RelationsInfo info, boolean allowFreeVars,int gainMeasure, int beamWidth, boolean tryConstants) throws Exception {
		
		PropertyConfigurator.configure(LearningManager.log4jConfig);
		
		this.queryHandler = queryHandler;
		this.tChecker = tChecker;
		this.info = info;
		this.allowFreeVars = allowFreeVars;
		this.gainMeasure = gainMeasure;
		this.beamWidth = beamWidth;
		this.tryConstants = tryConstants;
	}


	public void learnRule(HeadPredicate head, int depth) throws Exception { 
		
		Rule rule;
		this.head = head;
		this.allowedRelations = head.getCandidateBodyRelations();
		this.depth = depth;
		this.inputArg = head.getInputArg();
		
		rulesCheckedMap = new HashMap<Integer,HashSet<Rule>>();	
		
		preprocessing();		
			
		// first add an empty rule
		rule=new Rule(new Literal(head.getHeadRelation(),65,66));
		if (head.getHeadRelation().getRelationsForm()==1) {
			rule.addLiteral(new Literal(RelationsInfo.NEQ,65,66));
		}
		
		rulesTree=new RuleTreeNode(rule,null,0);
		
		extendRule(rulesTree,1);
		
		postProcess(rulesTree);

		logger.log(Level.INFO,"Positive examples for the head in total: "+FactsForHead);
	
	}
	
	private void extendRule(RuleTreeNode node, int d) throws Exception  {
		
		logger.log(Level.INFO,"Extending rule: " + node.getRule().getRuleString() + " with d=" + d);
		
		if (node.getRule().getConfidence() < tChecker.getStoppingThreshold()) {
			
			// first try to refine the head
			if (inputArg==2) {
				refinePredicate(node,3,d,-1);//arg2JoinOnArg1
				refinePredicate(node,4,d,-1);//arg2JoinOnArg2
			}
			else {
				refinePredicate(node,1,d,-1);//arg1JoinOnArg1
				refinePredicate(node,2,d,-1);//arg1JoinOnArg2
			}

			// second try to refine the predicates in the body
			for (int i=0, len=node.getRule().getBodyLiterals().size();i<len;i++) {
				// do not refine auxiliary relations 
				if (node.getRule().getBodyLiterals().get(i).getRelation().isAuxiliary()) {
					continue;
				}

				refinePredicate(node,1,d,i);//arg1JoinOnArg1
				refinePredicate(node,2,d,i);//arg1JoinOnArg2
				refinePredicate(node,3,d,i);//arg2JoinOnArg1
				refinePredicate(node,4,d,i);//arg2JoinOnArg2
			}

			if(node.getChildren()!=null && node.getChildren().size()>0)
			{
				// if beam search on, compute gains for each child. Only the children with the highest gain will survive 
				if (beamWidth > 0 && d > 1) {
					eliminateChildrenOutOfBeam(node);
				}

				if (d < depth) {
					for (int i=0,len=node.getChildren().size(); i<len; i++) {
						extendRule(node.getChildren().get(i),d+1);
					}
				}

			}
		}		
	}
	
	private void refinePredicate(RuleTreeNode node, int joinCase, int d, int position) throws Exception
	{
		logger.log(Level.INFO,"Refining predicate from rule: " + node.getRule().getRuleString());
		
		Rule rule,newRule=null, freeRule=null,bindRule=null;
		int arg1=0,arg2=0,freeVar;
		ArrayList<Relation> candidateRelations;
		Literal newLiteral=null, freeLiteral=null;
		boolean canCloseConnection=false;
		boolean ruleStored=false;
		int mode1=0,mode2=0;
		boolean[] out;
		
		rule=node.getRule();
		
		Literal literal=(position==-1?rule.getHead():rule.getBodyLiterals().get(position));
		
		if (allowFreeVars) 
			candidateRelations=getJoinableRelations(joinCase, literal.getRelation()); // no restriction at all
		else
			candidateRelations=getRelationsFromAllowedRelations(d,joinCase, literal.getRelation()); // restriction according to depth
		
		switch(joinCase) {
			case 1:				
				arg1 = literal.getFirstArgument();
				mode1 = 1;
				arg2 = rule.getNextVariableNumber();		
				freeVar = 2;
				break;			
			case 2:				
				arg2 = literal.getFirstArgument();
				mode2 = 1;
				arg1 = rule.getNextVariableNumber();		
				freeVar = 1;
				break;
			case 3:			
				arg1 = literal.getSecondArgument();
				mode1 = 1;
				arg2 = rule.getNextVariableNumber();	
				freeVar = 2;
				break;
			case 4:			
				arg2 = literal.getSecondArgument();
				mode2 = 1;
				arg1 = rule.getNextVariableNumber();
				freeVar = 1;
				break;
			default: throw new IllegalArgumentException("Join case should be 1,2,3 or 4. It's " + joinCase + "instead.");
		}
		
		if (candidateRelations!=null && candidateRelations.size()>0) {
			for (int i=0,len=candidateRelations.size();i<len;i++) {
				canCloseConnection=false;
				ruleStored=false;	
				freeRule=null;
				bindRule=null;
				
				if (candidateRelations.get(i).getName().equals("hasUTCOffset") 
						|| candidateRelations.get(i).getName().equals("bornOnDate")
						|| candidateRelations.get(i).getName().equals("isOfGenre")) {
					continue;
				}
				
				
				// STEP 1: WITH FREE VARIABLE	
				newRule = rule.clone();
				newLiteral = new Literal(candidateRelations.get(i),arg1,mode1,arg2,mode2,freeVar);				
				out = considerElimination(newRule,rule,newLiteral,literal,d,position);
				
				
				if (out[0]) {
					continue;
				}
				else if(!out[1])
				{
					if (evaluateRule(node,newRule,d))
					{
						ruleStored=true;
						freeRule=newRule;
						if (newRule.bindsHeadVariables() && newRule.isTooGeneral()&& newRule.isGood()) {
							checkExtendRuleWithTypes(node, newRule, d);
						}
						if (newRule.bindsHeadVariables() && tryConstants &&!newRule.isTooGeneral() && head.getHeadRelation().getConstantInArg()!=0) {
							checkExtendRuleWithConstants(node,newRule,d);
						}
					}
				}
				
				// STEP 2: WITH BOUND VARIABLES
				if(inputArg==2) {
					if ((joinCase==1 || joinCase==3) && info.arg1JoinOnArg2.get(head.getHeadRelation())!=null &&
							info.arg1JoinOnArg2.get(head.getHeadRelation()).contains(candidateRelations.get(i)))
					{
						// form the new Literal
						newLiteral=new Literal(candidateRelations.get(i),arg1,65);
						freeLiteral=new Literal(RelationsInfo.NEQ,arg2,65);
						canCloseConnection=true;
					}
					else if ((joinCase==2 || joinCase==4)&& info.arg1JoinOnArg1.get(head.getHeadRelation())!=null &&
							info.arg1JoinOnArg1.get(head.getHeadRelation()).contains(candidateRelations.get(i)))
					{
						// form the new Literal
						newLiteral=new Literal(candidateRelations.get(i),65,arg2);
						freeLiteral=new Literal(RelationsInfo.NEQ,arg1,65);
						canCloseConnection=true;
					}
				}
				else {
					if ((joinCase==1 || joinCase==3) && info.arg2JoinOnArg2.get(head.getHeadRelation())!=null &&
							info.arg2JoinOnArg2.get(head.getHeadRelation()).contains(candidateRelations.get(i)))
					{
						// form the new Literal
						newLiteral=new Literal(candidateRelations.get(i),arg1,66);
						freeLiteral=new Literal(RelationsInfo.NEQ,arg2,66);
						canCloseConnection=true;
					}
					else if ((joinCase==2 || joinCase==4)&& info.arg2JoinOnArg1.get(head.getHeadRelation())!=null &&
							info.arg2JoinOnArg1.get(head.getHeadRelation()).contains(candidateRelations.get(i)))
					{
						// form the new Literal
						newLiteral=new Literal(candidateRelations.get(i),66,arg2);
						freeLiteral=new Literal(RelationsInfo.NEQ,arg1,66);
						canCloseConnection=true;
					}
				}

				if(canCloseConnection) {
					newRule=rule.clone();
					out=considerElimination(newRule,rule,newLiteral,literal,d,position);
					
					if (out[0] || out[1])
						continue;
					
					else  {
						if (evaluateRule(node,newRule,d)) {
							bindRule=newRule;
							if (newRule.bindsHeadVariables() && newRule.isTooGeneral() && newRule.isGood())
								checkExtendRuleWithTypes(node, newRule, d);
							
							head.getHeadRelation().setConstantInArg(2);
							if (newRule.bindsHeadVariables() && tryConstants &&!newRule.isTooGeneral() && head.getHeadRelation().getConstantInArg()!=0)
								checkExtendRuleWithConstants(node,newRule,d);

						}
					}					
					
					// STEP 3: for z!=y
					if (ruleStored  && freeRule.bindsHeadVariables() && bindRule!=null) {
						newRule=freeRule.clone();
						newRule.addLiteral(freeLiteral);
						newRule.setHasFreeVariables(false);	
						if (evaluateRule(node,newRule, d,freeRule,bindRule)) {
							
							if (newRule.bindsHeadVariables() && newRule.isTooGeneral() && newRule.isGood())
								checkExtendRuleWithTypes(node, newRule, d);
							if (newRule.bindsHeadVariables() && tryConstants &&!newRule.isTooGeneral() && head.getHeadRelation().getConstantInArg()!=0)
								checkExtendRuleWithConstants(node,newRule,d);
						}
					}					
					
				}
			}
		}
		
	}

	/**
	 * @param rule
	 * @param newLiteral
	 * @param literal
	 * @param d
	 * @param position
	 * @return
	 * 
	 * the method adds the newLiteral in the newRule which is a clone of old rule
	 * out[0]: the rule should be considered at all
	 * out[1]: the rule should be skipped but considered for further change on arguments
	 * @throws Exception 
	 */
	private boolean[] considerElimination(Rule newRule,Rule oldRule, Literal newLiteral,Literal literal, int d, int position) throws Exception {
		
		logger.log(Level.INFO,"Considering elimintaion Rule: " +  newRule.getRuleString() + " NewLiteral: " + newLiteral.getSparqlPattern());
		
		boolean[] out=new boolean[2];
		
		
		if (dontConsiderRelation(literal,newLiteral, d)) {
			out[0]=true;
			out[1]=true;
			return out;
		}
		
		newRule.addLiteral(newLiteral,position);
		// take care of non equalities
		checkToAddAuxiliaryRelation(newRule);
						
		if (!(dontConsiderRelation(oldRule,newLiteral,d) || dontConsiderRelation(newRule, d))) {
			out[0]=false;
			out[1]=false;
		}		
		else {
			out[0]=false;
			out[1]=true;			
		}
		
		if (out[0]==true) logger.log(Level.INFO, "Rule shouldn't be considered at all");
		if (out[1]==true) logger.log(Level.INFO, "Rule shoudld be skipped but but considered for further change on argumets");

		return out;
	}
	/**
	 * 	checks if the rule should be stored as a child of the node
	 * 	if yes it stores the rule and returns true
	 * @throws Exception 
	 */
 	private boolean evaluateRule(RuleTreeNode node,Rule rule, int d) throws Exception{
		boolean flag = false;
		
		logger.log(Level.INFO,"Evaluating Rule: "+rule.getRuleString());
		
		if (checkForSupportAndExactExamples(rule)) {				
			// compute Gain and store
			if(rule.bindsHeadVariables() && d>1 && rule.isGood()) 
				checkGainFromBestAncestor(node,rule);	// set the is good flag							
			
			node.addChild(new RuleTreeNode(rule,node,d));	
			flag=true;
		}
				
		// add in any case the rule in the checked rules to keep track of what you have already considered
		if (rulesCheckedMap.get(d)==null) 
			rulesCheckedMap.put(d, new HashSet<Rule>());

		rulesCheckedMap.get(d).add(rule);
				
		return flag;
	}
 	
	private boolean evaluateRule(RuleTreeNode node,Rule rule, int d,Rule freeRule, Rule bindRule) throws Exception
	{
		boolean flag=false;
		
		logger.log(Level.INFO,"Evaluating Rule: " + rule.getRuleString() + "With FreeRule: " + freeRule.getRuleString() + "With BindRule: " + bindRule.getRuleString());
		
		if (tChecker.checkComplementaryRule(rule, freeRule,bindRule, FactsForHead,inputArg)) {
			if(rule.bindsHeadVariables()&& d>1 && rule.isGood()) 
				checkGainFromBestAncestor(node,rule);	// set the is good flag. If beam search is on the best ancestor is the closest one							
			
			node.addChild(new RuleTreeNode(rule,node,d));
			flag=true;
			
		}
		
		// add in any case the rule in the checked rules to keep track of what you have already considered
		if (rulesCheckedMap.get(d)==null)
			rulesCheckedMap.put(d, new HashSet<Rule>());
	
		rulesCheckedMap.get(d).add(rule);
		return flag;
	}
	

// *************************** AUXILIARY METHODS *************************************
 	private ArrayList<Relation> getRelationsFromAllowedRelations(int d,int joinCase, Relation relation){

		if (allowedRelations.get(d-1)==null)
			return null;
		
		for (int i=0,len=allowedRelations.get(d-1).size();i<len;i++) {
			if (allowedRelations.get(d-1).get(i).hasRelation(relation)) {
				switch(joinCase) {
					case 1:
						return allowedRelations.get(d-1).get(i).arg1JoinOnArg1;						
					case 2:
						return allowedRelations.get(d-1).get(i).arg1JoinOnArg2;
					case 3:
						return allowedRelations.get(d-1).get(i).arg2JoinOnArg1;
					case 4:
						return allowedRelations.get(d-1).get(i).arg2JoinOnArg2;
				}
			}
		}
		
		return null;
	}
	
	private void checkExtendRuleWithTypes(RuleTreeNode node, Rule rule, int d) throws Exception
	{
		Rule newRule, newRule2;
		Literal lit;
		int var;
		
		if (inputArg!=2) {// 1 or 0 
			
			for (int i=0;i<numOfTries;i++) {
				newRule = rule.clone();
				var = newRule.getNextVariableNumber();
				lit = new Literal(info.getRelationFromRelations("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"),65,1,var,0,2);
				newRule.addLiteral(lit,0);	
				lit = new Literal(RelationsInfo.EQ,var,1,-1,0,typeConstantsForArg1[i]);
				newRule.addLiteral(lit);				
				
				if (inputArg==0) {
					for (int j=0;j<numOfTries;j++) {
						newRule2 = newRule.clone();
						var = newRule.getNextVariableNumber();
						lit = new Literal(info.getRelationFromRelations("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"),66,1,var,0,2);	
						newRule2.addLiteral(lit);				
						lit = new Literal(RelationsInfo.EQ,var,1,-1,0,typeConstantsForArg2[i]);
						newRule2.addLiteral(lit);
						
						// evaluate rule
						newRule2.setHasFreeVariables(false);
						evaluateRule(node,newRule2, d);
						
						if (newRule2.bindsHeadVariables() && tryConstants && head.getHeadRelation().getConstantInArg()!=0)
							checkExtendRuleWithConstants(node,newRule2,d+2);
					}
				}
				else {
					// evaluate rule
					newRule.setHasFreeVariables(false);
					evaluateRule(node,newRule, d);
					
					if (newRule.bindsHeadVariables() && tryConstants && head.getHeadRelation().getConstantInArg()!=0)
						checkExtendRuleWithConstants(node,newRule,d+1);
					
				}
				
			}			
		}
		else {
			
			for (int i=0;i<numOfTries;i++) {
				newRule=rule.clone();
				var=newRule.getNextVariableNumber();
				lit=new Literal(info.getRelationFromRelations("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"),66,1,var,0,2);
				newRule.addLiteral(lit,0);				
				lit=new Literal(RelationsInfo.EQ,var,1,-1,0,typeConstantsForArg2[i]);
				newRule.addLiteral(lit);
				
				// evaluate rule
				newRule.setHasFreeVariables(false);
				evaluateRule(node,newRule,  d);
				
				if (newRule.bindsHeadVariables() && tryConstants && head.getHeadRelation().getConstantInArg()!=0)
					checkExtendRuleWithConstants(node,newRule,d+1);		
			}
			
		}
	}
	private void checkExtendRuleWithConstants(RuleTreeNode node, Rule rule, int d) throws Exception {
		Rule newRule;
		Literal lit;
		int var=(head.getHeadRelation().getConstantInArg()==1?65:66);
		// first get the constants if any
		ArrayList<String> consts=queryHandler.findConstants(rule, tChecker.getPossiblePosToBeCoveredThreshold(), tChecker.getPositivesCoveredThreshold(), tChecker.getSupportThreshold(), FactsForHead, inputArg, d);
		
		for (int i=0, len=consts.size();i<len;i++) {
			newRule = rule.clone();
			lit = new Literal(RelationsInfo.EQ,var,1,-1,0,consts.get(i));
			newRule.addLiteral(lit);
			newRule.setHasFreeVariables(false);
			evaluateRule(node,newRule,  d);
		}		
	}
	
 	private ArrayList<Relation> getJoinableRelations(int joinCase, Relation relation) {
		switch(joinCase) {
			case 1: return info.arg1JoinOnArg1.get(relation);			
			case 2: return info.arg1JoinOnArg2.get(relation);
			case 3: return info.arg2JoinOnArg1.get(relation);
			default: return info.arg2JoinOnArg2.get(relation);
		}
	}
 	
	public void printRules() {
		boolean onlyGood=true,onlyGenerals=false;
		
		ArrayList<Rule> usefulRules=new ArrayList<Rule>();
		rulesTree.getRulesOfTree(usefulRules,onlyGood, onlyGenerals, allowFreeVars);
		
		for (int i=0,len=usefulRules.size();i<len;i++) {
			usefulRules.get(i).printRule(false);
		}
		
	}

	private void eliminateChildrenOutOfBeam(RuleTreeNode node) throws Exception {
		ArrayList<Rule> topRules=new ArrayList<Rule>();
		int pos;
		
		for (int i=0;i<node.getChildren().size();i++) {
			if (node.getChild(i).getRule().hasFreeVariables())// to be changed for relative info gain
				continue;
			
			// remove rules which don't improve gain
			if (node.getChild(i).getRule().getGain()==0) {
				node.getChildren().get(i).getRule().setIsInBeam(false);
				continue;
			}
			if (topRules.size()==0) {
				topRules.add(node.getChildren().get(i).getRule());
				continue;
			}
			if (topRules.size()<=beamWidth){ // add in correct position 
				pos=findPositionToAdd(topRules,node.getChildren().get(i).getRule(), 0, topRules.size());
				if (pos < 0)  {
					// calculate insertion point
					pos = -(pos + 1);
				}
				topRules.add(pos, node.getChildren().get(i).getRule());
			}
			else if(topRules.get(beamWidth-1).getGain()<node.getChildren().get(i).getRule().getGain()) {// add in correct position and remove last 
				pos=findPositionToAdd(topRules,node.getChildren().get(i).getRule(), 0, topRules.size());
				if (pos < 0)  {
					// calculate insertion point
					pos = -(pos + 1);
				}
				topRules.add(pos, node.getChildren().get(i).getRule());
				topRules.remove(topRules.size()-1);
			}
		}

		if (topRules.size()>0) {
			for (int i=0;i<topRules.size();i++) {
				topRules.get(i).setIsInBeam(true);
			}
		}
		for (int i=0;i<node.getChildren().size();i++) {
			if (!node.getChildren().get(i).getRule().isInBeam()) {
				node.getChildren().remove(i);
				i--;
			}
		}
	}
	
	private int findPositionToAdd(ArrayList<Rule> topRules,Rule rule, int from, int to) {
		int low = from;
		int high = to;

		for (; low <= high;)  {
			int mid = (low + high) >> 1;
			double midVal = (mid>topRules.size()-1?0:topRules.get(mid).getConfidence());

			if (midVal < rule.getConfidence())				
				high = mid - 1;
			else if (midVal > rule.getConfidence())
				low = mid + 1;
			
			else  {
				// key found: search for first occurrence linearly
				// this search is necessary in the presence of duplicates
				int pos = mid - 1;
				while (pos >= from && topRules.get(pos).getConfidence() == rule.getConfidence()) 
					pos--;
				
				// return last valid position
				return pos + 1;
			}

		}
		return -(low + 1); // key not found.
		
		
	}
	
	private void checkToAddAuxiliaryRelation(Rule rule) throws Exception {
		Literal lit;
		int j;
		ArrayList<Literal> literals = (ArrayList<Literal>)rule.getBodyLiterals().clone();
		literals.add(rule.getHead());
		boolean flag;
		
		for (int i=0,len=literals.size();i<len-1;i++) {
			
			Literal iLiteral = literals.get(i);
			
			if (iLiteral.getRelation().isAuxiliary()) {
				continue;
			}
			
			lit = null;
			flag = false;
			j = i+1;
			
			while(j<len) {
				
				Literal jLiteral = literals.get(j);
				
				if(iLiteral.getRelation().equals(jLiteral.getRelation())) {
					if (iLiteral.getFirstArgument() == jLiteral.getFirstArgument() && iLiteral.getSecondArgument() != jLiteral.getSecondArgument()) {
						lit = new Literal(RelationsInfo.NEQ,iLiteral.getSecondArgument(),jLiteral.getSecondArgument());						
					}
					else if (iLiteral.getSecondArgument() == jLiteral.getSecondArgument() && iLiteral.getFirstArgument() != jLiteral.getFirstArgument()) {
						lit = new Literal(RelationsInfo.NEQ,iLiteral.getFirstArgument(),jLiteral.getFirstArgument());						
					}
					if (lit != null) {
						for (int k=0; k < rule.getBodyLiterals().size()-1; k++) {
							if (rule.getBodyLiterals().get(k).equals(lit)) {
								flag=true;
								break;
							}
						}
						if (!flag) {
							rule.addLiteral(lit);
						}
						break;
					}
				}
				
				j++;
			}
			
		}
		
	}
	
	
	// *************************** METHODS FOR CHECKING **********************************
	
	private boolean checkForSupportAndExactExamples(Rule rule) throws Exception
	{		
		if (tChecker.checkSupportThreshold(rule,FactsForHead,inputArg)) {			
			if (rule.bindsHeadVariables()) {
				if (!rule.hasFreeVariables()||allowFreeVars) {
					if(checkForPositives(rule)) {
						return true;
					}
					else {
						return false;
					}
				}				
			}				
			return true;			
		}
		return false;
	}
 	
 	/**
 	 * @param oldNode: parent node
 	 * @param newRule: the rule to be added as child
 	 * @return true if the gain from the closest Final ancestor is positive
 	 */
 	private boolean checkGainFromFather(RuleTreeNode oldNode,Rule newRule) {
 		Rule rule;
 		
 		if (oldNode.getRule().bindsHeadVariables()) 
 			rule=oldNode.getRule();
 		else 
 			rule=oldNode.getFirstFinalParentRule();
 		
 		if (rule==null) 
 			return true;
 		
 		if(calcGain(rule,newRule)>0) 
 			return true;
 		else 
 			return false;
 	}
 	
 	/**
 	 * @param rule: some rule which is ancestor of the new rule
 	 * @param newRule
 	 * @return the gain of the new rule in compare with the old one
 	 */
 	private double calcGain(Rule rule, Rule newRule) {
 		
 		double gain;
 		switch(gainMeasure){
 			case 0:
 				// Use the accuracy measure: AG(c',c)=A(c')-A(c)=conf(c')-conf(c)
 				gain=newRule.getConfidence()-rule.getConfidence(); 		 		
 				break;
 			case 1:
 				// Use the weighted accuracy measure: WAG(c',c)=(conf(c')-conf(c))* exact(c')/exact(c)
 				gain=(newRule.getConfidence()-rule.getConfidence())* newRule.getPositivesCovered()/rule.getExamplesCovered(); 		 		
 				break; 				
 			case 2:
 				// Use the information gain measure: IG(c',c)=log2(conf(c'))-log2(conf(c))
 				gain=(Math.log(newRule.getConfidence())-Math.log(rule.getConfidence()))/Math.log(2); 		 		
 				break; 				
 			default://3
 				// Use the weighted information gain: WIG(c',c)=(log2(conf(c'))-log2(conf(c)))* exact(c')/exact(c)
 				gain=(Math.log(newRule.getConfidence())-Math.log(rule.getConfidence()))* newRule.getPositivesCovered()/(rule.getExamplesCovered()*Math.log(2)); 		 		
 				
 		}		
 		newRule.setGain(gain);
 		logger.log(Level.INFO,"Gain="+gain+" from rule " + newRule.getRuleString() + " in relation to " + rule.getRuleString());
 		return gain;
 	}
 	
 	/**
 	 * @param oldNode: some rule which is ancestor of the new rule
 	 * @param newRule: the rule to be added as child
 	 * 
 	 * 	sets the flag isGood of the newRule to true if it is better than its best ancestor 
 	 */
 	private void checkGainFromBestAncestor(RuleTreeNode oldNode,Rule newRule) {
 		
 		Rule lastParentRule=oldNode.getBestFinalParentRule();
 		if (lastParentRule!=null) {
 			if(calcGain(lastParentRule,newRule)>0) {
 				newRule.setIsGood(true);
 				logger.log(Level.DEBUG, "Rule " + newRule.getRuleString() + " is Good, i.e. it's better than its best ancestor");
 			}
 	 		else {
 	 			newRule.setIsGood(false);
 	 			logger.log(Level.DEBUG, "Rule " + newRule.getRuleString() + " is NOT Good, i.e. there exists a better ancestor rule");
 	 		}	
 		} 	
 		
 	}
 	
	private boolean checkForPositives(Rule rule) throws Exception {
		
		if (tChecker.checkPositivesThreshold(rule,inputArg))					
			return true;
		
		return false;		
	}

// *************************** METHODS FOR STRUCTURE PRUNING *************************
 	
	private boolean dontConsiderRelation(Literal literal,Literal newLiteral,int d) throws Exception
	{
		// exclude the type relation in the first stage
		if (newLiteral.getRelationName().equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))
			return true;
	
		// violate symmetric and functional property of relation
		if (literal.getRelation().equals(newLiteral.getRelation()))// && proc.getRelationsForm(newLiteral.getRelation())==1)		
			if (literal.getRelation().isFunction() && literal.getFirstArgument()==newLiteral.getFirstArgument()) {
				logger.log(Level.DEBUG, "New Literal violates symmetric and functional property of relation");
				return true; // avoid marriedTo on marriedTo
			}
				

		
 		// forbid livesIn(x,y):-livesIn(x,z)
 		// allow locatedIn(x,y):-locatedIn(x,z)  for transitivity
		if (newLiteral.getRelation().equals(head.getHeadRelation())) {
			if (newLiteral.getFirstArgument()==65 && newLiteral.getRelation().getRelationsForm()==2 && (inputArg==1 || inputArg==2)) {
				logger.log(Level.DEBUG, "Eliminated: New Literal of the kind rel(x,y):-rel(x,z) with different domain and range (no transitivity possible)");
				return true;// avoid livesIn(65,66) and livesIn(65,67)
			}
			//if (newLiteral.getFirstArgument()==66 && newLiteral.getRelation().getRelationsForm()==2 && inputArg==2) {
			//	logger.log(Level.DEBUG, "New Literal violates symmetric and functional property of relation");
			//	return true;// avoid livesIn(65,66) and livesIn(65,67)
			//}				
		}
		
		// new Literal is a copy of the head
		if (newLiteral.equals(new Literal(head.getHeadRelation(),65,66))) {
			logger.log(Level.DEBUG, "Eliminated: New Literal is a copy of the head");
			return true;
		}

		// new Literal is short-circuited
		if(newLiteral.getFirstArgument()==newLiteral.getSecondArgument()) {
			logger.log(Level.DEBUG, "Eliminated: New Literal short-circuited");
			return true;
		}
	
		return false;
	}
	
	private boolean dontConsiderRelation(Rule oldRule, Literal newLiteral, int d) {
		
		for (int i=0, len=oldRule.getBodyLiterals().size();i<len;i++) {
			Literal ilit = oldRule.getBodyLiterals().get(i);
			Relation irel = ilit.getRelation();
			
			if (irel.isAuxiliary()) {
				if (irel.equals(RelationsInfo.EQ)) {
					// If the any variable of the new literal is set to a constant by EQ relation
					if (ilit.getFirstArgument() == newLiteral.getFirstArgument())
						if (newLiteral.getRelation().getDomain().isChildOf(ilit.getConstant())) 
							continue;
						else {
							logger.log(Level.DEBUG, "Eliminated: New Literal's first argument is already fixed to a given type that doesn't match");
							return true;
						}
					if (ilit.getFirstArgument() == newLiteral.getSecondArgument() )
						if (newLiteral.getRelation().getRange().isChildOf(ilit.getConstant())) {
							logger.log(Level.DEBUG, "Eliminated: New Literal's seconsd argument is already fixed to a given type that doesn't match");
							continue;
						}
						else
							return true;			
				}
			}
			
			// I have the same literal twice
			if (ilit.equals(newLiteral)) {
				logger.log(Level.DEBUG, "Eliminated: New Literal already exists in the body");
				return true;
			}

			// I have same relation again in the body and...
			if (irel.equals(newLiteral.getRelation())) {
				// the functional property of the relation is violated
				if(irel.isFunction() && ilit.getFirstArgument()==newLiteral.getFirstArgument()) {
					logger.log(Level.DEBUG, "Eliminated: New Literal has a functional relation that already exists in body with different domain");
					return true;// avoid bornIn(65,66)bornIn(65,67)
				}
				if (oldRule.hasFreeVariables()) {
					// keep the new variables of the same relation always at the same side
					// avoid bornIn(65,67)bornIn(68,67)
					if (newLiteral.getFirstMode()==0 && ilit.getFirstMode()==1) {
						logger.log(Level.DEBUG, "Eliminated: New literal's relation already existent and with free variable at different argument");
						return true;
					}
					if (newLiteral.getSecondMode()==0 && ilit.getSecondMode()==1) {
						logger.log(Level.DEBUG, "Eliminated: New literal's relation already existent and with free variable at different argument");
						return true;
					}
				}				
			}	
		}
		return false;
	}
	
	private boolean dontConsiderRelation(Rule newRule, int d)
	{
		// I am in last step and I still have not binded the head's variables
		if (d==depth && !newRule.bindsHeadVariables()) {
			logger.log(Level.DEBUG, "Eliminated: New rule with unbinded variables at last step");
			return true;
		}
		
		if (!allowFreeVars) {
			// number of free variables in the body exceed the possible future bindings
			if (newRule.hasFreeVariables() && newRule.getNumOfFreeVariables() > ((depth-d)*2)) {
				logger.log(Level.DEBUG, "Eliminated: Number of free variables in the body exceed the possible future bindings");
				return true;
			}
			if (newRule.bindsHeadVariables() && newRule.hasFreeVariables() && newRule.getNumOfFreeVariables() >= ((depth-d)*2)) {
				logger.log(Level.DEBUG, "Eliminated: Number of free variables in the body exceed the possible future bindings");
				return true;
			}

		}
		
		// I have already considered this rule (or an equivalent one) before
		if (rulesCheckedMap.get(d)!=null) {
			if (rulesCheckedMap.get(d).contains(newRule)) {
				logger.log(Level.DEBUG, "Eliminated: Rule was already considered before");
				return true;
			}
			
			//for (int i=0,len=rulesCheckedMap.get(d).size();i<len;i++) {
			//	if (rulesCheckedMap.get(d).get(i).equals(newRule))
			//		return true;
			//}
		}
		return false;
	}
	
	
	//*************************** METHODS FOR POST PROCESSING **************************
	/**
	 * Checks for subsumption and high overlap between rules.
	 * @throws SQLException 
	 */
	private void postProcess(RuleTreeNode father) throws SQLException {
		
		if (father.getChildren()!=null) {
			ArrayList<RuleTreeNode> sameLevel = father.getChildren();
			RuleTreeNode node1;
			for (int i=0;i<sameLevel.size();i++) {
				node1=sameLevel.get(i);			
				auxiliaryForPostProcess(node1, sameLevel, i);
				// add also next level
				if (node1.getChildren()!=null) {
					postProcess(node1);				
					for (int j=0;j<node1.getChildren().size();j++) {
						auxiliaryForPostProcess(node1.getChildren().get(j), sameLevel, i);
					}			
				}
			}
			
		}
	}
	private void auxiliaryForPostProcess(RuleTreeNode node1, ArrayList<RuleTreeNode> sameLevel, int i) throws SQLException {
		
		RuleTreeNode node2;
		
		if (node1.getRule().isGood() && !node1.getRule().isTooGeneral()) {	
			for (int j=i+1;j<sameLevel.size();j++) {
				node2=sameLevel.get(j);			
				checkOverlap(node1,node2);					
			}				
		}
	}
	private void checkOverlap(RuleTreeNode node1, RuleTreeNode node2) throws SQLException
	{	
		Rule rule2=node2.getRule();
		Rule rule1=node1.getRule();	
		boolean goodRule=false;	
		int overlap=0;
		ArrayList<Rule> array=new ArrayList<Rule>();

		if (rule2.isGood() && !rule2.isTooGeneral()) {
			overlap=queryHandler.calcOverlap(rule1,rule2);
			goodRule=true;
		}
		
		// check for subsumption
		if (overlap==rule2.getBodySize()) { // rule2 subset of rule1=> all children of rule2 are subset of rule1 
			if (rule1.getConfidence()>rule2.getConfidence()) { // eliminate rule2
				rule2.setIsGood(false);
				logger.log(Level.INFO,"Eliminate Rule: " + rule2.getRuleString() + " because of Rule: " + rule1.getRuleString());
			}
			node2.getMoreSpecialRules(array);

			for (int j=0;j<array.size();j++){
				if (rule1.getConfidence()>array.get(j).getConfidence()) {
					array.get(j).setIsGood(false);
					logger.log(Level.INFO,"Eliminate Rule: " + array.get(j).getRuleString() + " because of Rule: " + rule1.getRuleString());
				}
			}
		}
		else if (overlap==rule1.getBodySize()) { // rule1 subset of rule2
			if (rule1.getConfidence()<rule2.getConfidence())  {
				rule1.setIsGood(false);
				logger.log(Level.INFO,"Eliminate Rule: " + rule1.getRuleString() + " because of Rule: " + rule2.getRuleString());			
			}
			node1.getMoreSpecialRules(array);

			for (int j=0;j<array.size();j++) {
				if (rule2.getConfidence()>array.get(j).getConfidence())  {
					array.get(j).setIsGood(false);
					logger.log(Level.INFO,"Eliminate Rule: " + array.get(j).getRuleString() + " because of Rule: " + rule2.getRuleString());					
				}
			}
		}
		else if(!(overlap==0 && goodRule)) {// no overlap at all=> children of rule1 and rule2 also have no overlap
			// else there is some overlap check overlap thersholds	
			checkNaiveOverlap(rule1,rule2,overlap);

			if (node2.getChildren()!=null ) {
				for (int j=0;j<node2.getChildren().size();j++) {
					checkOverlap(node1, node2.getChildren().get(j));
				}

			}
		}	
	}
	
	private void checkNaiveOverlap(Rule rule1,Rule rule2,int overlap) {
		float percent1,percent2;
		percent1=((float)overlap)/(float)rule1.getBodySize();
		percent2=((float)overlap)/(float)rule2.getBodySize();
		
		if (percent1>percent2) {
			// if the body with overlap above threshold has lower confidence then prune it
			if (percent1>tChecker.getUpperOverlapThreshold()){
				if (rule1.getConfidence()<rule2.getConfidence()){
					rule1.setIsGood(false);
					logger.log(Level.INFO,"Eliminate Rule: " + rule1.getRuleString() + " because of Rule: " + rule2.getRuleString());
				}
				
			}
			if (rule1.isGood() && percent2>tChecker.getUpperOverlapThreshold()) {
				if (rule1.getConfidence()>rule2.getConfidence()) {
					rule2.setIsGood(false);			
					logger.log(Level.INFO,"Eliminate Rule: " + rule2.getRuleString() + " because of Rule: "  + rule1.getRuleString());
				}
			}
		}
		else {
			if (percent2>tChecker.getUpperOverlapThreshold()) {
				if (rule1.getConfidence()>rule2.getConfidence()) {
					rule2.setIsGood(false);
					logger.log(Level.INFO,"Eliminate Rule: " + rule2.getRuleString() + " because of Rule: " + rule1.getRuleString());
				}
			}
			if (rule2.isGood() && percent1>tChecker.getUpperOverlapThreshold()) {
				if (rule1.getConfidence()<rule2.getConfidence()) {
					rule1.setIsGood(false);					
					logger.log(Level.INFO,"Eliminate Rule: " + rule1.getRuleString() + " because of Rule: " + rule2.getRuleString());
				}
				
			}
		}
		
	}
	//*************************** PREPROCESSING ***************************
	private void preprocessing() throws SQLException
	{
		FactsForHead = queryHandler.fireSampleQuery(head.getHeadRelation());	
		findConstantsForTypes();			
	}
	
	private void findConstantsForTypes() throws SQLException {
		if (inputArg!=2) {
			typeConstantsForArg1 = queryHandler.getTypeConstants(head.getHeadRelation().getName(), numOfTries, 1);
		}
		if (inputArg!=1) {
			typeConstantsForArg2 = queryHandler.getTypeConstants(head.getHeadRelation().getName(), numOfTries, 2);
		}		
	}


	//*************************** GETTERS *****************************
	public ArrayList<Rule> getLearnedRules() {
		
		boolean onlyGood=true;
		boolean onlyGenerals=false;	
		ArrayList<Rule> usefulRules=new ArrayList<Rule>();
		
		rulesTree.getRulesOfTree(usefulRules,onlyGood, onlyGenerals, allowFreeVars);	
		
		return usefulRules;
	}
	
	public RuleTreeNode getRuleNode() {
		return this.rulesTree;
	}
}

