package github.cluder.ean.checker;

import java.util.HashMap;

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
		return "Result [ean=" + ean + ", providerResults=" + providerResults + "]";
	}

	public void copyFrom(Result data) {
		this.providerResults = data.providerResults;
	}
}
