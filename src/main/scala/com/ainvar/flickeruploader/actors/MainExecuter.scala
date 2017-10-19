package com.ainvar.flickeruploader.actors

import akka.actor.{ActorRef, FSM}
import com.ainvar.flickeruploader.actors.MainExecuter.{MainData, Ready}

object MainExecuter{
  protected sealed trait State
  case object Ready extends State
  case object Running extends State
  protected sealed trait Data { val controller: ActorRef }
  case class MainData(controller: ActorRef) extends Data
}

class MainExecuter extends FSM[MainExecuter.State, MainExecuter.Data] {
  startWith(Ready, MainData(context.parent))
//  when(Ready){
//
//  }
}
