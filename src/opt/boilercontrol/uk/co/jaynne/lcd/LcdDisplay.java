package uk.co.jaynne.lcd;

import uk.co.jaynne.gpio.GpioControl;
import uk.co.jaynne.gpio.GpioControlFramboos;
import uk.co.jaynne.gpio.GpioPin;

/**
 * Allows output to a 16x2 character LCD display
 * Code ported from Python to Java with minor modifications based on 
 * http://www.raspberrypi-spy.co.uk/2012/07/16x2-lcd-module-control-using-python/
 * @author James Cooke
 *
 */
public class LcdDisplay {
	private GpioControl gpio;
	private GpioPin LCD_RS = GpioPin.PIN24_GPIO8;
	private GpioPin LCD_E = GpioPin.PIN22_GPIO25;
	private GpioPin LCD_D4 = GpioPin.PIN19_GPIO10;
	private GpioPin LCD_D5 = GpioPin.PIN16_GPIO23;
	//private GpioPin LCD_D6 = GpioPin.PIN13_GPIO27; //Does not work in Raspi Rev2. Did not investigate further
	private GpioPin LCD_D6 = GpioPin.PIN23_GPIO11;
	private GpioPin LCD_D7 = GpioPin.PIN12_GPIO18;
	private int LCD_WIDTH = 16;
	private boolean LCD_CHR = true;
	private boolean LCD_CMD = false;
	public static char LCD_LINE1 = 0x80;
	public static char LCD_LINE2 = 0xC0;
	private int E_PULSE = 50000;
	private int E_DELAY = 50000;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int CENTER = 2;
	
	
	private LcdDisplay() {
        gpio = GpioControlFramboos.getInstance();
		gpio.setAsOutput(LCD_RS);
		gpio.setAsOutput(LCD_E);
		gpio.setAsOutput(LCD_D4);
		gpio.setAsOutput(LCD_D5);
		gpio.setAsOutput(LCD_D6);
		gpio.setAsOutput(LCD_D7);
		lcd_init();
	}
	
	private static class SingletonHolder { 
        public static final LcdDisplay INSTANCE = new LcdDisplay();
	}

	public static LcdDisplay getInstance() {
		return SingletonHolder.INSTANCE;
	}
	

	public synchronized void close() {
		gpio.close(LCD_RS);
		gpio.close(LCD_E);
		gpio.close(LCD_D4);
		gpio.close(LCD_D5);
		gpio.close(LCD_D6);
		gpio.close(LCD_D7);
	}
	
	/**
	 * Write a string to the screen, left justified
	 * @param line the line to write to
	 * @param message messages > the screen width are truncated
	 */
	public synchronized void write(char line, String message) {
		write(line, message, LEFT);
	}
	
	/**
	 * Write a string to the screen
	 * @param line the line to write to
	 * @param message messages > the screen width are truncated
	 * @param justification set the justification of the message
	 */
	public synchronized void write(char line, String message, int justification) {
		//Set the line to write
		lcd_byte(line, LCD_CMD);
		
		//Trim strings > than the screen length
		int length = (message.length() > LCD_WIDTH) ? LCD_WIDTH : message.length();
		message = message.substring(0, length);
		
		if (justification == LEFT) {
			message = String.format("%-" + LCD_WIDTH + "s", message);
		}
		if (justification == RIGHT) {
			message = String.format("%" + LCD_WIDTH + "s", message);
		}
		if (justification == CENTER) {
			length = message.length();
			int diff = LCD_WIDTH - length;
			diff = diff / 2;
			int padded = length + diff;
			message = String.format("%" + padded + "s", message);
			message = String.format("%-" + LCD_WIDTH + "s", message);
		}
		lcd_string(message);
	}
	
	public void test() {
		lcd_byte(LCD_LINE1, LCD_CMD);
		lcd_string("Rasbperry Pi");
		lcd_byte(LCD_LINE2, LCD_CMD);
		lcd_string("Model B");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {}
	}

	private synchronized void lcd_string(String message) {
		char[] characters = message.toCharArray();
		for (int i = 0; i < LCD_WIDTH; i++) {
			lcd_byte((int)characters[i], LCD_CHR);
		}
	}
	
	public synchronized void lcd_init() {
		// Initialised the display
		lcd_byte((char)0x33, LCD_CMD);
		lcd_byte((char)0x32, LCD_CMD);
		lcd_byte((char)0x28, LCD_CMD);
		lcd_byte((char)0x0c, LCD_CMD);
		lcd_byte((char)0x06, LCD_CMD);
		lcd_byte((char)0x01, LCD_CMD);
	}

	private synchronized void lcd_byte(int bits, boolean mode) {
		/**
		 * Send byte to data pins bits = data mode = True for character False
		 * for command
		 **/
		gpio.setValue(LCD_RS, mode);

		// High bits
		gpio.setValue(LCD_D4, false);
		gpio.setValue(LCD_D5, false);
		gpio.setValue(LCD_D6, false);
		gpio.setValue(LCD_D7, false);
		if ((bits & 0x10) == 0x10) {
			gpio.setValue(LCD_D4, true);
		}
		if ((bits & 0x20) == 0x20) {
			gpio.setValue(LCD_D5, true);
		}
		if ((bits & 0x40) == 0x40) {
			gpio.setValue(LCD_D6, true);
		}
		if ((bits & 0x80) == 0x80) {
			gpio.setValue(LCD_D7, true);
		}

		// Toggle 'Enable' pin
		try {
			Thread.sleep(0, E_DELAY);
			gpio.setValue(LCD_E, true);
			Thread.sleep(0, E_PULSE);
			gpio.setValue(LCD_E, false);
			Thread.sleep(0, E_DELAY);
		} catch (InterruptedException e) {}

		// Low bits
		gpio.setValue(LCD_D4, false);
		gpio.setValue(LCD_D5, false);
		gpio.setValue(LCD_D6, false);
		gpio.setValue(LCD_D7, false);
		if ((bits & 0x01) == 0x01) {
			gpio.setValue(LCD_D4, true);
		}
		if ((bits & 0x02) == 0x02) {
			gpio.setValue(LCD_D5, true);
		}
		if ((bits & 0x04) == 0x04) {
			gpio.setValue(LCD_D6, true);
		}
		if ((bits & 0x08) == 0x08) {
			gpio.setValue(LCD_D7, true);
		}

		// Toggle 'Enable' pin
		try {
			Thread.sleep(0, E_DELAY);
			gpio.setValue(LCD_E, true);
			Thread.sleep(0, E_PULSE);
			gpio.setValue(LCD_E, false);
			Thread.sleep(0, E_DELAY);
		} catch (InterruptedException e) {}
	}
}
