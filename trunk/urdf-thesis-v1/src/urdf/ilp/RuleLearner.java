package urdf.ilp;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.bcel.generic.NEW;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import urdf.rdf3x.ResultSet;



/**
 * 	@author Christina Teflioudi
 *	This class tries to learn rules for given predicates  
 */
public class RuleLearner {
	
	private static Logger logger = Logger.getLogger(LearningManager.loggerName);
	
	QueryHandler queryHandler;
	ThresholdChecker tChecker;
	RelationsInfo info;
	
	boolean joinOnLiteral = false;
	
	HeadPredicate head;
	int FactsForHead;
	int depth;
	int inputArg;
	int beamWidth; // if 0 do exhaustive search - no search heuristic 
	
	HashMap<Integer, HashSet<BodyPredicate>> allowedRelations = new HashMap<Integer, HashSet<BodyPredicate>>();	
	HashMap<Integer,HashSet<Rule>> rulesCheckedMap;
	RuleTreeNode rulesTree;	
	
	boolean allowFreeVars = false;		//allow final rules with free variables
	boolean tryConstants = false;		//allow rules with constants
	int gainMeasure = 0; 	// 0: accuracy gain 1: weighted accuracy gain 2: Information gain 3: weighted information gain
	int numOfTries = 20;	// how many constants I am going to try for the type relation
	String[] typeConstantsForArg1;
	String[] typeConstantsForArg2;
	
	RuleWithNumericLiteralLearner numConstLearner = null;
	
	private HashMap<String,Integer> headSizesWithConstants = new HashMap<String, Integer>();
	
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
		
		numConstLearner = new RuleWithNumericLiteralLearner(queryHandler, tChecker, info);
		numConstLearner.setNumberOfBuckets(10);
	}


	public void learnRule(HeadPredicate head, int depth) throws Exception { 
		
		Rule rule;
		this.head = head;
		this.allowedRelations = head.getCandidateBodyRelations();
		this.depth = depth;
		this.inputArg = head.getInputArg();
		System.out.println("Inputarg="+inputArg);
		
		rulesCheckedMap = new HashMap<Integer,HashSet<Rule>>();	
		
		preprocessing();		
			
		// first add an empty rule
		rule=new Rule(new Literal(head.getHeadRelation(),65,66), inputArg);
		rule.setHeadSize(FactsForHead);
		if (head.getHeadRelation().getRelationsForm()==1) {
			rule.addLiteral(new Literal(RelationsInfo.NEQ,65,66));
		}
		
		rulesTree=new RuleTreeNode(rule,null,0);
		
		extendRule(rulesTree,1);
		
		//postProcess(rulesTree);

		logger.log(Level.DEBUG,"Positive examples for the head in total: "+FactsForHead);
	
	}
	
	private void extendRule(RuleTreeNode node, int d) throws Exception  {
		
		logger.log(Level.INFO,"Extending rule: " + node.getRule().getRuleString() + " with d=" + d);
		
		if (node.getRule().getConfidence() < tChecker.getStoppingThreshold()) {
			
			//if (node.getRule().getNumOfFreeVariables()<(depth-d+1)) {
			//	refinePredicate(node,5,d,-1);//arg1joinOnArg1 and arg2joinOnArg2
			//	refinePredicate(node,6,d,-1);//arg2JoinOnArg1 and arg2joinOnArg1
			//}
			
			// first try to refine the head
			if (inputArg==1) {		
				refinePredicate(node,1,d,-1);//arg1JoinOnArg1
				refinePredicate(node,2,d,-1);//arg1JoinOnArg2
			}
			if (inputArg==2) {				
				refinePredicate(node,3,d,-1);//arg2JoinOnArg1
				refinePredicate(node,4,d,-1);//arg2JoinOnArg2
			}
			/*if (inputArg==0) {				
				refinePredicate(node,1,d,-1);//arg1JoinOnArg1
				refinePredicate(node,2,d,-1);//arg1JoinOnArg2
				refinePredicate(node,3,d,-1);//arg2JoinOnArg1
				refinePredicate(node,4,d,-1);//arg2JoinOnArg2
			}*/
			

			// second try to refine the predicates in the body
			for (int i=0, len=node.getRule().getBodyLiterals().size();i<len;i++) {
				// do not refine auxiliary relations 
				if (node.getRule().getBodyLiterals().get(i).getRelation().isAuxiliary())
					continue;
				
				if (d>2) {
					// TODO Try to do internal Joins with no New Variable:
					//refinePredicate(node,7,d,i); //arg1JoinOnArg1 with given literal, arg2 with smth else in body
					//refinePredicate(node,8,d,i); //arg1JoinOnArg2 with given literal, arg2 with smth else in body
					//refinePredicate(node,9,d,i); //arg2JoinOnArg1 with given literal, arg1 with smth else in body
					//refinePredicate(node,10,d,i);//arg2JoinOnArg2 with given literal, arg1 with smth else in body
					
				}
				
				if (node.getRule().getNumOfFreeVariables()<(depth-d+1)) {
					refinePredicate(node,5,d,i);//arg1joinOnArg1 and arg2joinOnArg2
					refinePredicate(node,6,d,i);//arg2JoinOnArg1 and arg2joinOnArg1
				}
					
				refinePredicate(node,1,d,i);//arg1JoinOnArg1
				refinePredicate(node,2,d,i);//arg1JoinOnArg2
				refinePredicate(node,3,d,i);//arg2JoinOnArg1
				refinePredicate(node,4,d,i);//arg2JoinOnArg2
			}

			if(node.getChildren()!=null && node.getChildren().size()>0) {
				// if beam search on, compute gains for each child. Only the children with the highest gain will survive 
				if (beamWidth > 0 && d > 1) {
					eliminateChildrenOutOfBeam(node);
				}

				if (d < depth) {
					// Try to combine different numeric constants added in last extension
					combineNumericConstants(node,d+1);
					// Extend rule
					for (int i=0,len=node.getChildren().size(); i<len; i++) {
						extendRule(node.getChildren().get(i),d+1);
					}
				}

			}
		}		
	}
	
	private void combineNumericConstants(RuleTreeNode node, int depth) throws Exception {
		
		logger.log(Level.INFO,"Combining numeric constant for multivariable regression");
		
		int numChildren = node.getChildren().size();
		RuleTreeNode childA = null, childB = null;
		for (int i=0; i<numChildren; i++) {
			childA = node.getChild(i);
			if (childA.getRule().hasNumericConstant()) {
				for (int j=i+i; j<numChildren; j++) {
					childB = node.getChild(j);
					if (childB.getRule().hasNumericConstant()) {
						logger.log(Level.INFO,"Combining numeric constant from:\n\t\t"+childA.getRule().getRuleString()+"\n\t\t"+childB.getRule().getRuleString());
						numConstLearner.evaluateRule2D(childA,childB);
					}
				}
			}
		}
	}
	
	private void refinePredicate(RuleTreeNode node, int joinCase, int d, int position) throws Exception {
		Rule rule = node.getRule();
		Literal literal=(position==-1?rule.getHead():rule.getBodyLiterals().get(position));
		
		logger.log(Level.DEBUG,"Refining predicate from rule (joinCase="+joinCase+" at "+literal.getRelationName()+"): " + rule.getRuleString());
		System.out.println("Refining predicate from rule (joinCase="+joinCase+" at "+literal.getRelationName()+"): " + rule.getRuleString());
		if (node.getParent()!=null) System.out.println(" **** " + node.getParent().getRule().getRuleString());

		Rule newRule=null;
		Rule freeRule=null;
		Rule bindRule=null;
		
		int arg1 = 0;
		int arg2 = 0;
		int mode1 = 0;
		int mode2 = 0;
		int freeVar;
		
		HashSet<Relation> candidateRelations;
		Literal newLiteral = null;
		Literal freeLiteral = null;
		
		boolean canCloseConnection=false;
		boolean ruleStored=false;
		
		boolean[] out;
		
		if (!joinOnLiteral && literal.getRelation().isRangeLiteral() && (joinCase==3 || joinCase==4)) { 
			logger.log(Level.DEBUG, "Tried to join new rule relation on a literal argument from "+literal.getRelationName());
			return;
		}
		
		if (allowFreeVars) 
			candidateRelations = getJoinableRelations(joinCase, literal.getRelation()); // no restriction at all
		else
			candidateRelations = getRelationsFromAllowedRelations(d,joinCase, literal.getRelation()); // restriction according to depth
		
		
		switch(joinCase) {
			case 1:				
				arg1 = literal.getFirstArgument();
				mode1 = 1;
				arg2 = rule.getNextVariableNumber();		
				freeVar = 2;
				if (literal.getRelation().getDomain().equalsOrChildOf(info.getTypeFromTypes("<http://yago-knowledge.org/resource/wordnet_location_100027167>"))) {
					if (candidateRelations==null) candidateRelations = new HashSet<Relation>();
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasGDP>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasUnemployment>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasEconomicGrowth>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasHDI>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPoverty>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPopulation>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasInflation>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasArea>"));
				}
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
				if (literal.getRelation().getRange().equalsOrChildOf(info.getTypeFromTypes("<http://yago-knowledge.org/resource/wordnet_location_100027167>"))) {
					if (candidateRelations==null) candidateRelations = new HashSet<Relation>();
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasGDP>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasUnemployment>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasEconomicGrowth>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasHDI>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPoverty>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPopulation>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasInflation>"));
					candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasArea>"));
				}
				break;
			case 4:			
				arg2 = literal.getSecondArgument();
				mode2 = 1;
				arg1 = rule.getNextVariableNumber();
				freeVar = 1;
				break;
			case 5:
				arg1 = literal.getFirstArgument();
				mode1 = mode2 = 1;
				arg2 = literal.getSecondArgument();		
				freeVar = 0;
				//System.out.println("===========Case5("+(char)arg1+","+(char)arg2+")");
				break;	
			case 6:
				arg1 = literal.getSecondArgument();
				mode1 = mode2 = 1;
				arg2 = literal.getFirstArgument();		
				freeVar = 0;
				//System.out.println("===========Case6("+(char)arg1+","+(char)arg2+")");
				break;	
			default: throw new IllegalArgumentException("Join case should be 1,2,3 or 4. It's " + joinCase + "instead.");
		}			
		
		if (candidateRelations!=null && candidateRelations.size()>0) {
			int i=0;
			for (Relation iCandidate: candidateRelations) {
				++i;
				logger.log(Level.DEBUG, i+"th-CandidateRelation = "+iCandidate.getName());
		
				canCloseConnection=false;
				ruleStored=false;	
				freeRule=null;
				bindRule=null;
				
				//if (iCandidate.getName().equals("hasUTCOffset") || iCandidate.getName().equals("bornOnDate") || iCandidate.getName().equals("isOfGenre")) {
				//	continue;
				//}
				if (iCandidate.equals(literal.getRelation()) && (joinCase==5 || joinCase==6)) {
					logger.log(Level.DEBUG, "Trying to join both args on same relation: "+iCandidate.getName());
					continue;
				}
				
				if (iCandidate.isRangeLiteral() && (joinCase==2 || joinCase==4 || joinCase==5 || joinCase==6)) {
					logger.log(Level.DEBUG, "Trying to join on literal from relation: "+iCandidate.getName());
					continue;
				}
				
				if (iCandidate.isRangeLiteral()) System.out.println("====== Candidate with Literal (joinCase="+joinCase+"): "+ iCandidate.getSimpleName() + "  MIN="+iCandidate.getMinValue()+" MAX="+iCandidate.getMaxValue());
				
	
				
				// STEP 1: WITH FREE VARIABLE	
				newRule = rule.clone();
				newLiteral = new Literal(iCandidate,arg1,mode1,arg2,mode2,freeVar);		
				
				out = considerElimination(newRule,rule,newLiteral,literal,d,position);
				
				// +++++++++++++++++++++++++++
				// If candidate relation has numerical range, try to find best ranges
				if (!out[0] && newRule!=null && newRule.bindsHeadVariables() && iCandidate.isRangeLiteral() && iCandidate.getMaxValue()!=Float.NaN && iCandidate.getMinValue()!=Float.NaN) {
					if (joinCase==1 || joinCase==3) {
						System.out.println("============Testing literal ranging"+newRule.getRuleString());
						// Checks whether first argument is set to constant (Is contained in a EQ literal)
						boolean extend = true;
						for (Literal l: newRule.getBodyLiterals()) {
							if (l.getRelation().equals(info.EQ)  && arg1==l.getFirstArgument()) {
								extend = false;
								break;
							}
						}
						if (extend) {
							newLiteral.setFreeVariable(0);
							//checkExtendRuleWithLiteralRanging(node,newRule,arg2,d);
							numConstLearner.evaluateRule(node,newRule, arg2);
						}

					} 
					continue;	
				}
				// ----------------------------
				
				// +++++++++++++++++++++++++++
				// Try to add constants to non-head variables
				if (!out[0] && newRule!=null && newRule.bindsHeadVariables() && !iCandidate.isRangeLiteral())
					checkConstantsAtBody(node, newRule, d);
				// ----------------------------
				
				
				if (out[0]) {
					continue;
				}
				else if(!out[1]) {
					if (evaluateRule(node,newRule,d)) {
						ruleStored = true;
						freeRule = newRule;
						if (newRule.bindsHeadVariables() && newRule.isTooGeneral()&& newRule.isGood()) {
							checkExtendRuleWithTypes(node, newRule, d);
						}
						if (newRule.bindsHeadVariables() && tryConstants &&!newRule.isTooGeneral() && head.getHeadRelation().getConstantInArg()!=0 && inputArg!=0) {
							checkExtendRuleWithConstants(node,newRule,d);
						}
					}
				}
				
				// STEP 2: WITH BOUND VARIABLES
				if(inputArg==2) {
					if ((joinCase==1 || joinCase==3) && info.arg1JoinOnArg2.get(head.getHeadRelation())!=null &&
							info.arg1JoinOnArg2.get(head.getHeadRelation()).contains(iCandidate))
					{
						// form the new Literal
						newLiteral=new Literal(iCandidate,arg1,65);
						freeLiteral=new Literal(RelationsInfo.NEQ,arg2,65);
						canCloseConnection=true;
					}
					else if ((joinCase==2 || joinCase==4)&& info.arg1JoinOnArg1.get(head.getHeadRelation())!=null &&
							info.arg1JoinOnArg1.get(head.getHeadRelation()).contains(iCandidate))
					{
						// form the new Literal
						newLiteral=new Literal(iCandidate,65,arg2);
						freeLiteral=new Literal(RelationsInfo.NEQ,arg1,65);
						canCloseConnection=true;
					}
				}
				else {
					if ((joinCase==1 || joinCase==3) && info.arg2JoinOnArg2.get(head.getHeadRelation())!=null &&
						 info.arg2JoinOnArg2.get(head.getHeadRelation()).contains(iCandidate))
					{
						// form the new Literal
						newLiteral=new Literal(iCandidate,arg1,66);
						freeLiteral=new Literal(RelationsInfo.NEQ,arg2,66);
						canCloseConnection=true;
					}
					else if ((joinCase==2 || joinCase==4)&& info.arg2JoinOnArg1.get(head.getHeadRelation())!=null &&
							  info.arg2JoinOnArg1.get(head.getHeadRelation()).contains(iCandidate))
					{
						// form the new Literal
						newLiteral=new Literal(iCandidate,66,arg2);
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
							if (newRule.bindsHeadVariables() && tryConstants && !newRule.isTooGeneral() && head.getHeadRelation().getConstantInArg()!=0 && inputArg!=0)
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
							if (newRule.bindsHeadVariables() && tryConstants &&!newRule.isTooGeneral() && head.getHeadRelation().getConstantInArg()!=0 && inputArg!=0)
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
	private boolean[] considerElimination(Rule newRule,Rule oldRule, Literal newLiteral, Literal literal, int d, int position) throws Exception {
		
		logger.log(Level.DEBUG,"Considering elimintaion Rule: " +  newRule.getRuleString() + " NewLiteral: " + newLiteral.getSparqlPattern());
		
		boolean[] out=new boolean[2];
		
		
		if (dontConsiderRelation(literal,newLiteral, d) || dontConsiderRelation(oldRule,newLiteral,d)) {
			out[0]=true;
			out[1]=true;
			return out;
		}
		
		newRule.addLiteral(newLiteral,position);
		// take care of non equalities
		checkToAddAuxiliaryRelation(newRule);
						
		if (!(/*dontConsiderRelation(oldRule,newLiteral,d) || */dontConsiderRelation(newRule, d))) {
			out[0]=false;
			out[1]=false;
		}		
		else {
			out[0]=false;
			out[1]=true;			
		}
		
		if (out[0]==true) logger.log(Level.DEBUG, "Rule shouldn't be considered at all");
		if (out[1]==true) logger.log(Level.DEBUG, "Rule shoudld be skipped but but considered for further change on argumets");

		return out;
	}
	/**
	 * 	checks if the rule should be stored as a child of the node
	 * 	if yes it stores the rule and returns true
	 * @throws Exception 
	 */
 	private boolean evaluateRule(RuleTreeNode node,Rule rule, int d) throws Exception{
		boolean flag = false;
		
		logger.log(Level.DEBUG,"Evaluating Rule: "+rule.getRuleString());
		
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
 	
	private boolean evaluateRule(RuleTreeNode node,Rule rule, int d,Rule freeRule, Rule bindRule) throws Exception {
		
		boolean flag=false;
		
		logger.log(Level.DEBUG,"Evaluating Rule: " + rule.getRuleString() + "With FreeRule: " + freeRule.getRuleString() + "With BindRule: " + bindRule.getRuleString());
		
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
 	private HashSet<Relation> getJoinableRelations(int joinCase, Relation relation) {
		switch(joinCase) {
			case 1: return info.arg1JoinOnArg1.get(relation); 			
			case 2: return info.arg1JoinOnArg2.get(relation); 
			case 3: return info.arg2JoinOnArg1.get(relation); 
			case 4: return info.arg2JoinOnArg2.get(relation); 
			case 5: return intersectSets(info.arg1JoinOnArg1.get(relation), info.arg2JoinOnArg2.get(relation));
			case 6: return intersectSets(info.arg1JoinOnArg2.get(relation), info.arg2JoinOnArg1.get(relation));
			default: throw new IllegalArgumentException("Join case should be a number between 1 and 4");
		}
	}
	private HashSet<Relation> getRelationsFromAllowedRelations(int d,int joinCase, Relation relation){

		if (allowedRelations.get(d-1)==null)
			return null;
		
		for (BodyPredicate bp : allowedRelations.get(d-1)) {
			if (bp.hasRelation(relation)) {
				switch(joinCase) {
					case 1: return bp.arg1JoinOnArg1;						
					case 2: return bp.arg1JoinOnArg2;
					case 3: return bp.arg2JoinOnArg1;
					case 4: return bp.arg2JoinOnArg2;
					case 5: case 6: return getJoinableRelations(joinCase, relation);
				}
			}
		}
		
		return null;
	}
	
	private void checkExtendRuleWithTypes(RuleTreeNode node, Rule rule, int d) throws Exception
	{
		logger.log(Level.INFO, "Extending rule with type, rule = "+rule.getRuleString());
		
		Rule newRule, newRule2;
		Literal lit;
		int var;
		Relation typeRelation = info.getRelationFromRelations("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>");
		
		if (inputArg!=2) {// 1 or 0 
			
			for (int i=0;i<numOfTries;i++) {
				newRule = rule.clone();
				var = newRule.getNextVariableNumber();
				lit = new Literal(typeRelation,65,1,var,0,2);
				newRule.addLiteral(lit,0);	
				lit = new Literal(RelationsInfo.EQ,var,1,-1,0,typeConstantsForArg1[i]);
				newRule.addLiteral(lit);				
				
				if (inputArg==0) {
					for (int j=0;j<numOfTries;j++) {
						newRule2 = newRule.clone();
						var = newRule.getNextVariableNumber();
						lit = new Literal(typeRelation,66,1,var,0,2);	
						newRule2.addLiteral(lit);				
						lit = new Literal(RelationsInfo.EQ,var,1,-1,0,typeConstantsForArg2[i]);
						newRule2.addLiteral(lit);
						
						// evaluate rule
						newRule2.setHasFreeVariables(false);
						evaluateRule(node,newRule2, d);
						
						if (newRule2.bindsHeadVariables() && tryConstants && head.getHeadRelation().getConstantInArg()!=0 && inputArg!=0)
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
				lit=new Literal(typeRelation,66,1,var,0,2);
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
	private void checkConstantsAtBody(RuleTreeNode node, Rule rule, int d) {
		// Tries to apply constants no non-head variable
		
		int lastVar = rule.getNextVariableNumber()-1;
		int position = Math.max(-1, rule.getBodySize()-1);
		
		// If body has any variable inexistent in the Head
		if (lastVar > 66 && rule.getConstantInArg()==0) { 
			HashSet<Integer> candidateVariables = new HashSet<Integer>();
			// First add all variables (apart from head's)
			for (Literal lit: rule.getBodyLiterals()) {
				Relation rel = lit.getRelation();
				if (!rel.isAuxiliary() && !rel.isRangeLiteral()) {
					if (lit.getFreeVariable()==1)
						candidateVariables.add(new Integer(lit.getFirstArgument()));
					if (lit.getFreeVariable()==2)
						candidateVariables.add(new Integer(lit.getSecondArgument()));
				}
			}
			
			System.out.println("..........CandidateVariables from: "+rule.getRuleString());
			for (Integer i: candidateVariables) System.out.println((char)i.intValue());
			
			
			float minAcc = tChecker.confidenceThreshold;
			float minPos = Math.max(tChecker.positivesCoveredThreshold, tChecker.supportThreshold*rule.getHeadSize());
			for (Integer i: candidateVariables) {
				try {
					ResultSet rs = queryHandler.retriveLiteralDistribution(rule, i.intValue());
					String constant = null;
					int match,pos=0,tot=0;
					float acc;
					while (rs.next()) {
						match = rs.getInt(2);
						// In the result set, match=1 comes always before match=0
						if (match==1) {
							pos = rs.getInt(3);				
							if (pos >= minPos) {
								constant = rs.getString(1);
								System.out.println((char)i.intValue()+"="+constant+" has "+pos+" positives..");
							}
						} else {
							if (constant!=null) { // If any match=1 already found before, it's expected that next constant is same with match=0, otherwise bodysize==positives -> acc=1
								if (constant.equals(rs.getString(1))) {								
									tot = pos+rs.getInt(3);
									//System.out.println((char)i.intValue()+"="+constant+" has "+tot+" bodysize..");
									acc = ((float)pos)/((float)tot);
									//System.out.println((char)i.intValue()+"="+constant+" has "+acc+" accuracy..");		
								} else { // bodysize==positives -> acc=1
									tot = pos;
									acc = 1;
								}
								if (acc>=minAcc) {
									System.out.println(rule.getRuleString()+" "+(char)i.intValue()+"="+constant+"   (acc="+acc+"="+pos+"/"+tot+")");
									Literal newLit = new Literal(info.EQ, i.intValue(), 1, -1, 0, constant);
									Rule newRule = rule.clone();
									newRule.addLiteral(newLit,position);
									newRule.setBodySize(tot);
									newRule.setPositivesCovered(pos);
									newRule.setConfidence(acc);
									newRule.setSupport((float)pos/(float)newRule.getHeadSize());			
									checkGainFromBestAncestor(node, newRule);
									newRule.setIsGood(true);
									newRule.setIsTooGeneral(false);
									System.out.println(newRule.printRule(false));
									node.addChild(new RuleTreeNode(newRule, node, d));
									System.out.println("Number of children from parent: "+node.getChildren().size());
									RuleTreeNode r = node;
									while (r!=null) {										
										System.out.println("Parent: "+node.getRule().printRule(false));
										r = r.getParent();
									}
								}
								constant = null;
							} 
						}
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	private void checkExtendRuleWithConstants(RuleTreeNode node, Rule rule, int d) throws Exception {
		Rule newRule;
		Literal lit;
		int var;
		int arg = head.getHeadRelation().getConstantInArg();
		switch(arg) {
			case 1: var=65; break;
			case 2: var=66; break;
			default: throw new IllegalArgumentException("TO DO! how to implement case 0?");
		}
		// first get the constants if any
		ArrayList<String> consts = queryHandler.findConstants(rule, tChecker.getPossiblePosToBeCoveredThreshold(), tChecker.getPositivesCoveredThreshold(), tChecker.getSupportThreshold(), FactsForHead, inputArg, numOfTries);
		
		for (int i=0, len=consts.size();i<len;i++) {
					
			newRule = rule.clone();
			newRule.setConstantInArg(arg);
			newRule.setConstant(consts.get(i));
		
			lit = new Literal(RelationsInfo.EQ,var,1,-1,0,consts.get(i));
			newRule.addLiteral(lit);
			newRule.setHasFreeVariables(false);
			
			Integer prevCalcHeadSize = headSizesWithConstants.get(consts.get(i).intern());
			if (rule.getHeadSize()<0 && prevCalcHeadSize!=null && prevCalcHeadSize>0) 
				rule.setHeadSize(prevCalcHeadSize);
			
			evaluateRule(node,newRule,d);
			
			if (newRule.getHeadSize()>0)
				headSizesWithConstants.put(newRule.getConstant().intern(), newRule.getHeadSize());
		}		
	}
	
	private void checkExtendRuleWithLiteralRanging(RuleTreeNode node, Rule rule, int literalArg, int d) throws Exception {
		
		logger.log(Level.INFO, "!!!!!!!!!!!! - Checking ranging of rule with Literal: "+rule.getRuleString());
		
		ResultSet rs = queryHandler.retriveLiteralDistribution(rule, literalArg);
	
		float minAcc = tChecker.confidenceThreshold;
		float minPos = Math.max(tChecker.positivesCoveredThreshold, tChecker.supportThreshold*rule.getHeadSize());
		minAcc/=2;
		minPos/=2;
		float first = Float.NaN;
		float  last = Float.NaN;
		float  curr = Float.NaN;
		float lastAcc = 0;
		int lastPos = 0, lastTot = 0;
		int currPos=0, currNeg=0, currTot=0;
		int  rowPos=0,  rowNeg=0,  rowTot=0;
		float rowAcc = -1;
		boolean firstRow = true;
		int position = Math.max(-1, rule.getBodySize()-1);
		while (rs.next()) {
			float value = rs.getFloat(1);
			boolean match = rs.getInt(2)==1;
			int count = rs.getInt(3);
			
			System.out.println("first="+first+" last="+last);
			System.out.println("value="+value+"  match="+match+"  count="+count);
			
			// got a new value
			if (curr!=value && !firstRow) {
				rowAcc = ((float) rowPos)/((float) rowTot);
				if (rowAcc >= minAcc) {
					if (Float.isNaN(first)) 
						first = curr;
					else if ((currPos+rowPos)>=minPos/* && (curr-first)>=minRange*/)  {
						last = curr;
						lastAcc = ((float) currPos)/((float) currTot);
						lastPos = currPos+rowPos;
						lastTot = currTot+rowTot;
					}
					
					currPos += rowPos;
					currTot += rowTot;
					currNeg += rowNeg;
					
					
				} else {
					if (!Float.isNaN(first)) {
						int tempPos = currPos+rowPos;
						int tempTot = currTot+rowTot;
						float tempAcc = ((float) tempPos)/((float) tempTot);
						if (tempAcc < minAcc) {
							if (!Float.isNaN(first) && !Float.isNaN(last)) {
								System.out.println("[" + first + ".." + last + "] Acc="+lastAcc);
								Rule newRule = rule.clone();
								Literal gtLit = new Literal(info.GT, literalArg, 1, -1, 0, Float.toString(first));	
								Literal ltLit = new Literal(info.LT, literalArg, 1, -1, 0, Float.toString(last));
								gtLit.setConstNeedsQuotes(true);
								ltLit.setConstNeedsQuotes(true);
								newRule.addLiteral(gtLit,position);					
								newRule.addLiteral(ltLit,position);
								newRule.setBodySize(lastTot);
								newRule.setConfidence(lastAcc);
								newRule.setSupport(((float)lastPos)/((float)newRule.getHeadSize()));
								newRule.setExamplesCovered(lastTot);
								newRule.setPositivesCovered(lastPos);
								checkGainFromBestAncestor(node, newRule);
								newRule.setIsGood(true);
								newRule.setIsTooGeneral(false);
								System.out.println(newRule.getRuleString());
								node.addChild(new RuleTreeNode(newRule, node, d));
							}
							first = last = Float.NaN;
							currPos = currNeg = currTot = 0;
						} else {
							currPos += rowPos;
							currTot += rowTot;
							currNeg += rowNeg;
						}
					}
				}
				
				curr = value;
				rowPos = rowNeg = rowTot = 0;
			} 
			if (firstRow) {
				curr = value;
				firstRow = false;
			}

			if (match) 
				rowPos = count;
			else
				rowNeg = count;
			rowTot += count;
		}
		if (!Float.isNaN(first) && !Float.isNaN(last) && rowAcc>=minAcc && rowPos>=minPos) {
			System.out.println("[" + first + ".." + last + "] Acc="+lastAcc);
			Rule newRule = rule.clone();
			Literal gtLit = new Literal(info.GT, literalArg, 1, -1, 0, Float.toString(first));	
			Literal ltLit = new Literal(info.LT, literalArg, 1, -1, 0, Float.toString(last));
			gtLit.setConstNeedsQuotes(true);
			ltLit.setConstNeedsQuotes(true);
			newRule.addLiteral(gtLit,position);					
			newRule.addLiteral(ltLit,position);
			newRule.setBodySize(lastTot);
			newRule.setConfidence(lastAcc);
			newRule.setSupport(((float)lastPos)/((float)newRule.getHeadSize()));
			newRule.setExamplesCovered(lastTot);
			newRule.setPositivesCovered(lastPos);
			checkGainFromBestAncestor(node, newRule);
			newRule.setIsGood(true);
			newRule.setIsTooGeneral(false);
			System.out.println(newRule.getRuleString());
			node.addChild(new RuleTreeNode(newRule, node, d));
		}
		
	}
	
	private static HashSet<Relation> intersectSets(HashSet<Relation> set1, HashSet<Relation> set2) {
		HashSet<Relation> result = new HashSet<Relation>();
		for (Relation r: set1) {
			if (set2.contains(r))
				result.add(r);
		}
		return result;
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
					if (iLiteral.getFirstArgument() == jLiteral.getFirstArgument() && iLiteral.getSecondArgument() != jLiteral.getSecondArgument()) 
						lit = new Literal(RelationsInfo.NEQ,iLiteral.getSecondArgument(),jLiteral.getSecondArgument());						
					else if (iLiteral.getSecondArgument() == jLiteral.getSecondArgument() && iLiteral.getFirstArgument() != jLiteral.getFirstArgument()) 
						lit = new Literal(RelationsInfo.NEQ,iLiteral.getFirstArgument(),jLiteral.getFirstArgument());						
					if (iLiteral.getRelation().isSymmetric()) { // If symmetric relation, also check arg1arg2 and arg2arg1
						if (iLiteral.getFirstArgument() == jLiteral.getSecondArgument() && iLiteral.getSecondArgument() != jLiteral.getFirstArgument()) 
							lit = new Literal(RelationsInfo.NEQ,iLiteral.getSecondArgument(),jLiteral.getSecondArgument());						
						else if (iLiteral.getSecondArgument() == jLiteral.getFirstArgument() && iLiteral.getFirstArgument() != jLiteral.getSecondArgument()) 
							lit = new Literal(RelationsInfo.NEQ,iLiteral.getFirstArgument(),jLiteral.getFirstArgument());						
						
					}
					if (lit != null) {
						for (Literal ruleLit: rule.getBodyLiterals()) {
							if (ruleLit.equals(lit)) {
								flag=true;
								break;
							}
						}
						if (!flag) {
							rule.addAuxiliaryLiteral(lit);
						}
						break;
					}
				}
				
				j++;
			}
			
		}
		
	}
	
	
	// *************************** METHODS FOR CHECKING **********************************
	
	private boolean checkForSupportAndExactExamples(Rule rule) throws Exception {		
		
		logger.log(Level.DEBUG, "Checking for Support and Exact examples");
		
		if (tChecker.checkSupportThreshold(rule,inputArg)) {			
			if (rule.bindsHeadVariables()) {
				if (!rule.hasFreeVariables() || allowFreeVars) {
					return tChecker.checkPositivesThreshold(rule,inputArg);
				}				
			}				
			return true;			
		}
		return false;
	}
 	
 	/**
 	 * @param rule: some rule which is ancestor of the new rule
 	 * @param newRule
 	 * @return the gain of the new rule in compare with the old one
 	 */
 	private double calcGain(Rule rule, Rule newRule) {
 		logger.log(Level.DEBUG,"Calculating Gain from rule " + newRule.getRuleString() + "=" + newRule.getConfidence() + " in relation to " + rule.getRuleString() + "=" + rule.getConfidence());
 		
 		double gain;
 		switch(gainMeasure){
 			case 0:
 				// Use the accuracy measure: AG(c',c)=A(c')-A(c)=conf(c')-conf(c)
 				gain = newRule.getConfidence()-rule.getConfidence(); 		 		
 				break;
 			case 1:
 				// Use the weighted accuracy measure: WAG(c',c)=(conf(c')-conf(c))* exact(c')/exact(c)
 				gain = (newRule.getConfidence()-rule.getConfidence())* newRule.getPositivesCovered()/rule.getExamplesCovered(); 		 		
 				break; 				
 			case 2:
 				// Use the information gain measure: IG(c',c)=log2(conf(c'))-log2(conf(c))
 				gain = (Math.log(newRule.getConfidence())-Math.log(rule.getConfidence()))/Math.log(2); 		 		
 				break; 				
 			default://3
 				// Use the weighted information gain: WIG(c',c)=(log2(conf(c'))-log2(conf(c)))* exact(c')/exact(c)
 				gain = (Math.log(newRule.getConfidence())-Math.log(rule.getConfidence()))* newRule.getPositivesCovered()/(rule.getExamplesCovered()*Math.log(2)); 		 		
 				
 		}		
 		newRule.setGain(gain);
 		logger.log(Level.DEBUG,"Gain="+gain);
 		return gain;
 	}
 	
 	/**
 	 * @param oldNode: some rule which is ancestor of the new rule
 	 * @param newRule: the rule to be added as child
 	 * 
 	 * 	sets the flag isGood of the newRule to true if it is better than its best ancestor 
 	 */
 	private void checkGainFromBestAncestor(RuleTreeNode oldNode,Rule newRule) {
 		
 		logger.log(Level.DEBUG, "Calculating gain from best ancestor");
 		Rule lastParentRule = oldNode.getBestFinalParentRule();
 		if (lastParentRule!=null) {
 			logger.log(Level.DEBUG, "Best parent rule ="+lastParentRule.getRuleString()+" with conf="+lastParentRule.getConfidence());
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
 

// *************************** METHODS FOR STRUCTURE PRUNING *************************
 	
	private boolean dontConsiderRelation(Literal literal,Literal newLiteral,int d) throws Exception
	{
		// exclude the type relation in the first stage
		if (newLiteral.getRelationName().equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"))
			return true;
	
		// violate symmetric and functional property of relation
		if (literal.getRelation().equals(newLiteral.getRelation())) {// && proc.getRelationsForm(newLiteral.getRelation())==1)		
			if ((literal.getRelation().isFunction() || literal.getRelation().isRangeLiteral()) && literal.getFirstArgument()==newLiteral.getFirstArgument()) {
				logger.log(Level.DEBUG, "New Literal violates symmetric and functional property of relation");
				return true; // avoid marriedTo on marriedTo
			}
		}
				

		
 		// forbid livesIn(x,y):-livesIn(x,z)
 		// allow locatedIn(x,y):-locatedIn(x,z)  for transitivity
		if (newLiteral.getRelation().equals(head.getHeadRelation())) {
			if (newLiteral.getFirstArgument()==65 && newLiteral.getRelation().getRelationsForm()==2 && (inputArg==1 || inputArg==2)) {
				logger.log(Level.DEBUG, "Eliminated: New Literal of the kind rel(x,y):-rel(x,z) with different domain and range (no future transitivity possible)");
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
						if (newLiteral.getRelation().getRange().isChildOf(ilit.getConstant())) 
							continue;
						else {
							logger.log(Level.DEBUG, "Eliminated: New Literal's seconsd argument is already fixed to a given type that doesn't match");
							return true;
						}
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
			for (Rule r : rulesCheckedMap.get(d)) {
				if (r.equals(newRule)) {
					logger.log(Level.DEBUG, "Eliminated: Rule was already considered before");
					return true;
				}
			}
			//if (rulesCheckedMap.get(d).contains(newRule)) {
			//	logger.log(Level.DEBUG, "Eliminated: Rule was already considered before");
			//	return true;
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
		Rule rule2 = node2.getRule();
		Rule rule1 = node1.getRule();	
		boolean goodRule=false;	
		int overlap = 0;
		ArrayList<Rule> array=new ArrayList<Rule>();

		if (rule2.isGood() && !rule2.isTooGeneral()) {
			overlap=queryHandler.calculateOverlap(rule1,rule2);
			goodRule=true;
		}
		
		// check for subsumption
		if (overlap==rule2.getBodySize()) { // rule2 subset of rule1=> all children of rule2 are subset of rule1 
			if (rule1.getConfidence()>rule2.getConfidence()) { // eliminate rule2
				rule2.setIsGood(false);
				logger.log(Level.DEBUG,"Eliminate Rule: " + rule2.getRuleString() + " because of Rule: " + rule1.getRuleString());
			}
			node2.getMoreSpecialRules(array);

			for (int j=0;j<array.size();j++){
				if (rule1.getConfidence()>array.get(j).getConfidence()) {
					array.get(j).setIsGood(false);
					logger.log(Level.DEBUG,"Eliminate Rule: " + array.get(j).getRuleString() + " because of Rule: " + rule1.getRuleString());
				}
			}
		}
		else if (overlap==rule1.getBodySize()) { // rule1 subset of rule2
			if (rule1.getConfidence()<rule2.getConfidence())  {
				rule1.setIsGood(false);
				logger.log(Level.DEBUG,"Eliminate Rule: " + rule1.getRuleString() + " because of Rule: " + rule2.getRuleString());			
			}
			node1.getMoreSpecialRules(array);

			for (int j=0;j<array.size();j++) {
				if (rule2.getConfidence()>array.get(j).getConfidence())  {
					array.get(j).setIsGood(false);
					logger.log(Level.DEBUG,"Eliminate Rule: " + array.get(j).getRuleString() + " because of Rule: " + rule2.getRuleString());					
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
					logger.log(Level.DEBUG,"Eliminate Rule: " + rule1.getRuleString() + " because of Rule: " + rule2.getRuleString());
				}
				
			}
			if (rule1.isGood() && percent2>tChecker.getUpperOverlapThreshold()) {
				if (rule1.getConfidence()>rule2.getConfidence()) {
					rule2.setIsGood(false);			
					logger.log(Level.DEBUG,"Eliminate Rule: " + rule2.getRuleString() + " because of Rule: "  + rule1.getRuleString());
				}
			}
		}
		else {
			if (percent2>tChecker.getUpperOverlapThreshold()) {
				if (rule1.getConfidence()>rule2.getConfidence()) {
					rule2.setIsGood(false);
					logger.log(Level.DEBUG,"Eliminate Rule: " + rule2.getRuleString() + " because of Rule: " + rule1.getRuleString());
				}
			}
			if (rule2.isGood() && percent1>tChecker.getUpperOverlapThreshold()) {
				if (rule1.getConfidence()<rule2.getConfidence()) {
					rule1.setIsGood(false);					
					logger.log(Level.DEBUG,"Eliminate Rule: " + rule1.getRuleString() + " because of Rule: " + rule2.getRuleString());
				}
				
			}
		}
		
	}
	//*************************** PREPROCESSING ***************************
	private void preprocessing() throws SQLException {
		
		FactsForHead = queryHandler.calculateRelationSize(head.getHeadRelation());	
		findConstantsForTypes();			
	}
	
	private void findConstantsForTypes() throws SQLException {
		
		if (inputArg!=2) {
			typeConstantsForArg1 = queryHandler.retrieveTypeConstants(head.getHeadRelation().getName(), numOfTries, 1);
		}
		if (inputArg!=1) {
			typeConstantsForArg2 = queryHandler.retrieveTypeConstants(head.getHeadRelation().getName(), numOfTries, 2);
		}		
	}


	//*************************** GETTERS *****************************
	public ArrayList<Rule> getLearnedRules() {
		
		boolean onlyGood=true;
		boolean onlyGenerals=false;	
		ArrayList<Rule> usefulRules=new ArrayList<Rule>();
		
		System.out.println("Getting Rules of Tree");
		rulesTree.getRulesOfTree(usefulRules,onlyGood, onlyGenerals, allowFreeVars);	
		
		return usefulRules;
	}
	
	public RuleTreeNode getRuleNode() {
		return this.rulesTree;
	}
}


