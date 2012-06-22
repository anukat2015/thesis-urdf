package urdf.webservice;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import urdf.api.UBindingSet;
import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UHardRule;
import urdf.api.ULineageAnd;
import urdf.api.ULiteral;
import urdf.api.URelation;
import urdf.api.USoftRule;
import urdf.reasoner.Sampling;
import urdf.reasoner.URDF;
import urdf.tools.RuleParser;
import MPII_SPARQL_endpoint.services.MPII_SPARQL_service;

public class URDF_service3 extends MPII_SPARQL_service {

	URelation HASDESTINATION = URelation.valueOf("hasDestination");
	URelation HASDESTINATIONADDRESS = URelation.valueOf("hasDestinationAddress");
	URelation HASDESTINATIONDATE = URelation.valueOf("hasDestinationDate");

	URelation HASEVENT = URelation.valueOf("hasEvent");
	URelation HASANYEVENT = URelation.valueOf("hasAnyEvent");
	URelation HASEMAILEVENT = URelation.valueOf("hasEmailEvent");
	URelation HASCALENDAREVENT = URelation.valueOf("hasCalendarEvent");
	URelation HASJOINTEVENT = URelation.valueOf("hasJointEvent");
	URelation EVENTLOCATION = URelation.valueOf("eventLocation");
	URelation EVENTDATE = URelation.valueOf("eventDate");

	URelation HASPARTICIPANT = URelation.valueOf("hasParticipant");
	URelation JOINTCORRESPONDENCE = URelation.valueOf("hasEmailCorrespondence");
	URelation LOCATIONREFERENCE = URelation.valueOf("hasLocation");
	URelation HOMEADDRESS = URelation.valueOf("LOCATIONREFERENCE");
	URelation LOCATIONNAME = URelation.valueOf("hasLocationName");
	URelation LOCATIONID = URelation.valueOf("hasLocationID");
	URelation LOCATIONDISPLAY = URelation.valueOf("hasLocationDisplay");
	URelation DATEREFERENCE = URelation.valueOf("hasDate");
	URelation HASEMAIL = URelation.valueOf("hasEmailAddress");
	URelation RECIPIENT = URelation.valueOf("hasRecipient");
	URelation SENDER = URelation.valueOf("hasSender");
	URelation ROOMMATES = URelation.valueOf("roomMates");
	URelation HASPHONE = URelation.valueOf("hasPhone");
	URelation FIRSTNAME = URelation.valueOf("hasFirstName");
	URelation LASTNAME = URelation.valueOf("hasLastName");

	public String processQuery(String queryString) {

		ArrayList<USoftRule> softRules;
		ArrayList<UHardRule> hardRules;

		StringBuffer resultString = new StringBuffer();

		try {

			softRules = new ArrayList<USoftRule>(Arrays.asList(

			// all rules

			    new USoftRule("S1", new ULiteral(ROOMMATES, "?LASTNAME_A", "?LASTNAME_B"), 1.0, new ULiteral(LASTNAME, "?ID_A", "?LASTNAME_A"), new ULiteral(
			        FIRSTNAME, "?ID_A", "?FIRSTNAME_A"), new ULiteral(LASTNAME, "?ID_B", "?LASTNAME_B"), new ULiteral(FIRSTNAME, "?ID_B", "?FIRSTNAME_B"),
			        new ULiteral(HASPHONE, "?ID_A", "?PHONE_A"), new ULiteral(HASPHONE, "?ID_B", "?PHONE_B"), new ULiteral(URelation.EQUALS, "?PHONE_A", "?PHONE_B"),
			        new ULiteral(URelation.NOTEQUALS, "?ID_A", "?ID_B")),

			    new USoftRule("S2", new ULiteral(JOINTCORRESPONDENCE, "?ID_A", "?ID_B"), 1.0, new ULiteral(HASEMAIL, "?ID_A", "?EMAIL_A"), new ULiteral(HASEMAIL,
			        "?ID_B", "?EMAIL_B"), new ULiteral(RECIPIENT, "?EVENTID", "?EMAIL_A"), new ULiteral(RECIPIENT, "?EVENTID", "?EMAIL_B"), new ULiteral(
			        URelation.NOTEQUALS, "?ID_A", "?ID_B"), new ULiteral(URelation.NOTEQUALS, "?EMAIL_A", "?EMAIL_B")),

			    new USoftRule("S3", new ULiteral(JOINTCORRESPONDENCE, "?ID_A", "?ID_B"), 1.0, new ULiteral(HASEMAIL, "?ID_A", "?EMAIL_A"), new ULiteral(HASEMAIL,
			        "?ID_B", "?EMAIL_B"), new ULiteral(SENDER, "?EVENTID", "?EMAIL_A"), new ULiteral(RECIPIENT, "?EVENTID", "?EMAIL_B"), new ULiteral(
			        URelation.NOTEQUALS, "?ID_A", "?ID_B"), new ULiteral(URelation.NOTEQUALS, "?EMAIL_A", "?EMAIL_B")),

			    new USoftRule("S4", new ULiteral(JOINTCORRESPONDENCE, "?ID_A", "?ID_B"), 1.0, new ULiteral(HASEMAIL, "?ID_A", "?EMAIL_A"), new ULiteral(HASEMAIL,
			        "?ID_B", "?EMAIL_B"), new ULiteral(SENDER, "?EVENTID", "?EMAIL_B"), new ULiteral(RECIPIENT, "?EVENTID", "?EMAIL_A"), new ULiteral(
			        URelation.NOTEQUALS, "?ID_A", "?ID_B"), new ULiteral(URelation.NOTEQUALS, "?EMAIL_A", "?EMAIL_B")),

			    // events
			    new USoftRule("S5", new ULiteral(HASDESTINATION, "?ID", "?DESTINATIONID"), 1.0, new ULiteral(HASANYEVENT, "?ID", "?DESTINATIONID")),

			    // office
			    new USoftRule("S6", new ULiteral(HASDESTINATION, "?ID", "?DESTINATIONID"), 1.0, new ULiteral(LOCATIONREFERENCE, "?ID", "?LOCATIONNAME"),
			        new ULiteral(LOCATIONID, "?DESTINATIONID", "?LOCATIONNAME")),

			    // home
			    new USoftRule("S7", new ULiteral(HASDESTINATION, "?ID", "?DESTINATIONID"), 1.0, new ULiteral(LOCATIONREFERENCE, "?ID", "?DESTINATIONID")),

			    new USoftRule("S8", new ULiteral(HASDESTINATIONADDRESS, "?DESTINATIONID", "?DESTINATIONADDRESS"), 1.0, new ULiteral(EVENTLOCATION, "?DESTINATIONID",
			        "?LOCATIONID"), new ULiteral(LOCATIONDISPLAY, "?LOCATIONID", "?DESTINATIONADDRESS")),

			    new USoftRule("S9", new ULiteral(HASDESTINATIONDATE, "?DESTINATIONID", "?DESTINATIONDATE"), 1.0, new ULiteral(DATEREFERENCE, "?DESTINATIONID",
			        "?DESTINATIONDATE")),

			    new USoftRule("S10", new ULiteral(HASDESTINATIONADDRESS, "?DESTINATIONID", "?DESTINATIONADDRESS"), 1.0, new ULiteral(LOCATIONDISPLAY,
			        "?DESTINATIONID", "?DESTINATIONADDRESS")),

			    new USoftRule("S11", new ULiteral(HASANYEVENT, "?ID", "?EVENTID"), 1.0, new ULiteral(HASEMAILEVENT, "?ID", "?EVENTID")),

			    new USoftRule("S12", new ULiteral(HASANYEVENT, "?ID", "?EVENTID"), 1.0, new ULiteral(HASCALENDAREVENT, "?ID", "?EVENTID")),

			    new USoftRule("S13", new ULiteral(HASEMAILEVENT, "?ID", "?EVENTID"), 1.0, new ULiteral(HASEMAIL, "?ID", "?EMAIL"), new ULiteral(RECIPIENT,
			        "?EVENTID", "?EMAIL")),

			    new USoftRule("S14", new ULiteral(HASEMAILEVENT, "?ID", "?EVENTID"), 1.0, new ULiteral(HASEMAIL, "?ID", "?EMAIL"), new ULiteral(SENDER, "?EVENTID",
			        "?EMAIL")),

			    new USoftRule("S15", new ULiteral(HASCALENDAREVENT, "?ID", "?EVENTID"), 1.0, new ULiteral(HASPARTICIPANT, "?EVENTID", "?ID")),

			    new USoftRule("S16", new ULiteral(HASPARTICIPANT, "?EVENTID", "?ID"), 1.0, new ULiteral(HASEVENT, "?ID", "?EVENTID")),

			    new USoftRule("S17", new ULiteral(HASJOINTEVENT, "?ID", "?EVENTID"), 1.0, new ULiteral(JOINTCORRESPONDENCE, "?ID", "?ID2"), new ULiteral(HASANYEVENT,
			        "?ID2", "?EVENTID")),

			    new USoftRule("S18", new ULiteral(EVENTLOCATION, "?EVENTID", "?LOCATIONID"), 1.0, new ULiteral(LOCATIONREFERENCE, "?EVENTID", "?LOCATIONID")),

			    new USoftRule("S19", new ULiteral(EVENTDATE, "?EVENTID", "?DATE"), 1.0, new ULiteral(DATEREFERENCE, "?EVENTID", "?DATE"))

			));

			hardRules = new ArrayList<UHardRule>();
			// hardRules = new ArrayList<UHardRule>(Arrays.asList(new UHardRule("H5", new ULiteral(HASEVENT, "?X", "??Y"))));

			System.out.println("URDF-QUERY: " + queryString + " " + RuleParser.parseQuery(queryString));
			URDF urdf = new URDF(this.getClass().getClassLoader().getResource("mmcidemo.ini").openStream(), softRules, hardRules);

			List<UFactSet> resultFacts = new ArrayList<UFactSet>();
			List<UBindingSet> resultBindings = new ArrayList<UBindingSet>();
			ArrayList<ULineageAnd> resultLineage = new ArrayList<ULineageAnd>();

			urdf.ground(RuleParser.parseQuery(queryString), resultFacts, resultBindings, resultLineage);
			Sampling.getConfAll(resultLineage);

			for (ULineageAnd result : resultLineage)
				System.out.println(result.toString(0));

			int i = 0;
			for (UFactSet facts : resultFacts) {
				// System.out.println("RESULTS: "+ facts);
				int j = 0;
				for (UFact fact : facts) {
					resultString.append(toString(fact));
					if (++j < facts.size())
						resultString.append(";");
				}
				resultString.append("\n");
				i++;
			}

			// System.out.println(resultString.toString());

			resultFacts.clear();
			resultBindings.clear();
			urdf.clear();
			urdf.close();

		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

		return resultString.toString();
	}

	private static String toString(UFact fact) {
		return fact.getRelationName() + "(" + fact.getFirstArgumentName() + "," + fact.getSecondArgumentName() + ")[" + UFact.TRUTH_LABELS[fact.getTruthValue()]
		    + "|" + NumberFormat.getInstance().format(fact.getBaseConfidence()) + "]";
	}

	public static void main(String[] args) {

		String[] queries = new String[] {
		    "hasDestination(Christoph_Stahl,?destinationid);hasDestinationDate(?destinationid,?destinationdate);hasDestinationAddress(?destinationid,?destinationaddress);isWithinMinutes(?destinationdate,\"10.11.2010 08:00:00\", 720)",
		    "hasDestination(Christoph_Stahl,?destinationid);hasDestinationAddress(?destinationid,?destinationaddress);",
		    "hasDestination(Christoph_Stahl,?destinationid);hasDestinationDate(?destinationid,?destinationdate);hasDestinationAddress(?destinationid,?destinationaddress);sameDay(?destinationdate, \"10.11.2010 08:00:00\")",
		    "hasDestination(Christoph_Stahl,?destinationid);hasDestinationDate(?destinationid,?destinationdate);hasDestinationAddress(?destinationid,?destinationaddress);isWithinMinutes(?destinationdate, \"10.11.2010 19:30:00\", 30)" };

		for (String query : queries)
			System.out.println(new URDF_service3().processQuery(query));
	}
}
