package com.ainvar.flickeruploader.action

import java.io._

import com.ainvar.flickeruploader.control._

import scala.annotation.tailrec
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

  def getFileTreeRec(f: File): Stream[File] =
    f #:: (if (f.isDirectory) f.listFiles().toStream.flatMap(getFileTreeRec)
    else Stream.empty)


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

