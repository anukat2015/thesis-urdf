package urdf.webservice;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;

public class YAGO_client {

  public String processQuery(String args[]) throws Exception {
    Options opts = new Options(args);
    args = opts.getRemainingArgs();
    if (args == null) {
      System.err.println("Usage: processQuery <query>");
      System.exit(1);
    }

    String query = args[0];

    URL url = new URL(opts.getURL());
    String user = opts.getUser();
    String passwd = opts.getPassword();

    Service service = new Service();
    Call call = (Call) service.createCall();

    call.setTargetEndpointAddress(url);
    call.setOperationName(new QName("urn:yago3", "getInlinksForAnchor"));
    call.addParameter("queryString", XMLType.XSD_STRING, ParameterMode.IN);
    call.setReturnType(XMLType.XSD_STRING);
    call.setUsername(user);
    call.setPassword(passwd);

    System.out.println("\nTARGET: " + opts.getURL() + " QUERY: " + query);

    Object ret = call.invoke(new Object[] { query });
    return (String) ret;
  }

  public static void main(String args[]) throws Exception {
    String[] queries = new String[] {
        "Berlin",
        "Paris",
        "New York",
        "Newton",
        "Einstein",
        "Java",
        "Mohawk River"};

    for (String query : queries)
      System.out.println(new YAGO_client().processQuery(new String[] { query, "-lhttp://infao5501.ag5.mpi-sb.mpg.de:8080/axis/servlet/AxisServlet", "-udfki",
          "-wdfki" }));
       
  }
}