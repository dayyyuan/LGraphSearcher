package edu.psu.chemxseer.structure.preprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Singlton AtomIntMap
 * 
 * @author dayuyuan
 * 
 */
public class AtomIntMap {
	private static AtomIntMap theMap;

	private Map<String, Integer> dic;

	public static AtomIntMap newInstance() {
		if (theMap == null)
			theMap = new AtomIntMap();
		return theMap;
	}

	public int getLabel(String itom) {
		if (dic.containsKey(itom))
			return dic.get(itom);
		else
			throw new NoSuchElementException();
	}

	private AtomIntMap() {
		this.dic = new HashMap<String, Integer>();
		dic.put("Al", 13);
		dic.put("Ar", 18);
		dic.put("As", 33);
		dic.put("Ag", 47);
		dic.put("Au", 79);
		dic.put("At", 85);
		dic.put("Ac", 89);
		dic.put("Am", 95);
		dic.put("Be", 4);
		dic.put("Br", 35);
		dic.put("Ba", 56);
		dic.put("Bi", 83);
		dic.put("Bh", 107);
		dic.put("Bk", 97);
		dic.put("B", 5);
		dic.put("Cl", 17);
		dic.put("Ca", 20);
		dic.put("Cr", 24);
		dic.put("Co", 27);
		dic.put("Cu", 29);
		dic.put("Cd", 48);
		dic.put("Cs", 55);
		dic.put("Ce", 58);
		dic.put("Cf", 98);
		dic.put("C", 6);
		dic.put("Db", 105);
		dic.put("Dy", 66);
		dic.put("Ds", 110);
		dic.put("Eu", 63);
		dic.put("Er", 68);
		dic.put("Es", 99);
		dic.put("Fe", 26);
		dic.put("Fr", 87);
		dic.put("F", 9);
		dic.put("Ga", 31);
		dic.put("Ge", 32);
		dic.put("Gd", 64);
		dic.put("He", 2);
		dic.put("Hf", 72);
		dic.put("Hg", 80);
		dic.put("Hs", 108);
		dic.put("Ho", 67);
		dic.put("H", 1);
		dic.put("In", 49);
		dic.put("Ir", 77);
		dic.put("I", 53);
		dic.put("Kr", 35);
		dic.put("K", 19);
		dic.put("Li", 3);
		dic.put("Lu", 71);
		dic.put("Lr", 103);
		dic.put("La", 57);
		dic.put("Mg", 12);
		dic.put("Mn", 25);
		dic.put("Mo", 42);
		dic.put("Mt", 109);
		dic.put("Md", 101);
		dic.put("Ne", 10);
		dic.put("Na", 11);
		dic.put("Ni", 28);
		dic.put("Nb", 41);
		dic.put("Nd", 60);
		dic.put("Np", 93);
		dic.put("No", 102);
		dic.put("N", 7);
		dic.put("Os", 76);
		dic.put("O", 8);
		dic.put("Pd", 46);
		dic.put("Pt", 78);
		dic.put("Pb", 82);
		dic.put("Po", 84);
		dic.put("Pr", 59);
		dic.put("Pm", 61);
		dic.put("Pa", 91);
		dic.put("Pu", 94);
		dic.put("P", 15);
		dic.put("Rb", 37);
		dic.put("Ru", 44);
		dic.put("Rh", 45);
		dic.put("Re", 75);
		dic.put("Rn", 86);
		dic.put("Ra", 88);
		dic.put("Si", 14);
		dic.put("Sc", 21);
		dic.put("Se", 34);
		dic.put("Sr", 38);
		dic.put("Sn", 50);
		dic.put("Sb", 51);
		dic.put("Sg", 106);
		dic.put("Sm", 62);
		dic.put("S", 16);
		dic.put("Ti", 22);
		dic.put("Tc", 43);
		dic.put("Te", 52);
		dic.put("Ta", 73);
		dic.put("Tl", 81);
		dic.put("Tb", 65);
		dic.put("Tm", 69);
		dic.put("Th", 90);
		dic.put("Uuc", 111);
		dic.put("Uub", 112);
		dic.put("Uuq", 114);
		dic.put("U", 92);
		dic.put("V", 23);
		dic.put("W", 74);
		dic.put("Xe", 54);
		dic.put("Yb", 70);
		dic.put("Y", 39);
		dic.put("Zn", 30);
		dic.put("Zr", 40);
	}
}