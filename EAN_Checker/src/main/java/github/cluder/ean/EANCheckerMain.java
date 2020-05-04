package github.cluder.ean;

public class EANCheckerMain {
	// https://www.medimops.de/produkte-C0/?fcIsSearch=1&searchparam=803341463127
	public static String SEARCH_URL = "https://www.medimops.de/produkte-C0/?fcIsSearch=1&searchparam=";
	public static String PLACEHOLDER = "$PARAM$";
	public static String EAN_FILE = "eans.txt";

	public static void main(String[] args) {
		new EANCheckerMain().start();
	}

	private void start() {
		EanCheckerUI eanCheckerUI = new EanCheckerUI();
		eanCheckerUI.setVisible(true);
	}

}
