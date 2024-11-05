import config.FlinkConfigLoader.FlinkConfig
import model.{AggregatedPageviews, Pageview}
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.time.Time
import serdes.PageviewJsonProtocol
import org.apache.flink.streaming.api.scala._

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import spray.json._
import DefaultJsonProtocol._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows

object Aggregation {

  implicit val aggregatedFormat = PageviewJsonProtocol.aggregatedPageviewFormat
  implicit val pageviewFormat = PageviewJsonProtocol.pageviewFormat

  def aggregateStream(pageViewStream: DataStream[Pageview], windowSizeMinutes: Long): DataStream[AggregatedPageviews] = {

    // TODO: Probably change the date format

    pageViewStream
      .map(pageview => (pageview.postcode, 1))
      .keyBy(_._1)
      .window(TumblingEventTimeWindows.of(Time.minutes(windowSizeMinutes)))
      .sum(1)
      .map { record =>
        val (postcode: String, count: Int) = record
        // Format the date to use in partitioning
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd/HHmm", Locale.ENGLISH).withZone(ZoneId.of("UTC"))
        AggregatedPageviews(
          postcode = postcode,
          datetime = formatter.format(java.time.Instant.now),
          pageview_count = count
        )
      }
  }
}
