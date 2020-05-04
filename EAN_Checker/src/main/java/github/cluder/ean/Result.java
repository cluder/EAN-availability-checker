package github.cluder.ean;

public class Result {
	public String ean;
	public String productName = "";
	public String outOfStock = "";
	public String price = "";
	public String url = "";

	public Result(String ean) {
		this.ean = ean;
	}

	@Override
	public String toString() {
		return String.format("%s, %s, %s, %s", ean, productName, outOfStock, price);
	}
}
