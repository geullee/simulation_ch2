package kr.geul.simulation_ch2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import kr.geul.options.exception.AtTheMoneyException;
import kr.geul.options.exception.DuplicateOptionsException;
import kr.geul.options.exception.InconsistentArgumentLengthException;
import kr.geul.options.exception.InconsistentOptionException;
import kr.geul.options.exception.InvalidArgumentException;

public class Figure1_exponential extends Simulation {

	public static void run(String[] args) throws IOException, InvalidArgumentException, 
	InconsistentArgumentLengthException, AtTheMoneyException, DuplicateOptionsException, 
	InconsistentOptionException {
		
		String fileName = "d:/Chapter_2/_f1_" + Math.round(Double.parseDouble(args[0])) + ".csv";
		
		File file = new File(fileName);
		
		PrintWriter printWriter = new PrintWriter(new FileOutputStream(file, true));
		
		underlyingPrice = Double.parseDouble(args[0]);
		discountedUnderlyingPrice = Math.round((underlyingPrice * 
				  Math.exp(-dividendRate * timeToMaturity)) * 10.0) / 10.0;
		fullSampleLeftEnd = Math.round(Math.round(underlyingPrice) / 2.0 * 10.0) / 10.0; 
		fullSampleRightEnd = Math.round(Math.round(underlyingPrice) * 2.0 * 10.0) / 10.0;
		strikePrices = getStrikePrices_exponentialGap();
		
		System.out.println("S: " + underlyingPrice + ", Se-qt: "
				+ discountedUnderlyingPrice + ", Kmin: " + fullSampleLeftEnd
				+ ", Kmax: " + fullSampleRightEnd);
		
		readFullPrices_differentS(args[0]);
		setOptionCurves(fullSampleLeftEnd, fullSampleRightEnd);
		double[] bkmEstimates = getBKMEstimates();
		
		System.out.println("vol: " + bkmEstimates[0] + ", skew: "
				+ bkmEstimates[1] + ", kurt: " + bkmEstimates[2]); 
		
		for (int i = 0; i < bkmEstimates.length; i++) {
			printWriter.print(bkmEstimates[i]);
			if (i < bkmEstimates.length - 1)
				printWriter.print(",");
		}
		
		printWriter.println("");
		printWriter.close();
		
	}

}
