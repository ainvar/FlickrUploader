package com.ainvar.flickeruploader.actors

import akka.actor.Props
import com.ainvar.flickeruploader.actors.UploaderActor.{RecUpload, Upload}
import com.ainvar.flickeruploader.flickreye.Flik
/*
Docs::
https://www.flickr.com/services/api/upload.api.html

 */

object UploaderActor{
  case class Upload(flik: Flik, folderPath: String) extends Message
  case class RecUpload(flik: Flik, folderPath: String) extends Message

  def props = Props(new UploaderActor)
}

class UploaderActor extends BaseActor {

  override def receive: PartialFunction[Any, Unit] = {
    case Upload(flik, path) => flik.uploadFotos(path)
    case RecUpload(flik, path) => flik.RecUpload(path)
    case x => log.info(s"Message $x not managed!")
  }
}
