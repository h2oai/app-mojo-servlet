package ai.h2o;

import java.io.*;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.*;
import javax.servlet.*;

import hex.genmodel.ModelMojoReader;
import hex.genmodel.MojoReaderBackend;
import hex.genmodel.MojoReaderBackendFactory;
import hex.genmodel.easy.prediction.RegressionModelPrediction;
import hex.genmodel.easy.*;

public class PredictRegressionServlet extends HttpServlet {
  // Set to true for demo mode (to print the predictions to stdout).
  // Set to false to get better throughput.
  private static final boolean VERBOSE = true;

  private static EasyPredictModelWrapper loadMojo(String name) {
    URL mojoUrl = PredictRegressionServlet.class.getResource(name);
    try {
      MojoReaderBackend r = MojoReaderBackendFactory.createReaderBackend(mojoUrl, MojoReaderBackendFactory.CachingStrategy.MEMORY);
      return new EasyPredictModelWrapper(ModelMojoReader.readFrom(r));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static EasyPredictModelWrapper regressionModel;

  static {
    regressionModel = loadMojo("/regression_model.zip");
  }

  @SuppressWarnings("unchecked")
  private void fillRowDataFromHttpRequest(HttpServletRequest request, RowData row) {
    Map<String, String[]> parameterMap;
    parameterMap = request.getParameterMap();
    if (VERBOSE) System.out.println();
    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
      String key = entry.getKey();
      String[] values = entry.getValue();
      for (String value : values) {
        if (VERBOSE) System.out.println("Key: " + key + " Value: " + value);
        if (value.length() > 0) {
          row.put(key, value);
        }
      }
    }
  }

  private RegressionModelPrediction predictRegression (RowData row) throws Exception {
    return regressionModel.predictRegression(row);
  }

  private String createJsonResponse(RegressionModelPrediction p) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    sb.append("  \"value\" : ").append(p.value).append("\n");
    sb.append("}\n");

    return sb.toString();
  }

  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RowData row = new RowData();
    fillRowDataFromHttpRequest(request, row);

    try {
      RegressionModelPrediction p = predictRegression(row);
      String s = createJsonResponse(p);

      // Emit the prediction to the servlet response.
      response.getWriter().write(s);
      response.setStatus(HttpServletResponse.SC_OK);

      if (VERBOSE) System.out.println("prediction(regression value): " + p.value);
    }
    catch (Exception e) {
      // Prediction failed.
      System.out.println(e.getMessage());
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, e.getMessage());
    }
  }
}
