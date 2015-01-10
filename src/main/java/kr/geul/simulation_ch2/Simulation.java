package kr.geul.simulation_ch2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import kr.geul.bkm.BKMEstimator;
import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.DuplicateOptionsException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InconsistentOptionException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.CallOption;
import kr.geul.options.option.Option;
import kr.geul.options.option.PutOption;
import kr.geul.options.structure.OptionCurve;

public class Simulation {
	
	static boolean isRunOnServer = false;
	
	/* ATM option setting */
	final static ATMSetting atmSetting = ATMSetting.callAndPutATM;

	/* Parameter name arrays */
	final static String[] bsParameterNames = {"S", "K", "R", "T", "D", "C", "sigma"};
	
	/* Parameter values */
	final static double riskFreeRate = 0.07, dividendRate = 0.0,
			timeToMaturity = (1.0 / 12.0), defaultOptionPrice = 0.0;
	static double underlyingPrice = 1000.0, 
				  discountedUnderlyingPrice = Math.round((underlyingPrice * 
				  Math.exp(-dividendRate * timeToMaturity)) * 10.0) / 10.0;
	static double bsSigma = 0.2;

	/* Simulation settings */
	static double gapBetweenStrikePrices = 2.5, 
			fullSampleLeftEnd = Math.round(underlyingPrice / 2.0 * 10.0) / 10.0, 
			fullSampleRightEnd = Math.round(underlyingPrice * 2.0 * 10.0) / 10.0;
	final static int numberOfLines = 5;
	final static int numberOfXAxisPoints = 1; 

	/* Strike price and option arrays */
	static double[] strikePrices = getStrikePrices();
	static int numberOfFullObservations = strikePrices.length;
	static Option[] bsOptions  = new Option[numberOfFullObservations],
			svjOptions = new Option[numberOfFullObservations];
	
	/* Option curves and estimation result arrays */
	static OptionCurve bsOptionCurve, svjOptionCurve;
	static double bsTrueVol, bsTrueSkew, bsTrueKurt, svjTrueVol, svjTrueSkew, svjTrueKurt;
	static double[][] bsVols = new double[numberOfLines][numberOfXAxisPoints], 
			bsSkews  = new double[numberOfLines][numberOfXAxisPoints], 
			bsKurts  = new double[numberOfLines][numberOfXAxisPoints], 
			svjVols  = new double[numberOfLines][numberOfXAxisPoints], 
			svjSkews = new double[numberOfLines][numberOfXAxisPoints], 
			svjKurts = new double[numberOfLines][numberOfXAxisPoints];

	/* BKM Estimator */
	static BKMEstimator bkmEstimator = new BKMEstimator();

	protected static double[] getBKMEstimates() 
			throws DuplicateOptionsException, InconsistentOptionException {

		double[] bsBKMEstimates, bkmEstimates = new double[7];

		bkmEstimator.setOptions(bsOptionCurve);
		bsBKMEstimates = bkmEstimator.getEstimates();
		bkmEstimates[0] = bsBKMEstimates[0];
		bkmEstimates[1] = bsBKMEstimates[1];
		bkmEstimates[2] = bsBKMEstimates[2];
		bkmEstimates[3] = bsBKMEstimates[3];
		bkmEstimates[4] = bsBKMEstimates[4];
		bkmEstimates[5] = bsBKMEstimates[5];
		bkmEstimates[6] = bsBKMEstimates[6];
		
		return bkmEstimates;

	}
		
	/* Get the number of strike prices */
	protected static double[] getStrikePrices() {

		ArrayList<Double> prices = new ArrayList<Double>();
		
		for (double strikePrice = 0; 
				strikePrice < fullSampleRightEnd + gapBetweenStrikePrices; 
				strikePrice += gapBetweenStrikePrices) {	

			if (strikePrice > gapBetweenStrikePrices && strikePrice > discountedUnderlyingPrice &&
				strikePrice - gapBetweenStrikePrices < discountedUnderlyingPrice) {
			
				switch (atmSetting) {

				case noATM:
					break;

				case callATMOnly:
					prices.add(discountedUnderlyingPrice);
					break;

				case putATMOnly:
					prices.add(discountedUnderlyingPrice);
					break;

				default:
					prices.add(discountedUnderlyingPrice);
					prices.add(discountedUnderlyingPrice);

				}
					
			}
			
			if (Math.round(strikePrice * 100.0) == Math.round(discountedUnderlyingPrice * 100.0)) {
				
				switch (atmSetting) {

				case noATM:
					break;

				case callATMOnly:
					prices.add(strikePrice);
					break;

				case putATMOnly:
					prices.add(strikePrice);
					break;

				default:
					prices.add(strikePrice);
					prices.add(strikePrice);

				}
				
			}
			
			else if (strikePrice >= fullSampleLeftEnd && strikePrice <= fullSampleRightEnd) 
				prices.add(strikePrice);
					
		}

		double[] strikePrices = new double[prices.size()];

		for (int i = 0; i < strikePrices.length; i++) {
			strikePrices[i] = prices.get(i);
		}

		return strikePrices;

	}
	
	protected static double[] getStrikePrices_exponentialGap() {

		ArrayList<Double> prices = new ArrayList<Double>();
		
		for (double exponent = Math.log(0.5); 
				exponent < Math.log(2.01); 
				exponent += (1.0 / 15.0) * Math.log(2.0)) {	

			if (Math.round(exponent * 1000) / 1000.0 == 0) {
				
				switch (atmSetting) {

				case noATM:
					break;

				case callATMOnly:
					prices.add(discountedUnderlyingPrice);
					break;

				case putATMOnly:
					prices.add(discountedUnderlyingPrice);
					break;

				default:
					prices.add(discountedUnderlyingPrice);
					prices.add(discountedUnderlyingPrice);

				}
				
			}
			
			else if (discountedUnderlyingPrice * Math.exp(exponent) >= fullSampleLeftEnd && 
					discountedUnderlyingPrice * Math.exp(exponent) <= fullSampleRightEnd) 
				prices.add(discountedUnderlyingPrice * Math.exp(exponent));
					
		}

		double[] strikePrices = new double[prices.size()];

		for (int i = 0; i < strikePrices.length; i++) {
			strikePrices[i] = prices.get(i);
		}

		return strikePrices;

	}
	
	protected static double[][] getPricesAndImpVols() throws IOException {
		
		double[][] result = new double[strikePrices.length][7];
		String fileName;
		
		if (isRunOnServer == true)
			fileName = "./FullPrices_ch2.csv";
		else
			fileName = "d:/Chapter_2/FullPrices_ch2.csv";
		
		File file = new File(fileName);
		String line;

		BufferedReader fileReader = 
				new BufferedReader(new FileReader(file));
		
		int location = 0;
		
		while ((line = fileReader.readLine()) != null) {
			
			String[] data = line.split(",", -1);
			
			for (int i = 0; i < data.length; i++) {
				if (data[i].length() > 0)
					result[location][i] = Double.parseDouble(data[i]);
			}
			
			location++;
			
		}
		
		fileReader.close();
		
		return result;
				
	}
	
protected static double[][] getPricesAndImpVols_differentS(String s) throws IOException {
		
		double[][] result = new double[strikePrices.length][7];
		String fileName;
		
		if (isRunOnServer == true)
			fileName = "./FullPrices_ch2_" + Math.round(Double.parseDouble(s)) + ".csv";
		else
			fileName = "d:/Chapter_2/FullPrices_ch2_" + Math.round(Double.parseDouble(s)) + ".csv";
		
		File file = new File(fileName);
		String line;

		BufferedReader fileReader = 
				new BufferedReader(new FileReader(file));
		
		int location = 0;
		
		while ((line = fileReader.readLine()) != null) {
			
			String[] data = line.split(",", -1);
			
			for (int i = 0; i < data.length; i++) {
				if (data[i].length() > 0)
					result[location][i] = Double.parseDouble(data[i]);
			}
			
			location++;
			
		}
		
		fileReader.close();
		
		return result;
				
	}
	
	protected static void readFullPrices() throws InvalidArgumentException, 
	InconsistentArgumentLengthException, AtTheMoneyException, IOException {

		double[][] pricesAndImpVols = getPricesAndImpVols();
		
		for (int index = 0; index < strikePrices.length; index++) {
			
			double strikePrice = strikePrices[index];
			Option bsOption;
			
			double[] bsValues = {underlyingPrice, strikePrice, riskFreeRate, 
					timeToMaturity, dividendRate, pricesAndImpVols[index][1], pricesAndImpVols[index][3]};
			
			if (strikePrices[0] >= discountedUnderlyingPrice) {
				
				bsOption = new CallOption("BS");
				
			}
			
			else if (strikePrice < discountedUnderlyingPrice ||  
					(strikePrice == discountedUnderlyingPrice && 
					(atmSetting == ATMSetting.putATMOnly || 
					(atmSetting == ATMSetting.callAndPutATM && (strikePrices[index - 1] < discountedUnderlyingPrice))
					))) {

				bsOption = new PutOption("BS");				

			}	
			
			else {
				
				bsOption = new CallOption("BS");
				
			}
			
			bsOption.set(bsParameterNames, bsValues);

			bsOptions[index] = bsOption;
			
		}
		
	}
	
	protected static void readFullPrices_differentS(String s) throws InvalidArgumentException, 
	InconsistentArgumentLengthException, AtTheMoneyException, IOException {

		double[][] pricesAndImpVols = getPricesAndImpVols_differentS(s);
		
		for (int index = 0; index < strikePrices.length; index++) {
			
			double strikePrice = strikePrices[index];
			System.out.println(strikePrice);
			Option bsOption;
			
			double[] bsValues = {underlyingPrice, strikePrice, riskFreeRate, 
					timeToMaturity, dividendRate, pricesAndImpVols[index][1], pricesAndImpVols[index][3]};
			
			if (strikePrices[0] >= discountedUnderlyingPrice) {
				
				bsOption = new CallOption("BS");
				
			}
			
			else if (strikePrice < discountedUnderlyingPrice ||  
					(strikePrice == discountedUnderlyingPrice && 
					(atmSetting == ATMSetting.putATMOnly || 
					(atmSetting == ATMSetting.callAndPutATM && (strikePrices[index - 1] < discountedUnderlyingPrice))
					))) {

				bsOption = new PutOption("BS");				

			}	
			
			else {
				
				bsOption = new CallOption("BS");
				
			}
			
			bsOption.set(bsParameterNames, bsValues);

			bsOptions[index] = bsOption;
			
		}
		
	}
	
	protected static void setOptionCurves(double left, double right) throws DuplicateOptionsException, InconsistentOptionException {

		int leftEndStrikeIndex = 0, rightEndStrikeIndex = 0;

		for (int i = 0; i < strikePrices.length; i++) {

			double strike = strikePrices[i];
			if (Math.round(strike * 100.0) == Math.round(left * 100.0) || 
				(i > 0 && strikePrices[i - 1] < left && strike > left))  
				leftEndStrikeIndex = i;

			else if (Math.round(strike * 100.0) == Math.round(right * 100.0) ||
				(i > 0 && strikePrices[i - 1] < right && strike > right) ||
				(i == strikePrices.length - 1 && strike < right))
				rightEndStrikeIndex = i;

		}
		
		bsOptionCurve = new OptionCurve();

		for (int i = leftEndStrikeIndex; i <= rightEndStrikeIndex; i++) {
			
			bsOptionCurve.add(bsOptions[i]);

		}

	}
	
}
