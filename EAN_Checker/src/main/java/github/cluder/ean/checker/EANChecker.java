package github.cluder.ean.checker;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	final int sleepTime = 100;
	List<String> eans = new ArrayList<>();

	public EANChecker() {
	}

	public List<String> readEans() {
		try {
			eans = Files.lines(Paths.get(EANCheckerMain.EAN_FILE)).collect(Collectors.toList());
			return eans;
		} catch (Exception e) {
			log.error("Error reading eans: " + e.getMessage(), e);
		}

		return Collections.emptyList();
	}

	public List<Result> checkAllEans() {

		List<Result> results = new ArrayList<>();
		for (String ean : eans) {
			results.add(checkEAN(ean));
		}
		return results;
	}

	private Result checkEAN(String ean) {
		Result result = new Result(ean);
		log.info("checking {} ", ean);

		for (AbstractProvider provider : ProductProviders.getProviders()) {
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
				final String contentEncoding = conn.getContentEncoding();

				log.debug("{} {} {}", responseCode, contentType);
				switch (responseCode) {
				case 200:
					final Document parsed = Jsoup.parse(conn.getInputStream(), "UTF-8", url.getHost());
					ProviderResult pr = provider.checkProduct(ean, parsed);
					result.providerResults.put(provider.getName(), pr);
					break;
				case 404:
					pr = new ProviderResult();
					pr.providerName = provider.getName();
					pr.outOfStock = "Artikel nicht gefunden (404)";
					result.providerResults.put(provider.getName(), pr);
					break;
				default:
					log.info("got response code {} for {}", responseCode, ean);
					break;
				}

				Thread.sleep(sleepTime);
			} catch (Exception e) {
				log.error("error checking {} {}", ean, e.getMessage(), e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		}
		log.info(result.toString());
		return result;
	}

}
