package com.ainvar.flickeruploader

import akka.actor.{Actor, ActorLogging, AllForOneStrategy, SupervisorStrategy}
import akka.actor.SupervisorStrategy.{Decider, Escalate}

package actors {
  trait Message

  trait EscalateStrategy {
    self: Actor =>
    final val escalateStrategy: SupervisorStrategy = {
      def escalateDecider: Decider = {
        case _ ⇒ Escalate
      }
      AllForOneStrategy()(escalateDecider)
    }
  }

  abstract class BaseActor extends Actor with EscalateStrategy with ActorLogging {
    override val supervisorStrategy = escalateStrategy
  }

}
