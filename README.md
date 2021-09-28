### Test Automation for Keywords(functional), a data driven approach

* The test suite created and geared to capture the product Id, title and its position from the search pi response for the different search vendors
* The core functionality included in it are
  * Read input from an excel
    * for general test ==> different search terms as an input
    * for precision test ==> search terms & product_ids (Pipe separated id's up to 5)
  * Create search request for each search terms and execute query against the search vendor provided search-api
  * Capture a part of the response (product's id, name & its position in the results) and write them into a java List object for each search terms. 
* The output file used to validate the precision (using excel functions manually) compared to the current search engine provided results against the different search vendors        

This project uses the following stacks and libraries:

* **Java VM** as platform
* **Junit** - unit testing framework
* **Rest Assured** - Java DSL for easy testing of REST services
* **Apache POI** - Read from & write to an excel file 
* **Google Guava** -  for RateLimiting & Strings check

**sample inputs for different tests**
- For General test ==> test-input.xlsx or test-input-largerset.xlsx
- For precision test ==> test-input-precision.xlsx

* For Endeca Test to Run
```
gradle test --tests org.search.api.usecase.EndecaSearchForProducts
```

- For Bloomreach Test to Run
```
gradle test --tests org.search.api.usecase.BRSMSearchForProducts
gradle test --tests org.search.api.usecase.BRSMPrecisionTest
```

- For Empathy Test to Run
```
gradle test --tests org.search.api.usecase.EmpathySearchForProducts
gradle test --tests org.search.api.usecase.EmpathyPrecisionTest
```