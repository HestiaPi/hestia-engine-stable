package uk.co.jaynne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import uk.co.jaynne.datasource.ConfigSqlSource;
import uk.co.jaynne.datasource.interfaces.ConfigSource;

/**
 * Program to control a boiler with two channels - heating and water.
 * Schedules are set in a database with the option to override them via
 * "boost" buttons
 * @author James Cooke
 * @version 1.1
 *
 */
public class BoilerControl {

	public static void main(String[] args) {
		
		ConfigSource config = new ConfigSqlSource();
		//The scheduler thread deals with checking whether any channels are due to come on
		Thread scheduler = new Thread(new Scheduler());
		scheduler.start();
		
		boolean pinsHigh = false; //pins are default low
		//Get the configured pins value
		if (config.get("pinsHigh") != null) {
			pinsHigh = config.get("pinsHigh").getBoolValue();
		}
		//Monitors the water boost button for presses
		Thread wBoost = new Thread(new BoostMonitor(ControlBroker.SWITCH1, true, false, false, false, pinsHigh));
		wBoost.start();
		//Monitors the heating boost button for presses
		Thread hBoost = new Thread(new BoostMonitor(ControlBroker.SWITCH2, false, true, false, false, pinsHigh));
		hBoost.start();
		//Monitors the temperature + button for presses
		Thread plusmonitor = new Thread(new BoostMonitor(ControlBroker.SWITCH3, false, false, true, false, pinsHigh));
		plusmonitor.start();
		//Monitors the temperature - button for presses
		Thread minusmonitor = new Thread(new BoostMonitor(ControlBroker.SWITCH4, false, false, false, true, pinsHigh));
		minusmonitor.start();
		//Socket server
		Thread socketServer = new Thread(new SocketServer());
		socketServer.start();
		//LCD output
		Thread lcd = new Thread(new LcdOutput());
		lcd.start();

		// open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String input = "";

		//Check input for a press of the q button to exit
		while (!input.equals("q")) {
			try {
				System.out.println("Press q to exit");
				input = br.readLine();
			} catch (IOException ioe) {
				System.out.println("IO error trying to read your response!");
				System.exit(1);
			}
		}
		try {
			System.out.println("Stopping scheduler");
			scheduler.interrupt();
			scheduler.join();
			System.out.println("Stopping water boost monitor");
			wBoost.interrupt();
			wBoost.join();
			System.out.println("Stopping heating boost monitor");
			hBoost.interrupt();
			hBoost.join();
			System.out.println("Stopping temperature + monitor");
			plusmonitor.interrupt();
			plusmonitor.join();
			System.out.println("Stopping temperature - monitor");
			minusmonitor.interrupt();
			minusmonitor.join();
			System.out.println("Stopping socket server");
			socketServer.interrupt();
			socketServer.join();
			System.out.println("Stopping lcd output");
			lcd.interrupt();
			while (lcd.isAlive()) {
				lcd.join(500);
				if (lcd.isAlive()) {
					System.out.println("Stubourn thread, trying again");
					lcd.interrupt();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Turning heating and water off");
		ControlBroker control = ControlBroker.getInstance();
		if (control.isWaterBoostOn()) {
			control.toggleWaterBoostStatus();
		}
		if (control.isHeatingBoostOn()) {
			control.toggleHeatingBoostStatus();
		}
		control.turnHeatingOff();
		control.turnWaterOff();
		control.turnBacklightOff();
		
		//TODO
		//control.close() leaves GPIO HIGH so if java exits, it will leave boiler ON !!!
		//if you dont call close() at the end, the GPIO pins will be left busy 
		//and running java again will fail to control them, hence you will need to restart RasPi to fix
		control.close();  
		System.out.println("Exiting");
	}
}
