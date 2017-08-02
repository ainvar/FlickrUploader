package com.ainvar.flickeruploader

import java.io.File
import java.security.MessageDigest

import org.slf4j.Logger
import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.collection.SortedMap
//import play.api.libs.ws.ahc.AhcWSClient
import com.aetrion.flickr._

object Flickr {
  def isSupportedFlickrType(f: File) : Boolean = {
    println(f.getAbsolutePath)
    val lowerName = f.getName.toLowerCase()
      lowerName.endsWith(".png") ||
      lowerName.endsWith(".jpg") ||
      lowerName.endsWith(".jpeg") ||
      lowerName.endsWith(".tif") ||
      lowerName.endsWith(".tiff") ||
      lowerName.endsWith(".mp4") ||
      lowerName.endsWith(".mov") ||
      lowerName.endsWith(".wmv") ||
      lowerName.endsWith(".mov") ||
      lowerName.endsWith(".mpeg")
  }
}

class Flickr {
  val apiKey = "mykey"
  val secret = "mysecret"

//  val endpoint = "http://api.flickr.com/services/rest"
//
//  val apiKey: String
//  val apiSecret: String
//
//  val params = new {
//    def apply(method: String) = SortedMap("api_key" -> apiKey, "method" -> method)
//  }
//
//  val http = new Http with Threads {
//    self.info("Starting new Threaded Http Connection")
//
//    override lazy val log = new DLogger {
//      def info(msg: String, items: Any*) {
//        self.info(msg.format(items: _*))
//      }
//    }
//  }
//
//  def get[T](query: SortedMap[String,Any])(block: Seq[xml.Elem] => Box[T]): Box[T] =
//    http(endpoint <<? query <> {rsp => handleResponse(rsp)(block)})
//
//  def multipartUpload[T](files: Seq[(String,java.io.File)], query: SortedMap[String,Any])(block: Seq[xml.Elem] => Box[T]): Box[T] =
//    http(query.foldLeft(files.foldLeft(:/("api.flickr.com") / "services" / "upload"){
//      case (r,(k,v)) => r << (k,v)
//    }){
//      case (r,(k,v)) => r.next{r.add(k, new StringBody(v.toString))}
//    } <> {rsp => handleResponse(rsp)(block)})
//
//  def handleResponse[T](rsp: xml.Elem)(block: Seq[xml.Elem] => Box[T]): Box[T] = rsp match {
//    case rsp if (rsp \ "@stat").text == "ok" =>
//      block(rsp.child.collect{case x: xml.Elem => x})
//    case rsp if (rsp \ "@stat").text == "fail" =>
//      Failure("Flickr API Error "+(rsp \ "err" \ "@code").text+": "+(rsp \ "err" \ "@msg").text)
//    case rsp =>
//      Failure("Flickr API Unknown Error: "+rsp)
//  }
//
//  def sign(query: SortedMap[String,Any]) =
//    query + ("api_sig" -> md5SumString(apiSecret+query.map{case (k,v) => k+v}.mkString))
//
//  object test {
//    def echo(values: (String, String)*) =
//      get(params("flickr.test.echo") ++ values){
//        result =>
//          Full(result.map(x => (x.label, x.text)).toMap)
//      }
//  }
//
//  object auth {
//    def getFrob =
//      get(sign(params("flickr.auth.getFrob"))){
//        result =>
//          Box(result.map{
//            case <frob>{frob}</frob> => frob.toString
//          }.headOption)
//      }
//
//    def loginUrl(frob: String, perms: String) =
//      (:/("api.flickr.com") / "services" / "auth" <<? sign(SortedMap("api_key" -> apiKey, "frob" -> frob, "perms" -> perms))).to_uri
//
//    def getToken(frob: String) =
//      get(sign(params("flickr.auth.getToken") + ("frob" -> frob))){
//        result =>
//          Full(Token((result \ "token").text,
//            (result \ "perms").text,
//            User((result \ "user" \ "@nsid").text,
//              (result \ "user" \ "@username").text,
//              (result \ "user" \ "@fullname").text)))
//      }
//
//    def checkToken(token: String) =
//      get(sign(params("flickr.auth.checkToken") + ("auth_token" -> token))){
//        result =>
//          Full(Token((result \ "token").text,
//            (result \ "perms").text,
//            User((result \ "user" \ "@nsid").text,
//              (result \ "user" \ "@username").text,
//              (result \ "user" \ "@fullname").text)))
//      }
//
//    def getFullToken(token: String) =
//      get(sign(params("flickr.auth.getFullToken") + ("mini_token" -> token))){
//        result =>
//          Full(Token((result \ "token").text,
//            (result \ "perms").text,
//            User((result \ "user" \ "@nsid").text,
//              (result \ "user" \ "@username").text,
//              (result \ "user" \ "@fullname").text)))
//      }
//  }
//
//  def upload(token: String,
//             photo: java.io.File,
//             title: Option[String] = None,
//             description: Option[String] = None,
//             tags: Option[List[String]] = None,
//             isPublic: Option[Boolean] = None,
//             isFriend: Option[Boolean] = None,
//             isFamily: Option[Boolean] = None,
//             safetyLevel: Option[Int] = None,
//             contentType: Option[Int] = None,
//             hidden: Option[Boolean] = None) =
//    multipartUpload(
//      List("photo" -> photo),
//      sign(SortedMap(List(Some("api_key" -> apiKey),
//        Some("auth_token" -> token),
//        title.map("title" -> _),
//        description.map("description" -> _),
//        tags.map("tags" -> _.mkString(" ")),
//        isPublic.map(x => "is_public" -> (if (x) "1" else "0")),
//        isFriend.map(x => "is_friend" -> (if (x) "1" else "0")),
//        isFamily.map(x => "is_family" -> (if (x) "1" else "0")),
//        safetyLevel.map("safety_level" -> _.toString),
//        contentType.map("content_type" -> _.toString),
//        hidden.map(x => "hidden" -> (if (x) "1" else "0"))).flatten: _*))
//    ){result => Box(result.filter(_.label == "photoid").map(_.text).headOption)}
//
//  def md5SumString(bytes : String) : String = {
//    val md5 = MessageDigest.getInstance("MD5")
//    md5.reset()
//    md5.update(bytes.toArray.map(_.toByte))
//
//    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
//  }
//
//  def shutdown: Unit = {
//    log.info("Shutting down Threaded Http Connection")
//    http.shutdown
//  }

}

