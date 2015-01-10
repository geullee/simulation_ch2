package kr.geul.simulation_ch2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InvalidArgumentException;
import kr.geul.options.option.CallOption;
import kr.geul.options.option.Option;
import kr.geul.options.option.PutOption;

public class GenerateFullPrices_exponential extends Simulation {

	public static void run(String[] args) throws InvalidArgumentException, InconsistentArgumentLengthException, 
		AtTheMoneyException, FileNotFoundException {
		
		underlyingPrice = Double.parseDouble(args[0]);
		discountedUnderlyingPrice = Math.round((underlyingPrice * 
				  Math.exp(-dividendRate * timeToMaturity)) * 10.0) / 10.0;
		fullSampleLeftEnd = (Math.round(underlyingPrice) / 2.0 * 10.0) / 10.0; 
		fullSampleRightEnd = (Math.round(underlyingPrice) * 2.0 * 10.0) / 10.0;
		strikePrices = getStrikePrices_exponentialGap();
		bsOptions = new Option[strikePrices.length];
		generateFullPrices();
		printResults();
		
	}
	
	private static void generateFullPrices() throws InvalidArgumentException, 
	InconsistentArgumentLengthException, AtTheMoneyException {

		boolean isATMOptionGenerated = false;
		
		for (int index = 0; index < strikePrices.length; index++) {
			
			double strikePrice = strikePrices[index];
			System.out.println(strikePrice);
			Option bsOption;
			
			double[] bsValues = {discountedUnderlyingPrice, strikePrice, riskFreeRate, 
					timeToMaturity, dividendRate, defaultOptionPrice, bsSigma};
			
			if (strikePrice < discountedUnderlyingPrice ||  
					(strikePrice == discountedUnderlyingPrice && 
					(atmSetting == ATMSetting.putATMOnly || 
					(atmSetting == ATMSetting.callAndPutATM && isATMOptionGenerated == false)
					))) {

				bsOption = new PutOption("BS");			

			}	
			
			else {
				
				bsOption = new CallOption("BS");
				
			}
			
			bsOption.set(bsParameterNames, bsValues);

			bsOption.evaluate();

			bsOptions[index] = bsOption;
			
			if (strikePrice == discountedUnderlyingPrice)
				isATMOptionGenerated = true;
			
		}
		
	}
	
	private static void printResults() throws FileNotFoundException, InvalidArgumentException {
		
		String fileName;
		
		if (isRunOnServer == true) 
			fileName = "./FullPrices_ch2_" + Math.round(underlyingPrice) + ".csv";				
		else
			fileName = "d:/Chapter_2/FullPrices_ch2_" + Math.round(underlyingPrice) + ".csv";
		
		File file = new File(fileName);
		
		PrintWriter printWriter = new PrintWriter(new FileOutputStream(file, true));
		
		/* K, bsPrice, svjPrice, bsIV, svjIV */ 

		for (int i = 0; i < strikePrices.length; i++) {
			
			printWriter = new PrintWriter(new FileOutputStream(file, true));
			printWriter.println(strikePrices[i] + ","
					+ bsOptions[i].getOptionPrice() + ","
					+ bsOptions[i].getBSImpVol() + ",");

			System.out.println(strikePrices[i] + ","
					+ bsOptions[i].getOptionPrice() + ","
					+ bsOptions[i].getBSImpVol() + ",");
			
			printWriter.close();
			
		}
		
	}
	
}
