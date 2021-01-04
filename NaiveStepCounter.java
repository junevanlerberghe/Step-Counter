package fhs.cs.stepcounter;

import java.util.Arrays;

import fhs.cs.stepcounter.dataexplorer.CSVData;
import fhs.cs.stepcounter.interfaces.StepCounter;

public class NaiveStepCounter implements StepCounter {
	private CSVData data;
	private double[] mags;
	private int[] stepIndexes = new int[1000];

	/***
	 * Construct a step counter object pre-loaded with data. Data may be
	 * re-set/changed using the loadData(CSVData data) method.
	 * 
	 * @param data
	 */
	public NaiveStepCounter(CSVData data) {
		this.data = data;
	}

	/***
	 * No argument constructor. Client must run loadData(CSVData data) before they
	 * can count steps
	 */
	public NaiveStepCounter() {	
		data = null;
	}

	public static double[] averageOfN(double[] array, int n) {
		
		if (array.length < n) return null;
		
		double[] output = new double[array.length - (n -1)];
		double sum = 0;
		for (int i = 0; i < output.length; i++) {
			for(int j = i; j < i + n; j++) {
				sum += array[j];
			}
			output[i] = sum / n;
			sum = 0;
		}
		return output;
 
	}

	public static double calculateNMean(double[] arr, int index, int n) {
		double output = 0;
		for (int i = 0; i <  n; i++){
				output += arr[index + i];
		} return output/n;
	}

	/***
	 * Return the number of steps represented by the data in CSVData object.
	 * 
	 * @param data
	 *            a CSVData object which is a wrapper for the raw sensor data.
	 *            Extract the specific data you want using the .getDataForColumns
	 *            method. You can specify column names to get a 2d array of the
	 *            data.
	 * @return the number of steps represented by the data.
	 */
	@Override
	public int countSteps() {
		int steps = 0, nextFree = 0;
		double[][] accels = data.getDataForColumns(new String[] { "x acc", "y acc", "z acc" });
		mags = calculateMagnitudesFor(accels);
		mags = averageOfN(mags, 6);
	
		double mean = calculateMean(mags);
		double sd =  calculateStandardDeviation(mags, mean);
		double threshold = 2;
		double smallThreshold = 0.5;

		for(int i = 1; i < mags.length - 1; i++){
			if (mags[i] > mags[i - 1] && mags[i] > mags[i + 1]) {
				if (mags[i] > threshold){
					steps++;
					if(nextFree < stepIndexes.length){
						stepIndexes[nextFree] = i;
						nextFree++;
					}
				}
			}
		}
//		
//		for(int n = 1; n < mags.length - 1; n++){
//			if(mags[n] < mags[n - 1] && mags[n] < mags[n+1]){
//				if(mags[n] < smallThreshold){
//					steps++;
//					if(nextFree < stepIndexes.length) {
//						stepIndexes[nextFree] = n;
//						nextFree++;
//					}
//				}
//			}
//		}
		return steps;
	}

	/***
	 * Calculate the magnitude for a vector with x, y, and z components.
	 * 
	 * @param x
	 *            the x component
	 * @param y
	 *            the y component
	 * @param z
	 *            the z component
	 * @return the magnitude of the vector
	 */
	public static double calculateMagnitude(double x, double y, double z) {
		return Math.sqrt(x*x + y*y + z*z);
	}

	/***
	 * Takes a 2d array with 3 columns representing the 3 axes of a sensor.
	 * Calculates the magnitude of the vector represented by each row. Returns a new
	 * array with the same number of rows where each element contains this
	 * magnitude.
	 * 
	 * @param accelData
	 *            2d array of 3 axis acceleration data. Column 0 is x, column 1 is
	 *            y, column 2 is z
	 * 
	 * @return an array with n rows and each element is the magnitude of the vector
	 *         for the corresponding row in the sensorData array
	 */
	private static double[] calculateMagnitudesFor(double[][] accelData) {
		double[] arr = new double[accelData.length]; 

		for (int i = 0; i < accelData.length; i++){
			arr[i] = calculateMagnitude(accelData[i][0],accelData[i][1],accelData[i][2]);
		}
		
		return arr;
	}

	/***
	 * Return an array of values that can be used to graph. These could be values
	 * for one sensor, or the result of calculations involving several sensors.
	 * 
	 * @return an array of sensor values for graphing
	 */
	public double[] getDataForGraphing() {
		if(this.mags == null) this.countSteps();
		return this.mags;
	}

	/***
	 * Return an array giving the indexes in getDataForGraphing() where you
	 * calculate steps occurring. This can be used to visualize/debug.
	 * 
	 * @return an array where the ith element contains the index corresponding to
	 *         the ith step in getDataForGraphing()
	 */
	public int[] getStepIndexes() {
		if (this.stepIndexes == null) countSteps();
		return this.stepIndexes;
	}

	/***
	 * Return the standard deviation of the data.
	 * 
	 * @param arr
	 *            the array of the data
	 * @param mean
	 *            the mean of the data (must be pre-calculated).
	 * @return the standard deviation of the data.
	 */
	private static double calculateStandardDeviation(double[] arr, double mean) {
		double sd = 0;
		for(double num: arr) {
            sd += Math.pow(num - mean, 2);
        }

        return Math.sqrt(sd/10);
	}

	/***
	 * Return the mean of the data in the array
	 * 
	 * @param arr
	 *            the array of values
	 * @return the mean of the data
	 */
	private static double calculateMean(double[] arr) {
		double num = 0;
		for (int i = 0; i < arr.length; i++) {
			num += arr[i];
		}
		return num / (double)arr.length;
	}

	@Override
	public void loadData(CSVData csvdata) {
		this.data = csvdata;
		countSteps();
	}
}