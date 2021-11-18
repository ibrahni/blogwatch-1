# Blog Watch

This project contains UI tests for baeldung.com


### Overview

The project uses Selenium framework, Crawler4J, jsoup and REST Assured for UI tests . The tests can be run in GUI or headless mode. UI browser has been configured with Firefox using gecodriver and tested with Firefox 56.0 (64 bit) on Windows. Headless mode works with Chrome and HtmlUnit browsers.


### Running Tests from the IDE

The default configuration executes tests with headless mode in windows environment and target URL is https://www.baeldung.com. The configuration can be changed using following properties:

  - _spring.profiles.active_ - environment variable to either "headless-browser" and "ui-browser"
  - _target.env_ - environment variable should be set to "win" or "linux" for headless browser
  - _base.url_ - to target base URL, for example _http://www.baeldung.com_

These can be set as environment variables via the Eclipse run configuration. 


### Running Tests Using Maven 

Three Maven profiles are available for running tests: 
  - _headless-browser-windows_
  - _headless-browser-linux_ 
  - _ui-brower-windows_

The target URL for all profiles is https://www.baeldung.com. The can be changed using following property

- _base.url_ - to target base URL, for example _http://www.baeldung.com_

### Headless Browser selection

Available headless browsers

- _HtmlUnit_
- _chrome-headless_

Headless browser can be configured using following system property

- _headless.browser.name_


### Updating List of Posts and Pages


Run _UpdateArticlesAndPagesLinks#updateLinks_ test for updataing list of articles and pages. 

### JUnit Tags

Following tags are available for running tests selectively. Refer Java docs in _GlobalConstants.java_ for details
  - _hourly_
  - _daily_
  - _weekly_
  - _editorial_
  - _github-related_
  - _technical_

### Excluding a URL for tests running in the bi-monthly bild

URLs can be added to the follwoing file to skip a specific test from the bi-monthly build - https://github.com/eugenp/blogwatch/blob/master/src/main/resources/exceptions-for-tests.yaml

### Launch Mode
 
Set environment variable "LAUNCH_FLAG" to either _true_ or _false_ to set launch mode. Default is _false_


### On Jenkins
 
 The tests are running here, [on Jenkins](http://jenkins.baeldung.com/view/site-monitor/view/site-watch/job/sites-monitor/job/site-watch/)
 
### Tests
 https://docs.google.com/spreadsheets/d/1r3i9FGrh_4cjZWYfZ649xgObOPoqaxBVa4FK-0GGDaI
