package urdf.ilp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.rdf3x.ResultSet;
import urdf.rdf3x.Statement;

public class RangingAlgorithm {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Connection conn = Driver.connect("src/rdf3x.properties");
			Statement stmt = (Statement) conn.createStatement();
			//String query = "select count ?u where {{?x <http://yago-knowledge.org/resource/directed> ?c . ?c <http://yago-knowledge.org/resource/hasDuration> ?u} match {?x <http://yago-knowledge.org/resource/actedIn> ?c}} order by asc(?u)";
			String query = "select count ?u where {{?x <http://yago-knowledge.org/resource/wasBornOnDate> ?u} match {?x <http://yago-knowledge.org/resource/hasGivenName> \"Charles\"}} order by asc(?u)";
			ResultSet rs = (ResultSet) stmt.executeQuery(query);
			
			float minRange = (float) 5;
			float minAcc = (float) 0.3;
			float minPos = 5;
			String first = null;
			String  last = null;
			String  curr = null;
			float lastAcc = 0;
			int currPos=0, currNeg=0, currTot=0;
			int  rowPos=0,  rowNeg=0,  rowTot=0;
			float rowAcc = -1;
			boolean firstRow = true;
			while (rs.next()) {
				String value = rs.getString(1).substring(1,5);
				boolean match = rs.getInt(2)==1;
				int count = rs.getInt(3);
				
				// got a new value
				if (curr!=value && !firstRow) {
					rowAcc = ((float) rowPos)/((float) rowTot);
					if (rowAcc >= minAcc) {
						if (first==null) 
							first = curr;
						else if ((currPos+rowPos)>=minPos/* && (curr-first)>=minRange*/)  {
							last = curr;
							lastAcc = ((float) currPos)/((float) currTot);
						}
						
						currPos += rowPos;
						currTot += rowTot;
						currNeg += rowNeg;
						
						
					} else {
						if (first!=null) {
							int tempPos = currPos+rowPos;
							int tempTot = currTot+rowTot;
							float tempAcc = ((float) tempPos)/((float) tempTot);
							if (tempAcc < minAcc) {
								if (first!=null && last!=null)
									System.out.println("[" + first + ".." + last + "] Acc="+lastAcc);
								first = last = null;
								currPos = currNeg = currTot = 0;
							} else {
								currPos += rowPos;
								currTot += rowTot;
								currNeg += rowNeg;
							}
						}
					}
					
					curr = value;
					rowPos = rowNeg = rowTot = 0;
				} 
				if (firstRow) {
					curr = value;
					firstRow = false;
				}

				if (match) 
					rowPos = count;
				else
					rowNeg = count;
				rowTot += count;
			}
			if (first!=null && last!=null)
				System.out.println("[" + first + ".." + last + "] Acc="+lastAcc);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
