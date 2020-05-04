package github.cluder.ean.provider;

import java.util.ArrayList;
import java.util.List;

public class ProductProviders {
	static List<AbstractProvider> providers = new ArrayList<>();

	static {
		providers.add(new MedimopsProvider());
		providers.add(new ReBuyProvider());
		providers.add(new AmazonDeProvider());
	}

	static public List<AbstractProvider> getProviders() {
		return providers;
	}

}
