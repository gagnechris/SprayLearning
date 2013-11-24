package com.example

import akka.actor.{ ActorSystem, Props }
import akka.actor.ActorDSL._
import akka.io.IO
import akka.io.Tcp._
import akka.event.Logging
import spray.can.Http
import com.example.service.CustomerServiceActor

object Boot extends App {
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")
  val log = Logging(system, getClass)

  val callbackActor = actor(new Act {
    become {
      case b @ Bound(connection) => log.info(b.toString)
      case cf @ CommandFailed(command) => log.error(cf.toString)
      case all => log.debug("SprayLearning App Received a message from Akka.IO: " + all.toString)
    }
  })

  // create and start our service actor
  val service = system.actorOf(Props[CustomerServiceActor], "customer-service")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http).tell(Http.Bind(service, "localhost", 8080), callbackActor)
}