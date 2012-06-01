package urdf.ilp;



import java.io.Serializable;

/**
 * @author Christina Teflioudi
 *
 */
public class Type implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	private String name;
	private Type superType=null;
	
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
		if (this.superType==null)
			return false;
		if (this.superType.equals(parent))
			return true;
		return this.superType.isChildOf(parent);
	}

	public boolean equals(Type type)
	{
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

}
