/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package urdf.tools.gazetteer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author vsetty
 */
public class GeoFIPSCodes {
	private String strFile;
	HashMap<String, String> FIPSCodeMap = null;

	public GeoFIPSCodes(String fileName) {
		this.strFile = fileName;
	}

	public void init() {
		try {
			FIPSCodeMap = new HashMap<String, String>();
			RandomAccessFile rand = new RandomAccessFile(strFile, "r");
			String line = null;
			while ((line = rand.readLine()) != null) {
				String tokens[] = line.trim().split("\t");
				if (tokens.length != 2)
					continue;
				FIPSCodeMap.put(tokens[0], normalize(tokens[1]));
			}
		} catch (IOException ex) {
			Logger.getLogger(GeoFIPSCodes.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static String normalize(String s) {
		StringBuffer token = new StringBuffer();
		boolean first = true;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				token.append('_');
				first = true;
			} else {
				if (!first && Character.isLetter(c))
					token.append(Character.toLowerCase(c));
				else if (Character.isLetter(c))
					token.append(Character.toUpperCase(c));
				else
					token.append(c);
				first = false;
			}
		}
		return token.toString();
	}

	public String getPlaceName(String strcode) {
		return FIPSCodeMap.get(strcode);
	}

}
