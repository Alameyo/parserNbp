package pl.parser.nbp;

/**
 * Author: Paweł Ścibiorski
 * This class take user input which have to meet certain pattern
 * WWW RRRR-MM-DD RRRR-MM-DD for example USD 2007-04-13 2008-02-20
 * values in pattern are currency code, date of the first query 
 * and date of last query.
 */
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class MasterDecider {
	private String query;
	private String kodWaluty, dataKonca, dataPoczatku;

	// pattern of input
	private Pattern pattern = Pattern.compile("[A-Z]{3} [0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{4}-[0-9]{2}-[0-9]{2}");

	public void masterTranslator() throws ParserConfigurationException, SAXException {
		boolean inputing = true;
		Scanner inputCode = new Scanner(System.in);
		while (inputing == true) {

			try {
				// query have to be inputed in pattern WWW RRRR-MM-DD RRRR-MM-DD
				// for example EUR 2013-01-28 2013-01-31
				query = inputCode.nextLine(); // inputing of data from user

				Matcher matcher = pattern.matcher(query); // checking if data
															// meets
															// the format
				boolean isPatternFound = matcher.matches();
				if (isPatternFound == true) {

					kodWaluty = query.substring(0, 3);
					dataPoczatku = query.substring(6, 8) + query.substring(9, 11) + query.substring(12, 14);
					dataKonca = query.substring(17, 19) + query.substring(20, 22) + query.substring(23, 25);
					if (checkDateProgress(dataPoczatku, dataKonca) == false) {
						inputing = false;
					}
				}

			} catch (Exception e) { //in case of bad input
				inputing = true;
			}

		}
		inputCode.close();

	}

	public String getKodWaluty() {
		return kodWaluty;
	}

	public String getDataKonca() {
		return dataKonca;
	}

	public String getDataPoczatku() {
		return dataPoczatku;
	}

	boolean checkDateProgress(String beginDate, String endDate) {
		boolean badIn;
		if (Integer.valueOf(beginDate) > Integer.valueOf(endDate)) {
			badIn = true;
		} else {
			badIn = false;
		}
		return badIn;

	}
}
