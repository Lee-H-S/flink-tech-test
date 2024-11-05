### Flink Application Tech Test

#### Aggregation Application Task ####
This is the application that aggregates the pageview data, by the minute, and sinks it into a local directory. 

Other features I'd have tried to include if I could afford to spend more time on it:

- Writing to S3, and mocking it for the tests
- Logging
- Writing metrics to Cloudwatch, allowing for alarms to be triggered
- Automating the CI/CD - Add a script to run the tests, build the jar, deploy to S3, run the terraform, etc
- Writing the Terraform Properly

##### Design Decisions #####
Considering the load is only 100k messages per day, I decided to deploy the application on a medium EC2 - it could probably handle significantly more, so gives us some slack for sudden spikes - but also keeps the cost and deployment complexity relatively low.

EMR would be good for much higher loads that are more variable with its built in auto-scaling, and cluster sizes - whereas Managed Flink would reduce deployment complexity. Both of these options come at an increase cost.


##### Running The Tests #####

You can run the tests for the application using docker, by running:

`docker build -t my-flink-app .`

`docker run --rm my-flink-app`