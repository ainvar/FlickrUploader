package com.ainvar.flickeruploader.actors

import akka.actor.Props
import com.ainvar.flickeruploader.flickreye.Flik

object  BackupActor {
  // protocols
  case class BackupAll (flik: Flik, folderPath: String) extends Message
  def props = Props(new BackupActor)
}

class BackupActor extends BaseActor {
  override def receive: PartialFunction[Any, Unit] = {
    case BackupActor.BackupAll => ???
  }
}
