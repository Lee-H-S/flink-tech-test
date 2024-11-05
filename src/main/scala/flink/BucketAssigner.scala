package flink

import org.apache.flink.streaming.api.functions.sink.filesystem.BucketAssigner
import spray.json._
import DefaultJsonProtocol._
import model.AggregatedPageviews
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.SimpleVersionedStringSerializer
import serdes.PageviewJsonProtocol

class CustomBucketAssigner extends BucketAssigner[String, String] {

  implicit val aggregatedPageviewFormat = PageviewJsonProtocol.aggregatedPageviewFormat

  override def getBucketId(element: String, context: BucketAssigner.Context): String = {
    // TODO: Partition by day, month, year, then the time
    val record = element.parseJson.convertTo[AggregatedPageviews]
    s"postcode=${record.postcode}/date=${record.datetime}"
  }

  override def getSerializer: SimpleVersionedSerializer[String] = SimpleVersionedStringSerializer.INSTANCE
}
