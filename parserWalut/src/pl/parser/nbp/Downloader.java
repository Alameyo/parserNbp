package pl.parser.nbp;

/**
 * Author: Paweł Ścibiorski
 * This class connect to proper XML from nbp's website, save it 
 * into Document class object. Parser object take Document object
 * and kodWaluty as parameters into toParse() method which
 * parse through XML to find adequate data.
 * */
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import org.xml.sax.SAXException;

public class Downloader {
	private BigDecimal sumOfbuy, sumOfSell, divider;
	private String question;
	private String date;
	ArrayList<BigDecimal> listOfSell;
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private Document doc;

	private int i, j, k;

	private int y1, y2;
	private int m1, m2;
	private int d1, d2;
	private Parser parser1;
	private boolean found;
	private boolean firstCheck;

	Downloader() throws ParserConfigurationException {
		sumOfbuy = new BigDecimal(0);
		sumOfSell = new BigDecimal(0);
		divider = new BigDecimal(0);
		listOfSell = new ArrayList<BigDecimal>();
		question = null;
		i = 0; // number of directory in year
		j = 0;
		k = 1;
		parser1 = new Parser();
		factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
	}
	/**
	 * Checks urls from beginning date to end date, save XML from url into  document
	 * and take information from document by toParse method. At the end count mean
	 * and standard deviation from this information.
	 */
	void download(String kodWaluty, String dataPoczatku, String dataKonca)
			throws ParserConfigurationException, SAXException {
		this.date = dataPoczatku;
		y1 = Character.getNumericValue(date.charAt(0)); // getting value of
																// date from
																// String
		y2 = Character.getNumericValue(date.charAt(1));
		m1 = Character.getNumericValue(date.charAt(2));
		m2 = Character.getNumericValue(date.charAt(3));
		d1 = Character.getNumericValue(date.charAt(4));
		d2 = Character.getNumericValue(date.charAt(5));
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int ad2 = year%10;
		year = year/10;
		int ad1 = year%10;
		firstCheck = true; // for first query the number of directory in
								// year is unknown,so there is need to find
								// directory with valid date
		do {
			found = false;

			while (found == false && i < 3) {

				question = "http://www.nbp.pl/kursy/xml/c" + i + "" + j + "" + k + "z" + date
						+ ".xml";
				System.out.println(question +ad1 + ad2 );
				try {

					doc = builder.parse(question);
					parser1.toParse(doc, kodWaluty);
					found = true;
					divider = divider.add(new BigDecimal(1));
					sumOfbuy = sumOfbuy.add(parser1.getBigOneBuy());
					sumOfSell = sumOfSell.add(parser1.getBigOneSell());
					listOfSell.add(parser1.getBigOneSell());
					k++; // change number of document in year
					if (k >= 10) {
						k = 0;
						j++;
						if (j >= 10) {
							j = 0;
							i++;
						}
					}

					firstCheck = false; // stop searching for number of
												// document with provided
												// beginning date
					break;

				} catch (IOException e) {
					// in case that document wasn't found at that address and
					// algorithm is searching for first document since given
					// date
					if (firstCheck == true) {
						k++;
						if (k >= 10) {
							k = 0;
							j++;
							if (j >= 10) {
								j = 0;
								i++;
							}
						}
					} else {
						break;
					}
				}
			}
			if (firstCheck == true) {
				i = 0;
				j = 0;
				k = 1;
			}
			dateProgress();
		} while (date.compareTo(dataKonca) < 0);
		// last query which is out of loop
		if (dataPoczatku.equals(dataKonca) == false) {
			question = "http://www.nbp.pl/kursy/xml/c" + i + "" + j + "" + k + "z" + dataKonca
					+ ".xml";

			try {
				doc = builder.parse(question);
				parser1.toParse(doc, kodWaluty);
				found = true;
				sumOfbuy = sumOfbuy.add(parser1.getBigOneBuy());
				sumOfSell = sumOfSell.add(parser1.getBigOneSell());
				listOfSell.add(parser1.getBigOneSell());
				divider = divider.add(new BigDecimal(1));
			} catch (IOException e) {
			}
		}
		BigDecimal mean1 = mean(sumOfbuy, divider);
		System.out.println(mean1);
		BigDecimal mean2 = mean(sumOfSell, divider);
		BigDecimal deviation = standardDeviation(mean2, listOfSell, divider);
		System.out.println(deviation);
	}
	/**
	 * Count mean of input
	 * @param averge
	 * @param divider
	 * @return averge
	 */
	BigDecimal mean(BigDecimal averge, BigDecimal divider) {
		try{
		averge = averge.divide(divider, 4);

		return averge;
		}catch(java.lang.ArithmeticException e){
			BigDecimal zero = new BigDecimal(0);
			return zero;
		}
	}

	/**
	 * Count standard deviation
	 * @param averge
	 * @param list
	 * @param divider
	 * @return
	 */
	private BigDecimal standardDeviation(BigDecimal averge, ArrayList<BigDecimal> list, BigDecimal divider) {

		BigDecimal temp = new BigDecimal(0);
		BigDecimal sumOfDeviation = new BigDecimal(0);
		for (int i = 0; i < divider.intValue(); i++) {
			temp = list.get(i).subtract(averge);
			temp = temp.pow(2);
			sumOfDeviation = sumOfDeviation.add(temp);
		}
		try{
		sumOfDeviation = sumOfDeviation.divide(divider, 8, RoundingMode.HALF_UP);
		sumOfDeviation = squareRoot(sumOfDeviation);
		sumOfDeviation = sumOfDeviation.setScale(4, RoundingMode.HALF_UP);
		}catch( java.lang.ArithmeticException e){
			sumOfDeviation = new BigDecimal(0);
		}
		return sumOfDeviation;

	}

	/**
	 *  implementation of square root of BigDecimal
	 * @param div
	 * @return div
	 */
	private BigDecimal squareRoot(BigDecimal div) {
		div = new BigDecimal(Math.sqrt(div.doubleValue()));
		return div;
	}

	/**
	 *  change day for next day
	 */
	void dateProgress() {
		d2++;
		if (d2 >= 10) {
			d1++;
			d2 = 0;
			}
		if (d1>=3 && d2>2){
				
				m2++;
				d1 = 0;
				d2 =1;
				if (m1 == 1 && m2 > 2) {
					y2++;
					m1 = 0;
					i = 0;
					j = 0;
					k = 1;
					
					m2 = 1;
					if (y2 >= 10) {
						y1++;
						y2 = 0;
					}
				}
				if (m2 >= 10) {
					m1++;
					m2 = 0;
				}
			
		}
		date = "" + y1 + "" + y2 + "" + m1 + "" + m2 + "" + d1 + "" + d2;
	}
}