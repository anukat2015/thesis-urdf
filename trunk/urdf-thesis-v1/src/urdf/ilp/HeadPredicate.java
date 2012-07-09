package urdf.ilp;

import java.util.*;

/**
 * 
 * @author Christina Teflioudi
 * @since 04.2010
 * @version 1.0
 */

public class HeadPredicate{
	
	private Relation headRelation;
	private Type domain;
	private Type range;
	private int inputArg;
	private RelationsInfo relationsInfo;
	private HashMap<Integer, HashSet<BodyPredicate>> candidateBodyRelations = new HashMap<Integer, HashSet<BodyPredicate>>();
	
	public HeadPredicate(Relation headRelation, int depth, RelationsInfo relationsInfo,int inputArg) {
		
		this.headRelation=headRelation;	
		this.relationsInfo=relationsInfo;
		this.inputArg=(inputArg==-1?headRelation.getInputArg():inputArg);

		this.domain=headRelation.getDomain(); 	//.getFirstParent();
		this.range=headRelation.getRange(); 	//.getFirstParent();

		if (inputArg==2)
			findCandidatesFor2ndArg(new BodyPredicate(this.headRelation), 0, depth,1);
		else
			findCandidatesFor1stArg(new BodyPredicate(this.headRelation), 0, depth,2);			
		
//		Set<Integer> d=candidateBodyRelations.keySet();
//		Iterator<Integer> it=d.iterator();
//		
//		while (it.hasNext())
//		{
//			int index=it.next();
//			System.out.println("Candidates in level "+index+": ");
//			for (int i=0, len=candidateBodyRelations.get(index).size();i<len;i++)
//			{
//				System.out.println(candidateBodyRelations.get(index).get(i).getRelation().getName());
//			}
//		}
		
		
	}
	/**
	 * 
	 * calculates the candidate relations for a given head predicate. I prune according to the depth
	 * I start with the second argument and try to reach the first one
	 * @param relation
	 * @param proc
	 * @param d
	 * @param depth
	 * @param bindOnArg 
	 * @return
	 */
	private boolean findCandidatesFor2ndArg(BodyPredicate bodyPred, int d, int depth, int bindOnArg) {
		
		boolean canCloseConnection=false;
		BodyPredicate newPredicate;


		if (candidateBodyRelations.get(d)==null)
			candidateBodyRelations.put(d, new HashSet<BodyPredicate>());

		if (d<depth) {
			if (bindOnArg==2) {
				if(relationsInfo.arg1JoinOnArg1.get(this.headRelation)!=null && relationsInfo.arg1JoinOnArg1.get(this.headRelation).contains(bodyPred.getRelation())) {
					canCloseConnection=true;
					candidateBodyRelations.get(d).add(bodyPred);
					bodyPred.addRelation(this.headRelation, 2);
				}
				if (relationsInfo.arg1JoinOnArg1.get(bodyPred.getRelation())!=null) {
					
					for (Relation joinableArg1Arg1: relationsInfo.arg1JoinOnArg1.get(bodyPred.getRelation())) {
						newPredicate=createNewOrGetPredicate(d, joinableArg1Arg1);

						if (findCandidatesFor2ndArg(newPredicate,d+1,depth,1))  {
							canCloseConnection=true;
							candidateBodyRelations.get(d).add(bodyPred);
							bodyPred.addRelation(joinableArg1Arg1, 1)	;			
						}

					}
				}

				if (relationsInfo.arg1JoinOnArg2.get(bodyPred.getRelation())!=null) {
					
					for (Relation joinableArg1Arg2: relationsInfo.arg1JoinOnArg2.get(bodyPred.getRelation())) {
						newPredicate=createNewOrGetPredicate(d, joinableArg1Arg2);

						if (findCandidatesFor2ndArg(newPredicate,d+1,depth,2))  {
							canCloseConnection=true;
							candidateBodyRelations.get(d).add(bodyPred);
							bodyPred.addRelation(joinableArg1Arg2, 2)	;			
						}
					}
				}
			}



			if (bindOnArg==1) {
				if(relationsInfo.arg1JoinOnArg2.get(this.headRelation)!=null && relationsInfo.arg1JoinOnArg2.get(this.headRelation).contains(bodyPred.getRelation())) {
					canCloseConnection=true;
					candidateBodyRelations.get(d).add(bodyPred);
					bodyPred.addRelation(this.headRelation, 1);
				}
				if (relationsInfo.arg2JoinOnArg1.get(bodyPred.getRelation())!=null) {
					for (Relation joinableArg2Arg1: relationsInfo.arg2JoinOnArg1.get(bodyPred.getRelation())) {
						newPredicate=createNewOrGetPredicate(d, joinableArg2Arg1);

						if (findCandidatesFor2ndArg(newPredicate,d+1,depth,1))  {
							canCloseConnection=true;
							candidateBodyRelations.get(d).add(bodyPred);
							bodyPred.addRelation(joinableArg2Arg1, 3)	;			
						}
					}
				}

				if (relationsInfo.arg2JoinOnArg2.get(bodyPred.getRelation())!=null) {
					for (Relation joinableArg2Arg2: relationsInfo.arg2JoinOnArg2.get(bodyPred.getRelation())) {
						newPredicate=createNewOrGetPredicate(d, joinableArg2Arg2);

						if (findCandidatesFor2ndArg(newPredicate,d+1,depth,2))  {
							canCloseConnection=true;
							candidateBodyRelations.get(d).add(bodyPred);
							bodyPred.addRelation(joinableArg2Arg2, 4)	;			
						}
					}
				}
			}
			return canCloseConnection;
		}
		else {
			// TODO Fix logic
			if ((bindOnArg==2 && relationsInfo.arg1JoinOnArg1.get(this.headRelation)!=null && relationsInfo.arg1JoinOnArg1.get(this.headRelation).contains(bodyPred.getRelation()))
					||(bindOnArg==1 && relationsInfo.arg1JoinOnArg2.get(this.headRelation)!=null && relationsInfo.arg1JoinOnArg2.get(this.headRelation).contains(bodyPred.getRelation())))
			{
				candidateBodyRelations.get(d).add(bodyPred);

				if (bindOnArg==1)
					bodyPred.addRelation(this.headRelation, 1);
				else
					bodyPred.addRelation(this.headRelation, 2);
				
				return true;
			}
			return false;
		}
	}
	
	/**
	 * 
	 * calculates the candidate relations for a given head predicate. I prune according to the depth
	 * I start with the first argument and try to reach the second one
	 * @param relation
	 * @param proc
	 * @param d
	 * @param depth
	 * @param bindOnArg 
	 * @return
	 */
	private boolean findCandidatesFor1stArg(BodyPredicate bodyPred, int d, int depth, int bindOnArg) {
		
		boolean canCloseConnection=false;
		BodyPredicate newPredicate;


		if (candidateBodyRelations.get(d)==null)
			candidateBodyRelations.put(d, new HashSet<BodyPredicate>());

		if (d<depth) {
			if (bindOnArg==2) {
				if(relationsInfo.arg2JoinOnArg1.get(this.headRelation)!=null && relationsInfo.arg2JoinOnArg1.get(this.headRelation).contains(bodyPred.getRelation())) {
					canCloseConnection=true;
					candidateBodyRelations.get(d).add(bodyPred);
					bodyPred.addRelation(this.headRelation, 2);
				}
				if (relationsInfo.arg1JoinOnArg1.get(bodyPred.getRelation())!=null) {
					for (int i=0,len=relationsInfo.arg1JoinOnArg1.get(bodyPred.getRelation()).size();i<len;i++) {
						newPredicate=createNewOrGetPredicate(d, relationsInfo.arg1JoinOnArg1.get(bodyPred.getRelation()).get(i));

						if (findCandidatesFor1stArg(newPredicate,d+1,depth,1)) {
							canCloseConnection=true;
							candidateBodyRelations.get(d).add(bodyPred);
							bodyPred.addRelation(relationsInfo.arg1JoinOnArg1.get(bodyPred.getRelation()).get(i), 1)	;			
						}

					}
				}

				if (relationsInfo.arg1JoinOnArg2.get(bodyPred.getRelation())!=null){
					for (int i=0,len=relationsInfo.arg1JoinOnArg2.get(bodyPred.getRelation()).size();i<len;i++) {
						newPredicate=createNewOrGetPredicate(d, relationsInfo.arg1JoinOnArg2.get(bodyPred.getRelation()).get(i));

						if (findCandidatesFor1stArg(newPredicate,d+1,depth,2)) {
							canCloseConnection=true;
							candidateBodyRelations.get(d).add(bodyPred);
							bodyPred.addRelation(relationsInfo.arg1JoinOnArg2.get(bodyPred.getRelation()).get(i), 2)	;			
						}
					}
				}
			}


			if (d>0) {
				if (bindOnArg==1) {
					if(relationsInfo.arg2JoinOnArg2.get(this.headRelation)!=null && relationsInfo.arg2JoinOnArg2.get(this.headRelation).contains(bodyPred.getRelation())) {
						canCloseConnection=true;
						candidateBodyRelations.get(d).add(bodyPred);
						bodyPred.addRelation(this.headRelation, 1);
					}
					if (relationsInfo.arg2JoinOnArg1.get(bodyPred.getRelation())!=null) {
						for (int i=0,len=relationsInfo.arg2JoinOnArg1.get(bodyPred.getRelation()).size();i<len;i++) {
							newPredicate=createNewOrGetPredicate(d, relationsInfo.arg2JoinOnArg1.get(bodyPred.getRelation()).get(i));

							if (findCandidatesFor1stArg(newPredicate,d+1,depth,1))  {
								canCloseConnection=true;
								candidateBodyRelations.get(d).add(bodyPred);
								bodyPred.addRelation(relationsInfo.arg2JoinOnArg1.get(bodyPred.getRelation()).get(i), 3)	;			
							}
						}
					}

					if (relationsInfo.arg2JoinOnArg2.get(bodyPred.getRelation())!=null) {
						for (int i=0,len=relationsInfo.arg2JoinOnArg2.get(bodyPred.getRelation()).size();i<len;i++) {
							newPredicate=createNewOrGetPredicate(d, relationsInfo.arg2JoinOnArg2.get(bodyPred.getRelation()).get(i));

							if (findCandidatesFor1stArg(newPredicate,d+1,depth,2))  {
								canCloseConnection=true;
								candidateBodyRelations.get(d).add(bodyPred);
								bodyPred.addRelation(relationsInfo.arg2JoinOnArg2.get(bodyPred.getRelation()).get(i), 4)	;			
							}
						}
					}
				}
			}
			return canCloseConnection;
		}
		else {
			if ((bindOnArg==2 && relationsInfo.arg2JoinOnArg1.get(this.headRelation)!=null && relationsInfo.arg2JoinOnArg1.get(this.headRelation).contains(bodyPred.getRelation()))
					||(bindOnArg==1 && relationsInfo.arg2JoinOnArg2.get(this.headRelation)!=null && relationsInfo.arg2JoinOnArg2.get(this.headRelation).contains(bodyPred.getRelation())))
			{
				candidateBodyRelations.get(d).add(bodyPred);
				
				if (bindOnArg==1)
					bodyPred.addRelation(this.headRelation, 1);
				else
					bodyPred.addRelation(this.headRelation, 2);
				
				return true;
			}
			return false;
		}
	}

	private BodyPredicate createNewOrGetPredicate(int d, Relation relation) {
		if (candidateBodyRelations.get(d+1)==null)
			return new BodyPredicate(relation);

		for (BodyPredicate bp : candidateBodyRelations.get(d+1)) {
			if (bp.hasRelation(relation)) {
				//System.out.println(candidateBodyRelations.get(d+1).get(i).getRelation().getName());
				return bp;
			}  
		}
		return new BodyPredicate(relation);
	}


	//************** GET METHODS*************************
	public Relation getHeadRelation() {
		return headRelation;
	}
	
	public HashMap<Integer, HashSet<BodyPredicate>> getCandidateBodyRelations() {
		return candidateBodyRelations;
	}
	
	public Type getDomain() {
		return this.domain;
	}
	
	public Type getRange() {
		return this.range;
	}
	
	public int getInputArg() {
		return this.inputArg;
	}

}
