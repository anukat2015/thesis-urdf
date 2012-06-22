package urdf.ilp;



import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Christina Teflioudi
 *
 */
public class Type implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	private String name;
	private Type superType=null;
	
	public Type(String name, Connection conn) throws SQLException {
 		String query;
 		ResultSet rs;
		Statement stmt = conn.createStatement();
		
		query = "SELECT arg2 FROM facts WHERE relation='subclassOf' AND arg1='" + name + "'";
		rs = stmt.executeQuery(query);
		if (rs!=null && rs.next()) {
			this.superType = new Type(rs.getString(1), conn);
		}
 		
	}
	
	public Type(String name, String iniFile) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		this(name, (new urdf.rdf3x.Driver()).connect(iniFile));
	}
	
	public Type(String name)
	{
		this.name=name;
	}
	
	public Type(String name, Type superType)
	{
		this.name=name;
		this.superType=superType;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Type getSuperType()
	{
		return superType;
	}
	
	public void setSuperType(Type superType)
	{
		this.superType=superType;
	}
	
	public boolean isChildOf(String parentName)
	{
		if (this.superType==null)
			return false;
		if (this.superType.name.equals(parentName))
			return true;
		return this.superType.isChildOf(parentName);
	}
	
	public boolean isChildOf(Type parent)
	{
		if (parent==null) return false;
		if (this.superType==null)
			return false;
		if (this.superType.equals(parent))
			return true;
		return this.superType.isChildOf(parent);
	}

	public boolean equals(Type type)
	{
		if (type==null || type.name==null) return false;
		if (!this.name.equals(type.name))
			return false;
		if (this.superType==null && type.superType==null)
			return true;
		if (!this.superType.equals(type.superType))
			return false;		
		return true;
	}
	
	public Type getRootParent()
	{
		if(this.superType==null)
			return this;
		return this.superType.getRootParent();
	}
	
	public void setName(String newName) {
		this.name = newName;
	}

}
