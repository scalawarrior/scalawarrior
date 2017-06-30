package compiler

import java.io._
import java.util.zip.ZipInputStream

import org.scalajs.core.tools.io._

import scala.reflect.io.{Streamable, VirtualDirectory}
import scala.util.Random

/**
 * Loads the jars that make up the classpath of the scala-js-fiddle
 * compiler and re-shapes it into the correct structure to satisfy
 * scala-compile and scalajs-tools
 */
object Classpath {
  /**
   * In memory cache of all the jars used in the compiler. This takes up some
   * memory but is better than reaching all over the filesystem every time we
   * want to do something.
   */
  lazy val loadedFiles = {
    println("Loading files...")
    val jarFiles = for {
      name <- Seq[String](
//        "/compiler/scala-library-2.12.2.jar",
//        "/compiler/scalajs-library_2.12-0.6.18.jar",
//        "/compiler/scala-warrior.jar"
      )
    } yield {
      val stream = getClass.getResourceAsStream(name)
      println("Loading file" + name + ": " + stream)
      if (stream == null) {
        throw new Exception(s"Classpath loading failed, jar $name not found")
      }
      name -> Streamable.bytes(stream)
    }

    val bootFiles = for {
      prop <- Seq(/*"java.class.path", */"sun.boot.class.path")
      path <- System.getProperty(prop).split(System.getProperty("path.separator"))
      vfile = scala.reflect.io.File(path)
      if vfile.exists && !vfile.isDirectory
    } yield {
      path.split("/").last -> vfile.toByteArray()
    }
    println("Files loaded...")
    jarFiles ++ bootFiles
  }
  /**
   * The loaded files shaped for Scalac to use
   */
  lazy val scalac = for((name, bytes) <- loadedFiles) yield {
    println(s"Loading $name for Scalac")
    val in = new ZipInputStream(new ByteArrayInputStream(bytes))
    val entries = Iterator
      .continually(in.getNextEntry)
      .takeWhile(_ != null)
      .map((_, Streamable.bytes(in)))

    val dir = new VirtualDirectory(name, None)
    for{
      (e, data) <- entries
      if !e.isDirectory
    } {
      val tokens = e.getName.split("/")
      var d = dir
      for(t <- tokens.dropRight(1)){
        d = d.subdirectoryNamed(t).asInstanceOf[VirtualDirectory]
      }
      val f = d.fileNamed(tokens.last)
      val o = f.bufferedOutput
      o.write(data)
      o.close()
    }
    println(dir.size)
    dir
  }
  /**
   * The loaded files shaped for Scala-Js-Tools to use
   */
  lazy val scalajs = {
    println("Loading scalaJSClassPath")
    val loadedJars: Seq[IRFileCache.IRContainer] = {
      for ((name, bytes) <- loadedFiles) yield {
        val jarFile = (new MemVirtualBinaryFile(name) with VirtualJarFile)
          .withContent(bytes)
          .withVersion(Some(name)) // unique through the lifetime of the server
        IRFileCache.IRContainer.Jar(jarFile)
      }
    }

    val cache = (new IRFileCache).newCache
    val res = cache.cached(loadedJars)
    println("Loaded scalaJSClassPath")
    res
  }
}