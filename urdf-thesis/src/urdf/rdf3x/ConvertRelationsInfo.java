package urdf.rdf3x;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import urdf.ilp.Relation;
import urdf.ilp.RelationsInfo;
import urdf.ilp.Type;

public class ConvertRelationsInfo {
	
	private static final String prefRDFSyntax = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String prefRDFSchema = "<http://www.w3.org/2000/01/rdf-schema#";
	private static final String prefYAGO = "<http://yago-knowledge.org/resource/";
	
	public static void main(String[] args) {
    	FileInputStream fileIn;
    	ObjectInputStream in;
    	RelationsInfo relationsInfo;
    	try {
			fileIn =new FileInputStream("relationsInfoForYago2.ser");
			in = new ObjectInputStream(fileIn);
	    	relationsInfo = (RelationsInfo) in.readObject();       
	    	in.close();
	    	fileIn.close();
	    	
	    	Hashtable<String,Relation> relations = relationsInfo.getAllRelations();
	    	Hashtable<String,Type> types = relationsInfo.getAllTypes();
	    	
	    	Hashtable<String,Relation> newRelations = (Hashtable<String, Relation>) relations.clone();
	    	for (String key: newRelations.keySet()) {
	    		Relation r = relations.get(key);
	    		//relations.remove(key);
	    		String newkey = null;
    			if (key.equals("type")) newkey = prefRDFSyntax + "type>";
    			else if (key.equals("hasRange")) newkey = prefRDFSchema + "range>"; 
    			else if (key.equals("hasDomain")) newkey = prefRDFSchema + "domain>"; 
    			else if (key.equals("hasLabel")) newkey = prefRDFSchema + "label>"; 
    			else if (key.equals("isSubClassOf")) newkey = prefRDFSchema + "subClassOf>"; 	
    			else if (key.equals("isSubPropertyOf")) newkey = prefRDFSchema + "subPropertyOf>";
    			else newkey = prefYAGO + key + ">";
    			
    			r.setName(newkey);
    			relations.put(newkey, r);	
	    	}
	    	
	    	Hashtable<String,Type> newTypes = (Hashtable<String, Type>) types.clone();
	    	for (String key: newTypes.keySet()) {
	    		Type t = types.get(key);
	    		types.remove(key);
	    		
	    		String newkey = prefYAGO + key;
    			
    			t.setName(newkey);
    			types.put(newkey, t);	
	    	}
	    	
	    	for (Relation r : relationsInfo.arg1JoinOnArg1.keySet()) {
	    		System.out.println(r.getName());
	    	}
	    	
	    	
	    	FileOutputStream fileOut = new FileOutputStream("relationsInfoForRDF3X.ser");	    	  
		    ObjectOutputStream out = new ObjectOutputStream(fileOut);
		    out.writeObject(relationsInfo);
		         
		    out.close();
	        fileOut.close();
	    	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
 
    	
    	
	}
}
