package com.ainvar.flickeruploader.action

import java.io._

import com.ainvar.flickeruploader.control._

import scala.annotation.tailrec
/*
Todo:  convert in scala way IO access, using lib as:
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

  def saveStream(file:File, stream: BufferedInputStream) = {
    val fos: FileOutputStream = new FileOutputStream(file)
    try {
      var c = 0
      while ( {
        c = stream.read
        c != -1
      }) {
        fos.write(c)
      }
    } catch {
      case e: IOException => e.printStackTrace
    } finally {
      fos.flush()
      fos.close()
    }
  }

  def getFileTreeRec(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTreeRec)
    else Stream.empty)

  //TODO not tailrec - need adjustment
  def getFileTreeTailRec(f: File, acc: Stream[File] = Stream.empty): Stream[File] =
    if (f.isDirectory) {
      println("Directory!!!  :  " + f.getAbsolutePath)
      f.listFiles().toStream.flatMap(file => getFileTreeTailRec(file, acc))
    }
    else
      f #::acc

  def getAllFolders(f: File, acc: Stream[File] = Stream.empty): Stream[File] =
    if (f.isDirectory) {
      println("Directory!!!  :  " + f.getAbsolutePath)
      f.listFiles().toStream.flatMap(file => getFileTreeTailRec(file, f #:: acc))
    }
    else
      acc
}

