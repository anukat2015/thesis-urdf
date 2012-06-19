package urdf.ilp.old;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * @author chteflio
 * UNCOMPLETED: relative to recursion plans
 */
public class PostProcessor 
{
	float threshold=(float)0.92;
	HashMap<String,ArrayList<Rule>> inMap;
	HashMap<String,RuleTreeNode> inTree;
	HashMap<String,ArrayList<Rule>> outMap;
	
	HashMap<Integer,ArrayList<Rule>> definitions;
	
	public HashMap<String,ArrayList<Rule>> process(HashMap<String,ArrayList<Rule>> inMap,HashMap<String,RuleTreeNode> inTree)
	{
		this.inMap=inMap;
		this.inTree=inTree;
		
		definitions=new HashMap<Integer,ArrayList<Rule>>();
		
		// find Definitions
		Set<String> set=inTree.keySet();
		Iterator<String> it=set.iterator();		
		
		while(it.hasNext())
		{
			findDefinitionsFromNode(inTree.get(it.next()));
		}		
		
		// check for equivalence
		it=set.iterator();
		
		while (it.hasNext())
		{
			checkRulesForEquivalence(inMap.get(it.next()));			
		}		
		
		outMap=new HashMap<String,ArrayList<Rule>>();	
		
		return outMap;		
	}
	private void findDefinitionsFromNode(RuleTreeNode node)
	{
		if (node!=null)
		{
			if (node.getRule().isGood() && !node.getRule().isTooGeneral() && node.getRule().getConfidence()>threshold)
			{
				if (definitions.get(node.getDepth())==null)
					definitions.put(node.getDepth(), new ArrayList<Rule>());
				definitions.get(node.getDepth()).add(node.getRule());
			}
			
			if (node.getChildren()!=null)
			{
				for (int i=0, len=node.getChildren().size();i<len;i++)
				{
					findDefinitionsFromNode(node.getChildren().get(i));
				}
			}
		}		
	}

	private void checkRulesForEquivalence(ArrayList<Rule> inRules)
	{
		outMap.put(inRules.get(0).getHead().getRelationName(), new ArrayList<Rule>());
		int d;
		Rule defRule;
		Set<Integer> depth=definitions.keySet();
		Iterator<Integer> it=depth.iterator();
		
		while(it.hasNext())
		{
			d=it.next();
			
			for (int i=0,len1=definitions.get(d).size();i<len1;i++) // i for definitions
			{
				defRule=definitions.get(d).get(i);
				
				checkRulesAuxiliary(defRule, inRules);
				
				
			}
			
		}
		
		
	}
	private void checkRulesAuxiliary(Rule defRule,ArrayList<Rule> inRules)
	{
		Rule newRule;
		for (int i=0, len1=inRules.size();i<len1;i++) // i for relations in inRules
		{
			for (int d=0;d<inRules.get(i).getBodyLiterals().size();d++)
			{
				
			}
		}
	}
	
	/**
	 * @param originalRule
	 * @param definitionRule
	 * @return
	 * @throws Exception
	 * 
	 * creates the rule if I encorporate the definition rule into the original one
	 * I can use the newly created rule as definition rule to see if it is the same 
	 * with some other rule. In this case the returned rule will be headRelation:-headRelation
	 */
	public Rule getEquivalentRule(Rule originalRule, Rule definitionRule) throws Exception
	{
		ArrayList<Literal> orBodyLit=originalRule.getBodyLiterals();
		ArrayList<Literal> defBodyLit=definitionRule.getBodyLiterals();
		boolean exists,flag, dontLookFor65=false,dontLookFor66=false;
		ArrayList<Relation> arg1OnArg1, arg1OnArg2, arg2OnArg1, arg2OnArg2;
		int counter1, counter2, counter3, counter4;
		Rule newRule=new Rule(originalRule.getHead());
		Relation rel65=null,rel66=null;
		int arg65=1,arg66=1;
		
		for (int i=0, len1=defBodyLit.size();i<len1;i++)
		{
			exists=false;
			
			arg1OnArg1 = new ArrayList<Relation>();
			arg1OnArg2 = new ArrayList<Relation>();
			arg2OnArg1 = new ArrayList<Relation>();
			arg2OnArg2 = new ArrayList<Relation>();			
			
			// find bindings with other literals
			for (int j=0;j<len1;j++)  
			{
						
				if (defBodyLit.get(j).getFirstArgument()==65 && !dontLookFor65)
				{
					arg65=1;
					rel65=defBodyLit.get(j).getRelation();
				}
				if (defBodyLit.get(j).getSecondArgument()==65 && !dontLookFor65)
				{
					arg65=2;
					rel65=defBodyLit.get(j).getRelation();
				}
				if (defBodyLit.get(j).getFirstArgument()==66 && !dontLookFor66)
				{
					arg66=1;
					rel66=defBodyLit.get(j).getRelation();
				}
				if (defBodyLit.get(j).getSecondArgument()==66 && !dontLookFor66)
				{
					arg66=2;
					rel66=defBodyLit.get(j).getRelation();
				}
				if (i==j)
				{
					continue;
				}
				if (defBodyLit.get(i).getFirstArgument()==defBodyLit.get(j).getFirstArgument())
				{
					arg1OnArg1.add(defBodyLit.get(j).getRelation());
				}
				if (defBodyLit.get(i).getFirstArgument()==defBodyLit.get(j).getSecondArgument())
				{
					arg1OnArg2.add(defBodyLit.get(j).getRelation());
				}
				if (defBodyLit.get(i).getSecondArgument()==defBodyLit.get(j).getFirstArgument())
				{
					arg2OnArg1.add(defBodyLit.get(j).getRelation());
				}
				if (defBodyLit.get(i).getSecondArgument()==defBodyLit.get(j).getSecondArgument())
				{
					arg2OnArg2.add(defBodyLit.get(j).getRelation());
				}				
			}			
			
			for (int j=0, len2=orBodyLit.size();j<len2;j++)
			{
				flag=false;
				for (int k1=0;k1<len1 && i==0;k1++)
				{
					if (defBodyLit.get(k1).getRelation().equals(orBodyLit.get(j).getRelation()))
					{
						flag=true;
						break;
					}
				}
				if (!flag && i==0)
				{
					newRule.addLiteral(orBodyLit.get(j));
				}
				
				// all relations in the defBody should also be in the orBody
				if (defBodyLit.get(i).getRelation().equals(orBodyLit.get(j).getRelation()))
				{
					exists=true;
					counter1=0;
					counter2=0;
					counter3=0;
					counter4=0;
					
						for (int k2=0;k2<len2;k2++)
						{
							if (orBodyLit.get(j).getFirstArgument()==orBodyLit.get(k2).getFirstArgument()
									&& arg1OnArg1.contains(orBodyLit.get(k2).getRelation()))
							{
								counter1++;
							}
							if (orBodyLit.get(j).getFirstArgument()==orBodyLit.get(k2).getSecondArgument()
									&& arg1OnArg2.contains(orBodyLit.get(k2).getRelation()))
							{
								counter2++;
							}
							if (orBodyLit.get(j).getSecondArgument()==orBodyLit.get(k2).getFirstArgument()
									&& arg2OnArg1.contains(orBodyLit.get(k2).getRelation()))
							{
								counter3++;
							}
							if (orBodyLit.get(j).getSecondArgument()==orBodyLit.get(k2).getSecondArgument()
									&& arg2OnArg2.contains(orBodyLit.get(k2).getRelation()))
							{
								counter4++;
							}
						}
						
						if (counter1<arg1OnArg1.size() || counter2<arg1OnArg2.size() || counter3<arg2OnArg1.size()
								||counter4<arg2OnArg2.size())
						{
							return null;
						}
						if (orBodyLit.get(j).getRelation().equals(rel65) && !dontLookFor65)
						{
							dontLookFor65=true;
							if (arg65==1)
							{
								arg65=orBodyLit.get(j).getFirstArgument();
							}
							else
							{
								arg65=orBodyLit.get(j).getSecondArgument();
							}
						}
						if (orBodyLit.get(j).getRelation().equals(rel66) && !dontLookFor66)
						{
							dontLookFor66=true;
							if (arg66==1)
							{
								arg66=orBodyLit.get(j).getFirstArgument();
							}
							else
							{
								arg66=orBodyLit.get(j).getSecondArgument();
							}
						}
					
				}	
			}
			if (!exists)
			{
				return null;
			}
			
		}
		newRule.addLiteral(new Literal(definitionRule.getHead().getRelation(),arg65,arg66));
		return newRule;
		
	}
	
	public static void main(String[] args)
	{
		try
		{
			RelationPreProcessor preprocessor=new RelationPreProcessor(args[0]);			
			RelationsInfo info=preprocessor.getRelationsInfo();
			
			Rule orRule= new Rule(new Literal(info.getRelationFromRelations("bornIn"),65,66));
			Literal lit=new Literal(info.getRelationFromRelations("hasSuccessor"),65,67);			
			orRule.addLiteral(lit);
			lit=new Literal(info.getRelationFromRelations("politicianOf"),67,68);
			orRule.addLiteral(lit);
			lit=new Literal(info.getRelationFromRelations("hasCapital"),68,66);
			orRule.addLiteral(lit);
			
			Rule defRule=new Rule(new Literal(info.getRelationFromRelations("politicianOf"),65,66));
			lit=new Literal(info.getRelationFromRelations("hasSuccessor"),65,67);
			defRule.addLiteral(lit);
			lit=new Literal(info.getRelationFromRelations("politicianOf"),67,66);
			defRule.addLiteral(lit);
			
			PostProcessor pp=new PostProcessor();
			Rule newRule=pp.getEquivalentRule(orRule,defRule);
			
			newRule=pp.getEquivalentRule(newRule, newRule);
			
			int i=0;
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		
		
	}

}
