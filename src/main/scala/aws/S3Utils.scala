package aws

object S3Utils {
  def joinWithSlash(s1: String, s2: String): String = {
    val withSlash = if (s1.endsWith("/")) s1 else s"$s1/"
    s"$withSlash$s2"
  }
}