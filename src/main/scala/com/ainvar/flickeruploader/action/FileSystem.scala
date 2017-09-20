package com.ainvar.flickeruploader.action

import java.io._

import com.ainvar.flickeruploader.control._
/*
To convert in scala way IO access, using lib as:
scala-io
rapture.io
Or libs from the Java world, such as Google Guava or Apache Commons IO.
* */
object FileSystem {
  def readTextFile(filename: String): String = {
    val source = scala.io.Source.fromFile(filename)
    try source.mkString finally source.close()
  }

  def exist(filePath: String): Boolean = {
    val fileTemp = new File(filePath)
    if(fileTemp.exists()) true else false
  }

//  import scala.collection.JavaConversions._

  def getFileTree(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTree)
    else Stream.empty)

}

