[![Build Status](https://travis-ci.org/Earnix/correlation-matrix-k.svg?branch=master)](https://travis-ci.org/Earnix/correlation-matrix-k)
# Correlation-Matrix-K

Library provides correlation matrix UI component for Swing. As input, it receives 2-dimensional array data with correlation cofficients.

A correlation matrix is a table showing correlation coefficients between variables. Each cell in the table shows the correlation between two variables. A correlation matrix is used as a way to summarize data, as an input into a more advanced analysis, and as a diagnostic for advanced analyses.

## Getting Started

### Prerequisites
* `Java SE 8` or later
* `Maven` or `Gradle` dependency management

### Usage
Add `Maven` or `Gradle` dependency to your project:
```
<dependency>
	<groupId>com.earnix.eo.gui</groupId>
	<artifactId>correlation-matrix-k</artifactId>
	<version>1.0.0</version>
</dependency>
```
``` implementation 'com.earnix.eo.gui:correlation-matrix-k:1.0'```

Then integrate it into your code:

```java
JFrame frame = new JFrame();
frame.setSize(800, 800);
frame.setResizable(true);
frame.setLocationByPlatform(true);

double [][] correlations = //
double[][] correlationsSqr = //
List<RowType> rowsTypes = //
List<String> rowsTitles = //

CorrelationMatrix matrix = new CorrelationMatrix(rowsTypes, rowsTitles, correlations, correlationsSqr);
frame.getContentPane().add(matrix);
frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
frame.setVisible(true);
```


See [example code](/src/main/java/com/earnix/eo/gui/correlation/Example.java)


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing

## Versioning

## Authors

* **Taras Maslov** - *Initial work* - [linight](https://github.com/linight)

## License

See [LICENSE.MD](/LICENSE.md)

## Acknowledgments

