package urdf.reasoner;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.UGroundedSoftRule;
import urdf.api.USignedFact;
import urdf.api.USoftRulePartition;

public class LinearMAXSAT {

  public double MAX_SAT;

  private HashSet<UGroundedSoftRule> softRuleGroundings;

  private HashSet<UGroundedHardRule> hardRuleGroundings;

  private Map<UFact, HashSet<UGroundedSoftRule>> invSoftRules = null;

  public LinearMAXSAT(HashSet<UGroundedSoftRule> softRuleGroundings, HashSet<UGroundedHardRule> hardRuleGroundings,
      Map<UFact, HashSet<UGroundedSoftRule>> invSoftRules) {
    this.softRuleGroundings = softRuleGroundings;
    this.hardRuleGroundings = hardRuleGroundings;
    this.invSoftRules = invSoftRules;
  }

  public static int steps = 0, TRUE_FACTS = 0, FALSE_FACTS = 0;

  public void processMAXSAT() {

    HashSet<UGroundedSoftRule> remainingSoftRules = new HashSet<UGroundedSoftRule>();
    remainingSoftRules.addAll(softRuleGroundings);

    double p = 0.63;

    steps = 0;
    TRUE_FACTS = 0;
    FALSE_FACTS = 0;

    // For each competitor set S_k, O(Sum_k|S_k|)
    for (UGroundedHardRule S_k : hardRuleGroundings)
      getProbabilityHardRule(S_k, p);

    // For each soft rule C_i, O(Sum_i|C_i|)
    for (UGroundedSoftRule C_i : softRuleGroundings) {
      double p_C_i = getProbabilitySoftRule(C_i);
      //W_t += C_i.getWeight() * p_C_i;
      if (p_C_i == 1.0) { // || p_C_i == 0.0) {
        //System.out.println("REMOVE: " + C_i);
        remainingSoftRules.remove(C_i);
      }
      steps++;
    }

    // For each competitor set S_k
    for (UGroundedHardRule S_k : hardRuleGroundings) {

      UFact f_max = null;
      double W_t0 = 0, W_tf, W_tf_max = 0;

      //System.out.println("REMAINING: " + remainingSoftRules.size());

      if (remainingSoftRules.size() > 0) {

        // For each f in S_k
        for (UFact f : S_k) {

          W_tf = 0;

          for (UGroundedSoftRule C_i : invSoftRules.get(f)) {
            if (!remainingSoftRules.contains(C_i))
              continue;
            W_tf += C_i.getWeight() * getProbabilitySoftRuleAllFalseExceptOne(C_i, S_k, f); // O(|C_i|)
          }

          if (W_tf > W_tf_max) {
            f_max = f;
            W_tf_max = W_tf;
          }
        }

        // For each f in S_k
        for (UFact f : S_k) {

          W_t0 = 0;

          for (UGroundedSoftRule C_i : invSoftRules.get(f)) {
            if (!remainingSoftRules.contains(C_i))
              continue;
            W_t0 += C_i.getWeight() * getProbabilitySoftRuleAllFalse(C_i, S_k); // O(|C_i|)
          }
        }
      }

      for (UFact f : S_k) {
        if (f.getTruthValue() == UFact.UNKNOWN) {
          if (W_t0 >= W_tf_max || f != f_max) {
            f.setTruthValue(UFact.FALSE); // choose minimal model if undecided
            FALSE_FACTS++;
          } else {
            f.setTruthValue(UFact.TRUE);
            TRUE_FACTS++;
          }
          for (UGroundedSoftRule C_i : invSoftRules.get(f)) {
            if (C_i.isSatisfied() != UFact.UNKNOWN)
              remainingSoftRules.remove(C_i);
          }
        }
        steps++;
      }
    }
  }

  public double getProbabilitySoftRule(UGroundedSoftRule softRule) {
    if (softRule.partitions == null)
      softRule.partitions = getPartitions(softRule);

    double q_c = 1;
    for (USoftRulePartition partition : softRule.partitions) {
      steps++;
      if (partition.n > 1) {
        q_c = 0;
        break;
      } else if (partition.n == 1)
        partition.q_r = (1 - partition.p_i);
      else
        partition.q_r = partition.p_i;
      //System.out.println("  PART: " + partition + "  " + partition.p_i + "  " + partition.q_r);
      q_c *= (1 - partition.q_r);
      steps++;
    }

    //System.out.println("SOFTRULE: " + softRule + " -> " + (1 - q_c));
    return 1 - q_c;
  }

  public double getProbabilitySoftRuleAllFalseExceptOne(UGroundedSoftRule softRule, UGroundedHardRule hardRule, UFact fact) {

    double q_c = 1;
    for (USoftRulePartition partition : softRule.partitions) {
      if (partition.n > 1) {
        System.err.println("CLAUSE ALREADY SATISFIED!");
        return 1;
      }
      for (USignedFact literal : partition) {
        steps++;
        if ((literal.equals(fact) && literal.sign) || (!literal.equals(fact) && !literal.sign && hardRule.contains(literal.fact))) {
          return 1;
        }
      }
      if (partition.n == 1)
        partition.q_r = (1 - partition.p_i);
      else
        partition.q_r = partition.p_i;
      q_c *= (1 - partition.q_r);
      steps++;
    }

    //System.out.println("SOFTRULE-1: " + softRule + " -> " + (1 - q_c));
    return 1 - q_c;
  }

  public double getProbabilitySoftRuleAllFalse(UGroundedSoftRule softRule, UGroundedHardRule hardRule) {

    double q_c = 1;
    for (USoftRulePartition partition : softRule.partitions) {
      if (partition.n > 1) {
        System.err.println("CLAUSE ALREADY SATISFIED!");
        return 1;
      }
      for (USignedFact literal : partition) {
        steps++;
        if (!literal.sign && hardRule.contains(literal.fact)) {
          return 1;
        }
      }
      if (partition.n == 1)
        partition.q_r = (1 - partition.p_i);
      else
        partition.q_r = partition.p_i;
      q_c *= (1 - partition.q_r);
      steps++;
    }

    //System.out.println("SOFTRULE-0: " + softRule + " -> " + (1 - q_c));
    return 1 - q_c;
  }

  public void getProbabilityHardRule(UGroundedHardRule hardRule, double p) {
    if (hardRule.size() == 0)
      return;
    
    // sorts hard rule in decreasing order of probabilities
    hardRule.sort(false);
    
    double max_w_i = hardRule.getFact(0).w_i > 0 ? hardRule.getFact(0).w_i : 0, sum_w_i = 0, x = 0, tmp; // needs to be sorted!

    UFactSet facts = new UFactSet();
    for (UFact f_i : hardRule) {
      steps++;
      sum_w_i += f_i.w_i;
      tmp = p * (max_w_i / sum_w_i);
      if (tmp * (facts.size() + 1) > 1)
        break;
      facts.add(f_i);
      x = tmp;
    }

    //System.out.print("HARDRULE: ");
    for (UFact f_i : hardRule) {
      steps++;
      if (facts.contains(f_i))
        f_i.p_i = x;
      else
        f_i.p_i = 0;
      //System.out.print(f_i + " -> " + f_i.w_i + " " + f_i.p_i + "\t");
    }
    //System.out.println();
  }

  public Collection<USoftRulePartition> getPartitions(UGroundedSoftRule softRule) {
    Map<UGroundedHardRule, USoftRulePartition> partitions = new HashMap<UGroundedHardRule, USoftRulePartition>();

    UFact head = softRule.getHead();
    USoftRulePartition partition = new USoftRulePartition();
    partition.add(new USignedFact(head, true));
    partition.p_i = head.p_i;
    partition.n = 0;
    partitions.put(head.getGroundedHardRule(), partition);
    steps++;

    for (UFact fact : softRule) {
      steps++;
      if ((partition = partitions.get(fact.getGroundedHardRule())) == null) {
        partition = new USoftRulePartition();
        partitions.put(fact.getGroundedHardRule(), partition);
      }
      partition.add(new USignedFact(fact, false));
      partition.p_i += fact.p_i;
      partition.n++;
    }

    return partitions.values();
  }
}
