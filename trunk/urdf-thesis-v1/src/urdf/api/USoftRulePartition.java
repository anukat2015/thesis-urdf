package urdf.api;

import java.util.HashSet;

public class USoftRulePartition extends HashSet<USignedFact> {

  private static final long serialVersionUID = -7345556152741382709L;

  public double p_i, q_r;

  public int n;

  public USoftRulePartition() {
    super();
    this.p_i = 0;
    this.n = 0;
    this.q_r = 0;
  }
}
