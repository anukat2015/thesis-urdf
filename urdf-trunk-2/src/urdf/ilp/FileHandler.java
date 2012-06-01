package urdf.ilp;



import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


/**
 * 	@author Christina Teflioudi
 *	
 *	Class that helps me manipulate files containing facts
 *	and construct files for alchemy
 */
public class FileHandler 
{
	public void filterRuleFile(String file,float threshold, boolean forURDF)throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));		
		BufferedWriter out= new BufferedWriter(new FileWriter(file.substring(0, file.length()-".txt".length())+"_filter"+threshold+".txt"));
		String rule="", measures="", parameters="",read;
		float conf;
		
		read = in.readLine();
		do
		{
			if (!forURDF)
			{
				if (read.indexOf("<-")>0)
				{
					rule=read;
				}
				else if (read.indexOf("confidence")>=0)
				{
					measures=read;
				}
				else if (read.indexOf("N+(c)")>=0)
				{
					parameters=read;
					//measures.substring("confidence: ".length(), measures.indexOf(" support: "));
					
					conf = Float.valueOf(measures.substring("confidence: ".length(), measures.indexOf(" support: ")).trim()).floatValue();
					
					if (conf>=threshold)
					{
						out.write("\n");
						out.write(rule+"\n");
						out.write(measures+"\n");
						out.write(parameters+"\n");
					}
					
				}
			}
			else
			{
				if(read.contains("<="))
				{
					conf = Float.valueOf(read.substring(read.indexOf("[")+1 , read.indexOf("]")).trim()).floatValue();
					
					if (conf>=threshold)
					{
						out.write(read+"\n");
					}
				}
			}
						
			read = in.readLine();
		}while(read!=null);
		
		
		in.close();
		out.flush();
		out.close();
		
	}
	
	public void findAccuracyForRules(String file) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(file));
		BufferedWriter out= new BufferedWriter(new FileWriter("new_"+file));
		
		String rule="", measures="", parameters="", read="";
		int positives=0, body=0;
		float accuracy;
		
		read = in.readLine();
		do
		{
			if (read.indexOf("<-")>0)
			{
				rule=read;
			}
			else if (read.indexOf("confidence")>=0)
			{
				measures=read;
			}
			else if (read.indexOf("N+(c)")>=0)
			{
				parameters=read;
				positives=Integer.parseInt(read.substring(read.indexOf("N+(c):")+7, read.indexOf("E+(c)")-1));				
				body=Integer.parseInt(read.substring(read.indexOf("B(c):")+6, read.indexOf("E+:")-1));				
				accuracy=((float)positives)/(float)body;
				
				out.write("\n");
				out.write(rule+"\n");
				out.write(measures+" acc: "+accuracy+"\n");
				out.write(parameters+"\n");
			}
			
			
			read = in.readLine();
		}while(read!=null);
		
		in.close();
		out.flush();
		out.close();
		
	}
	public void normalizeQueryFile(String inQueryName, String relation) throws IOException
	{
   		   		
   		int startOfFirstArg=0;
   		int startOfSecondArg=0;
   		String firstArg="";
   		String secondArg="";
   		String read;   	
   		String s="";
   		String sign;
   		//Reader in=new UTF8Reader(new File(inQueryName));

   		BufferedReader in = new BufferedReader(new FileReader(inQueryName));
   		BufferedWriter out= new BufferedWriter(new FileWriter("out"+inQueryName));
   		BufferedWriter out2= new BufferedWriter(new FileWriter("intermediate_"+relation));
   		
   		
   		read =  in.readLine();
		while(read!=null)//read a line from file and save into a string
   		{	
			read = in.readLine();
			if (read!=null && read.indexOf(relation)>=0)
			{
				if (read.indexOf(relation)>0)
				{
					sign=read.substring(read.indexOf(relation)-1, read.indexOf(relation));
				}
				else
				{
					sign="";
				}
				
				startOfFirstArg=read.indexOf("(")+1;
				firstArg=read.substring(startOfFirstArg, read.indexOf(", "));
				//System.out.println(firstArg);
				
				startOfSecondArg=read.indexOf(", ")+2;
				secondArg=read.substring(startOfSecondArg, read.lastIndexOf(")"));
				//System.out.println(secondArg);
				
				s=relation+"("+repairString(firstArg)+","+repairString(secondArg)+") \n";						
				out.write(s);
				s=sign+s;
				out2.write(s);
			}
			
   		}
		out.flush();
		out.close();
		out2.flush();
		out2.close();
		in.close();
	}
	
	public void filterResultFile(String relation,String inputFile, double threshold)throws IOException
	{
		String read, strProb;
		int startOfProbability,countAll=0,countPassed=0;
		
	
   		BufferedReader in = new BufferedReader(new FileReader(inputFile));
   		//BufferedReader in = new BufferedReader(new FileReader(relation+".results"));
   		BufferedWriter out= new BufferedWriter(new FileWriter(relation+"_Filt_"+threshold+".results"));
   		

		do//read a line from file and save into a string
   		{	
			read = in.readLine();
			
			if (read!=null)// && read.indexOf(relation)>=0)
			{
				
				startOfProbability=read.lastIndexOf(") ")+2;
				
				strProb=read.substring(startOfProbability, read.length());
				
				System.out.println(read);

				
				countAll++;
				if (Double.parseDouble(strProb)>threshold)
				{
					countPassed++;					
					out.write(read+"\n");
				}
								
			}
			
   		}
		while(read!=null);
			
		out.write("\n \nNumber of facts that existed initially: "+countAll+"\n");
		out.write("Number of facts that passed the threshold: "+countPassed+"\n");
		out.write("Threshold: "+threshold);
		out.flush();
		out.close();
		in.close();  
		//in2.close();
	}

	public void sampleFile(int num,String file) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(file));
		BufferedWriter out= new BufferedWriter(new FileWriter("sample_"+num+"_"+file));
		String read="",s;
		ArrayList<String> str=new ArrayList<String>(1000);
		ArrayList<String> str2=new ArrayList<String>(num);
		
		
		
		do//read a line from file and save into a string
   		{	
			read = in.readLine();
			
			if (read!=null)// && read.indexOf(relation)>=0)
			{				
				str.add(read);								
			}
			
   		}
		while(read!=null);
		Random ran=new Random();
		
		if (str.size()<num)
			return;
		
		
		
			while (str2.size()<num)
			{
				s=str.get(ran.nextInt(str.size()));
				if (!str2.contains(s))
				{
					out.write(s+"\n");
					str2.add(s);
				}
				
							
			}
			out.write("number of facts: "+num);
		
		
		out.flush();
		out.close();
		in.close();
		
	}
	
	
/*	public void filterResultFile(String relation,double threshold)throws IOException
	{
		String read, strProb, intermediate,sign;
		int startOfProbability, countAll=0,countPositives=0,countPassed=0;
		
	
   		BufferedReader in = new BufferedReader(new FileReader("C:/Documents and Settings/chteflio/Desktop/politicianOfLazy.results"));
   		//BufferedReader in = new BufferedReader(new FileReader(relation+".results"));
   		//BufferedReader in2 = new BufferedReader(new FileReader("intermediate_"+relation));
   		BufferedWriter out= new BufferedWriter(new FileWriter(relation+"_Filt_"+threshold+".results"));
   		
   		//read =  in.readLine();
   		//intermediate=in2.readLine();
		do//read a line from file and save into a string
   		{	
			read = in.readLine();
			//intermediate=in2.readLine();
			if (read!=null)// && read.indexOf(relation)>=0)
			{
				
				startOfProbability=read.lastIndexOf(") ")+2;
				strProb=read.substring(startOfProbability, startOfProbability+5);
				
				System.out.println(read);
				//System.out.println(intermediate);
				//if (intermediate.indexOf(read.substring(0,startOfProbability))>0)
				//{
				//	sign=intermediate.substring(intermediate.indexOf(relation)-1, intermediate.indexOf(relation));
				//}
				//else
				//{
				//	sign="";
				//}
				
				//countAll++;
				//if (Double.parseDouble(strProb)>=threshold)
				//{
				//	countPassed++;
				//	if (sign.equals("+"))
				//	{
				//		countPositives++;
				//	}
				//	out.write(sign+read+"\n");
				//}
								
			}
			
   		}
		while(read!=null);
			
		out.write("\n \nNumber of facts that existed initially: "+countAll+"\n");
		out.write("Number of facts that passed the threshold: "+countPassed+"\n");
		out.write("Number of positive facts that passed the threshold: "+countPositives+"\n");
		out.write("Threshold: "+threshold);
		out.flush();
		out.close();
		in.close();  
		//in2.close();
	}*/
  	private String repairString(String s)
  	{
		StringBuffer sb = new StringBuffer();
		
		int index=s.indexOf("\\'");
		while (index>=0)
		{
			
			s=s.substring(0,index)+s.substring(index+4, s.length());
			index=s.indexOf("\\'");
		}
		
		for(int x = 0 ; x < s.length() ; x++) 
		{
			char c = s.charAt(x);
			if(!((c>=65 && c<=90)||(c>=97 && c<=122)))
			{
				//Here you can append anything, or you can simply append nothing as I did.
				// sb.append("&#"); 
				// sb.append((int) c);
				sb.append("");
			}
			else 
			{
				sb.append(c);
			}
		}
		return "A"+sb.toString();
  	}
  	
  	/**
  	 * @param evalFile the file that contains the already evaluated facts
  	 * @param fileToBeEval the file with the facts to be evaluated
  	 * @param relation the relation of the facts (e.g. politicianOf)
  	 * @throws IOException
  	 */
  	public void helpEvaluation(String evalFile, String fileToBeEval, String relation, boolean forAlchemy) throws IOException
  	{
  		System.out.println(fileToBeEval);
  		BufferedReader in2 = new BufferedReader(new FileReader(fileToBeEval));
  		BufferedWriter out= new BufferedWriter(new FileWriter(fileToBeEval+"new"));
  		
  		String read1,read2="",sign,s="";

  		if (!forAlchemy)
  		{
  	  		for (int i=0;i<15;i++)
  	  		{
  	  			read2 =  in2.readLine();
  	  			out.write(read2+"\n");
  	  		}
  		}
  		while (read2!=null)
  		{
  			read2 =  in2.readLine();
  			if (read2!=null)
  			{
  				//System.out.println("=========================");
  	  			//System.out.println("read2: "+read2);
  	  			BufferedReader in1 = new BufferedReader(new FileReader(evalFile));
  	  			read1 = in1.readLine();

  	  			while (read1!=null)
  	  			{
  	  				read1 = in1.readLine();	

  	  				//System.out.println("read1: "+read1);
  	  				if (read1!=null && read1.indexOf(relation)>=0)
  	  				{
  	  					s=read2;
  	  					
  	  					if (read1.indexOf(relation)>0)
  	  					{
  	  						sign=read1.substring(read1.indexOf(relation)-1, read1.indexOf(relation));
  	  					}
  	  					else
  	  					{
  	  						sign="";
  	  					}
  	  					//s=read1.substring(read1.indexOf(relation),read1.lastIndexOf(")"));
  	  					//System.out.println(s);

  	  					// if read2 contains read1
  	  					if (read2.indexOf(read1.substring(read1.indexOf(relation),read1.lastIndexOf(")")))	>=0)
  	  					{
  	  						s=sign+read2;	
  	  						break;
  	  						
  	  					}
  	  				}	
  	  			}
  	  			out.write(s+"\n");
  	  			//in1.close();
  			}
  			
  		}
		out.flush();
		out.close();
		in2.close();
  		
  	}
  	public void sillyMethodForMLN(ArrayList<String> files) throws IOException
  	{
  		String read;
  		boolean flag,flag2=false;
  		BufferedReader in;
  		BufferedWriter out= new BufferedWriter(new FileWriter("isMarriedToCom.mln"));
  		
  		out.write("A={");
  		
  		for (int i=0;i<files.size();i++)
  		{
  			in=new BufferedReader(new FileReader(files.get(i)));
  			flag=false;
  			
  			do//read a line from file and save into a string
  	   		{	
  				read = in.readLine();
  				
  				if (read!=null)// && read.indexOf(relation)>=0)
  				{				
  					if (read.indexOf("A={")>=0)
  					{
  						read=read.substring(3);
  						flag=true;
  					}
  					if (read.indexOf("}")>=0)
  					{
  						flag=false;
  						//break;
  					}
  					if (flag)
  					{
  						if (flag2)
  						{
  							out.write("\n");
  						}
  						
  						out.write(read);
  						flag2=true;
  					}
  					
  					
  												
  				}
  				
  	   		}
  			while(read!=null);
  			
  			if (i<files.size()-1)
  			{
  				out.write(",");
  			}
  			
  			
  			in.close();	
  		}
  		out.write("}"+"\n");
  		
  		
  		
  		out.write("isMarriedTo(A, A) \n");
  		out.write("notEquals(A, A) \n");
  		
  		out.write("-6.24412  isMarriedTo(a1,a2) \n");
  		out.write("6.83045  isMarriedTo(a1,a2) v !isMarriedTo(a2,a1) v !notEquals(a1,a2) \n");
  		
  		
  		out.flush();
  		out.close();
  		
  	}
  	public void sillyMethodForDB(ArrayList<String> files) throws IOException
  	{
  		String read;
  		BufferedReader in;
  		BufferedWriter out= new BufferedWriter(new FileWriter("isMarriedToCom.db"));
  		
  		for (int i=0;i<files.size();i++)
  		{
  			in=new BufferedReader(new FileReader(files.get(i)));
  			
  			
  			do//read a line from file and save into a string
  	   		{	
  				read = in.readLine();
  				
  				if (read!=null)// && read.indexOf(relation)>=0)
  				{				
  					if (read.indexOf("isMarriedTo")>=0 || read.indexOf("notEquals")>=0)
  					{
  						out.write(read+"\n");
  					}
  					
  					
  												
  				}
  				
  	   		}
  			while(read!=null);
  			
  			
  			
  			
  			in.close();	
  		}
  		
  		out.flush();
  		out.close();
  	}
  	
	public void filterResultFileForProbURDF(String file, double threshold)throws IOException
	{
		String read, strProb;
		int startOfProbability, endOfProbability, countAll=0,countPassed=0;
		
	
   		BufferedReader in = new BufferedReader(new FileReader(file+".out"));
   		BufferedWriter out= new BufferedWriter(new FileWriter(file+"_Filt_"+threshold+".out"));
   		

		do//read a line from file and save into a string
   		{	
			read = in.readLine();
			
			if (read!=null)// && read.indexOf(relation)>=0)
			{
				
				if (read.lastIndexOf('@')>=0)
				{
					//System.out.println(read);
					startOfProbability=read.lastIndexOf('@')+1;
					
					
					endOfProbability=read.length();
					strProb=read.substring(startOfProbability, (endOfProbability-startOfProbability>5?startOfProbability+5:endOfProbability));
					
					
					//System.out.println(intermediate);
					//if (intermediate.indexOf(read.substring(0,startOfProbability))>0)
					//{
					//	sign=intermediate.substring(intermediate.indexOf(relation)-1, intermediate.indexOf(relation));
					//}
					//else
					//{
					//	sign="";
					//}
					
					countAll++;
					if (Double.parseDouble(strProb)>=threshold)
					{
						countPassed++;					
						out.write(read+"\n");
					}
				}
				else
				{
					out.write(read+"\n");
				}
				

								
			}
			
   		}
		while(read!=null);
			
		out.write("\n \nNumber of facts that existed initially: "+countAll+"\n");
		out.write("Number of facts that passed the threshold: "+countPassed+"\n");
		out.write("Threshold: "+threshold);
		out.flush();
		out.close();
		in.close();  
	}
	public void removeProbabilities(String file)throws IOException
	{
		String read, str;
		int startOfProbability;
		
	
   		BufferedReader in = new BufferedReader(new FileReader(file+".out"));
   		BufferedWriter out= new BufferedWriter(new FileWriter(file+"_new.out"));
   		

		do//read a line from file and save into a string
   		{	
			read = in.readLine();
			
			if (read!=null)// && read.indexOf(relation)>=0)
			{
				
				if (read.lastIndexOf('@')>=0)
				{
					//System.out.println(read);
					startOfProbability=read.lastIndexOf('@');				
					str=read.substring(0, startOfProbability);
					out.write(str+"\n");
				}			
			}
			
   		}
		while(read!=null);
		
		out.flush();
		out.close();
		in.close();  
	}
  	public void removeDuplicates(String inFile) throws Exception
  	{
  		System.out.println(inFile);
  		boolean existsFlag;
  		BufferedReader in1 = new BufferedReader(new FileReader(inFile));
  		BufferedWriter out= new BufferedWriter(new FileWriter(inFile+"new"));
  		
  		
  		String read1,read2="";
  		
  		read1 =  in1.readLine();
  		while (read1!=null)
  		{
  			read1 =  in1.readLine();
  			
  			if (read1!=null)
  			{
  				if (read1.lastIndexOf(")")<0)
  				{
  					continue;
  				}
  				System.out.println("=========================");
  	  			System.out.println("read1: "+read1);
  	  			
  				read1 =read1.substring(0, read1.lastIndexOf(")")+1);				
  				
  				BufferedReader in2 = new BufferedReader(new FileReader(inFile+"new"));
  	  			read2 = in2.readLine();
  	  			existsFlag=false;
  	  			while (read2!=null)
  	  			{
  	  				
  	  				read2 = in2.readLine();	
  	  				
  	  				
  	  				//System.out.println("read1: "+read1);
  	  				if (read2!=null)
  	  				{
  	  					System.out.println("read2:"+read2);
  	  					if (read2.lastIndexOf(")")<0)
  	  					{
  	  						continue;
  	  					}
  	  					read2= read2.substring(0, read2.lastIndexOf(")")+1);
  	  					if (read1.equals(read2))
  	  					{
  	  						existsFlag=true;
  	  						break;
  	  					}  	  					
  	  				}	
  	  			}
  	  			
  	  			if (!existsFlag)
  	  			{
  	  				out.write(read1+"\n");
  	  			}
  	  			
  	  			in2.close();
  			}
  			
  		}
  		
		out.flush();
		out.close();
		in1.close();
  		
  	}
  	
  	public void dummyForRules(String inFile) throws Exception
  	{
   		BufferedReader in = new BufferedReader(new FileReader(inFile));
   		BufferedWriter out= new BufferedWriter(new FileWriter("new_"+inFile));
   		
   		String s=in.readLine();
   		String str;
   		
   		//s="S0: "+s;
   		int index=0;
   		
		StringBuffer sb = new StringBuffer();
		for(int x = 0 ; x < s.length() ; x++) 
		{
			char c = s.charAt(x);
			sb.append(c);
			if(c==93)// the ] character
			{
				//sb.append(" \nS"+index+": ");
				str="S"+index+": "+sb.toString();
				out.write(str+"\n");
				index++;
				sb= new StringBuffer();
			}

		}
		
		out.flush();
		out.close();
   		
  		
  	}
  	
  	public static void main (String[] args)
  	{
  		try 
  		{
  			
			FileHandler fh=new FileHandler();
			
//			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/politicianOfsupp0.0010_conf0.01_spec0.0_possPos1.txt", 0.12f, false);
//			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/graduatedFromsupp0.0010_conf0.01_spec0.0_possPos1.txt", 0.12f, false);
//			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/livesInsupp0.0010_conf0.01_spec0.0_possPos1.txt", 0.12f, false);
//			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/locatedInsupp0.0010_conf0.01_spec0.0_possPos1.txt", 0.12f, false);
//			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/bornInsupp0.0010_conf0.01_spec0.0_possPos1.txt", 0.12f, false);
			
			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/politicianOfsupp0.0010_conf0.01_spec0.0_possPos1ForURDF.txt", 0.12f, true);
			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/graduatedFromsupp0.0010_conf0.01_spec0.0_possPos1ForURDF.txt", 0.12f, true);
			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/livesInsupp0.0010_conf0.01_spec0.0_possPos1ForURDF.txt", 0.12f, true);
			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/locatedInsupp0.0010_conf0.01_spec0.0_possPos1ForURDF.txt", 0.12f, true);
			fh.filterRuleFile("D:/My Workspace/JavaWorkspace/URDF/withAccuracy/bornInsupp0.0010_conf0.01_spec0.0_possPos1ForURDF.txt", 0.12f, true);
			
			//fh.removeProbabilities("sample_100_politicianOfOnlyRules350_Filt_0.8new");
			//fh.findAccuracyForRules("accuracy.rtf"); 
			
			
			Double[] thresholds={0.6,0.8,0.90,0.95};			
			
/*			String[] files = {
					  "politicianOfOnlyRules150",
					  "hasChildOnlyRules150",
					  "hasPredecessorOnlyRules150",
					  "isMarriedToOnlyRules150",
					  "livesInOnlyRules150",
					  "politicianOfOnlyRules250",
					  "graduatedFromOnlyRules250",
					  "hasChildOnlyRules250",
					  "hasPredecessorOnlyRules250",
					  "hasSuccessorOnlyRules250",
					  "isMarriedToOnlyRules250",
					  "livesInOnlyRules250",
					  "locatedInOnlyRules250",
					  "politicianOfOnlyRules050",
					  "hasChildOnlyRules050",
					  "livesInOnlyRules050",
					  "politicianOfOnlyRules180",
					  "politicianOfOnlyRules080",
					  "hasChildOnlyRules080",
					  "hasPredecessorOnlyRules080",
					  "hasSuccessorOnlyRules080",
					  "politicianOfOnlyRules380",
					  "diedInOnlyRules380",
					  "livesInOnlyRules380",
					  "politicianOfOnlyRules1999",
					  "politicianOfOnlyRules3999"
					  };*/
			
/*			String[] files = {
					  "politicianOfRulesAndDB3999",
					  "politicianOfOnlyRules3999"
					  };
			
			for (int i=0;i<files.length;i++)
			{
				System.out.println("FILE PROCESSED: "+files[i]);
				
				for (int j=0;j<thresholds.length;j++)
				{
					System.out.println("Threshold : "+thresholds[j]);
					
					System.out.println("Filtering....");
					fh.filterResultFileForProbURDF(files[i], thresholds[j]);
					
					System.out.println("Sampling....");
					fh.sampleFile(100, files[i]+"_Filt_"+thresholds[j]+".out");
					
					
				}				
				
			}*/
	
			//fh.removeDuplicates("livesIn.rtf");
			
			
			//fh.helpEvaluation("hasChild.rtf", "sample_100_politicianOfOnlyRules150_Filt_0.6.out", "hasChild",false);

			//fh.filterResultFile("hasChild", "hasChildTest503.results", 0.01);

			
			//fh.sillyMethodForDB(files);			
			//fh.filterResultFile("locatedIn","locatedIn.results",0.004);			
			//fh.normalizeQueryFile("evalHasChild.rtf", "hasChild");
			//fh.sampleFile(30, "hasChild_Filt_0.06.results");
			
			//fh.helpEvaluation("diedIn.rtf", "diedInOnlyRules380.out", "diedIn",true);			
			//fh.helpEvaluation("bornIn.rtf", "bornInOnlyRules350.out", "bornIn",true);		
			//fh.helpEvaluation("hasChild.rtf", "sample_100_hasChildOnlyRules080_Filt_0.6.out", "hasChild",true);
			//fh.helpEvaluation("isMarriedTo.rtf", "isMarriedToOnlyRules350_Filt_0.9.out", "isMarriedTo",false);
			//fh.helpEvaluation("livesIn.rtf", "livesInOnlyRules380.out", "livesIn",false);
			//fh.helpEvaluation("hasSuccessor.rtf", "sample_100_hasSuccessorOnlyRules250_Filt_0.8.out", "hasSuccessor",true);			
			//fh.helpEvaluation("politicianOf.rtf", "sample_100_politicianOfOnlyRules350_Filt_0.6.out", "politicianOf",true);
			//fh.helpEvaluation("hasPredecessor.rtf", "sample_100_hasPredecessorOnlyRules350_Filt_0.6.out", "hasPredecessor",true);	
			//fh.helpEvaluation("locatedIn.rtf", "sample_100_locatedInOnlyRules350_Filt_0.6.out", "locatedIn",true);	
			//fh.helpEvaluation("graduatedFrom.rtf", "graduatedFromOnlyRules350_Filt_0.8.out", "graduatedFrom",true);

			//fh.dummyForRules("politicianOfForURDF.txt");
			//fh.dummyForRules("graduatedFromForURDF.txt");
			//fh.dummyForRules("hasPredecessorForURDF.txt");
			//fh.dummyForRules("hasSuccessorForURDF.txt");
			//fh.dummyForRules("isMarriedToForURDF.txt");
			//fh.dummyForRules("hasChildForURDF.txt");
			//fh.dummyForRules("livesInForURDF.txt");
			//fh.dummyForRules("locatedInForURDF.txt");
			//fh.dummyForRules("diedInForURDF.txt");
			//fh.dummyForRules("graduatedFromForURDF.txt");

			
			
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	}
  	
  	
}
