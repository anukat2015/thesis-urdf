package urdf.ilp.old;



import java.util.ArrayList;

/**
 * 	@author Christina Teflioudi
 *
 *	The Class that provides the structure for the rule hierarchy (which rule was produced from which rule)
 */
public class RuleTreeNode 
{
	private Rule rule;
	private ArrayList<RuleTreeNode> children;
	private RuleTreeNode parent;
	private int depth;
	
	public RuleTreeNode(Rule rule, RuleTreeNode parent, int depth)
	{
		this.rule=rule;
		this.parent=parent; //if the node is the root, the parent is null
		this.depth=depth;
	}
	
	public Rule getRule()
	{
		return this.rule;
	}
	public int getDepth()
	{
		return this.depth;
	}
	public ArrayList<RuleTreeNode> getChildren()
	{
		return this.children;
	}
	public RuleTreeNode getChild(int index) throws Exception
	{
		if (children==null || index>=children.size())
			throw new Exception("No child in this position!");
		return this.children.get(index);
	}
	public RuleTreeNode getParent()
	{
		return this.parent;
	}
	public void addChild(RuleTreeNode child)
	{
		if (children==null)
		{
			children=new ArrayList<RuleTreeNode>();
		}
		children.add(child);
	}
	
	/**
	 * @param rules: the more general than this rule
	 */
	public void getMoreGeneralRules(ArrayList<Rule> rules)
	{		
		if (depth>1)
		{
			if (this.parent.getRule().bindsHeadVariables()) //isFinal before
			{
				rules.add(this.parent.getRule());
			}				
			this.parent.getMoreGeneralRules(rules);				
		}
	}
	
	/**
	 * @param rules: the more special than this rule
	 */
	public void getMoreSpecialRules(ArrayList<Rule> rules)
	{
		if (children!=null && children.size()>0)
		{
			for (int i=0, len=children.size();i<len;i++)
			{
				if(children.get(i).getRule().bindsHeadVariables()) //isFinal before
				{
					rules.add(children.get(i).getRule());
				}
				children.get(i).getMoreSpecialRules(rules);				
			}
		}
	}
	
	/**
	 * @return the first Parent that connects head and body (a rule with valid accuracy). The youngest ancestor
	 */
	public Rule getFirstFinalParentRule()
	{
		if (depth==0 || depth==1)
			return null;
		
		if (this.parent.getRule().bindsHeadVariables()) //isFinal before
			return this.parent.getRule();
		
		this.parent.getFirstFinalParentRule();
		
		return null;
	}
	/**
	 * @return the Parent that connects head and body (a rule with valid accuracy) and has the highest confidence value
	 */
	public Rule getBestFinalParentRule()
	{
		Rule parentRule;
		
		if (depth==0)
			return null;
		
		parentRule=this.parent.getBestFinalParentRule();
		
		if (parentRule!=null)
		{
			if (rule.bindsHeadVariables() && rule.getConfidence()>parentRule.getConfidence())
			{
				return rule; 
			}
			return parentRule;
		}
			
		if (parentRule==null && !rule.bindsHeadVariables())
			return null;
		if (parentRule==null && rule.bindsHeadVariables())
			return rule;
		
		return null;
	}

	/**
	 * @param rulesArray
	 * @param onlyGoodRules
	 * @param onlyGeneralRules
	 * @param allowFreeVars
	 * 
	 * good=true, general=true:  good and general rules
	 * good=true, general=false: good rules which are not general=> the rules to output
	 */
	public void getRulesOfTree(ArrayList<Rule> rulesArray,boolean onlyGoodRules, boolean onlyGeneralRules, boolean allowFreeVars)
	{
		if (rulesArray==null)
		{
			rulesArray=new ArrayList<Rule>();
		}		

		if (this.parent!=null)
		{
			if (allowFreeVars || !rule.hasFreeVariables())
			{
				
				
				if (!onlyGoodRules || rule.isGood())
				{
					if (onlyGeneralRules)
					{
						if (rule.isTooGeneral())
						{
							rulesArray.add(rule);
						}
					}
					else
					{
						if (!rule.isTooGeneral())
						{
							rulesArray.add(rule);
						}
					}
					
				}			
			}
		}
		if(children!=null && children.size()>0)
		{
			for (int i=0,len=children.size();i<len;i++)
			{
				children.get(i).getRulesOfTree(rulesArray,onlyGoodRules, onlyGeneralRules,allowFreeVars);
			}
		}
		
	}
	
}