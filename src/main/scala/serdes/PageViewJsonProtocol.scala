package serdes

import model.{AggregatedPageviews, Pageview}
import serdes.PageviewJsonProtocol.{jsonFormat3, jsonFormat4}
import spray.json.DefaultJsonProtocol

// JSON formatting for Flink serialization
object PageviewJsonProtocol extends DefaultJsonProtocol {
  implicit val pageviewFormat = jsonFormat4(Pageview)
  implicit val aggregatedPageviewFormat = jsonFormat3(AggregatedPageviews)
}
