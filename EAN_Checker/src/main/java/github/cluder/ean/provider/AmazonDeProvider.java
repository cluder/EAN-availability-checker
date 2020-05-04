package github.cluder.ean.provider;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import github.cluder.ean.checker.ProviderResult;

public class AmazonDeProvider extends AbstractProvider {
	@Override
	public String getName() {
		return "Amazon.de";
	}

	@Override
	public String getSearchUrl(String value) {
		return "https://www.amazon.de/s?k=" + value;
	}

	@Override
	public ProviderResult checkProduct(String ean, Document dom) {
		ProviderResult pr = new ProviderResult();
		pr.providerName = getName();

		Element ele = dom.selectFirst(
				"#search > div.s-desktop-width-max.s-desktop-content.sg-row > div.sg-col-20-of-24.sg-col-28-of-32.sg-col-16-of-20.sg-col.sg-col-32-of-36.sg-col-8-of-12.sg-col-12-of-16.sg-col-24-of-28 > div > span:nth-child(5) > div:nth-child(1) > div.sg-col-20-of-24.s-result-item.s-asin.sg-col-0-of-12.sg-col-28-of-32.sg-col-16-of-20.sg-col.sg-col-32-of-36.sg-col-12-of-16.sg-col-24-of-28 > div > span > div > div > div:nth-child(2) > div.sg-col-4-of-12.sg-col-8-of-16.sg-col-16-of-24.sg-col-12-of-20.sg-col-24-of-32.sg-col.sg-col-28-of-36.sg-col-20-of-28 > div > div:nth-child(1) > div > div > div:nth-child(1) > h2 > a > span");
		if (ele != null) {
			pr.productName = ele.text();
		}
		ele = dom.selectFirst(
				"#search > div.s-desktop-width-max.s-desktop-content.sg-row > div.sg-col-20-of-24.sg-col-28-of-32.sg-col-16-of-20.sg-col.sg-col-32-of-36.sg-col-8-of-12.sg-col-12-of-16.sg-col-24-of-28 > div > span:nth-child(5) > div:nth-child(1) > div.sg-col-20-of-24.s-result-item.s-asin.sg-col-0-of-12.sg-col-28-of-32.sg-col-16-of-20.sg-col.sg-col-32-of-36.sg-col-12-of-16.sg-col-24-of-28 > div > span > div > div > div:nth-child(2) > div.sg-col-4-of-12.sg-col-8-of-16.sg-col-16-of-24.sg-col-12-of-20.sg-col-24-of-32.sg-col.sg-col-28-of-36.sg-col-20-of-28 > div > div:nth-child(2) > div.sg-col-4-of-24.sg-col-4-of-12.sg-col-4-of-36.sg-col-4-of-28.sg-col-4-of-16.sg-col.sg-col-4-of-20.sg-col-4-of-32 > div > div.a-section.a-spacing-none.a-spacing-top-small > div:nth-child(2) > div > a > span > span:nth-child(2) > span.a-price-whole");
		if (ele != null) {
			pr.price = ele.text();
		}

		ele = dom.selectFirst(
				"#search > div.s-desktop-width-max.s-desktop-content.sg-row > div.sg-col-20-of-24.sg-col-28-of-32.sg-col-16-of-20.sg-col.sg-col-32-of-36.sg-col-8-of-12.sg-col-12-of-16.sg-col-24-of-28 > div > span:nth-child(4) > div > span > div > div > div:nth-child(1)");
		if (ele != null) {
			pr.outOfStock = ele.text();
		}

		return pr;
	}
}
