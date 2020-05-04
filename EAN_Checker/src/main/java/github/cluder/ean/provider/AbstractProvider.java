package github.cluder.ean.provider;

import org.jsoup.nodes.Document;

import github.cluder.ean.checker.ProviderResult;

public abstract class AbstractProvider {

	public abstract String getSearchUrl(String searchTxt);

	public abstract String getName();

	public abstract ProviderResult checkProduct(String ean, Document dom);

	@Override
	public String toString() {
		return getName();
	}
}
