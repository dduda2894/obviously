## ASOS scraper


### How it works

The scraper first finds all categories and their respective URLs. 
It then iterates over each category loading its url. 
Once the page loads the scraper clicks on a button to load more products. 
As the page loads more products the website makes an API call to download the product details. 
The scraper gets the URL of the API and then makes only API calls to get the details of all products.

Note:
For the purpose of this test, the scraper only picks one sub category from each available category. 
Some subcategory have different layout. Only the most prominent layout was addressed in the craper.

###  ASOS product search API

The maximum number of products that can be downloaded per one request is 200.

It is unclear why sometimes the API return a higher or lower number of products than the website.
Further investigation into this is necessary.

