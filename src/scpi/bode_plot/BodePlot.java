package scpi.bode_plot;

import scpi.siglent.SDG1025;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import scpi.rigol.DS1054Z;

public class BodePlot{
	DS1054Z scope;
	SDG1025 generator;
	FileWriter writer;

	public BodePlot() throws IOException{	
		this.scope = new DS1054Z("192.168.123.2", 5555);
		this.generator = new SDG1025("192.168.123.4");
		this.writer = new FileWriter(new File("bode_plot.csv")); 
		
	}
	
	private void findRangeVoltage() throws IOException {
		double voltageCoarse[] = {1E-3, 2E-3, 5E-3, 1E-2, 2E-2, 5E-2, 0.1, 0.2, 0.5, 1, 2, 5, 10};
		
		//Start from 2V/div
		int currentScaleIndex = 10;
		
		while(true) {
			this.scope.setVerticalScale(1, voltageCoarse[currentScaleIndex], false);
			double voltage = this.scope.measure(1);
			if(voltage > 2.0/5*(voltageCoarse[currentScaleIndex]*8) || currentScaleIndex == 0)
				break;
			else
				currentScaleIndex--;
			
		}
		
	}
	
	private void getBodePlot(double startFreq, double stopFreq, int numPoints) throws IOException {
		double time[] = {5E-9, 1E-8, 2E-8, 5E-8, 1E-7, 2E-7, 5E-7, 1E-6, 2E-6, 5E-6, 1E-5, 2E-5, 5E-5, 1E-4, 2E-4, 5E-4,
				 1E-3, 2E-3, 5E-3, 1E-2, 2E-2, 5E-2, 1E-1, 2E-1, 5E-1, 1, 2, 5, 10, 20, 50};
		
		
		// Generate log spaced frequency values
		double [] logSpace = new double[numPoints];
		double delta = Math.log10(stopFreq/startFreq)/numPoints;
		double accDelta = 0;
		
		for(int i=0; i< numPoints; i++) {
			logSpace[i] = Math.pow(10, Math.log10(startFreq)+accDelta);
			accDelta += delta;
		}
		
		
		try {
			this.generator.setFrequency(logSpace[0]);
			Thread.sleep(10);
			this.generator.setVoltage(10);
			Thread.sleep(10);
			this.generator.output(true);
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(int i=0; i< numPoints; i++) {
			
			this.generator.setFrequency(logSpace[i]);
			// Display approx three period of sine on the oscilloscope
			double period = (3/logSpace[i]);
			
			// Find appropriate scale
			int index = 0;
			while(period/12>time[index]){
				index++;
			}
			
			this.scope.setHorizontalScale(time[index-1]);
			
			this.findRangeVoltage();
			double voltage = this.scope.measure(1);
			System.out.println(voltage);
			this.writer.write(Double.toString(logSpace[i])+","+Double.toString(20*Math.log(voltage/10))+"\n");
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.generator.output(false);
		
	}
	
	private void closeConnection() throws IOException {
		this.scope.close();
		this.generator.close();
		this.generator.writeCommand("USB0::62701::60986::SDG10GAX3R0381::0::INSTR");
		this.writer.close();
	}
	
	public static void main(String[] args) {
		BodePlot bode;
		
		try {
			bode = new BodePlot();
			
			bode.getBodePlot(10, 10000, 20);
		
			bode.closeConnection();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}