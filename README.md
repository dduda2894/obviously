## How to start a scraper

First, in the `config.properties` file in the `resources` folder change `scraper` property to the scraper that you want to run.
E.g  asosScraper, debenhamsScraper

Then run `ScraperLauncher` class

## How both scrapers work

The scraper first finds all categories and their respective URLs.
It then iterates over each category loading its url.
Once the page loads the scraper clicks on a button to load more products.
As the page loads more products the website makes an API call to download the product details.
The scraper gets the URL of the API and then makes only API calls to get the details of all products.

Note:
For the purpose of this test, the scraper only picks one sub category from each available category, the first one at the top left corner.
Some subcategory page have different layout. Only the layout where products are displayed in first page was addressed in the
scraper.


## ASOS scraper

### ASOS product search API

The maximum number of products that can be downloaded per one request is 200.

It is unclear why sometimes the API return a higher or lower number of products than the website.
Further investigation into this is necessary.


## Debenhams scraper

### Debenhams product search API

The maximum number of products that can be downloaded per one request is 1000. 
However, it appears to allow to query the highest amount of products by a unique query when 40 products are downloaded per request.
It does not allow to download more than 2000 products for any unique given query, be it through the browser(website) or direct API call.

## WebDriverException

A WebDriverException exception is thrown mid run. The scrapers continues to run. Needs to be investigated, but does not
appear to affect the result.

## Saving the data to the in-memory database

Many products are listed in multiple categories. As a result they appear multiple times in the products list.
This prevents products from being saved in the database because of duplicate ID. 
This could be addressed y merging the duplicates before saving to the database.
For the purpose of this test only 10 products are saved into the database and later retrieved and logged in the console.