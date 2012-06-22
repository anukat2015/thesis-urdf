package urdf.reasoner.function;

import urdf.api.UArgument;
import urdf.api.UFactSet;
import urdf.api.URelation;

// Use this abstract class template to implement more function calls. 
// A call has to return, for a given predicate and arguments, a finite set of grounded facts. 
public abstract class FunctionCall {

  protected FunctionCall() {
  }

  public abstract UFactSet call(UArgument argument1, UArgument argument2, URelation relation, int compareValue) throws Exception;

  public abstract void close() throws Exception;

}
