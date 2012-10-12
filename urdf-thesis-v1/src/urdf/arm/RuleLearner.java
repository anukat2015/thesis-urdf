package urdf.arm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;

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
	
	private static int maxLevels = 5;
	
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
		
		/*candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/adjhsg>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/adjinc>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/agep>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/anc>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/anc1p>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/anc2p>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/cit>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/cow>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/ddrs>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/decade>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/deye>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/dout>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/dphy>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/drem>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/drivesp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/ds>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/dwrk>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/eng>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/esp>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/esr>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fagep>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fancp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fcitp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fcowp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fddrsp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fdeyep>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fdoutp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fdphyp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fdremp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fdwrkp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fengp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fer>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fesrp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fferp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fgclp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fgcmp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fgcrp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fhisp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/findp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fintp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fjwdp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fjwmnp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fjwrip>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fjwtrp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/flanp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/flanxp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fmarp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fmigp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fmigsp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fmilpp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fmilsp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fmilyp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/foccp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/foip>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fpap>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fpobp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fpowsp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fracp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/frelp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fretp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fschgp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fschlp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fschp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fsemp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fsexp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fssip>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fssp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fwagp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fwkhp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fwklp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fwkwp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/fyoep>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/gcl>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/gcm>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/gcr>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/hisp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/indp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/intp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/jwap>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/jwdp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/jwmnp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/jwrip>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/jwtr>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/lanp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/lanx>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mar>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mig>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/migpuma>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/migsp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mil>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mily>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpa>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpb>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpc>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpd>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpe>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpf>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpg>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlph>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpi>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpj>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mlpk>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/msp>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/naicsp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nativity>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nwab>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nwav>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nwla>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nwlk>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nwre>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/oc>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/occp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/oip>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/paoc>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/pap>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/pernp>", 		null, null));
		//candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/pincp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/pobp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/povpip>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/powpuma>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/powsp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/puma>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/pwgtp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/qtrbir>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rac1p>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rac2p>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rac3p>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racaian>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racasn>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racblk>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racnhpi>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racnum>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racsor>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racwht>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rc>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rel>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/retp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rt>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sch>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/schg>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/schl>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/semp>", 		null, null));
		//candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/serialno>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sex>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sfn>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sfr>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/socp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sporder>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/ssip>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/ssp>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/st>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/uwrk>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/vps>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/wagp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/waob>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/wkhp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/wkl>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/wkw>", 			null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/yoep>", 		null, null));
		*/
		
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sex>", 		null, null));
		//candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/st>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sch>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rel>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racwht>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racblk>",	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racasn>",	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/qtrbir>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/oc>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nativity>", null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mar>", 		null, null));
		//candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/lanp>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/esr>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/dphy>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/schl>", 	null, null));
		//candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/occp>", 	null, null));
		
		
		// For Root
		root.queryNodeProperties(queryHandler);
		//System,out.println(root);
		//ArrayTools.print(root.getDistribution());
		
		
		LinkedList<AssociationRuleNode> nextLevel = new LinkedList<AssociationRuleNode>(); 
		// First level with candidate relations
		for (Relation r : candidateRelations) {
			AssociationRuleNode newNode = root.clone();
			newNode.addItem(r);
			newNode.addParent(root);
			newNode.queryNodeGroupProperties(queryHandler);
			root.addChild(newNode);
			nextLevel.add(newNode);
			
			//System,out.println("\n\n"+newNode+"\n\t"+newNode.getInfo());
			//ArrayTools.print(newNode.getDistribution());
			for (AssociationRuleNode n: newNode.getConstants()) {
				//System,out.println(n+"\n\t"+n.getInfo());
				//ArrayTools.print(n.getDistribution());
			}
		}
		
		LinkedList<AssociationRuleNode> level = nextLevel;
		nextLevel = new LinkedList<AssociationRuleNode>();
		maxLevels = 5;
		//System,out.println("Starting level 2 =)\tNumber of ");
		for (int depth=1; depth<maxLevels; depth++) {
			Object[] leaves = level.toArray();
			for (int i=0; i<(leaves.length-1); i++) {
				AssociationRuleNode iNode = (AssociationRuleNode) leaves[i];
				for (int j=i+1; !iNode.isPruned() && j<leaves.length; j++) {
					AssociationRuleNode jNode = (AssociationRuleNode) leaves[j];
					if (!jNode.isPruned() && !iNode.isPruned()) {
						//System,out.println("Joining "+iNode.getRelationSetNames()+" with "+jNode.getRelationSetNames());
						try {
							
							AssociationRuleNode newNode = AssociationRuleNode.joinNodes(iNode, jNode);
							////System,out.println("\n"+newNode);
							newNode.addParent(iNode);
							newNode.addParent(jNode);
							jNode.addChild(newNode);
							iNode.addChild(newNode);	
							//newNode.queryNodeProperties(queryHandler);
							////System,out.println("\t"+newNode.getInfo());
							nextLevel.add(newNode);
							
							for (AssociationRuleNode iConst: iNode.getConstants()) {
								if (!iConst.isPruned())
									for (AssociationRuleNode jConst: jNode.getConstants()) {
										if (!jConst.isPruned())
											try {
												AssociationRuleNode newConstNode = AssociationRuleNode.joinNodes(iConst, jConst);
												//System,out.println(newConstNode);
												newConstNode.addParent(iConst);
												newConstNode.addParent(jConst);
												newNode.addConstant(newConstNode);
												newConstNode.queryNodeProperties(queryHandler);	
												//System,out.println("\t"+newConstNode.getInfo());
															
											} catch (IllegalArgumentException e) {
												//System,out.println(e.getMessage());
											}
									}
							}
							newNode.queryNodeProperties(queryHandler);
							//System,out.println("\n"+newNode);
							//System,out.println("\t"+newNode.getInfo());
							
						} catch (IllegalArgumentException e) {
							//System,out.println(e.getMessage());
						}
					}
				}
			}

			level = nextLevel;
			nextLevel = new LinkedList<AssociationRuleNode>();
		}
		
		AssociationRuleNode.searchRules(root);
		
	}
	
	public static void main (String[] args) throws Exception {
		Connection connPartition = Driver.connect("src/rdf3x-data91.properties");
		QueryHandler qh = new QueryHandler(connPartition);
		Relation rootRelation = new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/pincp>", null, null);
		RuleLearner learner = new RuleLearner(qh, null, null, rootRelation);
		learner.learn();
		
	}
}
