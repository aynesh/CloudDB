package app_ecsServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
import com.workday.insights.timeseries.arima.struct.ForecastResult;

import app.common.Forecast;

public class ECSKVServerTrafficForecast {
	public static Forecast getForecast(String dataFileLocation) throws IOException {
	   	FileReader fileReader = new FileReader(dataFileLocation);
    	BufferedReader bufferedReader = new BufferedReader(fileReader);

    	String value = bufferedReader.readLine();
    	List<Double> readConsistancyData = new ArrayList<Double>();
    	List<Double> writeConsitancyData = new ArrayList<Double>(); 
    	while((value=bufferedReader.readLine())!=null) {
    		String[] line=value.split("\t");
    		readConsistancyData.add(Double.parseDouble(line[0]));
    		writeConsitancyData.add(Double.parseDouble(line[1]));
    	}
    	
    	bufferedReader.close();
    	

		double[] readData = readConsistancyData.stream().mapToDouble( f-> f!= null ? f : 0.0).toArray();
		double[] writeData = writeConsitancyData.stream().mapToDouble( f-> f!= null ? f : 0.0).toArray();

		// Set ARIMA model parameters.
		int p = 3;
		int d = 0;
		int q = 3;
		int P = 1;
		int D = 1;
		int Q = 0;
		int m = 0;
		int forecastSize = 1;
		
		ArimaParams arimaParams = new ArimaParams(p, d, q, P, D, Q, m);

		// Obtain forecast result. The structure contains forecasted values and performance metric etc.
		ForecastResult forecastResult = Arima.forecast_arima(readData, forecastSize, arimaParams);

		// Read forecast values
		double[] forecastDataRead = forecastResult.getForecast(); // in this example, it will return { 2 }

		
		forecastResult=  Arima.forecast_arima(writeData, forecastSize, arimaParams);
		double[] forecastDataWrite = forecastResult.getForecast(); 
//		// You can obtain upper- and lower-bounds of confidence intervals on forecast values.
//		// By default, it computes at 95%-confidence level. This value can be adjusted in ForecastUtil.java
//		double[] uppers = forecastResult.getForecastUpperConf();
//		double[] lowers = forecastResult.getForecastLowerConf();
//
//		// You can also obtain the root mean-square error as validation metric.
//		double rmse = forecastResult.getRMSE();
//
//		// It also provides the maximum normalized variance of the forecast values and their confidence interval.
//		double maxNormalizedVariance = forecastResult.getMaxNormalizedVariance();
//
//		// Finally you can read log messages.
//		String log = forecastResult.getLog();
		System.out.println(forecastDataRead[0]);
		System.out.println(forecastDataWrite[0]);
		return new Forecast() {

			@Override
			public int getReadStats() {
				return (int)Math.round(forecastDataRead[0]);
			}

			@Override
			public int getWriteStats() {
				return (int)Math.round(forecastDataWrite[0]);
			} };
	}
}
