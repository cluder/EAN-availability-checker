package github.cluder.ean.provider;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import github.cluder.ean.checker.ProviderResult;

public class ReBuyProvider extends AbstractProvider {

	@Override
	public String getName() {
		return "ReBuy.de";
	}

	@Override
	public String getSearchUrl(String value) {
		return "https://www.rebuy.de/kaufen/suchen?q=" + value;
	}

	@Override
	public ProviderResult checkProduct(String ean, Document dom) {
//		try {
//			Files.write(dom.toString().getBytes(), new File(getName() + "_" + ean + "_" + ".html"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		ProviderResult pr = new ProviderResult(getName());

		Elements notFoundElement = dom.getElementsByClass("no-results-text");
		if (notFoundElement != null && notFoundElement.size() > 0) {
			pr.outOfStock = notFoundElement.text();
			pr.available = false;
		}

		Elements title = dom.getElementsByClass("title my-3");
		if (title != null && title.size() > 0) {
			pr.productName = title.text();
		}

		// Preis
		Elements price = dom.getElementsByClass("font-weight-bold mr-3 price-font-size ng-star-inserted");
		if (price != null && price.size() > 0) {
			pr.price = price.text();
		}

		return pr;

	}

}
