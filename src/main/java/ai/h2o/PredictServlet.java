package ai.h2o;

import java.io.*;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.*;
import javax.servlet.*;

import hex.genmodel.ModelMojoReader;
import hex.genmodel.MojoReaderBackend;
import hex.genmodel.MojoReaderBackendFactory;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.RegressionModelPrediction;
import hex.genmodel.easy.*;

public class PredictServlet extends HttpServlet {
  // Set to true for demo mode (to print the predictions to stdout).
  // Set to false to get better throughput.
  private static final boolean VERBOSE = true;

  private static EasyPredictModelWrapper loadMojo(String name) {
    URL mojoUrl = PredictServlet.class.getResource(name);
    try {
      MojoReaderBackend r = MojoReaderBackendFactory.createReaderBackend(mojoUrl, MojoReaderBackendFactory.CachingStrategy.MEMORY);
      return new EasyPredictModelWrapper(ModelMojoReader.readFrom(r));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static EasyPredictModelWrapper model;

  static {
    model = loadMojo("/model.zip");
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
    return model.predictRegression(row);
  }

  private String createJsonResponse(RegressionModelPrediction p) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    sb.append("  \"value\" : ").append(p.value).append("\n");
    sb.append("}\n");

    return sb.toString();
  }

  private BinomialModelPrediction predictBinomial (RowData row) throws Exception {
    return model.predictBinomial(row);
  }

  private String createJsonResponse(BinomialModelPrediction p) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    sb.append("  \"labelIndex\" : ").append(p.labelIndex).append(",\n");
    sb.append("  \"label\" : \"").append(p.label).append("\",\n");
    sb.append("  \"classProbabilities\" : ").append("[\n");
    for (int i = 0; i < p.classProbabilities.length; i++) {
      double d = p.classProbabilities[i];
      if (Double.isNaN(d)) {
        throw new RuntimeException("Probability is NaN");
      }
      else if (Double.isInfinite(d)) {
        throw new RuntimeException("Probability is infinite");
      }

      sb.append("    ").append(d);
      if (i != (p.classProbabilities.length - 1)) {
        sb.append(",");
      }
      sb.append("\n");
    }
    sb.append("  ]\n");
    sb.append("}\n");

    return sb.toString();
  }

  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RowData row = new RowData();
    fillRowDataFromHttpRequest(request, row);

    try {
      String s;
      switch (model.getModelCategory()) {
        case Regression: {
          RegressionModelPrediction p = predictRegression(row);
          s = createJsonResponse(p);
          if (VERBOSE) System.out.println("prediction(regression value): " + p.value);
          break;
        }
        case Binomial: {
          BinomialModelPrediction p = predictBinomial(row);
          s = createJsonResponse(p);
          if (VERBOSE) System.out.println("prediction(binomial p[1]: " + p.classProbabilities[1]);
          break;
        }
        default:
          throw new RuntimeException("Unhandled model category");
      }

      // Emit the prediction to the servlet response.
      response.getWriter().write(s);
      response.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception e) {
      // Prediction failed.
      System.out.println(e.getMessage());
      response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, e.getMessage());
    }
  }
}
