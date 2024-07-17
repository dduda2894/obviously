## ASOS scraper

### How to start

Run main method in src/main/java/org/example/scrapers/asos/AsosScraper.java

### How it works

The scraper first finds all categories and their respective URLs. 
It then iterates over each category loading its url. 
Once the page loads the scraper clicks on a button to load more products. 
As the page loads more products the website makes an API call to download the product details. 
The scraper gets the URL of the API and then makes only API calls to get the details of all products.

Note:
For the purpose of this test, the scraper only picks one sub category from each available category. 
Some subcategory have different layout. Only the layout where product are displayed on first page was addressed in the scraper.

###  ASOS product search API

The maximum number of products that can be downloaded per one request is 200.

It is unclear why sometimes the API return a higher or lower number of products than the website.
Further investigation into this is necessary.

### WebDriverException

A WebDriverException exception is thrown mid run. The scraper continues to run. Needs to be investigated, but does not appear to affect the result.

### products.csv file

After a successful completion of scraping the products are stored in a products.csv file.

The category field contains a primary and a sub categories separated by a symbol '/'. 
When the anchor text of the category in the website is "View All", the primary category and sub category in the file match.