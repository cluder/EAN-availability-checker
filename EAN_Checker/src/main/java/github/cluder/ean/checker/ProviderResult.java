package github.cluder.ean.checker;

public class ProviderResult {
	final public String providerName;
	public String outOfStock = "";
	public String price = "";
	public String productName = "";
	public boolean available = true;

	public ProviderResult(String name) {
		providerName = name;
	}

	@Override
	public String toString() {
		return "ProviderResult [providerName=" + providerName + ", outOfStock=" + outOfStock + ", price=" + price
				+ ", productName=" + productName + "]";
	}

	public Object getDisplayString() {
		StringBuilder sb = new StringBuilder();
		if (outOfStock.isEmpty()) {
			final int maxProdNameLen = 30;
			String prodNameFixed = productName;
			if (productName.length() > maxProdNameLen) {
				prodNameFixed = productName.substring(0, maxProdNameLen - 2) + "..";
			}
			sb.append(String.format("%-30s", prodNameFixed));

			sb.append(" | ").append(String.format("%s", price));
		} else {
			sb.append(outOfStock);
		}

		return sb.toString();
	}

}
