import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.windowing.time.Time
import spray.json._
import serdes.PageviewJsonProtocol._
import PageviewAggregationSinkApp._
import config.FlinkConfigLoader.FlinkConfig
import model.{AggregatedPageviews, Pageview}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import serdes.PageviewTimestampAssigner

import java.time.Duration

// Helper class for testing Flink sinks
class CollectSink extends SinkFunction[AggregatedPageviews] {
  override def invoke(value: AggregatedPageviews): Unit = {
    println(value)
    CollectSink.values.append(value)
  }
}
object CollectSink {
  val values: scala.collection.mutable.ListBuffer[AggregatedPageviews] = scala.collection.mutable.ListBuffer[AggregatedPageviews]()
}

class PageviewAggregationTestSpec extends AnyFlatSpec with Matchers {

  // Test that JSON is parsed correctly into Pageview case class
  "Pageview JSON parsing" should "correctly parse a JSON string into a Pageview object" in {
    val jsonStr = """{"user_id":1234,"postcode":"SW19","webpage":"www.website.com/index.html","timestamp":1611662684}"""
    val pageview = jsonStr.parseJson.convertTo[Pageview]
    pageview.user_id shouldBe 1234
    pageview.postcode shouldBe "SW19"
    pageview.webpage shouldBe "www.website.com/index.html"
    pageview.timestamp shouldBe 1611662684L
  }

  // Test timestamp extraction for event time processing
  "Timestamp extraction" should "extract correct timestamps for event time processing" in {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val pageviewStream = env.fromElements(
      Pageview(1234, "SW19", "www.website.com/index.html", 1611662684),
      Pageview(1235, "SW20", "www.website.com/about.html", 1611662744)
    ).assignTimestampsAndWatermarks(
      WatermarkStrategy
        .forBoundedOutOfOrderness[Pageview](java.time.Duration.ofSeconds(10))
        .withTimestampAssigner(new PageviewTimestampAssigner)
    )

    pageviewStream.map(_.timestamp).executeAndCollect().toList should contain allOf (1611662684, 1611662744)
  }

  // Test aggregation logic with 1-minute windows
  "Pageview aggregation" should "aggregate pageviews by postcode and count correctly in 1-minute windows" in {
    CollectSink.values.clear()

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    // Create a pageview stream with example data
    val pageviewStream = env.fromElements(
      Pageview(1234, "SW19", "www.website.com/index.html", 1611662684),
      Pageview(1235, "SW19", "www.website.com/contact.html", 1611662740),
      Pageview(1236, "SW19", "www.website.com/about.html", 1611662741),
      Pageview(1237, "SW20", "www.website.com/services.html", 1611662744)
    ).assignTimestampsAndWatermarks(
      WatermarkStrategy
        .forBoundedOutOfOrderness[Pageview](Duration.ofSeconds(5)) // Reduced delay for testing
        .withTimestampAssigner(new PageviewTimestampAssigner)
    )

    // Apply the aggregation logic

    val aggregatedStream = Aggregation.aggregateStream(pageviewStream, 1)

    // Use executeAndCollect for immediate results
    val results = aggregatedStream.executeAndCollect().toList

    // Check the results
    results should matchPattern {
      case List(AggregatedPageviews("SW19", _, 3),AggregatedPageviews("SW20", _, 1) ) =>
    }
  }

  // Test output serialization to JSON format
  "AggregatedPageviews JSON serialization" should "correctly serialize an AggregatedPageviews object to JSON" in {
    val aggregatedPageview = AggregatedPageviews("SW19", "2023-10-01T00:00:00Z", 5)
    val json = aggregatedPageview.toJson.toString
    json shouldEqual """{"datetime":"2023-10-01T00:00:00Z","pageview_count":5,"postcode":"SW19"}"""
  }
}
