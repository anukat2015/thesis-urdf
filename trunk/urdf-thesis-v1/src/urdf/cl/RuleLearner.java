package urdf.cl;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.PropertyConfigurator;

import urdf.ilp.LearningManager;
import urdf.ilp.QueryHandler;
import urdf.ilp.Relation;
import urdf.ilp.RelationsInfo;
import urdf.ilp.Rule;
import urdf.ilp.ThresholdChecker;
import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.rdf3x.ResultSet;
import edu.emory.mathcs.backport.java.util.Collections;

public class RuleLearner {

	private QueryHandler queryHandler;
	private ThresholdChecker tChecker;
	private static RelationsInfo info;
	private Relation rootRelation;
	private CorrelationLatticeNode root;
	private Collection<NumericalRule> rules = new HashSet<NumericalRule>();
	private long elapsedTime;
	private long firstLevelTime;
	
	private float indepThreshold = 15;
	private float klDivThreshold = (float) 0.05;
	private int suppThreshold = 25;
	private int maxNodesPerLevel = 25;
	private float confidenceThreshold = (float) 0.75;
	private int numOfBuckets = 25;
	
	private static int maxLevels = 3;
	
	public RuleLearner(QueryHandler queryHandler,ThresholdChecker tChecker, RelationsInfo info, Relation rootRelation) {
		
		PropertyConfigurator.configure(LearningManager.log4jConfig);
		
		this.queryHandler = queryHandler;
		this.tChecker = tChecker;
		this.info = info;
		this.rootRelation = rootRelation;
	}
	
	public CorrelationLatticeNode getRoot() {
		return root;
	}
	
	public Collection<NumericalRule> getLearnedRules() {
		return rules;
	}
	
	public void learn( ) throws SQLException, IOException, InterruptedException {
				


		//HashSet<Relation> candidateRelations = info.arg1JoinOnArg1.get(rootRelation);
		ArrayList<Relation> candidateRelations = new ArrayList<Relation>();

		/*
		candidateRelations.add(new Relation("<http://dbpedia.org/property/Country>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/averageEarnings>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/popularProfession>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/commonLanguages>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/continent>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/religion>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/averageLiteracy>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/populationDensityRank>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/Altitude>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/latp>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/irrigatedArea>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/languageSpoken>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/countryType>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/Skyline>", 		null, null));
		*/
		/*
		//candidateRelations.add(new Relation("<http://dbpedia.org/property/gdpNominalPerCapita>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/gini>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/unitRainDays>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/TempWinter>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/TempSummer>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/SexRatio>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/TempAnnual>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/literacyFemale>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/literacyMale>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/TimezoneDst>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/UtcOffset>", 		null, null));
		candidateRelations.add(new Relation("<http://dbpedia.org/property/Timezone>", 		null, null));
		*/
		
		
		/*candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sex>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/st>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racwht>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racblk>",	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/racasn>",	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/qtrbir>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/nativity>", null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/mar>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/lanp>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/esr>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/dphy>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/schl>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/occp>", 	null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/sch>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/rel>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/esp>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/oc>", 		null, null));
		candidateRelations.add(new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/agep>", 	null, null));
		*/
		
		//candidateRelations.add(new Relation("<http://purl.org/ontology/mo/publishing_location>", 		null, null));
		//candidateRelations.add(new Relation("<http://purl.org/ontology/mo/track_number>", 	null, null));
		
		info = RelationsInfo.readFromDisk("relationsInfoForYago2s.ser");
		
		
		//candidateRelations.add(info.getRelationFromRelations("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isLocatedIn>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasGini>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasHDI>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/imports>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/exports>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasExpenses>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasInflation>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasEconomicGrowth>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasImport>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasRevenue>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasExport>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasUnemployment>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasNeighbor>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasCapital>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasOfficialLanguage>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasCurrency>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPoverty>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasGDP>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/dealsWith>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/participatedIn>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/owns>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPopulationDensity>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasLatitude>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasLongitude>"));
		
		
		/*candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasGender>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/livesIn>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/wasBornIn>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasWonPrize>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/playsFor>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isAffiliatedTo>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isMarriedTo>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isLeaderOf>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isPoliticianOf>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/graduatedFrom>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasWeight>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/wasBornOnDate>"));
		//candidateRelations.add(info.getRelationFromRelations("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"));
		*/

		/*candidateRelations.add(new Relation("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>", 	null, null));
		candidateRelations.add(new Relation("<http://purl.org/dc/elements/1.1/creator>", 			null, null));
		candidateRelations.add(new Relation("<http://xmlns.com/foaf/0.1/maker>",					null, null));
		candidateRelations.add(new Relation("<http://swrc.ontoware.org/ontology#editor>",			null, null));
		candidateRelations.add(new Relation("<http://purl.org/dc/elements/1.1/publisher>", 			null, null));
		candidateRelations.add(new Relation("<http://purl.org/dc/terms/issued>", 					null, null));
		candidateRelations.add(new Relation("<http://swrc.ontoware.org/ontology#journal>", 			null, null));
		candidateRelations.add(new Relation("<http://purl.org/dc/terms/partOf>", 					null, null));

		String q = "SELECT COUNT ?o WHERE {?s <http://purl.org/dc/terms/references> ?o} ORDER BY ASC(COUNT)";
		ResultSet rs = queryHandler.executeQuery(q);
		String newRelation = "<http://purl.org/dc/terms/citations>";
		while (rs.next()) {
			String count = rs.getString("count");
			String subject = rs.getString(1);
			queryHandler.executeQuery("INSERT DATA {"+subject+" "+newRelation+" \""+count+"\" . }");
		}
		Thread.sleep(5000);
		*/
		
		CorrelationLattice lattice = new CorrelationLattice(rootRelation, candidateRelations, maxLevels);
		lattice.setDivergenceThreshold(klDivThreshold);
		lattice.setSupportThreshold(suppThreshold);
		lattice.setConfidenceThreshold(confidenceThreshold);
		lattice.setIndependenceThreshold(indepThreshold);
		lattice.setNumberOfBuckets(numOfBuckets);
		
		lattice.buildLattice(queryHandler);
		root = lattice.getRoot();
		
		String path = "lattice-"+lattice.getRootRelation().getSimpleName()+".ser";
		
		// Persisting lattice
		
	    /*XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("lattice-"+root.getRootLiteral().getRelation().getSimpleName()+".xml")));
		e.writeObject(root);
	    XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream("lattice-"+root.getRootLiteral().getRelation().getSimpleName()+".xml")));
	    CorrelationLatticeNode readRoot = (CorrelationLatticeNode) d.readObject();
		System.out.println("\n\n\n\nRead Root:");
		readRoot.breadthFirst();
		*/
		
		/*
		root.persist("lattice-"+root.getRootLiteral().getRelation().getSimpleName()+".ser");
		CorrelationLatticeNode readRoot = CorrelationLatticeNode.readFromDisk("lattice-"+root.getRootLiteral().getRelation().getSimpleName()+".ser");
		System.out.println("\n\n\n\nRead Root:");
		readRoot.breadthFirst();
		*/
		
		//rules = CorrelationLatticeNode.searchRules(root);
		lattice.searchRules(root);	
		
		lattice.persist(path);
		
		lattice.breadthFirst();
	
		
		// Serialize / save it
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("rules-income.ser"));
		oos.writeObject(rules);
		

		
	}
	
	public static void main (String[] args) throws Exception {
		
		String output = "";
		
		float confidenceThreshold = (float)0.7;
		
		//double[] indepTSArray = {/*0.0,	5.0,	10.0,	15.0,	20.0, 	30.0,*/	50.0/*,	75.0,	100.0*/};
		//double[] kldivTSArray = {/*0.000,	*/0.0125, 0.0250, 0.0325, 0.0500,	0.0625,	0.0750,	0.0825,	0.100};
		//int[] suppTSArray = {25/*, 50, 75, 100, 125, 150, 300, 500, 1000*/};
		double[] indepTSArray = {0.0};
		int[] suppTSArray = {25};
		//int[] maxNodesArray = {5,10,15,20,25,30,40,50,60,70,80,90,100,110,120,130,140,150,200,250,300,350,400,450,500};
		//int[] maxNodesArray = {10,20,30,40,50,60,70,80,90,100};
		//int[] maxNodesArray = {10,50,100};
		int[] maxNodesArray = {Integer.MAX_VALUE};
		double[] kldivTSArray = {Float.NEGATIVE_INFINITY};
		
		for (int measure=1; measure<=1; measure++)
		for (int numBuckets=25; numBuckets<=25; numBuckets+=25)
		for (int l=0; l<maxNodesArray.length; l++)
		for (int k=0; k<suppTSArray.length; k++)
		for (int i=0; i<indepTSArray.length; i++) {
			for (int j=0; j<kldivTSArray.length; j++) {
				//Connection connPartition = Driver.connect("src/rdf3x-dblp.properties");
				//Connection connPartition = Driver.connect("src/rdf3x-data91-train.properties");
				Connection connPartition = Driver.connect("src/rdf3x-yago2s.properties");
				
				
				QueryHandler qh = new QueryHandler(connPartition);
				
				String path = "relationsInfoForYago2s.ser";
				info = RelationsInfo.readFromDisk(path);
				info.persist(path);
				
				//Relation rootRelation = new Relation("<http://purl.org/dc/terms/citations>", null, null);
				//Relation rootRelation = info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasHeight>");
				//Relation rootRelation = info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPopulation>");
				Relation rootRelation = info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasNumberOfPeople>");
				//Relation rootRelation = new Relation("<http://data-gov.tw.rpi.edu/vocab/p/91/pincp>", null, null);
				//Relation rootRelation = new Relation("<http://dbpedia.org/property/populationDensity>", null, null);
				//Relation rootRelation = new Relation("<http://dbpedia.org/property/gdpNominalPerCapita>", null, null);
				//Relation rootRelation = new Relation("<http://purl.org/ontology/mo/duration_ms>", null, null);
				
				
				RuleLearner learner = new RuleLearner(qh, null, null, rootRelation);
				
				ArrayTools.measure = measure;
				
				learner.indepThreshold = (float)indepTSArray[i];
				learner.klDivThreshold = (float)kldivTSArray[j];
				learner.suppThreshold = suppTSArray[k];
				learner.maxNodesPerLevel = maxNodesArray[l];
				learner.confidenceThreshold = confidenceThreshold;
				learner.numOfBuckets = numBuckets;
				
				//RuleLearner learner = new RuleLearner(qh, null, null, info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPopulation>"));
				learner.learn();
				CorrelationLatticeNode root = learner.getRoot();
				
				//CorrelationLatticeNode root = CorrelationLatticeNode.readFromDisk("correlation-lattice-income.ser");
				//Collection<NumericalRule> learnedRules = CorrelationLatticeNode.searchRules(root);
				Collection<NumericalRule> learnedRules = learner.getLearnedRules();
				for (NumericalRule r: learnedRules) {
					System.out.println(r.getRuleString()); System.out.print("\t");
					ArrayTools.print(r.getAccuracyDistribution());
				}
				
				root.breadthFirstWithSuggestions(null);
				
				//Deserialize load it
				/*CorrelationLatticeNode root = CorrelationLatticeNode.readFromDisk("correlation-lattice-income.ser");
				Histogram hs = root.getHistogram();
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream("rules-income.ser"));
				Set<NumericalRule> learnedRules = (HashSet<NumericalRule>) ois.readObject();
				for (NumericalRule r: learnedRules) {
					System.out.println(r.getRuleString());
				}
				if (root.getRootLiteral()==null) System.out.println("FODEO!");
				*/
				//RuleTester rt = new RuleTester(qh,hs,rootRelation);
				
				
				/*
				// Test on test partition
				connPartition = Driver.connect("src/rdf3x-data91-test.properties");
				
				qh = new QueryHandler(connPartition);
				
				float negatives = 0;
				float positives = 0;
				float avgGain = 0;
				RuleTester rt = new RuleTester(root,qh);
				for (NumericalRule r: learnedRules) {
					rt.testRule(r, learner.suppThreshold, confidenceThreshold);

					
					boolean isSpecialization = false;
					NumericalRule generalization = null;
					for (NumericalRule r1: learnedRules) {
						if (r.isSpecializationOf(r1)) {
							isSpecialization = true;
							generalization = r1;
							break;
						}		
					}
					
					if (r.observedOverallConfidence<confidenceThreshold) {
						float gain = r.observedConfidence/r.observedOverallConfidence;
						if (!Float.isNaN(gain)) {
							float observedTotal = (float)(r.observedPositives+r.observedNegatives);
							float currentTotal = (float)(positives+negatives);
							avgGain = (avgGain*(currentTotal) + gain*(observedTotal))/(currentTotal+observedTotal);
						}
						if (!isSpecialization) {
							positives += r.observedPositives;
							negatives += r.observedNegatives;
						} else {
							//System.out.println("Specialization of: "+generalization.getRuleString());
						}
					}
				}
				
				output += "\n\nkldiv="+learner.klDivThreshold+"\tindep="+learner.indepThreshold + "\tminsup="+learner.suppThreshold + "\tmaxnodes="+learner.maxNodesPerLevel + "\tbuckets="+numBuckets + "\tmeasure="+measure + 
						  "\nElapsedTime="+learner.elapsedTime+" \t("+learner.firstLevelTime+" + "+(learner.elapsedTime-learner.firstLevelTime)+")" +
						  "\nacc="+(positives/(positives+negatives)) + 
						  "\nsup="+(positives+negatives) +
						  "\ngain="+(avgGain) +
						  "\nnodes="+root.countNonPruned() +
						  "\nrules="+learnedRules.size();
				*/
				
				
				CorrelationLatticeNode.reset();
				
			}
			
			
		}
		System.out.println("\n\n\n\nFinalOutput"+output);
		
	}
}
