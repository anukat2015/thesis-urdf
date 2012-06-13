package urdf.cmd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;

public class CmdInvoker {
	public static void main(String args[]) {
		 
       /* try {
            Runtime rt = Runtime.getRuntime();
            //Process pr = rt.exec("cd \\home\\adeoliv\\Documents\\Thesis\\rdf3x-0.3.7\\bin");
            //Process pr = rt.exec("cd /home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin");
            Process pr = rt.exec("cd /home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin; rdf3xquery db;");
            //Process pr = rt.exec("c:\\helloworld.exe");

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line=null;

            while((line=input.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = pr.waitFor();
            System.out.println("Exited with error code "+exitVal);

        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }*/
		/*
		Process proc = null;
		try {
		   proc = Runtime.getRuntime().exec("/bin/bash");
		}
		catch (IOException e) {
		   e.printStackTrace();
		}
		if (proc != null) {
		   BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		   PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
		   out.println("rdf3xquery /home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin/db");
		   //out.println("cd /home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin");
		   //out.println("rdf3xquery db");
		   //out.println("select ?x where {?x ?y ?z} limit 20");
		   try {
		      String line;
		      while ((line = in.readLine()) != null) {
		         System.out.println(line);
		      }
		      proc.waitFor();
		      in.close();
		      out.close();
		      proc.destroy();
		   }
		   catch (Exception e) {
		      e.printStackTrace();
		   }
		}*/

		String line;
		Scanner scan = new Scanner(System.in);

		Process process;
		try {
			process = Runtime.getRuntime ().exec ("/bin/bash");
			OutputStream stdin = process.getOutputStream ();
			InputStream stderr = process.getErrorStream ();
			InputStream stdout = process.getInputStream ();

			BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
			BufferedReader errrdr = new BufferedReader (new InputStreamReader(stderr));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
			PrintWriter out = new PrintWriter(writer, true);

			out.println("ls");
			
			while ((line = reader.readLine ()) != null) {
				System.out.println ("Stdout: " + line);
			}
			out.println("cd home/adeoliv/Documents/Thesis/rdf3x-0.3.7/bin");
			out.println("rdf3xquery db query >output");
			
			while ((line = reader.readLine ()) != null) {
				System.out.println ("Stdout: " + line);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
    }
}
