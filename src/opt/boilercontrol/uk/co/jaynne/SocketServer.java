package uk.co.jaynne;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SocketServer extends Thread {

	private String DELIMINATOR = ":";
	private String PARAMETER_SEPARATOR = ",";
	private String QUOTES = "\"";
	private int TIMEOUT;
	private int PORT;
	private ControlBroker broker;

	public SocketServer(int port, int timeout) {
		PORT = port;
		TIMEOUT = timeout;
		broker = ControlBroker.getInstance();
	}

	public SocketServer() {
		this(20000, 5000); // default port and timeout
	}

	public void run() {
		ServerSocket listenSock = null; // the listening server socket
		Socket sock = null; // the socket that will actually be used for
							// communication
		
		try {
			listenSock = new ServerSocket(PORT);
			listenSock.setSoTimeout(TIMEOUT);
		
			while (!Thread.interrupted()) {
				try {
					sock = listenSock.accept(); //blocks until timeout

					BufferedReader br = 
							new BufferedReader(
									new InputStreamReader(sock.getInputStream())
							);
					BufferedWriter bw = 
							new BufferedWriter(
									new OutputStreamWriter(sock.getOutputStream())
							);
					String line = "";
					
					while ((line = br.readLine()) != null) {
						HashMap<String, String> args = null;
						try { //parse string to hashmap
							args = parseString(line);
						} catch (Exception e) {
							System.out.println("Invalid string received");
							break;
						}
						HashMap<String, String> reply = null;
						
						Iterator<Entry<String, String>> itr = args.entrySet().iterator();
						
						while (itr.hasNext()) {
							Map.Entry<String,String> map = (Entry<String, String>)itr.next();
							if (map.getKey().equals("boost")) {
								reply = boost(args);
							} else if (map.getKey().equals("status")) {
								reply = status(args);
							} else {
								reply = new HashMap<String, String>();
								reply.put("Result", "ERROR");
								reply.put("Message", "Invalid arguments");
							}
						}
						
						bw.write(MapToJson(reply));
						bw.write("\n");
						bw.flush();
					}
	
					// Closing streams and the current socket (not the listening
					// socket!)
					bw.close();
					br.close();
					sock.close();
					
				} catch (SocketTimeoutException e) {
					//do nothing socket will listen again
				} catch (IOException ex) {
					ex.printStackTrace();
				} 
			}
		
			listenSock.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Boost button logic
	 * @param args
	 * @return
	 */
	private HashMap<String,String> boost(HashMap<String,String> args) {
		HashMap<String,String> result = new HashMap<String,String>();
		String system = args.get("boost");

		/* Time is not mandatory anymore. [inc/dec]DesiredTemp comes without time */
		int time = 0;
		if ((system.equals("heating")) || (system.equals("water"))) {
			if (!args.containsKey("time")) {
				result.put("Result", "ERROR");
				result.put("Message", "Invalid arguments");
				return result;
			} else {
				time = Integer.parseInt(args.get("time"));
			}
		}
		
		switch (system) {
			case "heating" :
				boolean heating = broker.toggleHeatingBoostStatus(time);
				result.put("Result", "OK");
				if (heating) {
					result.put("Heating", "ON");
					result.put("HeatingBoost", "ON");
				} else {
					result.put("Heating", "OFF");
					result.put("HeatingBoost", "OFF");
				}
				return result;
			case "water" :
				boolean water = broker.toggleWaterBoostStatus(time);
				result.put("Result", "OK");
				
				if (water) {
					result.put("Water", "ON");
					result.put("WaterBoost", "ON");
				} else {
					result.put("Water", "OFF");
					result.put("WaterBoost", "OFF");
				}
				return result;
			case "incdesiredtemp" :
				broker.increaseDesiredTemp();
				result.put("Result", "OK");
				return result;
			case "decdesiredtemp" :
				broker.decreaseDesiredTemp();
				result.put("Result", "OK");
				return result;
			default :
				result.put("Result", "ERROR");
				result.put("Message", "Invalid argument");
				return result;
		}
	}
	
	private HashMap<String,String> status(HashMap<String,String> args) {
		HashMap<String,String> result = new HashMap<String,String>();
		String status = args.get("status");

		switch (status) {
			case "onoroff" :
				result.put("Result", "OK");
				result.put("Heating", (broker.isHeatingOn()) ? "ON" : "OFF");
				result.put("Water", (broker.isWaterOn()) ? "ON" : "OFF");
				result.put("WaterBoost", (broker.isWaterBoostOn()) ? "ON" : "OFF");
				result.put("HeatingBoost", (broker.isHeatingBoostOn()) ? "ON" : "OFF");
				result.put("HeatingBoostTime", 
						Long.toString(broker.getHeatingBoostOffTime()));
				result.put("WaterBoostTime", 
						Long.toString(broker.getWaterBoostOffTime()));
				result.put("DesiredTemp", 
						String.valueOf(broker.getdesired_temp()));
				result.put("boostTimeH", 
						String.valueOf(broker.getboostTimeH()));
				result.put("boostTimeW", 
						String.valueOf(broker.getboostTimeW()));
				return result;
			default :
				result.put("Result", "ERROR");
				result.put("Message", "Invalid argument");
				return result;
		}
	}
	
	/**
	 * Parses a string in format key1:value1,key2:value2 to a hashmap
	 * @param input string to parse
	 * @return HashMap of key/values
	 * @throws Exception if format is not correct
	 */
	private HashMap<String, String> parseString(String input) throws Exception
	{
		HashMap<String, String> output = new HashMap<String, String>();
		
		//Split around commas
		String[] firstSplit = input.split(PARAMETER_SEPARATOR);
		
		for (String parameter : firstSplit) {
			//Split the semicolons
			String[] secondSplit = parameter.trim().split(DELIMINATOR);
			
			if (secondSplit.length != 2) {
				throw new Exception("Invalid input received");
			}
			
			output.put(secondSplit[0], secondSplit[1]);
		}
		
		return output;
	}
	
	private String MapToJson(HashMap<String,String> map)
	{
		StringBuilder output = new StringBuilder("{");
		if (map != null) {
			Iterator<Entry<String, String>> itr = map.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();
				output.append(QUOTES + entry.getKey() + QUOTES + 
						DELIMINATOR + 
						QUOTES + entry.getValue() + QUOTES);
				if (itr.hasNext()) {
					output.append(PARAMETER_SEPARATOR);
				}
			}
		}
		output.append("}");
		return output.toString();
	}

}
