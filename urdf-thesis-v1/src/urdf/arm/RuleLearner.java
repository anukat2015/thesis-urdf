package urdf.arm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

import org.apache.log4j.PropertyConfigurator;

import urdf.ilp.LearningManager;
import urdf.ilp.Literal;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.ilp.RelationsInfo;
import urdf.ilp.RuleWithNumericLiteralLearner;
import urdf.ilp.ThresholdChecker;
import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;

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
	
	public void learn( ) throws SQLException {
		root = new AssociationRuleNode(rootRelation);
		
		//HashSet<Relation> candidateRelations = info.arg1JoinOnArg1.get(rootRelation);
		HashSet<Relation> candidateRelations = new HashSet<Relation>();
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sex>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/st>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sch>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rel>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racwht>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racblk>",	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racasn>",	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/qtrbir>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/oc>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nativity>", null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mar>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/lanp>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/esr>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/dphy>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/schl>", 	null, null));
		
		// For Root
		root.queryNodeProperties(queryHandler);
		System.out.println(root);
		ArrayTools.print(root.getDistribution());
		
		
		// First level with candidate relations
		for (Relation r : candidateRelations) {
			AssociationRuleNode newNode = root.clone();
			newNode.addItem(r);
			newNode.queryNodeGroupProperties(queryHandler);
			root.addChild(newNode);
			newNode.addParent(root);
			
			System.out.println(newNode);
			ArrayTools.print(newNode.getDistribution());
			for (AssociationRuleNode n: newNode.getConstants()) {
				System.out.println(n);
				ArrayTools.print(n.getDistribution());
			}
		}
		
	}
	
	public static void main (String[] args) throws Exception {
		Connection connPartition = Driver.connect("src/rdf3x-data91.properties");
		QueryHandler qh = new QueryHandler(connPartition);
		Relation rootRelation = new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/pincp>", null, null);
		RuleLearner learner = new RuleLearner(qh, null, null, rootRelation);
		learner.learn();
		
	}
}
