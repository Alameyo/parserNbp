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
		this.sumOfbuy = new BigDecimal(0);
		this.sumOfSell = new BigDecimal(0);
		this.divider = new BigDecimal(0);
		this.listOfSell = new ArrayList<BigDecimal>();
		this.question = null;
		this.i = 0; // number of directory in year
		this.j = 0;
		this.k = 1;
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
		this.y1 = Character.getNumericValue(date.charAt(0)); // getting value of
																// date from
																// String
		this.y2 = Character.getNumericValue(date.charAt(1));
		this.m1 = Character.getNumericValue(date.charAt(2));
		this.m2 = Character.getNumericValue(date.charAt(3));
		this.d1 = Character.getNumericValue(date.charAt(4));
		this.d2 = Character.getNumericValue(date.charAt(5));
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int ad2 = year%10;
		year = year/10;
		int ad1 = year%10;
		this.firstCheck = true; // for first query the number of directory in
								// year is unknown,so there is need to find
								// directory with valid date
		do {
			this.found = false;

			while (this.found == false && i < 3) {

				this.question = "http://www.nbp.pl/kursy/xml/c" + this.i + "" + this.j + "" + this.k + "z" + date
						+ ".xml";
				System.out.println(this.question +ad1 + ad2 );
				try {

					this.doc = builder.parse(this.question);
					this.parser1.toParse(doc, kodWaluty);
					this.found = true;
					this.divider = this.divider.add(new BigDecimal(1));
					this.sumOfbuy = this.sumOfbuy.add(parser1.getBigOneBuy());
					this.sumOfSell = this.sumOfSell.add(parser1.getBigOneSell());
					this.listOfSell.add(parser1.getBigOneSell());
					this.k++; // change number of document in year
					if (this.k >= 10) {
						this.k = 0;
						this.j++;
						if (this.j >= 10) {
							this.j = 0;
							this.i++;
						}
					}

					this.firstCheck = false; // stop searching for number of
												// document with provided
												// beginning date
					break;

				} catch (IOException e) {
					// in case that document wasn't found at that address and
					// algorithm is searching for first document since given
					// date
					if (this.firstCheck == true) {
						this.k++;
						if (this.k >= 10) {
							this.k = 0;
							this.j++;
							if (this.j >= 10) {
								this.j = 0;
								this.i++;
							}
						}
					} else {
						break;
					}
				}
			}
			if (this.firstCheck == true) {
				this.i = 0;
				this.j = 0;
				this.k = 1;
			}
			dateProgress();
		} while (date.compareTo(dataKonca) < 0);
		// last query which is out of loop
		if (dataPoczatku.equals(dataKonca) == false) {
			this.question = "http://www.nbp.pl/kursy/xml/c" + this.i + "" + this.j + "" + this.k + "z" + dataKonca
					+ ".xml";

			try {
				this.doc = builder.parse(this.question);
				this.parser1.toParse(doc, kodWaluty);
				this.found = true;
				this.sumOfbuy = this.sumOfbuy.add(parser1.getBigOneBuy());
				this.sumOfSell = this.sumOfSell.add(parser1.getBigOneSell());
				this.listOfSell.add(parser1.getBigOneSell());
				this.divider = this.divider.add(new BigDecimal(1));
			} catch (IOException e) {
			}
		}
		BigDecimal mean1 = mean(this.sumOfbuy, this.divider);
		System.out.println(mean1);
		BigDecimal mean2 = mean(this.sumOfSell, this.divider);
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
		this.d2++;
		if (this.d2 >= 10) {
			this.d1++;
			this.d2 = 0;
			}
		if (this.d1>=3 && this.d2>2){
				
				this.m2++;
				this.d1 = 0;
				this.d2 =1;
				if (this.m1 == 1 && this.m2 > 2) {
					this.y2++;
					this.m1 = 0;
					this.i = 0;
					this.j = 0;
					this.k = 1;
					
					this.m2 = 1;
					if (this.y2 >= 10) {
						this.y1++;
						this.y2 = 0;
					}
				}
				if (this.m2 >= 10) {
					this.m1++;
					this.m2 = 0;
				}
			
		}
		this.date = "" + this.y1 + "" + this.y2 + "" + this.m1 + "" + this.m2 + "" + this.d1 + "" + this.d2;
	}
}