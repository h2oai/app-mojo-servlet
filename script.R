## Set your working directory
setwd("/Users/tomk/0xdata/ws/app-mojo-servlet")

library(h2o)
h2o.init(nthreads = -1)

print("Import call center data...")
df  <- h2o.importFile(path = "data/callcenter_data.csv")
df$priority <- as.factor(df$priority)
df$is_long_call <- df$total_time > 100
df$is_long_call <- as.factor(df$is_long_call)

rand  <- h2o.runif(df, seed = 1234567)
train <- df[rand$rnd <= 0.8, ]
valid <- df[rand$rnd > 0.8, ]

# myY = "total_time"
# myX = c("priority", "type")
#
#model <- h2o.gbm(x = myX, y = myY,
#                 training_frame = train, validation_frame = valid,
#                 score_each_iteration = T,
#                 ntrees = 2, max_depth = 5, learn_rate = 0.05,
#                 model_id = "model")
#print(model)

myY = "is_long_call"
myX = c("priority", "type")

model <- h2o.gbm(x = myX, y = myY,
                 training_frame = train, validation_frame = valid,
                 score_each_iteration = T,
                 ntrees = 2, max_depth = 5, learn_rate = 0.05,
                 model_id = "model")
print(model)

# Download generated MOJO for model
if (! file.exists("src/main/resources")) {
  dir.create("src/main/resources")
}
h2o.download_mojo(model, path = "src/main/resources")
