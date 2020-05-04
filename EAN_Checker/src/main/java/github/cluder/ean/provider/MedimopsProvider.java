package github.cluder.ean.provider;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import github.cluder.ean.checker.ProviderResult;

public class MedimopsProvider extends AbstractProvider {
	public static String SEARCH_URL = "https://www.medimops.de/produkte-C0/?fcIsSearch=1&searchparam=";

	@Override
	public String getName() {
		return "medimops.de";
	}

	@Override
	public String getSearchUrl(String value) {
		return SEARCH_URL + value;
	}

	@Override
	public ProviderResult checkProduct(String ean, Document dom) {
		ProviderResult pr = new ProviderResult();
		pr.providerName = getName();

		// not found medimops
		Element ele = dom.selectFirst("#body > div.grid-12.alpha.omega.mx-search-no-result > p");
		if (ele != null) {
			pr.outOfStock = ele.text();
		}

		final Elements outOfStockEle = dom.getElementsByClass("mx-details-basket-out-of-stock");
		if (outOfStockEle != null && outOfStockEle.size() > 0) {
			pr.outOfStock = outOfStockEle.text();
		}

		// available
		ele = dom.selectFirst("#mx-details-price");
		if (ele != null) {
			pr.price = ele.text();
		}

		ele = dom.selectFirst("#test_product_name");
		if (ele != null) {
			pr.productName = ele.text();
		}
		return pr;
	}
}
