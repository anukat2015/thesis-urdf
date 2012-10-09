package urdf.arm;

import java.util.HashSet;

import org.apache.log4j.PropertyConfigurator;

import urdf.ilp.LearningManager;
import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.ilp.RelationsInfo;
import urdf.ilp.RuleWithNumericLiteralLearner;
import urdf.ilp.ThresholdChecker;

public class RuleLearner {

	private QueryHandler queryHandler;
	private ThresholdChecker tChecker;
	private RelationsInfo info;
	private Relation rootRelation;
	private AssociationRuleNode root;
	
	public RuleLearner(QueryHandler queryHandler,ThresholdChecker tChecker, RelationsInfo info, Relation rootRelation) {
		
		PropertyConfigurator.configure(LearningManager.log4jConfig);
		
		this.queryHandler = queryHandler;
		this.tChecker = tChecker;
		this.info = info;
		this.rootRelation = rootRelation;
	}
	
	public void learn( ) {
		root = new AssociationRuleNode(rootRelation);
		
		HashSet<Relation> candidateRelations = info.arg1JoinOnArg1.get(rootRelation);
		
		// For Root
		
		for (Relation r : candidateRelations) {
			AssociationRuleNode newNode = root.clone();
			if (newNode.addItem(r)) {
				
			}
		}
		
	}
}
