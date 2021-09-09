### Test Automation for Keywords(functional), a data driven approach

* The test suite created with a focus to capture the product Ids from the search-api response provided by the different search vendors
* The core functionality included in it are
  * Read input(search terms) from an excel
  * Create search request for each search terms and execute query against the search vendor provided search-api
  * Capture a part of the response (product id's) and write them into an excel against each search terms & the position it returned within the response. 
* The output file used to validate the precision (using excel functions manually) compared to the current search engine provided results with the new search vendors        

This project uses the following stacks and libraries:

* **Java VM** as platform
* **Rest Assured** test automation framework
* **Apache POI** to Read from & write to an excel file 

For Endeca Test to Run
```
gradle test --tests org.search.api.usecase.EndecaProductsTest
```

For Bloomreach Test to Run
```
gradle test --tests org.search.api.usecase.BloomReachProductsTest
```

For Empathy Test to Run
```
gradle test --tests org.search.api.usecase.EmpathyProductsTest
```