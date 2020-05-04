package github.cluder.ean.checker;

import java.util.HashMap;
import java.util.Map.Entry;

public class Result {
	public String ean;
	/**
	 * Map with result for each provider
	 */
	public HashMap<String, ProviderResult> providerResults = new HashMap<>();

	public Result(String ean) {
		this.ean = ean;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("ean:" + ean);
		for (Entry<String, ProviderResult> x : providerResults.entrySet()) {
			sb.append("    " + x.getKey()); // provider name
			sb.append("    " + x.getValue().productName);
			sb.append("    " + x.getValue().price);
			sb.append("    " + x.getValue().outOfStock);
		}

		return sb.toString();
	}
}
