package github.cluder.ean.checker;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import github.cluder.ean.EANCheckerMain;
import github.cluder.ean.provider.AbstractProvider;
import github.cluder.ean.provider.ProductProviders;

public class EANChecker {
	private final static Logger log = LoggerFactory.getLogger(EANChecker.class);
	final int sleepTime = 400;
	List<String> eans = new ArrayList<>();
	ExecutorService executor = Executors.newFixedThreadPool(ProductProviders.getProviders().size());

	public EANChecker() {
	}

	public List<String> readEans() {
		try {
			eans = Files.lines(Paths.get(EANCheckerMain.EAN_FILE)).filter(e -> {
				return !e.isEmpty() && !e.startsWith("#");
			}).collect(Collectors.toList());
			return eans;
		} catch (Exception e) {
			log.error("Error reading eans: " + e.getMessage(), e);
		}

		return Collections.emptyList();
	}

	public Result checkEAN(String ean) {
		Result result = new Result(ean);
		log.debug("checking {} ... ", ean);
		sleep(sleepTime);

		List<Callable<ProviderResult>> tasks = new ArrayList<>();
		for (AbstractProvider provider : ProductProviders.getProviders()) {
			Callable<ProviderResult> task = new Callable<ProviderResult>() {
				@Override
				public ProviderResult call() throws Exception {
					return checkEanForProvider(ean, provider);
				}
			};
			tasks.add(task);
		}

		try {
			final List<Future<ProviderResult>> futures = executor.invokeAll(tasks);
			for (Future<ProviderResult> future : futures) {
				final ProviderResult providerResult = future.get();
				if (providerResult != null) {
					result.providerResults.put(providerResult.providerName, providerResult);
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			log.info("exception during call: " + e.getMessage(), e);
		}

		log.debug(result.toString());
		return result;
	}

	static private void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			log.warn("sleep interrupted", e);
		}
	}

	private ProviderResult checkEanForProvider(String ean, AbstractProvider provider) {
		if (EANCheckerMain.TEST_MODE) {
			return createTestResult(ean, provider);
		}

		ProviderResult pr = null;
		try {
			final String searchUrl = provider.getSearchUrl(ean);
			final URL url = new URL(searchUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestProperty("user-agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.129 Safari/537.36");
			conn.setRequestProperty("authority", url.getHost());
			conn.setRequestProperty("Accept-Encoding", "identity"); // no gzip

			final int responseCode = conn.getResponseCode();
			final String contentType = conn.getContentType();

			log.debug("{} {} {}", responseCode, contentType);
			switch (responseCode) {
			case 200:
				final Document parsed = Jsoup.parse(conn.getInputStream(), "UTF-8", url.getHost());
				pr = provider.checkProduct(ean, parsed);
				break;
			case 404:
				pr = new ProviderResult(provider.getName());
				pr.outOfStock = "Artikel nicht gefunden (404)";
				pr.available = false;
				break;
			default:
				log.info("got response code {} for {}", responseCode, ean);
				pr = new ProviderResult(provider.getName());
				pr.outOfStock = "HTTP " + responseCode;
				pr.available = false;
				break;
			}

		} catch (Exception e) {
			log.error("error checking {} {}", ean, e.getMessage(), e);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
		}
		return pr;
	}

	private ProviderResult createTestResult(String ean, AbstractProvider provider) {
		ProviderResult result = new ProviderResult(provider.getName());
		result.available = Math.random() > 0.5 ? true : false;
		if (!result.available)
			result.outOfStock = "nicht Verfügbar";

		result.price = "9.99";
		result.productName = "Test Product";
		return result;
	}

	public List<String> getEans() {
		if (eans.isEmpty()) {
			eans = readEans();
		}
		return eans;
	}

}
