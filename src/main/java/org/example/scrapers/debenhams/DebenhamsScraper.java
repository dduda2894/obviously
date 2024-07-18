package org.example.scrapers.debenhams;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.ChromeLauncher;
import org.example.pojo.InterceptedHttpRequest;
import org.example.products.DebenhamsProduct;
import org.example.products.Product;
import org.example.scrapers.Scraper;
import org.example.utils.CommonUtils;
import org.example.utils.JsonUtil;
import org.example.utils.JsoupUtil;
import org.jsoup.Connection;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.http.Filter;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebenhamsScraper extends Scraper {

    public static String scraperName = "debenhamsScraper";

    public DebenhamsScraper() {
        super(DebenhamsScraper.class);
    }

    public String debenhamsUrl = "https://www.debenhams.com/";

    private List<Product> extractProductsJson(JsonObject jsonObject, String primaryCategory) {
        List<Product> products = new ArrayList<>();
//        Get all products from JSON
        JsonArray apiProducts = jsonObject.getAsJsonArray("results").get(0).getAsJsonObject().get("hits").getAsJsonArray();


//        Iterate over all products
        for (JsonElement apiProduct : apiProducts) {
            JsonObject apiProductJsonObject = apiProduct.getAsJsonObject();
            DebenhamsProduct product = DebenhamsProduct.productFromJson(apiProductJsonObject);
            product.setCategory(primaryCategory);
            products.add(product);
        }
        return products;
    }

    private Connection.Response callProductSearchApi(String url, Map<String, String> headers, String content, HttpMethod method, String page) throws IOException {
//       Set offset, the number of the document from which the next documents will be downloaded
        content = content.replaceAll("&page=.", "&page=" + page);
        ;
//        Execute request
        Connection.Method jsoupMethod = (method == HttpMethod.POST) ? Connection.Method.POST : Connection.Method.GET;
        Connection.Response response = JsoupUtil.execute(url, headers, jsoupMethod, content);
        return response;
    }

    private List<Product> extractAllProductsFromSearchApi(InterceptedHttpRequest interceptedHttpRequest, String primaryCategory) {
        List<Product> allProducts = new ArrayList<>();
        try {
//          The number of the product from which to get the next products
            int page = 1;

//          The number of products that, according to debenhams search API, are available in the category
            int itemCount = -1;
//           Loop until all product are downloaded and extracted
            while (true) {
//              Send request to debenhams API
                Connection.Response response = callProductSearchApi(interceptedHttpRequest.getUrl(), interceptedHttpRequest.getHeaders(), interceptedHttpRequest.getContent(), interceptedHttpRequest.getMethod(), Integer.toString(page));
//               Parse response
                JsonElement jsonElement = JsonUtil.parse(response.body());
                JsonObject jsonObject = jsonElement.getAsJsonObject();
//                Extract products from response
                List<Product> products = extractProductsJson(jsonObject, primaryCategory);
//               When no products are downloaded from API, exit the loop
                if (products.isEmpty()) {
                    logger.info("Scraped products count: {}", allProducts.size());
                    break;
                }
                allProducts.addAll(products);
                logger.info("Scraped pages {} products {}", page, allProducts.size());
                page++;

            }
        } catch (IOException e) {
//            If any of the requests failed, throw an exceptions
            throw new RuntimeException(e);
        }
        return allProducts;
    }

    private HttpHandler getHttpHandlerWithUrlFilter(HttpHandler next, String urlPart, InterceptedHttpRequest interceptedHttpRequest) {
        return (HttpHandler) req -> {
            if (req.getMethod() == HttpMethod.POST && req.getUri().contains(urlPart)) {
                logger.debug("API request URL {}", req.getUri());
                Map<String, String> headers = new HashMap();
                for (String headerName : req.getHeaderNames()) {
                    String headerValue = req.getHeader(headerName);
                    headers.put(headerName, headerValue);
                }
                try {
                    interceptedHttpRequest.setContent(new String(req.getContent().get().readAllBytes(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                interceptedHttpRequest.setRequest(req);
                interceptedHttpRequest.setUrl(req.getUri());
                interceptedHttpRequest.setHeaders(headers);
                interceptedHttpRequest.setMethod(req.getMethod());
            }
            return next.execute(req);
        };
    }

    private void loadMoreProducts(WebDriver driver) {
        WebElement loadMoreButton = driver.findElement(By.cssSelector("button[data-test-id='pagination-load-more']"));
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> loadMoreButton.isDisplayed());
//          Some categories have different layouts that require additional navigation.
//            For the purpose of this test only a single layout is addressed.
        Actions actions = new Actions(driver);
        actions.moveToElement(loadMoreButton).click().perform();
    }

    private InterceptedHttpRequest acquireSearchApiRequestDetails(WebDriver driver, String url) {
        InterceptedHttpRequest interceptedHttpRequest = new InterceptedHttpRequest();
//        Intercept product search request
        try (NetworkInterceptor searched = new NetworkInterceptor(driver, (Filter) next -> getHttpHandlerWithUrlFilter(next, "queries?x-algolia-agent=", interceptedHttpRequest))) {
            driver.get(url);
//          Load more products to trigger an API call
            loadMoreProducts(driver);
            int counter = 0;
//          Wait for a search product API call
            while (interceptedHttpRequest.getUrl() == null) {
                CommonUtils.sleep(1000);
                counter++;
                if (counter == 10) {
                    throw new RuntimeException("Failed to load more products");
                }
            }

        }
        return interceptedHttpRequest;
    }

    private List<Product> scrapeProducts(WebDriver driver, String url, String primaryCategory) {
        try {
//          Get the URL that is used to  call API to search for products
            InterceptedHttpRequest interceptedHttpRequest = acquireSearchApiRequestDetails(driver, url);
//          Use that URL to extract all available products from the category URL
            return extractAllProductsFromSearchApi(interceptedHttpRequest, primaryCategory);
        } catch (NoSuchElementException noSuchElementException) {
            logger.info("Ignore URL with different HTML layout: " + url);
            return new ArrayList<>();
        }

    }

    private List<Product> scrapeProductsFromPrimaryCategory(WebDriver driver, Map<String, Map<String, String>> categoriesAndUrls) {
        List<Product> allProducts = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> primaryCategoryAndUrls : categoriesAndUrls.entrySet()) {
            String primaryCategory = primaryCategoryAndUrls.getKey();
            Map<String, String> secondaryCategoryAndUrls = primaryCategoryAndUrls.getValue();
            List<Product> products = scrapeProductsFromSecondaryCategory(driver, secondaryCategoryAndUrls, primaryCategory);
            allProducts.addAll(products);
            logger.info("");
        }
        return allProducts;
    }

    private List<Product> scrapeProductsFromSecondaryCategory(WebDriver driver, Map<String, String> secondaryCategoryAndUrls, String primaryCategory) {
        List<Product> allProducts = new ArrayList<>();
//        Iterate over all secondary category records
        for (Map.Entry<String, String> secondaryCategoryAndUrl : secondaryCategoryAndUrls.entrySet()) {
            String secondaryCategory = secondaryCategoryAndUrl.getKey();
            String secondaryCategoryUrl = secondaryCategoryAndUrl.getValue();
            logger.info("Scrape: {}", secondaryCategoryUrl);
            logger.info("Category: {}", secondaryCategory);
//            Start scraping
            List<Product> products = scrapeProducts(driver, secondaryCategoryUrl, primaryCategory);
            allProducts.addAll(products);
            break;
        }
        return allProducts;
    }

    private Map<String, Map<String, String>> extractCategoriesAndRespectiveUrls(WebDriver driver) {
        Map<String, Map<String, String>> categoriesAndUrls = new HashMap<>();
        driver.get(debenhamsUrl);
        List<WebElement> navigation = driver.findElements(By.cssSelector("li[data-test-id='desktop-nav-topmenu-category-title']"));
        for (WebElement primaryCategoryListEelement : navigation) {
            WebElement primaryCategoryAnchorElement = primaryCategoryListEelement.findElement(By.cssSelector(".text-primary-nav-text"));
            String primaryCategory = primaryCategoryAnchorElement.getText();
            List<WebElement> subCategoriesAnchor = primaryCategoryListEelement.findElements(By.cssSelector("ul[data-test-id='desktop-nav-submenu'] li a"));
            Map<String, String> secondaryCategoryToUrls = new HashMap<>();
            for (WebElement webElement : subCategoriesAnchor) {
                String secondaryCategory = webElement.getAttribute("innerText");
                String categoryUrl = webElement.getAttribute("href");
                secondaryCategoryToUrls.put(secondaryCategory, categoryUrl);
            }
            logger.info("Extracted subcategory urls for category {} ", primaryCategory);
            categoriesAndUrls.put(primaryCategory, secondaryCategoryToUrls);
        }
        return categoriesAndUrls;
    }

    @Override
    public List<Product> scrape() {
        WebDriver driver = new ChromeLauncher().launch();
//       Set implicit  wait  to 10 seconds. Allows time for elements to become available
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        Map<String, Map<String, String>> categoriesAndUrls = extractCategoriesAndRespectiveUrls(driver);
//        Scrape all products from all categories
        List allProducts = scrapeProductsFromPrimaryCategory(driver, categoriesAndUrls);

        return allProducts;
    }


    public static void main(String[] args) {
        new DebenhamsScraper().scrape();
    }
}
