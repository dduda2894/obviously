package org.example.scrapers.asos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.products.AsosProduct;
import org.example.ChromeLauncher;
import org.example.pojo.InterceptedHttpRequest;

import org.example.products.Product;
import org.example.scrapers.Scraper;
import org.example.utils.*;
import org.jsoup.Connection;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.http.Filter;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class AsosScraper extends Scraper {

    public static String scraperName = "asosScraper";

    public AsosScraper(Class clazz) {
        super(clazz);
    }

    public AsosScraper() {
        super(AsosScraper.class);
    }

    public String url = "https://www.asos.com/men/";

    private boolean isCookiesAccepted = false;

    protected Map<String, Map<String, String>> extractCategoriesAndRespectiveUrls(WebDriver driver) {
        Map<String, Map<String, String>> categoriesAndUrls = new HashMap<>();
        driver.get(url);
//          Find primary and secondary category list
        WebElement menu = driver.findElement(By.className("tx7VWbM"));
        List<WebElement> categories = menu.findElements(By.tagName("button"));
//
        List<WebElement> secondaryCategories = menu.findElements(By.cssSelector(".EsGFLPm"));
        ListIterator<WebElement> categoriesIterator = categories.listIterator();
//           Iterate over all primary categories
//          Iterator is used because it provides index of the category.
//          That index is later used to access a list of corresponding secondary categories
        while (categoriesIterator.hasNext()) {
            int primaryCategoryIndex = categoriesIterator.nextIndex();
//              Get primary category text
            String primaryCategory = categoriesIterator.next().getAttribute("innerText");
//            Skip credit card section
            if (primaryCategory.equals("Credit Card")) {
                continue;
            }
//                Get all secondary categories
            List<WebElement> secondaryCategoryListElements = secondaryCategories.get(primaryCategoryIndex).findElements(By.cssSelector("ul li"));
            Map<String, String> secondaryCategoryToUrls = new HashMap<>();
            for (WebElement secondaryCategoryListElement : secondaryCategoryListElements) {
                String secondaryCategory = secondaryCategoryListElement.getAttribute("innerText");
                // TODO(Sometimes the secondary category value/name is blank. Investigate.)
//              Get secondary category URL
                String categoryUrl = secondaryCategoryListElement.findElement(By.tagName("a")).getAttribute("href");
                secondaryCategoryToUrls.put(secondaryCategory, categoryUrl);
                // TODO(Sometimes secondary category has a duplicate, but the url is different.
                //  Extract from the website and ddd to the map another subcategory,
                //  that is present in the website, to avoid that. )
//                For the purpose of the test only scrape the first secondary category
                break;
            }
            categoriesAndUrls.put(primaryCategory, secondaryCategoryToUrls);
        }
        return categoriesAndUrls;

    }


    protected void acceptCookies(WebDriver driver) {
        if (!isCookiesAccepted) {
            try {
                WebElement acceptCookiesButton = driver.findElement(By.id("onetrust-accept-btn-handler"));
                acceptCookiesButton.click();
                isCookiesAccepted = true;
            } catch (NoSuchElementException noSuchElementException) {
                throw new RuntimeException("Cookies pop-up unavailable");
            }
        }
    }

    protected void loadMoreProducts(WebDriver driver) {
        WebElement loadMoreButton = driver.findElement(By.className("loadButton_wWQ3F"));
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(d -> loadMoreButton.isDisplayed());
//          Some categories have different layouts that require additional navigation.
//            For the purpose of this test only a single layout is addressed.
        Actions actions = new Actions(driver);
        actions.moveToElement(loadMoreButton).click().perform();
    }

    protected HttpHandler getHttpHandlerWithUrlFilter(HttpHandler next, String urlPart, InterceptedHttpRequest interceptedHttpRequest) {
        return (HttpHandler) req -> {
            if (req.getUri().contains(urlPart)) {
                logger.debug("API request URL {}", req.getUri());
                Map<String, String> headers = new HashMap();
                for (String headerName : req.getHeaderNames()) {
                    String headerValue = req.getHeader(headerName);
                    headers.put(headerName, headerValue);
                }
                interceptedHttpRequest.setRequest(req);
                interceptedHttpRequest.setUrl(req.getUri());
                interceptedHttpRequest.setHeaders(headers);
                interceptedHttpRequest.setMethod(req.getMethod());
            }
            return next.execute(req);
        };
    }

    protected InterceptedHttpRequest acquireSearchApiRequestDetails(WebDriver driver, String url) {
        InterceptedHttpRequest interceptedHttpRequest = new InterceptedHttpRequest();
//        Intercept product search request
        try (NetworkInterceptor searched = new NetworkInterceptor(driver, (Filter) next -> getHttpHandlerWithUrlFilter(next, "api/product/search/v2/", interceptedHttpRequest))) {
            driver.get(url);
            acceptCookies(driver);
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

    protected List<Product> extractProductsJson(JsonObject jsonObject, String primaryCategory) {
        List<Product> products = new ArrayList<>();
//        Get all products from JSON
        JsonArray apiProducts = jsonObject.getAsJsonArray("products");
        String category = jsonObject.get("categoryName").getAsString();
//        Iterate over all products
        for (JsonElement apiProduct : apiProducts) {
            JsonObject apiProductJsonObject = apiProduct.getAsJsonObject();
            AsosProduct product = AsosProduct.productFromJson(apiProductJsonObject);
            product.setCategory(primaryCategory + " / " + category);
            products.add(product);
        }
        return products;
    }

    protected Connection.Response callProductSearchApi(String url, Map<String, String> headers, String offset, String limit) throws IOException {
//       Set offset, the number of the document from which the next documents will be downloaded
        String initialUrl = UrlUtil.replaceQueryParameter(url, "offset", offset);
//       Set the limit to a number of products that will be downloaded from the API
        initialUrl = UrlUtil.replaceQueryParameter(initialUrl, "limit", limit);
//        Execute request
        Connection.Response response = JsoupUtil.execute(initialUrl, headers, Connection.Method.GET);
        return response;
    }

    private void printProductCollectionStatusMessage(int itemCount, int productsCount) {
        //                TODO(Understand what affects item count and the number of products returned as for some categories these numbers do not align.)
        logger.info("Expected products: {}", itemCount);
        logger.info("Received products: {}", productsCount);
        if (itemCount > productsCount) {
            logger.info("The number of received products is LOWER.");
//
        } else if (itemCount < productsCount) {
            logger.info("The number of received products is HIGHER.");
//
        } else {
            logger.info("The number of received products is MATCHING.");
        }

    }

    protected List<Product> extractAllProductsFromSearchApi(InterceptedHttpRequest interceptedHttpRequest, String primaryCategory) {
        List<Product> allProducts = new ArrayList<>();
        try {
//          The number of the product from which to get the next products
            int offset = 0;
//            The number of products that will be downloaded per request
            int limit = 72;
//          The number of products that, according to asos search API, are available in the category
            int itemCount = -1;
//           Loop until all product are downloaded and extracted
            while (true) {
//              Send request to asos API
                Connection.Response response = callProductSearchApi(interceptedHttpRequest.getUrl(), interceptedHttpRequest.getHeaders(), Integer.toString(offset), Integer.toString(limit));
//               Parse response
                JsonElement jsonElement = JsonUtil.parse(response.body());
                JsonObject jsonObject = jsonElement.getAsJsonObject();
//                Extract the total number of products available. Do it once.
                if (itemCount == -1) {
                    itemCount = jsonObject.get("itemCount").getAsInt();
                }
//                Extract products from response
                List<Product> products = extractProductsJson(jsonObject, primaryCategory);
//               When no products are downloaded from API, exit the loop
                if (products.isEmpty()) {
                    printProductCollectionStatusMessage(itemCount, allProducts.size());
                    break;
                }
                allProducts.addAll(products);
                offset += 72;
            }
        } catch (IOException e) {
//            If any of the requests failed, throw an exceptions
            throw new RuntimeException(e);
        }
        return allProducts;
    }


    protected List<Product> scrapeProducts(WebDriver driver, String url, String primaryCategory) {
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

    protected List<Product> scrapeProductsFromSecondaryCategory(WebDriver driver, Map<String, String> secondaryCategoryAndUrls, String primaryCategory) {
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

    protected List<Product> scrapeProductsFromPrimaryCategory(WebDriver driver, Map<String, Map<String, String>> categoriesAndUrls) {
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
        new AsosScraper().scrape();
    }
}




