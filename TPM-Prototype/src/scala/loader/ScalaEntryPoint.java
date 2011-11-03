package scala.loader;

import java.util.ArrayList;
import java.util.List;

public class ScalaEntryPoint {

	public static void main(String[] args) {
		List<String> argList = new ArrayList<String>();
		argList.add("net.ra23.batman.Dispatchertest");
		for (String s : args) {
			argList.add(s);
		}
		scala.tools.nsc.MainGenericRunner.main(argList.toArray(new String[0]));
	}
}
