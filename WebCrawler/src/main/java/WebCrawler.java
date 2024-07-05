import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawler {

    private static final int MAX_THREADS = 10;
    private static final Set<String> visitedUrls = new HashSet<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

    public static void main(String[] args) {
        String startUrl = "https://yahoo.com";
        crawlPage(startUrl);
        
        // Shutdown the executor service and wait for tasks to finish
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void crawlPage(String url) {
        executorService.submit(() -> {
            if (visitedUrls.contains(url)) {
                return;
            }

            synchronized (visitedUrls) {
                if (visitedUrls.contains(url)) {
                    return;
                }
                visitedUrls.add(url);
            }

            try {
                Document document = Jsoup.connect(url).get();
                System.out.println("Crawling: " + url);

                Elements links = document.select("a[href]");
                for (Element link : links) {
                    String nextUrl = link.absUrl("href");
                    if (!nextUrl.isEmpty() && nextUrl.startsWith("http")) {
                        crawlPage(nextUrl);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error while crawling " + url + ": " + e.getMessage());
            }
        });
    }
}
