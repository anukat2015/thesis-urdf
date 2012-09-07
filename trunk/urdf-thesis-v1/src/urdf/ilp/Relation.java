package urdf.ilp;

import java.io.IOException;
import java.io.Serializable;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.Statement;
import java.sql.SQLException;

import urdf.rdf3x.Connection;
import urdf.rdf3x.Driver;
import urdf.rdf3x.ResultSet;
import urdf.rdf3x.Statement;

/**
 * @author Christina Teflioudi 
 */

public class Relation implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String name;
	private Type domain;
	private Type range;
	private float max = Float.NaN;
	private float min = Float.NaN;
	private boolean rangeIsLiteral = false;
	private boolean isFunction=false;
	private boolean isSymmetric=false;
	private int size=0; 								// number of facts in the relation
	private int inputArg;								// 0: both, 1: first, 2: second	
	private float mult1,mult2;
	private float idealMult1,idealMult2;
	private float var1, var2; // variances of multiplicities
	private int constantInArg=0;						// 0: no constants allowed, 1: constants in arg1 allowed, 2: constants in arg2 allowed
	private int distinctEntitiesArg1=0;
	private int distinctEntitiesArg2=0;
	

	
	public Relation(String name, Type domain, Type range) {
		this.name=name;
		this.domain=domain;
		this.range=range;
	}
	public Relation(String name, Type domain, Type range,int size,float mult1, float mult2, float var1, float var2, int constantInArg) {
		this(name, domain, range, size, mult1, mult2, constantInArg);
		this.var1 = var1;
		this.var2 = var2;		
		idealMult1 = (float) (this.mult1 + Math.sqrt(this.var1));
		idealMult2 = (float) (this.mult2 + Math.sqrt(this.var2));
	}
	
 	public Relation(String name, Type domain, Type range,int size,float mult1, float mult2, int constantInArg) {
		this.name	= name;
		this.domain	= domain;
		this.range	= range;
		this.size	= size;
		this.mult1	= mult1;
		this.mult2	= mult2;
		this.distinctEntitiesArg1 = Math.round((float)size / mult1);
		this.distinctEntitiesArg2 = Math.round((float)size / mult2);
		
		this.constantInArg=constantInArg;
		
		if (mult1==1) {
			isFunction=true;
			inputArg=1;
		}
		else if (mult2==1) {
			inputArg=2;
		}
		else if (mult1<2) {
			inputArg=1;
		}
		else if (mult1<2) {
			inputArg=2;
		}
		else {
			inputArg=0;
		}
		
		
	}
	
	// ************ GETTERS ***********
 	public float getVar(int arg){
 		if (arg==1)
 			return var1;
 		else
 			return var2; 		
 	}
 	
 	public int getDistinctEntities(int arg) {
 		switch(arg) {
 			case 1: return distinctEntitiesArg1;
 			case 2: return distinctEntitiesArg2;
 			default: throw new IllegalArgumentException("Illegal inputArg="+arg+", it should be 1 or 2");
 		}
 	}
	
 	public int getSize(){	
		return this.size;	
	}
	
	public int getConstantInArg(){
		return this.constantInArg;
	}
	
	public int getInputArg(){
		return this.inputArg;
	}
 	
	public String getName(){
 		return this.name;
 	}
 	
	public String getSimpleName() {
		if  (this.isAuxiliary()) 
			return getName();
		else
			return this.name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('#'))+1, name.length()-1);
 	}
	
 	public Type getDomain(){
 		return this.domain;
 	}
	
 	public Type getRange(){	
 		return this.range;
 	}
	
 	public boolean isFunction(){
 		return this.isFunction;
 	}
	
 	public boolean isSymmetric(){return this.isSymmetric;}
	
	public int getRelationsForm(){
		if (domain.isChildOf(range)||range.isChildOf(domain)|| domain.equals(range))
			return 1;		
		else
			return 2;
	}
	
	public float getMult1(){
		return this.mult1;
	}
	
	public float getMult2(){
		return this.mult2;
	}
	
	public float getMult(int arg){	
		if (arg==1)
			return this.mult1;
		else
			return this.mult2;
		
	}
	
	public float getIdealMult(int arg) {
		if (arg==1)
		{
			return idealMult1;
		}
		else
		{
			return idealMult2;
		}
	}
	
	// ************ SETTERS ***********
	public void setVar1(float var1){this.var1=var1;}
	public void setVar2(float var2){this.var2=var2;}
	public void setMult1(float mult1){this.mult1=mult1;}
	public void setMult2(float mult2){this.mult2=mult2;}
	
	public void setDistinctEntities(int arg, int num)
 	{
 		if (arg==1)
 			this.distinctEntitiesArg1=num;
 		else
 			this.distinctEntitiesArg2=num;
 	}
	public void setConstantInArg(int arg){this.constantInArg=arg;}
	public void setIdealMult(float mult, int arg){
		if (arg==1)
			idealMult1=mult;
		else
			idealMult2=mult;
	}
 	public void setIsFunction(boolean isFuction){	this.isFunction=isFuction;}
	public void setIsSymmetric(boolean isSymmetric){this.isSymmetric=isSymmetric;}
	public void setSize(int size){this.size=size;}
	public void setInputArg(int arg){this.inputArg=arg;}
	
	public boolean equals(Relation rel)
	{
		if (rel==null || rel.name==null) return false;
		if (!this.name.equals(rel.name))
			return false;
		if (this.domain!=rel.domain)
			return false;
		if (this.range!=rel.range)
			return false;
		if (this.isFunction!=rel.isFunction)
			return false;
		if (this.isSymmetric!=rel.isSymmetric)
			return false;
		
		return true;
	}	
	public boolean isAuxiliary()
	{
		if (this.equals(RelationsInfo.EQ)
				||this.equals(RelationsInfo.NEQ)
				||this.equals(RelationsInfo.GT)
				||this.equals(RelationsInfo.LT))
		{
			return true;
		}
		return false;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	public float getMaxValue() {
		return max;
	}
	public void setMaxValue(float max) {
		this.max = max;
	}
	public float getMinValue() {
		return min;
	}
	public void setMinValue(float min) {
		this.min = min;
	}
	public boolean isRangeLiteral() {
		return rangeIsLiteral;
	}
	public void setRangeIsLiteral(boolean rangeIsLiteral) {
		this.rangeIsLiteral = rangeIsLiteral;
	}
	
	
	
	//Query database to get Relation data
 	/*public Relation(String name, Connection conn) {
 		String query;
 		ResultSet rs;
		Statement stmt;
		try {
			stmt = (Statement) conn.createStatement();
			
			query = "SELECT arg2 FROM facts WHERE relation='hasRange' AND arg1='" + name + "'";
			rs = (ResultSet) stmt.executeQuery(query);
			if (rs!=null && rs.next()) 
				this.range = new Type(rs.getString(1), conn);
				

			query = "SELECT arg2 FROM facts WHERE relation='hasDomain' AND arg1='" + name + "'";
			rs = (ResultSet) stmt.executeQuery(query);
			if (rs!=null && rs.next()) 
				this.domain = new Type(rs.getString(1), conn);
				
			
			query = "SELECT n, mult1, mult2 FROM rel_stats WHERE relation='" + name + "'";
			rs = (ResultSet) stmt.executeQuery(query);
			if (rs!=null && rs.next()) {
				this.size = rs.getInt(1);
				this.mult1 = rs.getFloat(2);
				this.mult2 = rs.getFloat(3);
			}	
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	public Relation(String name, String iniFile) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
 		this(name,Driver.connect(iniFile));
 	}*/

}
