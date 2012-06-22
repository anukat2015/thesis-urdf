package urdf.api;

public class USignedFact {

  public boolean sign;

  public UFact fact;

  public USignedFact(UFact f, Boolean s) {
    this.fact = f;
    this.sign = s;
  }

  @Override
  public boolean equals(Object o) {
    return fact.equals(o);
  }

  @Override
  public int hashCode() {
    return fact.hashCode();
  }

  @Override
  public String toString() {
    return (sign ? "+" : "-") + fact.toString();
  }
}
