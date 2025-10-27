import java.util.*; 
import java.net.URL;     
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class client {

    public static void main (String [] args){

	    if(args.length == 0) {
	        System.out.println("Usage: java client <server>");
	        System.exit(1);
	    }

        Scanner scanner = new Scanner(System.in);
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        XmlRpcClient client=null;

        try {
            config.setServerURL(new URL("http://" + args[0] + ":" + 8888));
            client = new XmlRpcClient();
            client.setConfig(config);
        } catch (Exception e) {
	    System.err.println("Client error: "+ e);
	    }

    while(true){
        System.out.println("Available commands: buy[id], search[topic], lookup[id], exit");
        String input = scanner.nextLine();

        try {

        if(input.equals("exit")){
            System.out.println();
            System.out.println("exiting...");
            System.out.println();
            System.exit(1);
        }

        if(input.contains("buy")){
            input = input.substring(4);
            System.out.println("buying " + input + "...");
            Vector<Object> params = new Vector<Object>();
            params.addElement(Integer.valueOf(input));

            Object[] result = (Object[])client.execute("sample.buy", params.toArray());
            System.out.println(Arrays.toString(result));
            System.out.println();
        }

        if(input.contains("search")){
            input = input.substring(7);
            System.out.println("searching for " + input + "...");
            Vector<Object> params = new Vector<Object>();
            params.addElement((input));

            Object[] result = (Object[])client.execute("sample.search", params.toArray());
            System.out.println(Arrays.toString(result));
            System.out.println();
        }

        if(input.contains("lookup")){
            input = input.substring(7);
            System.out.println("looking up " + input + "...");
            Vector<Object> params = new Vector<Object>();
            params.addElement(Integer.valueOf(input));

            String result = (String)client.execute("sample.lookup", params.toArray());
            System.out.println(result);
            System.out.println();
        }

        } catch (Exception e) {
	    System.err.println("Client error: " + e);
	    }
    }
    }
}
