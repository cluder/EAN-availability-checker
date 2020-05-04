package github.cluder.ean;

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
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EANChecker {
	private final static Logger log = LoggerFactory.getLogger(EANChecker.class);

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
		final int sleepTime = 200;
		List<Result> results = new ArrayList<>();
		for (String ean : eans) {
			try {
				log.info("checking {} ", ean);
				final String searchUrl = EANCheckerMain.SEARCH_URL + ean;
				final URL url = new URL(searchUrl);
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestProperty("user-agent",
						"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.129 Safari/537.36");
				conn.setRequestProperty("authority", "www.medimops.de");

				final int responseCode = conn.getResponseCode();
				final String contentType = conn.getContentType();

				log.debug("{} {} {}", responseCode, contentType);
				switch (responseCode) {
				case 200:
					final Document parsed = Jsoup.parse(conn.getInputStream(), "UTF-8", "https://www.medimops.de");
					Result result = extractResult(parsed, ean);
					result.url = searchUrl;
					results.add(result);
					break;
				case 404:
					final Result res = new Result(ean);
					res.outOfStock = "Artikel nicht gefunden (404)";
					results.add(res);
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
		return results;
	}

	private Result extractResult(Document parsed, String ean) {
		Result result = new Result(ean);

		// nicht gefunden
		Element ele = parsed.selectFirst("#body > div.grid-12.alpha.omega.mx-search-no-result > p");

		// ausverkauft
		ele = parsed.selectFirst(
				"#body > div.mx-detail.container-16 > div:nth-child(1) > div.col.grid-px-315.alpha.omega > div.mx-details-basket.mx-blue-box > div > div.left > h2");
		if (ele != null) {
			result.outOfStock = ele.text();
		}

		// verfügbar
		ele = parsed.selectFirst("#mx-details-price");
		if (ele != null) {
			result.price = ele.text();
		}

		ele = parsed.selectFirst("#test_product_name");
		if (ele != null) {
			result.productName = ele.text();
		}
		log.info(result.toString());
		return result;
	}
}
