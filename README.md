# H2O generated MOJO model REST API servlet example

This example shows a generated MOJO being called using a REST API.


## Files

(Build files)

* build.gradle

(Dataset and model training script)

* data/callcenter_data.csv
* script.R

(Generated)

* src/main/resources/regression_model.zip

(Servlet Back-end)

* src/main/webapp/WEB-INF/web.xml
* src/main/java/ai/h2o/PredictRegressionServlet.java
* src/main/resources/regression_model.zip

(Output)

* build/libs/ROOT.war

## Steps to run

##### Step 1: Install H2O's R package if you don't have it yet.

<http://h2o.ai/download>

##### Step 2: Build the model.

The model MOJO file is dropped into src/main/resources/regression_model.zip

```
$ Rscript script.R
```

##### Step 3: Build the java WAR file.

```
$ ./gradlew build
```

The output of this process is build/libs/ROOT.war

##### Step 4: Deploy the .war file in a Jetty servlet container.

This is handy for testing.

```
$ ./gradlew jettyRunWar
```

##### Step 5: In another terminal window, run a transaction against the jetty server to make a prediction.

```
curl -v "http://localhost:8080/predict?priority=1&type=PS"
```

## Data

The dataset is telephone data recorded from a call-center of “Anonymous Bank”. The data can be found here: 

* <http://ie.technion.ac.il/serveng/callcenterdata/index.html>
