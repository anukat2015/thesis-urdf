package urdf.tools;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.LiveGraph.LiveGraph;
import org.LiveGraph.dataFile.write.DataStreamWriter;
import org.LiveGraph.dataFile.write.DataStreamWriterFactory;

import urdf.api.UFact;
import urdf.api.UFactSet;
import urdf.api.UGroundedHardRule;
import urdf.api.UGroundedSoftRule;
import urdf.reasoner.LinearMAXSAT;
import urdf.reasoner.URDF;

public class Plotter implements Runnable {

  public static final String DEMO_DIR = System.getProperty("user.dir");
  public static final int MIN_SLEEP = 0;
  public static final int MAX_SLEEP = 1000;

  public void run() {

    // Setup a data writer object:
    DataStreamWriter out = DataStreamWriterFactory.createDataWriter(DEMO_DIR, "maxsat");

    // Set a values separator:
    out.setSeparator(";");

    // Add a file description line:
    out.writeFileInfo("MAX-SAT demo.");

    // Set-up the data series:
    out.addDataSeries("|S| + |H|");
    out.addDataSeries("steps");
    out.addDataSeries("time");

    Random r = new Random(System.currentTimeMillis());

    int R = 3;
    double norm = 1;
    
    int m = 99, s = 100, h = 100, p = 1000;
    
    int step_m = 0, step_s = 100, step_h = 0, step_p = 0;

    for (int k = 0; k < 1000; k++) {

      long time = 0;
      int n = 0;
      
      for (int repeats = 0; repeats < R; repeats++) {

        n = 0;
        if (m > p - 1) {
          System.err.println("CLAUSE SIZE EXCEEDS #VARS!");
          return;
        }

        HashSet<UGroundedSoftRule> softRules = new HashSet<UGroundedSoftRule>();
        HashSet<UGroundedHardRule> hardRules = new HashSet<UGroundedHardRule>();

        UFactSet allFacts = new UFactSet();
        for (int i = 0; i < s; i++) {
          UFact head = new UFact(String.valueOf(Math.abs(r.nextInt()) % p), r.nextDouble());
          allFacts.add(head);
          n++;
          UFactSet body = new UFactSet(m);
          for (int j = 0; j < m; j++) {
            UFact fact;
            do {
              fact = new UFact(String.valueOf(Math.abs(r.nextInt()) % p), r.nextDouble());
            } while (head.equals(fact) || body.contains(fact));
            body.add(fact);
            allFacts.add(fact);
            n++;
          }
          UGroundedSoftRule clause = new UGroundedSoftRule(body, head, r.nextDouble());
          softRules.add(clause);
        }

        int i = 0, size = allFacts.size();
        UFactSet facts = new UFactSet();
        for (UFact fact : allFacts) {
          facts.add(fact);
          i++;
          if (i % Math.max((size / h), 2) == 0 && facts.size() > 0) {
            hardRules.add(new UGroundedHardRule(facts));
            // System.out.println((size / h) + "\t" + i + "  " + new UGroundedHardRule(facts));
            facts = new UFactSet();
          }
        }
        if (facts.size() > 0) {
          hardRules.add(new UGroundedHardRule(facts));
          //System.out.println((n / h) + "\t" + i + "  " + new UGroundedHardRule(facts));
        }
        n += i;

        long t = System.currentTimeMillis();

        Map<UFact, HashSet<UGroundedSoftRule>> map = URDF.invertRules(softRules, hardRules);
        //for (UGroundedHardRule hardRule : hardRules)
        //  hardRule.sort(false);
        LinearMAXSAT solver = new LinearMAXSAT(softRules, hardRules, map);
        solver.processMAXSAT();

        t = (System.currentTimeMillis() - t);

        double W = 0;
        for (UGroundedSoftRule C : softRules)
          if (C.isSatisfied() == UFact.TRUE)
            W += C.getWeight();

        System.out.println("K:" + k + "\tMAXSAT-STEPS:" + LinearMAXSAT.steps + "\tS+H=" + n + "\tSTEP-RATIO:" + (LinearMAXSAT.steps / (double) n) + "\tTIME:"
            + t + "\tTIME-RATIO:" + (t / (double) n) + "\tW:" + W);

        time += t;
      }

      //System.out.println(" > FACTS:" + allFacts.size() + "\tTRUE:" + LinearMAXSAT.TRUE_FACTS + "\tFALSE:" + LinearMAXSAT.FALSE_FACTS + "\tOPT:" + opt);

      if (k == 0)
    	  norm = LinearMAXSAT.steps / (time / R);
      
      // Set-up the data values:
      out.setDataValue(n);
      out.setDataValue(LinearMAXSAT.steps/ norm);
      out.setDataValue(time / R);

      // Write dataset to disk:
      out.writeDataSet();

      // Check for IOErrors:     
      if (out.hadIOException()) {
        out.getIOException().printStackTrace();
        out.resetIOException();
      }

      m += step_m;
      s += step_s;
      h += step_h;
      p += step_p;

      // Pause:
      Thread.yield();
      long sleep = (long) (MIN_SLEEP + (Math.random() * (MAX_SLEEP - MIN_SLEEP)));
      try {
        Thread.sleep(sleep);
      } catch (InterruptedException e) {
      }
      Thread.yield();

      System.gc();
    }

    // Finish:
    out.close();
  }

  public static void main(String[] unusedArgs) throws Exception {
    try {
      new File(DEMO_DIR, "\\maxsat.lgdat").delete();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Plotter plotter = new Plotter();
    new Thread(plotter).start();
    Thread.sleep(1000);
    LiveGraph app = LiveGraph.application();
    app.execStandalone(new String[] { "-dfs", DEMO_DIR + "\\maxsat.lgdfs" });
  }
}