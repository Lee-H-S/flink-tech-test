package serdes

import model.Pageview
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner

class PageviewTimestampAssigner extends SerializableTimestampAssigner[Pageview] {
  override def extractTimestamp(element: Pageview, recordTimestamp: Long): Long = {
    val eventTime = element.timestamp
    eventTime
  }
}
