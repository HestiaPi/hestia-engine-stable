package uk.co.jaynne.gpio;
/**
import static framboos.FilePaths.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GpioPin {
	// REV 1:
	// private static final int [] mappedPins = {17, 18, 21, 22, 23, 24, 25, 4, 0, 1, 8, 7, 10, 9, 11, 14, 15};
	// REV 2:
	private static final int [] mappedPins = {17, 18, 27, 22, 23, 24, 25, 4, 2, 3, 8, 7, 10, 9, 11, 14, 15};
	
	protected final int pinNumber;
	
	protected boolean isClosing = false;
	
	public GpioPin(int pinNumber, Direction direction) {
		this.pinNumber = mappedPins[pinNumber];
		writeFile(getExportPath(), Integer.toString(this.pinNumber));
		writeFile(getDirectionPath(this.pinNumber), direction.getValue());
	}

	public boolean getValue() {
		if (isClosing) {
			return false;
		}
		try {
			FileInputStream fis = new FileInputStream(getValuePath(pinNumber));
			boolean value = (fis.read() == '1');
			fis.close();
			return value;
		} catch (IOException e) {
			throw new RuntimeException("Could not read from GPIO file: " + e.getMessage());
		}
	}
	
	public void close() {
		isClosing = true;
		writeFile(getUnexportPath(), Integer.toString(pinNumber));
	}
	
	protected void writeFile(String fileName, String value) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(value.getBytes());
			fos.close();
		} catch (IOException e) {
			throw new RuntimeException("Could not write to GPIO file: " + e.getMessage());
		}
	}
	
	public enum Direction {
		IN("in"), 
		OUT("out"),
		PWM("pwm");
		
		private String value;

		Direction(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
}
/**/
public enum GpioPin {
	PIN1_3V3, PIN2_5V, PIN3_GPIO2, PIN4, PIN5_GPIO3, PIN6_GROUND, PIN7_GPIO4, 
	PIN8_GPIO14, PIN9, PIN10_GPIO15, PIN11_GPIO17, PIN12_GPIO18, PIN13_GPIO27, 
	PIN14, PIN15_GPIO22, PIN16_GPIO23, PIN17, PIN18_GPIO24, PIN19_GPIO10, 
	PIN20, PIN21_GPIO9, PIN22_GPIO25, PIN23_GPIO11, PIN24_GPIO8, PIN25, PIN26_GPIO7;
}