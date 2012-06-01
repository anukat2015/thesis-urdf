package urdf.webservice;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;

public class Location_client {

  public String getDistancePairs(String args[]) throws Exception {
    Options opts = new Options(args);
    args = opts.getRemainingArgs();
    if (args == null) {
      System.err.println("Usage: getDistancePairs <name1> <name2>");
      System.exit(1);
    }

    URL url = new URL(opts.getURL());
    String user = opts.getUser();
    String passwd = opts.getPassword();

    Service service = new Service();
    Call call = (Call) service.createCall();

    call.setTargetEndpointAddress(url);
    call.setOperationName(new QName("urn:location1", "getDistancePairs"));
    call.addParameter("name1", XMLType.XSD_STRING, ParameterMode.IN);
    call.addParameter("name2", XMLType.XSD_STRING, ParameterMode.IN);
    call.setReturnType(XMLType.XSD_STRING);
    call.setUsername(user);
    call.setPassword(passwd);

    System.out.println("\nTARGET: " + opts.getURL() + " QUERY: " + args[0] + " <> " + args[1]);

    Object ret = call.invoke(new Object[] { args[0], args[1] });
    return (String) ret;
  }

  public static void main(String args[]) throws Exception {
    String name1 = "Berlin"; // can change this e.g. to args[0]
    String name2 = "Potsdam"; // can change this e.g. to args[1]
    System.out.println(new Location_client().getDistancePairs(new String[] { name1, name2,
        "-lhttp://infao5501.ag5.mpi-sb.mpg.de:8080/axis/servlet/AxisServlet", "-udfki", "-wdfki" }));
  }
}