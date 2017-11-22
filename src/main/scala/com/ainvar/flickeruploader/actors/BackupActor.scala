package com.ainvar.flickeruploader.actors

import akka.actor.Props
import com.ainvar.flickeruploader.flickreye.Flik

object  BackupActor {
  // protocols
  case class BackupAll (flik: Flik, folderPath: String, test: Boolean) extends Message
  def props = Props(new BackupActor)
}

class BackupActor extends BaseActor {
  override def receive: PartialFunction[Any, Unit] = {
    case BackupActor.BackupAll(flik, folderPath, test) => flik.backupAll(folderPath, test)
    case x => log.info(s"Message $x not managed!")
  }
}
