## Set your working directory
# setwd("/Users/tomk/0xdata/ws/app-consumer-loan")

library(h2o)
h2o.init(nthreads = -1)

print("Import call center data...")
df  <- h2o.importFile(path = "data/callcenter_data.csv")
df$priority <- as.factor(df$priority)

rand  <- h2o.runif(df, seed = 1234567)
train <- df[rand$rnd <= 0.8, ]
valid <- df[rand$rnd > 0.8, ]

myY = "total_time"
myX = c("priority", "type")

model <- h2o.gbm(x = myX, y = myY,
                 training_frame = train, validation_frame = valid,
                 score_each_iteration = T,
                 ntrees = 100, max_depth = 5, learn_rate = 0.05,
                 model_id = "regression_model")
print(model)

# Download generated MOJO for model
if (! file.exists("src/main/resources")) {
  dir.create("src/main/resources")
}
h2o.download_mojo(model, path = "src/main/resources")
