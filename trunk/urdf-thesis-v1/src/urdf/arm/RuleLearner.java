package urdf.arm;

import java.io.FileOutputStream;
import java.io.IOException;
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
	
	private static int maxLevels = 5;
	
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
				
		root = new CorrelationLatticeNode(rootRelation);
		CorrelationLatticeNode.kldivThreshold = klDivThreshold;
		CorrelationLatticeNode.indepThreshold = indepThreshold;
		CorrelationLatticeNode.confidenceThreshold = confidenceThreshold;
		CorrelationLatticeNode.numOfBuckets = numOfBuckets;
		CorrelationLatticeNode.supportThreshold = suppThreshold;

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
		
		info = RelationsInfo.readFromDisk("relationsInfoForRdf3x.ser");
		//candidateRelations.add(info.getRelationFromRelations("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isLocatedIn>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasGini>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasHDI>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/imports>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/exports>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasExpenses>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasInflation>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasEconomicGrowth>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasImport>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasRevenue>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasExport>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasUnemployment>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasNeighbor>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasCapital>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasOfficialLanguage>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasCurrency>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPoverty>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasGDP>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/dealsWith>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/participatedIn>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/owns>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPopulationDensity>"));

		/*candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasGender>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/livesIn>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/wasBornIn>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasWonPrize>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/playsFor>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isAffiliatedTo>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isMarriedTo>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isLeaderOf>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/isPoliticianOf>"));
		candidateRelations.add(info.getRelationFromRelations("<http://yago-knowledge.org/resource/graduatedFrom>"));
		candidateRelations.add(info.getRelationFromRelations("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"));
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
		
		
		long t1 = System.currentTimeMillis();

		root.queryNodeProperties(queryHandler);		
		
		LinkedList<CorrelationLatticeNode> nextLevel = new LinkedList<CorrelationLatticeNode>(); 
		// First level with candidate relations
		/*for (Relation r : candidateRelations) {
			CorrelationLatticeNode newNode = root.clone();
			newNode.addItem(r);
			newNode.addParent(root);
			newNode.queryNodeGroupProperties(queryHandler);
			root.addChild(newNode);
			nextLevel.add(newNode);
			
			System.out.println("\n\n"+newNode+"\n\t"+newNode.getInfo());
			//ArrayTools.print(newNode.getDistribution());
			for (CorrelationLatticeNode n: newNode.getConstants()) {
				System.out.println(n+"\n\t"+n.getInfo());
				//ArrayTools.print(n.getDistribution());
			}
		}*/
		for (Relation r : candidateRelations) {
			CorrelationLatticeNode newNode = root.clone();
			newNode.addItem(r);
			newNode.addParent(root);
			newNode.queryNodeGroupProperties(queryHandler);

			
			//root.addChild(newNode);
			//nextLevel.add(newNode);
			
			
			System.out.println("\n"+newNode);
			System.out.println("\t"+newNode.getInfo());
			
			for (CorrelationLatticeNode n: newNode.getConstants()) {
				System.out.println("\n"+n);
				System.out.println("\t"+n.getInfo());
				root.addChild(n);
				n.addParent(root);
				nextLevel.add(n);
			}
		}
	
		
		root.analizeNode();
		
		firstLevelTime = System.currentTimeMillis() - t1;
		
		LinkedList<CorrelationLatticeNode> level = nextLevel;
		nextLevel = new LinkedList<CorrelationLatticeNode>();
		maxLevels = 5;
		//System,out.println("Starting level 2 =)\tNumber of ");
		/*for (int depth=1; depth<maxLevels; depth++) {	
			Collections.sort(level);2
			Object[] leaves = level.toArray();
			for (int i=0; i<(leaves.length-1); i++) {
				CorrelationLatticeNode iNode = (CorrelationLatticeNode) leaves[i];
				for (int j=i+1; !iNode.isPruned() && j<leaves.length; j++) {
					CorrelationLatticeNode jNode = (CorrelationLatticeNode) leaves[j];
					if (!jNode.isPruned() && !iNode.isPruned()) {
						//System,out.println("Joining "+iNode.getRelationSetNames()+" with "+jNode.getRelationSetNames());
						try {
							
							CorrelationLatticeNode newNode = CorrelationLatticeNode.joinNodes(iNode, jNode, queryHandler);
							////System,out.println("\n"+newNode);
							newNode.addParent(iNode);
							newNode.addParent(jNode);
							jNode.addChild(newNode);
							iNode.addChild(newNode);							
							
							//newNode.queryNodeMultiGroupProperties(queryHandler, iNode, jNode);
							//nextLevel.add(newNode);
							
							
							newNode.queryNodeProperties(queryHandler);
							
							////System,out.println("\t"+newNode.getInfo());
							nextLevel.add(newNode);
							
							for (CorrelationLatticeNode iConst: iNode.getConstants()) {
								if (!iConst.isPruned())
									for (CorrelationLatticeNode jConst: jNode.getConstants()) {
										if (!jConst.isPruned())
											try {
												CorrelationLatticeNode newConstNode = CorrelationLatticeNode.joinNodes(iConst, jConst, queryHandler);
												newConstNode.queryNodeProperties(queryHandler);
												int[] indepHipotheses = CorrelationLatticeNode.independentJoinDitribution(iConst, jConst);
												float indepMeasure = ArrayTools.chiSquare(ArrayTools.laplaceSmooth(indepHipotheses), ArrayTools.laplaceSmooth(newConstNode.getDistribution()));
												if (indepMeasure >= indepThreshold) {																										
													newConstNode.addParent(iConst);
													newConstNode.addParent(jConst);
													newNode.addConstant(newConstNode);			
												} 
															
											} catch (IllegalArgumentException e1) {
												//System.out.println("[Exception]: "+e1.getMessage());
											}
									}
							}
							//newNode.queryNodeProperties(queryHandler);
							
							
							newNode.analizeNode();
							System.out.println("\n"+newNode);
							System.out.println("\t"+newNode.getInfo());
		
						} catch (IllegalArgumentException e) {
							//System.out.println("[Exception]: "+e.getMessage());
						}
					}
				}
			}
			for (CorrelationLatticeNode node : nextLevel) {
				for (CorrelationLatticeNode nodeConst : node.getConstants()) {
					nodeConst.analizeNode();
					if (!nodeConst.isPruned()) {
						System.out.println(nodeConst);
						System.out.println("\t"+nodeConst.getInfo());
					}
						
				}
				node.analizeNode();
				if (!node.isPruned()) {
					System.out.println("\t"+node);
					System.out.println("\t\t"+node.getInfo());
				}
			}

			level = nextLevel;
			nextLevel = new LinkedList<CorrelationLatticeNode>();
		}*/
		for (int depth=1; depth<maxLevels; depth++) {	
			Collections.sort(level);
			Object[] leaves = level.toArray();
			//System.out.println("Top-"+maxNodesPerLevel);for (int i=0; i<leaves.length && i<maxNodesPerLevel; i++) {System.out.println( ((CorrelationLatticeNode)leaves[i]).maxKldivParents + "\t" + ((CorrelationLatticeNode)leaves[i]).toString());} System.out.println();
			int joinedpairs = 0;
			for (int i=1; i<leaves.length/* && i<maxNodesPerLevel*/; i++) {
				CorrelationLatticeNode iNode = (CorrelationLatticeNode) leaves[i];				
				for (int j=0; !iNode.isPruned() && j<i/* && j<maxNodesPerLevel*/; j++) {
					CorrelationLatticeNode jNode = (CorrelationLatticeNode) leaves[j];
					if (iNode.getSupport()>=suppThreshold && jNode.getSupport()>=suppThreshold)
						if (!jNode.isPruned() && !iNode.isPruned()) {
							if (joinedpairs<maxNodesPerLevel)
								try {
									CorrelationLatticeNode newNode = CorrelationLatticeNode.joinNodes(iNode, jNode, queryHandler);
									
									iNode.addChild(newNode);
									jNode.addChild(newNode);
									
									if (!newNode.isPruned() && newNode.getSupport()>=suppThreshold) {
										nextLevel.add(newNode);										
									}
									joinedpairs++;
									System.out.println("\n"+newNode);
									System.out.println("\t"+newNode.getInfo());
				
								} catch (IllegalArgumentException e) {
									/*if (iNode.toString().contains("filter(") && jNode.toString().contains("filter(")) {
										System.out.println(".............");
										System.out.println(iNode);
										System.out.println(jNode);
										System.out.println(e.getMessage());
									}*/
									
									//e.printStackTrace();
									//System.out.println(e.getMessage());
								}
						}
				}
			}
			for (CorrelationLatticeNode node : nextLevel)
				node.analizeNode();

			level = nextLevel;
			nextLevel = new LinkedList<CorrelationLatticeNode>();
		}
		
		long t2 = System.currentTimeMillis();
		elapsedTime = (t2-t1);

		System.out.println("!!!!!!!!!!!!!!!!!!!!!!! Printing rules");

		
		//rules = CorrelationLatticeNode.searchRules(root);
		rules = CorrelationLatticeNode.searchRules(root);		
		
		//String path = "correlation-lattice-income.ser";
		//root.persist(path);

		// Serialize / save it
		//ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("rules-income.ser"));
		//oos.writeObject(rules);
		//AssociationRuleNode readRoot = AssociationRuleNode.readFromDisk(path);
		//readRoot.breadthFirst();
	}
	
	public static void main (String[] args) throws Exception {
		
		String output = "";
		
		float confidenceThreshold = (float)0.7;
		
		//double[] indepTSArray = {/*0.0,	5.0,	10.0,	15.0,	20.0, 	30.0,*/	50.0/*,	75.0,	100.0*/};
		//double[] kldivTSArray = {/*0.000,	*/0.0125, 0.0250, 0.0325, 0.0500,	0.0625,	0.0750,	0.0825,	0.100};
		//int[] suppTSArray = {25/*, 50, 75, 100, 125, 150, 300, 500, 1000*/};
		double[] indepTSArray = {0.0};
		int[] suppTSArray = {5};
		//int[] maxNodesArray = {5,10,15,20,25,30,40,50,60,70,80,90,100,110,120,130,140,150,200,250,300,350,400,450,500};
		//int[] maxNodesArray = {10,20,30,40,50,60,70,80,90,100};
		//int[] maxNodesArray = {10,50,100};
		int[] maxNodesArray = {1000};
		double[] kldivTSArray = {0.0};
		
		for (int measure=0; measure<=0; measure++)
		for (int numBuckets=25; numBuckets<=25; numBuckets+=25)
		for (int l=0; l<maxNodesArray.length; l++)
		for (int k=0; k<suppTSArray.length; k++)
		for (int i=0; i<indepTSArray.length; i++) {
			for (int j=0; j<kldivTSArray.length; j++) {
				//Connection connPartition = Driver.connect("src/rdf3x-dblp.properties");
				//Connection connPartition = Driver.connect("src/rdf3x-data91-train.properties");
				Connection connPartition = Driver.connect("src/rdf3x-yago.properties");
				
				
				QueryHandler qh = new QueryHandler(connPartition);
				
				info = RelationsInfo.readFromDisk("relationsInfoForRdf3x.ser");
				
				//Relation rootRelation = new Relation("<http://purl.org/dc/terms/citations>", null, null);
				//Relation rootRelation = info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasHeight>");
				Relation rootRelation = info.getRelationFromRelations("<http://yago-knowledge.org/resource/hasPopulation>");
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
				
				root.printSuggestions(null);
				
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
