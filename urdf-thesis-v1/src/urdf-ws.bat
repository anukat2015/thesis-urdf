@echo off
set AXIS_LIB=..\lib
set AXISCLASSPATH=%AXIS_LIB%\axis.jar;%AXIS_LIB%\commons-discovery.jar;%AXIS_LIB%\commons-logging.jar;%AXIS_LIB%\jaxrpc.jar;%AXIS_LIB%\saaj.jar;%AXIS_LIB%\log4j-1.2.8.jar;%AXIS_LIB%\xml-apis.jar;%AXIS_LIB%\xercesImpl.jar;%AXIS_LIB%\wsdl4j-1.5.1.jar;%AXIS_LIB%\urdf.jar
java -cp .;%AXISCLASSPATH% urdf.webservice.URDF_client2 -lhttp://infao5501.ag5.mpi-sb.mpg.de:8080/axis/servlet/AxisServlet -udfki -wdfki %1