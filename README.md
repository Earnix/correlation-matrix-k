[![Build Status](https://travis-ci.org/Earnix/correlation-matrix-k.svg?branch=master)](https://travis-ci.org/Earnix/correlation-matrix-k)
# Correlation-Matrix-K

Library provides correlation matrix UI component for Swing. Consist of proportionally resizing correlation matrix and temperature scale pane. Provides presentation customization settings. Does not provide correlations calculation functionality.

A correlation matrix is a table showing correlation coefficients between variables. Each cell in the table shows the correlation between two variables. A correlation matrix is used as a way to summarize data, as an input into a more advanced analysis, and as a diagnostic for advanced analyses.

There are two display modes of correlation matrix. If there is enough space for cell to take equal or more then 16 pixels (is customizable `compactCellSize` property), correlations are displayed as ovals, where oval radius depends on correlation square absolute value, and fill color depends on square correlation sign. If there is not enough space - square correlations are displayed as rectangles with indication based on fill color. Positive corol, set in main component, is used for positive correlations, and negative respectively.

Highlight feature covers row and column of specific value after click on title. Zooming feature displays a region of correlation grid with vertical and horizontal labels. Value of correlation in cell may observed with tooltip. Each presentational setting of matrix can be customized in main component ([CorrelationMatrix](/src/main/java/com/earnix/eo/gui/correlation/CorrelationMatrix.java)). For example, grid lines width. Beware of background color of this component. It is used as backgound of grid, zoom, and as interpolated color of cells. Correlations should be calculated with following methods depending on data types: 

* Numeric with numeric - [Pearson correlation coefficient](https://en.wikipedia.org/wiki/Pearson_correlation_coefficient); 
* Nominal with nominal - [Cramér's V](https://en.wikipedia.org/wiki/Cram%C3%A9r%27s_V);
* Numeric with nominal - [ANOVA (ANalysis Of VAriance)](https://researchbasics.education.uconn.edu/anova_regression_and_chi-square/) ;

<p align="center"><img src="/screen.png"></img></p>

## Getting Started

### Prerequisites
* `Java SE 8` or later
* `Maven` or `Gradle` dependency management

### Usage
Download [release](https://github.com/Earnix/Correlation-Matrix-K/releases/tag/1.0), build with [Maven](https://maven.apache.org/), then add [Maven](https://maven.apache.org/) or `Gradle` dependency to your project:
```
<dependency>
	<groupId>com.earnix.eo.gui</groupId>
	<artifactId>correlation-matrix-k</artifactId>
	<version>1.0</version>
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
See small runnable [example].(/src/main/java/com/earnix/eo/gui/correlation/Example.java)

## Authors
[Taras Maslov](https://github.com/linight)
## License
See [LICENSE.MD](/LICENSE.md)

