/**
 * 
 */
package urdf.api {

import urdf.api.UObject;
	
/**
 * This class represents an option object which is used as container to hold all relevant options (regarding the reasoning process) available for the user via
 * the GUI.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 */
[Bindable]
[RemoteClass(alias="urdf.api.UOptions")]
public class UOptions extends UObject {

  /**
   * The flag that states if query optimization should be used or not. The default value is <i> true </i>.
   */
  private var queryOptimization:Boolean = true;

  /** The empty default constructor for the UOptions object. */
  public function UOptions() {
    super();
  }

  /**
   * Delivers the flag if query optimization should be used (is set).
   * 
   * @return true, if query optimization should bes used, false otherwise.
   */
  public function isQueryOptimization():Boolean {
    return queryOptimization;
  }

  /**
   * Sets the query optimization flag.
   * <p>
   * Set it to <i> true </i>, if query optimization should be used, to <i> false </i> otherwise. The <i> default </i> value is <i> true </i>.
   * 
   * @param queryOptimization
   *          the queryOptimization to set
   */
  public function setQueryOptimization(queryOptimization:Boolean):void {
    this.queryOptimization = queryOptimization;
  }

}

}