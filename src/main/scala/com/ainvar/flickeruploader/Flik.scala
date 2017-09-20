package com.ainvar.flickeruploader

import java.{io, util}
import java.io.{File, PrintWriter, Writer}
import java.security.MessageDigest
import java.util.ArrayList
import java.awt.Desktop
import java.net.URI
import javax.print.DocFlavor.URL

import com.flickr4java.flickr.people.User
import com.typesafe.scalalogging.LazyLogging
import org.scribe.model.{Token, Verifier}
import org.slf4j.Logger
import play.api.libs.json._
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.collection.SortedMap
import scala.io.StdIn
//import play.api.libs.ws.ahc.AhcWSClient

import java.awt.Desktop
import com.ainvar.flickeruploader.control._

import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.RequestContext
import com.flickr4java.flickr.FlickrException
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.auth.Auth
import com.flickr4java.flickr.auth.AuthInterface
import com.flickr4java.flickr.auth.Permission
import com.flickr4java.flickr.util.IOUtilities
import com.flickr4java.flickr.uploader.UploadMetaData


case class FirstStepAuthData(requestToken: Token, authUrl: String)
case class LastStepAuth(accessToken: Token, auth:Auth)

object Flik {
  val photoSuffixes = Set("jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff")

  val videoSuffixes = Set("3gp", "3gp", "avi", "mov", "mp4", "mpg", "mpeg", "wmv", "ogg", "ogv", "m2v")

  private def isValidSuffix(basefilename: String): Boolean = {
    if (basefilename.lastIndexOf('.') <= 0) return false
    val suffix = basefilename.substring(basefilename.lastIndexOf('.') + 1).toLowerCase
    if((photoSuffixes contains suffix) || (videoSuffixes contains suffix)) true else false
  }

  def getMimeType(suffix:String): String = suffix.toLowerCase() match {
    case "mpg" | "mpeg" => "video/mpeg"
    case "jpg" | "jpeg" => "image/jpg"
    case "mov" => "image/quicktime"
  }

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

class Flik  extends LazyLogging {
  val apiKey = ""
  val secret = ""

  private lazy val flickr = new Flickr(apiKey, secret, new REST)

  private lazy val authInterface = flickr.getAuthInterface

  private lazy val firstStepAuth = {
    Flickr.debugStream = false
    val authInterface = flickr.getAuthInterface

    val token: Token = authInterface.getRequestToken
    logger.info("token: " + token)

    val url = authInterface.getAuthorizationUrl(token, Permission.WRITE)

    if (Desktop.isDesktopSupported) Desktop.getDesktop.browse(new URI(url))

    logger.info("Follow this URL" + url + " to authorise yourself on Flickr")
    FirstStepAuthData(token, url)
  }

  private def getAccessToken(webToken: String): Token = {
    val authInterface = flickr.getAuthInterface
    authInterface.getAccessToken(firstStepAuth.requestToken, new Verifier(webToken))
  }

  private def getBasicMetadata = new UploadMetaData {
    setPublicFlag(false)
    setFriendFlag(false)
    setFamilyFlag(true)
  }

  def grantFirstAuth(webToken: String): LastStepAuth = {
    val accessToken = getAccessToken(webToken)
    val auth = authInterface.checkToken(accessToken)
    RequestContext.getRequestContext.setAuth(auth)
    LastStepAuth(accessToken, auth)
  }

  def grantAuth(accessToken: Token): Auth = {
    val auth = authInterface.checkToken(accessToken)
    RequestContext.getRequestContext.setAuth(auth)
    auth
  }

  def openAuthenticationUrl: String = firstStepAuth.authUrl

  def uploadFotos(folder: String) = {
    logger.info("I'm uploading...")
    val tagsFromFolderName: List[String] = folder.split("/").last.split("_").toList

    if(tagsFromFolderName.length >0) logger.info("tags found by folder name: " + tagsFromFolderName)
    else logger.info("no tag found from folder name :-( ")

    val d = new File(folder)

    val files = if (d.exists && d.isDirectory) {
                  d.listFiles.filter(_.isFile).toList
                } else {
                  List[File]()
                }

    logger.info("file discovered :" + files.length)

    for(file <- files){
      val suffix = file.getName.substring(file.getName.lastIndexOf('.') + 1).toLowerCase
      if(Flik.isValidSuffix(file.getName)) {
        val elems = file.getName.substring(0, file.getName.lastIndexOf('.')).split("-")

        val title = elems.head

        val allTags: List[String] = "ScalaImporter" :: tagsFromFolderName ::: ((elems.tail.headOption map {
          _.split("_").toList
        }) getOrElse List("ScalaImporter"))

        val tagCollection = new util.ArrayList[String]()

        allTags.foreach(tagCollection.add)

        val metaData = getBasicMetadata
        metaData.setFilemimetype(Flik.getMimeType(suffix))
        metaData.setTitle(title)
        metaData.setTags(tagCollection)
        metaData.setFilename(file.getName)

        val uploader = flickr.getUploader()

        val photoId = uploader.upload(file, metaData)

        logger.info(" File : " + file.getName + " uploaded: photoId = " + photoId)
      }
      else
        logger.info("No valid suffix: " + suffix)
    }
    logger.info(s"All pictures inside folder: $folder uploaded successfully!!")
    0
  }

  def uploadRec = {

  }

// uploader.upload()
  //val frob: String = authInterface.getFrob()

//  def startAuth(f: Flickr4, auth: AuthInterface) = {
//    val accessToken = auth.
//  }
//
//  def completeAuth = {
//
//  }
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

