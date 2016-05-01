package pl.parser.nbp;

/**
 * Author: Paweł Ścibiorski
 * This class parse through XML document using DOM style of parsing.
 * 
 * 
 */

import org.w3c.dom.*;

import java.math.BigDecimal;


public class Parser {
private BigDecimal bigOneBuy;
private BigDecimal bigOneSell;


	public void toParse(Document doc, String code) {
		this.bigOneBuy = new BigDecimal(0);
		this.bigOneSell = new BigDecimal(0);
		NodeList pozycjaList = doc.getElementsByTagName("pozycja");
		for (int temp = 0; temp < pozycjaList.getLength(); temp++) {
			Node node1 = pozycjaList.item(temp);

			if (node1.getNodeType() == Node.ELEMENT_NODE) {
				Element theElement = (Element) node1;

				
				String codeNow =(String)theElement.getElementsByTagName("kod_waluty").item(0).getTextContent();
			if(codeNow.equals(code) ==true){
			
			String valueNowStrBuy = (String)theElement.getElementsByTagName("kurs_kupna").item(0).getTextContent();
			valueNowStrBuy=valueNowStrBuy.replace(',', '.');
			this.bigOneBuy = this.bigOneBuy.add(new BigDecimal(valueNowStrBuy));
			
			String valueNowStrSell = (String)theElement.getElementsByTagName("kurs_sprzedazy").item(0).getTextContent();
			valueNowStrSell = valueNowStrSell.replace(',', '.');
			this.bigOneSell = this.bigOneSell.add(new BigDecimal(valueNowStrSell));
			}
			}

		}
		

	}

	public BigDecimal getBigOneBuy() {
		return bigOneBuy;
	}

	public BigDecimal getBigOneSell(){
		return bigOneSell;
		
	}
}