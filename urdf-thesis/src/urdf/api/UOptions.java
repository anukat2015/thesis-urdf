/**
 * 
 */
package urdf.api;

/**
 * This class represents an option object which is used as container to hold all relevant options (regarding the reasoning process) available for the user via
 * the GUI.
 * 
 * @author Timm Meiser
 * @since 07.01.10
 * @version 1.0
 */
public class UOptions extends UObject {

  /**
   * The flag that states if query optimization should be used or not. The default value is <i> true </i>.
   */
  private boolean queryOptimization = true;

  /** The empty default constructor for the UOptions object. */
  public UOptions() {
    super();
  }

  /**
   * Delivers the flag if query optimization should be used (is set).
   * 
   * @return true, if query optimization should bes used, false otherwise.
   */
  public boolean isQueryOptimization() {
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
  public void setQueryOptimization(boolean queryOptimization) {
    this.queryOptimization = queryOptimization;
  }

}
