package urdf.reasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import urdf.api.UArgument;
import urdf.api.UBindingSet;
import urdf.api.ULiteral;
import urdf.api.USoftRule;

public class SoftRuleIndex {

  HashMap<String, Set<USoftRule>> softRules;

  public SoftRuleIndex() {
    this.softRules = new HashMap<String, Set<USoftRule>>();
  }

  public SoftRuleIndex(List<USoftRule> softRules) {
    this.softRules = new HashMap<String, Set<USoftRule>>(softRules.size());
    for (USoftRule softRule : softRules)
      add(softRule);
  }

  public void add(USoftRule softRule) {
    
    // 8 cases for all 8 subsets of 3 attributes
    if (softRule.getHead().getRelation().isVariable()) {
      if (softRule.getHead().getFirstArgument().isVariable()) {
        if (softRule.getHead().getSecondArgument().isVariable()) {
          this.add("$$", softRule);
        } else {
          this.add("$$" + softRule.getHead().getSecondArgumentName(), softRule);
        }
      } else {
        if (softRule.getHead().getSecondArgument().isVariable()) {
          this.add("$" + softRule.getHead().getFirstArgumentName() + "$", softRule);
        } else {
          this.add("$" + softRule.getHead().getFirstArgumentName() + "$" + softRule.getHead().getSecondArgumentName(), softRule);
        }
      }
    } else {
      if (softRule.getHead().getFirstArgument().isVariable()) {
        if (softRule.getHead().getSecondArgument().isVariable()) {
          this.add(softRule.getHead().getRelationName() + "$$", softRule);
        } else {
          this.add(softRule.getHead().getRelationName() + "$$" + softRule.getHead().getSecondArgumentName(), softRule);
        }
      } else {
        if (softRule.getHead().getSecondArgument().isVariable()) {
          this.add(softRule.getHead().getRelationName() + "$" + softRule.getHead().getFirstArgumentName() + "$", softRule);
        } else {
          this.add(softRule.getHead().getRelationName() + "$" + softRule.getHead().getFirstArgumentName() + "$" + softRule.getHead().getSecondArgumentName(),
              softRule);
        }
      }
    }
  }

  private void add(String key, USoftRule softRule) {
    Set<USoftRule> rules;
    if ((rules = softRules.get(key)) == null) {
      rules = new HashSet<USoftRule>();
      softRules.put(key, rules);
    }
    // System.out.println("  ADD: " + key + " => " + softRule);
    rules.add(softRule);
  }

  public Set<USoftRule> get(ULiteral literal, UBindingSet bindings) {

    UArgument rel = getConstantArg(literal.getRelation(), bindings);
    UArgument arg1 = getConstantArg(literal.getFirstArgument(), bindings);
    UArgument arg2 = getConstantArg(literal.getSecondArgument(), bindings);

    Set<USoftRule> rules, allRules = new HashSet<USoftRule>();
    if ((rules = softRules.get("$$")) != null)
      allRules.addAll(rules);

    if (rel != null) {
      if ((rules = softRules.get(rel.getName() + "$$")) != null)
        allRules.addAll(rules);

      if (arg1 != null) {
        if ((rules = softRules.get(rel.getName() + "$" + arg1.getName() + "$")) != null)
          allRules.addAll(rules);

        if (arg2 != null) {
          if ((rules = softRules.get(rel.getName() + "$" + arg1.getName() + "$" + arg2.getName())) != null)
            allRules.addAll(rules);
        }
      }

      if (arg2 != null) {
        if ((rules = softRules.get(rel.getName() + "$$" + arg2.getName())) != null)
          allRules.addAll(rules);
      }
    }

    if (arg1 != null) {
      if ((rules = softRules.get("$" + arg1.getName() + "$")) != null)
        allRules.addAll(rules);

      if (arg2 != null) {
        if ((rules = softRules.get("$" + arg1.getName() + "$" + arg2.getName())) != null)
          allRules.addAll(rules);
      }
    }

    if (arg2 != null) {
      if ((rules = softRules.get("$$" + arg2.getName())) != null)
        allRules.addAll(rules);
    }

    //System.out.println("GET: " + literal + " " + allRules.size() + " => " + allRules);

    return allRules;
  }

  private UArgument getConstantArg(UArgument arg, UBindingSet bindings) {
    if (!arg.isVariable())
      return arg;
    return bindings.getBinding(arg);
  }
}
