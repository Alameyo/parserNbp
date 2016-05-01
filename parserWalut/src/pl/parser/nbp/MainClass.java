package pl.parser.nbp;

/**
 * Author: Paweł Ścibiorski
 * This is main class of application which parse through nbp's XML document
 * and search for informations about currencies
 *  
 */

import java.io.IOException;

import javax.xml.parsers.*;

import org.xml.sax.SAXException;

public class MainClass {
	//c073z070413
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		//declaration and use of master translator, it's a object which will 
		//  transform your code and date input into query for XML
		MasterDecider master = new MasterDecider(); 
		master.masterTranslator();					
		Downloader downloadXML = new Downloader();
		downloadXML.download(master.getKodWaluty(), master.getDataPoczatku(), master.getDataKonca());
		


}}