package edu.psu.chemxseer.structure.setcover.experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.preprocess.MyFactory;
import edu.psu.chemxseer.structure.preprocess.PreProcessTools2;
import edu.psu.chemxseer.structure.setcover.featureGenerator.FeatureWrapperSimple;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureWrapper;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.OneFeatureMultiClass;

public class ExpClassification {
	static String[] classificationFolderNames = new String[] { "MCF-7",
			"NCI-H23", "P388", "SF-295", "SW-620", "Yeast", "MOLT-4",
			"OVCAR-8", "PC-3", "SN12C", "UACC257" };
	static int[][] classificationCount = { { 2294, 25476 }, { 2057, 38296 },
			{ 2298, 39174 }, { 2025, 38246 }, { 2410, 38122 }, { 9568, 70033 },
			{ 3140, 36625 }, { 2079, 38437 }, { 1568, 25941 }, { 1955, 38049 },
			{ 1643, 38345 } };

	public static float[] runExpClassification(IMaxCoverSolver solver,
			String fileToStore, int K) throws IOException {
		long[] time = new long[1];
		double[] mem = new double[2];
		IFeatureWrapper[] result = null;
		float[] stat = new float[10];
		try {
			result = solver.runGreedy(K, stat);
		} catch (Exception e) {
			System.out.println("Skiped");
		}
		System.out.println("Time: " + time[0] + " Space: " + mem[0]
				+ " Ave_Space: " + mem[1] + " Coverage: "
				+ solver.coveredItemsCount());

		// save the experiment results
		IFeatureWrapper[] alReadySelected = solver.getFixedSelected();
		if (alReadySelected != null && alReadySelected.length > 0) {
			IFeatureWrapper[] temp = Arrays.copyOf(result, result.length
					+ alReadySelected.length);
			for (int i = 0; i < alReadySelected.length; i++)
				temp[result.length + i] = alReadySelected[i];
			result = temp;
		}
		// get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			// get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// create an instance of DOM
			dom = db.newDocument();

		} catch (ParserConfigurationException pce) {
			// dump it
			System.out
					.println("Error while trying to instantiate DocumentBuilder "
							+ pce);
			System.exit(1);
		}

		// create the root element
		Element rootEle = dom.createElement("Classification");
		dom.appendChild(rootEle);
		// No enhanced for
		for (IFeatureWrapper OneFeature : result) {
			// For each object create element and attach it to root
			Element featureElement = createFeatureElement(dom, OneFeature);
			rootEle.appendChild(featureElement);
		}
		return stat;
		// Serialize the dom
		// try
		// {
		// //print
		// OutputFormat format = new OutputFormat(dom);
		// format.setIndenting(true);
		// //to generate a file output use fileoutputstream instead of
		// system.out
		// XMLSerializer serializer = new XMLSerializer(
		// new FileOutputStream(new File(fileToStore)), format);
		// serializer.serialize(dom);
		//
		// } catch(IOException ie) {
		// ie.printStackTrace();
		// }

	}

	private static Element createFeatureElement(Document dom,
			IFeatureWrapper oneFeature) {
		Element featureElement = dom.createElement("pattern");

		// create id field
		Element idEle = dom.createElement("id");
		Text idText = dom.createTextNode(String.valueOf(oneFeature
				.getFetureID()));
		idEle.appendChild(idText);
		featureElement.appendChild(idEle);

		// create support field
		Element spEle = dom.createElement("support");
		Text spTxt = dom.createTextNode(String.valueOf(oneFeature
				.containedDatabaseGraphs().length
				+ oneFeature.containedQueryGraphs().length));
		spEle.appendChild(spTxt);
		featureElement.appendChild(spEle);

		// create what field
		Element whatEle = dom.createElement("what");
		Text whatTxt = dom.createTextNode(oneFeature.getFeature().getDFSCode());
		whatEle.appendChild(whatTxt);
		featureElement.appendChild(whatEle);

		// create where element
		Element whereEle = dom.createElement("where");
		int[] dbSupport = oneFeature.containedDatabaseGraphs();
		int[] querySupport = oneFeature.containedQueryGraphs();
		StringBuffer sbuf = new StringBuffer();
		for (int support : dbSupport) {
			sbuf.append(support);
			sbuf.append(" ");
		}
		for (int support : querySupport) {
			sbuf.append(support);
			sbuf.append(" ");
		}
		sbuf.delete(sbuf.length() - 1, sbuf.length());
		Text whereTxt = dom.createTextNode(sbuf.toString());
		whereEle.appendChild(whereTxt);
		featureElement.appendChild(whereEle);

		return featureElement;
	}

	private static String toXML(IFeatureWrapper oneFeature) {
		StringBuffer buf = new StringBuffer();
		int[] dbSupport = oneFeature.containedDatabaseGraphs();
		int[] querySupport = oneFeature.containedQueryGraphs();
		buf.append("<pattern>\n");
		buf.append("<id>");
		buf.append(oneFeature.getFetureID());
		buf.append("</id>\n");
		buf.append("<support>");
		buf.append(dbSupport.length + querySupport.length);
		buf.append("</support>\n");
		buf.append("<what>\n");
		buf.append(oneFeature.getFeature().getDFSCode());
		buf.append("\n</what>\n");
		buf.append("<where>");
		for (int support : dbSupport) {
			buf.append(support);
			buf.append(" ");
		}
		for (int support : querySupport) {
			buf.append(support);
			buf.append(" ");
		}
		buf.delete(buf.length() - 1, buf.length());
		buf.append("/<where\n>");
		buf.append("</pattern>");
		return buf.toString();
	}

	public static List<IFeatureWrapper> loadFeaturesClassification(
			String featureFile, boolean CORK) {
		List<IFeatureWrapper> features = new ArrayList<IFeatureWrapper>();
		// get the factory
		Document dom = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// parse using builder to get DOM representation of the XML file
			dom = db.parse("featureFile");
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		if (dom == null)
			return features;
		// get the root element
		Element docEle = dom.getDocumentElement();
		// get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("pattern");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				// get the employee element
				Element el = (Element) nl.item(i);
				// get the Employee object
				IFeatureWrapper e = getFeature(el, CORK);
				// add it to list
				features.add(e);
			}
		}
		return features;
	}

	/**
	 * Assumption, The returned IFeatureWrapper, put all supporting to the db
	 * support
	 * 
	 * @param el
	 * @return
	 */
	private static IFeatureWrapper getFeature(Element el, boolean CORK) {
		// 1. First Looking for ID
		int fID = Integer.parseInt(getTextValue(el, "<id>"));
		// 2. Then the Feature
		String gString = getTextValue(el, "<what>");
		if (CORK) {
			String[] graphString = gString.split("\n");
			List<String> temp = new ArrayList<String>();
			for (int i = 0; i < graphString.length; i++)
				temp.add(graphString[i]);
			Graph g = PreProcessTools2.parseGraph(temp);
			gString = MyFactory.getUnCanDFS().serialize(g);
		}
		// 3. Then the support
		String supports = getTextValue(el, "<where>");
		String[] supportTokens = supports.split(" ");
		int[] supportIDs = new int[supportTokens.length];
		for (int i = 0; i < supportTokens.length; i++)
			supportIDs[i] = Integer.parseInt(supportTokens[i]);

		// 4. construct a IFeatureWrapper and return
		int[][] finalSupport = new int[4][];
		finalSupport[0] = supportIDs;
		finalSupport[1] = finalSupport[2] = finalSupport[3] = new int[0];
		IFeatureWrapper result = new FeatureWrapperSimple(
				new OneFeatureMultiClass(fID, gString), finalSupport);
		return result;
	}

	/**
	 * I take a xml element and the tag name, look for the tag and get the text
	 * content
	 * 
	 * @param ele
	 * @param tagName
	 * @return
	 */
	private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}
}
