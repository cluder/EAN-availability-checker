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
//			Files.write(Paths.get(getName() + "_" + ean + "_" + ".html"), dom.toString().getBytes());
//		} catch (IOException e) {
//
//		}
		ProviderResult pr = new ProviderResult(getName());

		Elements notFoundElement = dom.getElementsByClass("no-results-text");
		if (notFoundElement != null && notFoundElement.size() > 0) {
			pr.outOfStock = notFoundElement.text();
			pr.available = false;
		}

		Elements notAvailable = dom.getElementsByClass("stock-count unavailable");
		if (notAvailable != null && notAvailable.size() > 0) {
			pr.outOfStock = notAvailable.text();
			pr.available = false;
		}

		Elements title = dom.getElementsByClass("title my-3");
		if (title != null && title.size() > 0) {
			pr.productName = title.text();
		}

		// Preis
		Elements price = dom.getElementsByClass("font-weight-bold mr-3 price-font-size");
		if (price != null && price.size() > 0) {
			pr.price = price.text();
		}

		return pr;

	}

}
