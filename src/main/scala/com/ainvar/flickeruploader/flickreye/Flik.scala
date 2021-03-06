package com.ainvar.flickeruploader.flickreye

import java.io.{BufferedInputStream, File, InputStream}
import java.net.{URI, URL}
import java.util
import java.util.function.Consumer

import com.ainvar.flickeruploader.action.FileSystem
import com.flickr4java.flickr.photos.{Photo, PhotoList, PhotosInterface, Size}
import com.flickr4java.flickr.photosets.Photoset
import com.typesafe.scalalogging.LazyLogging
import org.scribe.model.{Token, Verifier}
//import play.api.libs.ws.ahc.AhcWSClient

import java.awt.Desktop

import com.flickr4java.flickr.auth.{Auth, Permission}
import com.flickr4java.flickr.uploader.UploadMetaData
import com.flickr4java.flickr.{Flickr, REST, RequestContext}

import java.util.concurrent.TimeUnit

case class FirstStepAuthData(requestToken: Token, authUrl: String)
case class LastStepAuth(accessToken: Token, auth:Auth)

case class FileDetails(name:String, suffix: String, tags: Array[String], tagsFromFolder: Array[String])

trait Debug{
  def debugVars[T](obj: T):Any = {

    val vars = obj.getClass.getDeclaredFields

    for(v <- vars){
      v.setAccessible(true)
      println("Field: " + v.getName() + " => " + v.get(obj))
    }

    val resCalls = obj.getClass.getDeclaredMethods

    for(r <- resCalls){
      r.setAccessible(true)
      if(r.getParameterCount ==0)
        println("Methods: " + r.getName() + " => " + r.invoke(obj))
    }

  }
}

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

class Flik  extends LazyLogging with Debug {
  val apiKey = ""
  val secret = ""

  private val flickr = new Flickr(apiKey, secret, new REST)

  private lazy val userId = getCurrentRequestContext.getAuth.getUser.getId

  private val startTime = System.nanoTime()
  private var totCalls = 100
  private val limit = 3600
  private def incrementTotCalls = {totCalls = totCalls +1}

  private lazy val locRequestContext = RequestContext.getRequestContext

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

  private def getTagsFromAbsolutePath(absolutePath: String): Array[String] = {
    val paths = absolutePath.split("/")
    paths(paths.length-2).split("-")
  }

  private def getFileDetails(file: File): FileDetails ={
    val name = file.getName
    val elems = name.substring(0, name.lastIndexOf('.')).split("_")

    val tags =
      if(elems.tail.length ==0)
        Array[String]()
      else{
        println("######### tail:" + elems.tail.mkString("*"))
        elems.tail.head.split("-")

      }

    val suffix = file.getName.substring(file.getName.lastIndexOf('.') + 1).toLowerCase
    FileDetails(elems.head, suffix, tags, getTagsFromAbsolutePath(file.getAbsolutePath))
  }

  private def getElapsedTime: Long = TimeUnit.NANOSECONDS.toHours(System.nanoTime() - startTime)

  private def checkLimitExceeded = {
    val elapsed = getElapsedTime
    val secCalc = if(elapsed < 1) 1 else elapsed
    if (totCalls >= limit * secCalc) {
      logger.warn("Call number exceeded the limit for a time unity of one hour - Total calls: " + totCalls + " - time elapsed: " + getElapsedTime)
      true
    } else false
  }

  private def checkAuth = {
    if(RequestContext.getRequestContext.getAuth == null)
      RequestContext.getRequestContext.setAuth(locRequestContext.getAuth)
  }

  //todo: need to know how much time to wait, now the app wait for an hour to have for sure once again all the availability
  private def limitExceededBehaviour = {
    Thread.sleep(3600000)
  }

  private def downloadFoto(photo:Photo, photoI: PhotosInterface): InputStream = {
    if(checkLimitExceeded) limitExceededBehaviour
    incrementTotCalls
    photoI.getImageAsStream(photo, Size.ORIGINAL)
  }

  private def downloadVideo(video:Photo, photoI: PhotosInterface): InputStream = {
    if(checkLimitExceeded) limitExceededBehaviour
    incrementTotCalls
    photoI.getImageAsStream(video, Size.VIDEO_ORIGINAL)
  }

  private def getSuffix(fileName:String) = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase

  //private def isVideo(fileName: String) =

  private def saveMedia(albumFolder:String, fileName: String, photo:Photo, photoI: PhotosInterface) = {
    FileSystem.saveStream(
      new File(albumFolder, fileName),
      new BufferedInputStream(
        {
          if (Flik.videoSuffixes.contains(getSuffix(fileName)))
            downloadVideo(photo, photoI)
          else
            downloadFoto(photo, photoI)
        }
      )
    )
  }

  private def backupAllPhotosInSet(set: Photoset, albumFolder: File, test:Boolean = false): Int = {
    val photoSetI = getCurrentPhotosetInterface
    val photoI = getCurrentPhotosInterface

    val photoList: PhotoList[Photo] = photoSetI.getPhotos(set.getId, 500, 1)

    val countInSet = photoList.size()

    logger.info("discovered n. " + countInSet + "in album: " + albumFolder.getName)

    val prova = List[Photo](photoList.get(0))

    prova.foreach(f => {
      logger.info("Photo name: " + f.getTitle)

      val originalUrl: String = f.getOriginalUrl

      debugVars[Photo](f)

      val url: URL = new URL(originalUrl)

      // todo: name generation with tags
      var filename: String = url.getFile
      filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length)
      val albumFoldertPath = albumFolder.getCanonicalPath
      logger.info("Now writing " + filename + " to " + albumFoldertPath)

      if(!test)
        saveMedia(albumFoldertPath, filename, f, photoI)

      logger.info("Photo name: " + f.getTitle + " SAVED")
    })
    logger.info(albumFolder.getName + " backup finished!!")
    countInSet
  }

  private def getNewFolderPath(folderPath: String, folderName: String) = folderPath.concat("/" + folderName)

  private def backupPhotoset(photoSet:Photoset, backupFolder: String, test:Boolean = false): Int = {
    logger.info("Call counter: " + totCalls)
    val albumName = photoSet.getTitle
    val newFolderPath = getNewFolderPath(backupFolder, albumName)
    logger.info("Album name: " + albumName)
    val albumFolder = new File(newFolderPath)
    albumFolder mkdir()
    logger.info("Created new folder in: " + newFolderPath)

    backupAllPhotosInSet(photoSet, albumFolder, test)
  }

  private def upload(file: File, metaData: UploadMetaData)= {
    checkAuth
    val uploader = flickr.getUploader()
    val photoId = uploader.upload(file, metaData)
    logger.info(" File : " + file.getName + " uploaded: photoId = " + photoId)
    totCalls = totCalls + 1
    photoId
  }

  def getCurrentRequestContext: RequestContext = RequestContext.getRequestContext

  def getCurrentPhotosInterface = {
    checkAuth
    flickr.getPhotosInterface
  }

  def getCurrentPhotosetInterface = {
    checkAuth
    flickr.getPhotosetsInterface
  }

  def grantFirstAuth(webToken: String): LastStepAuth = {
    val accessToken = getAccessToken(webToken)
    val auth = authInterface.checkToken(accessToken)
    RequestContext.getRequestContext.setAuth(auth)
    locRequestContext
    userId
    LastStepAuth(accessToken, auth)
  }

  def grantAuth(accessToken: Token): Auth = {
    val auth = authInterface.checkToken(accessToken)
    RequestContext.getRequestContext.setAuth(auth)
    locRequestContext
    userId
    auth
  }

  def openAuthenticationUrl: String = firstStepAuth.authUrl

  def uploadFotos(folder: String): Int = {
    logger.info("I'm uploading...")
    val tagsFromFolderName: List[String] = folder.split("/").last.split("-").toList

    if(tagsFromFolderName.length >0) logger.info("tags found by folder name: " + tagsFromFolderName)
    else logger.info("no tag found from folder name :-( ")

    val d = new File(folder)

    val files: List[File] = if (d.exists && d.isDirectory) {
                  d.listFiles.filter(_.isFile).toList
                } else {
                  List[File]()
                }

    logger.info("file discovered :" + files.length)

    for(file <- files){
      incrementTotCalls
      val suffix = file.getName.substring(file.getName.lastIndexOf('.') + 1).toLowerCase
      if(Flik.isValidSuffix(file.getName)) {
        val elems = file.getName.substring(0, file.getName.lastIndexOf('.')).split("-")

        val title = elems.head

        val allTags: List[String] = "ScalaImporter" :: tagsFromFolderName ::: ((elems.tail.headOption map {
          _.split("-").toList
        }) getOrElse List("ScalaImporter"))

        val tagCollection = new util.ArrayList[String]()

        allTags.foreach(tagCollection.add)

        val metaData = getBasicMetadata
        metaData.setFilemimetype(Flik.getMimeType(suffix))
        metaData.setTitle(title)
        metaData.setTags(tagCollection)
        metaData.setFilename(file.getName)

        upload(file, metaData)
      }
      else
        logger.info("No valid suffix: " + suffix)
    }
    val numPics = files.length
    logger.info(s"All $numPics pictures inside folder: $folder uploaded successfully!!")
    numPics
  }

  def RecUpload(folder: String) = {
    incrementTotCalls
    logger.info("I'm uploading all tree...")
    val files: Stream[File] = FileSystem.getFileTreeTailRec(new File(folder))
    for(file <- files){
      val fileDetails = getFileDetails(file)
      if(Flik.isValidSuffix(file.getName)) {
        val title = fileDetails.name

        val allTags: List[String] =
          "ScalaImporterRec" ::
            fileDetails.tags.toList :::
            fileDetails.tagsFromFolder.toList


        val tagCollection = new util.ArrayList[String]()

        allTags.foreach(tagCollection.add)

        val metaData = getBasicMetadata
        metaData.setFilemimetype(Flik.getMimeType(fileDetails.suffix))
        metaData.setTitle(title)
        metaData.setTags(tagCollection)
        metaData.setFilename(file.getName)

        logger.info("all tags:" + allTags.mkString("**"))

        upload(file, metaData)

      }
      else
        logger.info("No valid suffix: " + fileDetails.suffix)
    }
    0
  }

  def backupAll(backupFolder:String, test:Boolean = false): Int = {
    require(userId != null)
    val photoSets: util.Collection[Photoset] = getCurrentPhotosetInterface.getList(userId).getPhotosets

    photoSets forEach(set => backupPhotoset(set, backupFolder, test))
    logger.info("All backup finished!!")
    photoSets size
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

