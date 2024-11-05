package utils

import java.nio.file.{Files, Path => JPath}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult
import java.nio.file.SimpleFileVisitor

object FileUtils {
  def deleteRecursively(path: JPath): Unit = {
    Files.walkFileTree(path, new SimpleFileVisitor[JPath]() {
      override def visitFile(file: JPath, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
      override def postVisitDirectory(dir: JPath, exc: java.io.IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }
}
