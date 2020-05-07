package github.cluder.ean;

import github.cluder.ean.ui.EanCheckerUI;

public class EANCheckerMain {
	public static String EAN_FILE = "eans.txt";
	public static boolean TEST_MODE = false;

	public static void main(String[] args) {
		new EANCheckerMain().start();
	}

	private void start() {
		EanCheckerUI eanCheckerUI = new EanCheckerUI();
		eanCheckerUI.setVisible(true);
	}

}
