package urdf.reasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.UGroundedSoftRule;

public class MAXSAT {

  public double MAX_SAT;

  private HashSet<UGroundedSoftRule> softRuleGroundings;

  private HashSet<UGroundedHardRule> hardRuleGroundings;

  private Map<UFact, HashSet<UGroundedSoftRule>> invSoftRules = null;

  public MAXSAT(HashSet<UGroundedSoftRule> softRuleGroundings, HashSet<UGroundedHardRule> hardRuleGroundings,
      Map<UFact, HashSet<UGroundedSoftRule>> invSoftRules) {
    this.softRuleGroundings = softRuleGroundings;
    this.hardRuleGroundings = hardRuleGroundings;
    this.invSoftRules = invSoftRules;
  }

  public void processMAXSAT() throws Exception {

    // int t = Integer.MAX_VALUE;
    // for (Map.Entry<UGroundedSoftRule, List<UGroundedSoft>> softRule : softRuleGroundings.entrySet())
    // if (softRule.getKey().getLiterals().size() > 0 && softRule.getKey().numNegated < t)
    // t = softRule.getKey().numNegated;

    double p = 0.63, q_ij; // p = 1 - p^t // complex number!

    // For each competitor set S_k
    for (UGroundedHardRule S_k : hardRuleGroundings)
      getProbabilityHardRule(S_k, p);

    // For each competitor set S_k
    for (UGroundedHardRule S_k : hardRuleGroundings) {

      UFact max_f_i = S_k.getFact(0);
      double max_f_w = 0;

      // For each f_i in S_k
      for (UFact f_i : S_k) {
        if (f_i.getTruthValue() != UFact.UNKNOWN)
          continue;

        double f_w = 0;

        for (UGroundedSoftRule softRule : invSoftRules.get(f_i)) {
          if (softRule.isSatisfied() == UFact.TRUE)
            continue;

          if (softRule.getHead() == f_i || containsAnyNegative(softRule, S_k, f_i))
            q_ij = 1;
          else
            q_ij = getProbabilitySoftRule(f_i, softRule);

          f_w += q_ij * softRule.getWeight();

        }
        if (f_w > max_f_w) {
          max_f_i = f_i;
          max_f_w = f_w;
        }
      }

      // For each f_i in S_k
      for (UFact f_i : S_k) {
        if (f_i.getTruthValue() != UFact.UNKNOWN)
          continue;

        double f_w = 0;

        for (UGroundedSoftRule softRule : invSoftRules.get(f_i)) {
          if (softRule.isSatisfied() == UFact.TRUE)
            continue;

          q_ij = getProbabilitySoftRule(null, softRule);
          f_w += q_ij * softRule.getWeight();

        }
        if (f_w > max_f_w) {
          max_f_i = null;
          max_f_w = f_w;
        }
      }

      //System.out.println("MAX_Fi " + max_f_i + " " + max_f_i.w_i  + " " + max_f_i.p_i + " " + max_f_i.w);

      // For each f_i in S_k
      for (UFact f_i : S_k) {
        if (max_f_i == null || f_i != max_f_i) {

          // Fact is set to FALSE
          f_i.setTruthValue(UFact.FALSE);

        } else {

          // Fact is set to TRUE
          f_i.setTruthValue(UFact.TRUE);

        }
      }
    }
  }

  public boolean containsAnyNegative(UGroundedSoftRule softRule, UFactSet facts, UFact fact) {
    for (UFact f : facts)
      if (f != fact && softRule.contains(f))
        return true;
    return false;
  }

  public void getProbabilityHardRule(UGroundedHardRule hardRule, double p) {
    double max_w_i = 0, sum_w_i = 0, x = 0, tmp = 0;
    hardRule.sort(false);
    if (hardRule.size() > 0)
      max_w_i = hardRule.getFact(0).w_i > 0 ? hardRule.getFact(0).w_i : 0;
    else
      return;

    UFactSet S = new UFactSet();
    for (UFact f_i : hardRule) {
      sum_w_i += f_i.w_i;
      tmp = p * (max_w_i / sum_w_i);
      if (tmp * (S.size() + 1) > 1)
        break;
      S.add(f_i);
      x = tmp;
    }

    for (UFact f_i : hardRule) {
      if (S.contains(f_i))
        f_i.p_i = x;
      else
        f_i.p_i = 0;

      //System.out.println("HARDRULE: " + f_i + " -> " + f_i.w_i + "\t" + f_i.p_i);
    }
  }

  public double getProbabilitySoftRule(UFact fact, UGroundedSoftRule softRule) {
    Map<UGroundedHardRule, double[]> partitions = new HashMap<UGroundedHardRule, double[]>(hardRuleGroundings.size());
    double[] partition;

    // partition the softrule according to its competitor sets
    UFact head = softRule.getHead();
    if (fact != null && head.getGroundedHardRule() != fact.getGroundedHardRule()) {
      if ((partition = partitions.get(head.getGroundedHardRule())) == null) {
        partition = new double[3];
        partitions.put(head.getGroundedHardRule(), partition);
      }
      partition[1] += head.p_i;
    }
    for (UFact f_i : softRule) {
      if (f_i == fact)
        continue;
      if ((partition = partitions.get(f_i.getGroundedHardRule())) == null) {
        partition = new double[3];
        partitions.put(f_i.getGroundedHardRule(), partition);
      }
      partition[0]++;
      partition[1] += f_i.p_i;
      partition[2] += f_i.p_i;
    }

    double q = 1;
    for (Map.Entry<UGroundedHardRule, double[]> partitionEntry : partitions.entrySet()) {
      partition = partitionEntry.getValue();

      double p_l = partition[2], q_j = partition[1];
      if (partition[0] > 1)
        return 1;
      else if (partition[0] == 1)
        q_j = (1 - p_l);
      else
        q_j = p_l;

      q *= (1 - q_j);
    }

    //System.out.println("SOFTRULE: " + softRule + " -> " + (1 - q) + "\n");

    return 1 - q;
  }

  public void sampleRandomMaxSat(UFactSet facts) throws Exception {
    int N = 1 << 16;
    MAX_SAT = 0;

    Random r = new Random(System.currentTimeMillis());
    for (int k = 0; k < N; k++) {

      for (UFact f_i : facts)
        f_i.setTruthValue(UFact.UNKNOWN);

      for (UFact f_i : facts) {

        boolean hasTrue = false;
        if (f_i.getGroundedHardRule() != null) {
          for (UFact f_l : f_i.getGroundedHardRule()) {
            if (f_l != f_i && f_l.getTruthValue() == UFact.TRUE) {
              hasTrue = true;
              break;
            }
          }
        }

        f_i.setTruthValue((!hasTrue && r.nextBoolean()) ? UFact.TRUE : UFact.FALSE);
      }

      double d;
      if ((d = evalMaxSat(facts)) > MAX_SAT)
        MAX_SAT = d;
    }
  }

  public void sampleAllMaxSat(UFactSet facts, int i) throws Exception {
    if (i == 0)
      MAX_SAT = 0;

    double d;
    if (i < facts.size()) {

      UFact f_i = facts.getFact(i);

      boolean hasTrue = false;
      if (f_i.getGroundedHardRule() != null) {
        for (UFact f_l : f_i.getGroundedHardRule()) {
          if (!f_l.equals(f_i) && f_l.getTruthValue() == UFact.TRUE) {
            hasTrue = true;
            break;
          }
        }
      }

      if (!hasTrue) {
        f_i.setTruthValue(UFact.TRUE);
        for (int j = i + 1; j < facts.size(); j++)
          facts.getFact(j).setTruthValue(UFact.UNKNOWN);
        sampleAllMaxSat(facts, i + 1);
      }

      f_i.setTruthValue(UFact.FALSE);
      for (int j = i + 1; j < facts.size(); j++)
        facts.getFact(j).setTruthValue(UFact.UNKNOWN);
      sampleAllMaxSat(facts, i + 1);

    } else if ((d = evalMaxSat(facts)) > MAX_SAT)
      MAX_SAT = d;
  }

  public double evalMaxSat(UFactSet facts) throws Exception {
    double w = 0;

    // Unary facts
    for (UFact f : facts) {
      if (f.getTruthValue() == UFact.UNKNOWN) {
        System.err.print("UNKOWN TRUTH ASSIGNMENT: " + f);
        return 0;
      } else if (f.getTruthValue() == UFact.TRUE) {
        w += f.w_i;
        if (f.getGroundedHardRule() != null) {
          for (UFact f_l : f.getGroundedHardRule()) {
            if (!f_l.equals(f) && f_l.getTruthValue() == UFact.TRUE) {
              System.err.print("HARDRULE VIOLATED: " + f.getGroundedHardRule());
              return 0;
            }
          }
        }
      }
    }

    // Grounded rules
    for (UGroundedSoftRule softRule : softRuleGroundings) {
      if (softRule.getHead().getTruthValue() == UFact.TRUE) {
        w += softRule.getWeight();
        continue;
      }
      for (UFact f_i : softRule) {
        if (f_i.getTruthValue() == UFact.FALSE) {
          w += softRule.getWeight();
          break;
        }
      }
    }

    return w;
  }
}
