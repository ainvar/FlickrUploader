package com.ainvar.flickeruploader

import java.io.{File, PrintWriter}

import scalafx.scene.control._
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.stage.DirectoryChooser
import com.flickr4java.flickr.auth.Permission
import com.typesafe.scalalogging.LazyLogging

//import scalafx.Includes._
//import scalafx.application.{JFXApp, Platform}
//import scalafx.application.JFXApp.PrimaryStage
//import scalafx.collections.ObservableBuffer
//import scalafx.geometry.Insets
//import scalafx.scene.Scene
//import scalafx.scene.chart.PieChart
//import scalafx.scene.control.ButtonBar.ButtonData
//import scalafx.scene.control._
//import scalafx.scene.layout.{BorderPane, GridPane, StackPane}
//import scalafx.stage.FileChooser

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}

import com.ainvar.flickeruploader.action.FileSystem

import org.scribe.model.{Token, Verifier}

//case class DialogTextData(tableName:String, connString:String, filePath:String)
case class DialogTextData(praticaId:String)

object Main extends JFXApp with LazyLogging {
  // UI build

  // Do something with command line arguments here we print them
  // Named arguments have a form: --name=value
  // For instance, "--output=alpha beta" is interpreted as named argument "output"
  // with value "alpha" and unnamed parameter "beta".
  logger.info("Command line arguments:\n" +
    "  unnamed: " + parameters.unnamed.mkString("[", ", ", "]") + "\n" +
    "  named  : " + parameters.named.mkString("[", ", ", "]"))

  var folderToUpload = ""

val lblFileSelected = new Label("Cartella delle foto")
//  private def createOpenButton() = new Button {
//    id = "openButton"
//    onAction = (ae: ActionEvent) => {
//      val fc = new FileChooser() {
//        title = "Pick a Sound File"
//      }
//
//      val selectedFile = fc.showOpenDialog(new Stage())
//      lblFileSelected.text = "Cartella delle foto:" + selectedFile
//    }
//    prefWidth = 32
//    prefHeight = 32
//  }

  // Show a dialog before primary stage is constructed
//  val fileChooser = new FileChooser()
//  val file = Option(fileChooser.showOpenDialog(new Stage()))

  // Primary stage is provided by JavaFX runtime,
  // but you can create other stages if you need to.
  stage = new PrimaryStage {
    title = "Pictures uploader for Flickr"
    val flik = new Flik
    scene = new Scene(600, 300) {
//      root = new BorderPane {
//        padding = Insets(25)
//        center = new Label {
//          text = file match {
//            case Some(f) => "Selected file: " + f.getPath
//            case None => "File not selected."
//          }
//        }
//      }

      val menuBar = new MenuBar
      val fileMenu = new Menu("File")
      val toolsMenu = new Menu("Tools")

      val openItem = new MenuItem("Choose folder")
      openItem.accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.ControlDown)

//      val saveItem = new MenuItem("Save")
//      saveItem.accelerator =  new KeyCodeCombination(KeyCode.S, KeyCombination.ControlDown)

      val exitItem = new MenuItem("Exit")
      exitItem.accelerator =  new KeyCodeCombination(KeyCode.X, KeyCombination.ControlDown)

      val uploadAllPixItem = new MenuItem("Upload all pictures")
      exitItem.accelerator =  new KeyCodeCombination(KeyCode.U, KeyCombination.ControlDown)

      fileMenu.items = List(openItem, new SeparatorMenuItem, exitItem)

      toolsMenu.items = List(uploadAllPixItem)

//      val checkMenu = new Menu("Checks")
//      val check1 = new CheckMenuItem("check 1")
//      val check2 = new CheckMenuItem("check 1")
//      checkMenu.items = List(check1, check2)
//
//      val radioMenu = new Menu("Radio")
//      val radioList = List(new RadioMenuItem("radio 1"),
//                      new RadioMenuItem("radio 2"),
//                      new RadioMenuItem("radio 3"))
//
//      val group = new ToggleGroup
//      group.toggles = radioList
//      radioMenu.items = radioList
//
//      val typeMenu = new Menu("Types")
//      typeMenu.items = List(checkMenu, radioMenu)

      menuBar.menus=List(fileMenu, toolsMenu)
      menuBar.prefWidth = 600

//      val menuButton = new MenuButton("Menu button")
//      menuButton.items = List(new MenuItem("Button 1"), new MenuItem("Button 2"), new MenuItem("Button 3"))
//
//      menuButton.layoutX = 29
//      menuButton.layoutY = 14

//      val splitMenuButton = new SplitMenuButton(new MenuItem("split 1"), new MenuItem("split 2"), new MenuItem("split 3"))
//      splitMenuButton.text = "Split menu button"
//      splitMenuButton.layoutX = 29
//      splitMenuButton.layoutY = 150

      val btnUpload = new Button(){
        layoutY = 100
        layoutX = 29
        text = "Upload"
      }

      val label = new Label("Welcome!!")
      label.layoutX = 29
      label.layoutY = 50

      val tokenByWeb = new TextField() {
        promptText = "Token from web!"
        visible = false
        layoutY = 70
        layoutX = 29
      }

      val contextMenu = new ContextMenu(new MenuItem("Context 1"), new MenuItem("Context 2"), new MenuItem("Context 3"))
      label.contextMenu = contextMenu

//      content = List(menuBar, menuButton, splitMenuButton, label)
      content = List(menuBar, label, tokenByWeb, btnUpload)

      exitItem.onAction = _ => {
        logger.info("Ciao!!")
        sys.exit(0)
      }

      openItem.onAction = _ => {
        val dc = new DirectoryChooser() {
          title="Select the folder that contains all the pictures to upload"
        }

        val selectedFolder: File = dc.showDialog(stage)
        folderToUpload = selectedFolder.toString
//        val fc = new FileChooser
//        val selectedFile = fc.showOpenDialog(stage)
        logger.info("Folder selected for upload: " + selectedFolder)
        label.text = "Ready: " + selectedFolder

        if(!FileSystem.exist("token.txt")) {
          btnUpload.disable = true
          val url = flik.openAuthenticationUrl
          label.text = "Need authentication to go ahead! Follow link " + url + " in the browser"
          tokenByWeb.visible = true
        }
        else
          tokenByWeb.visible = false

      }

      tokenByWeb.onKeyPressed =_ => {
        btnUpload.disable = false
      }

      btnUpload.onAction = _ => {
        logger.info("Upload button pressed!!")
        logger.info("Folder choosen for upload: " + folderToUpload)
        if(tokenByWeb.visible.value){
          val auth = flik.grantFirstAuth(tokenByWeb.text.value)

          val pw = new PrintWriter(new File("token.txt" ))
          pw.write(auth.accessToken.getToken)
          pw.write(";")
          pw.write(auth.accessToken.getSecret)
          pw.close()

          logger.info("Access granted!! Access token:" + auth.accessToken.getToken)
        }
        else{
          val tokenContent: List[String] = FileSystem.readTextFile("token.txt").split(";").toList
          val accessToken = new Token(tokenContent.head,tokenContent.last)
          val auth = flik.grantAuth(accessToken)
        }
        flik.uploadFotos(folderToUpload)
      }
    }
  }

//  def doAuth(api: Flickr): Auth = {
//    val authIntf = api.getAuthInterface
//
//    val prefix = "Auth.";
//    val conf = ConfigFactory.load();
//    val token = conf.getString(prefix + "token")
//    val secret = conf.getString(prefix + "secret");
//
//    var requestToken: Token = null
//
//    if (token.isEmpty()) {
//      val scanner = new Scanner(System.in)
//      val token = authIntf.getRequestToken()
//      val url = authIntf.getAuthorizationUrl(token, Permission.DELETE)
//      Desktop.getDesktop.browse(URI.create(url))
//      println("Paste in the token it gives you:")
//      print(">>")
//      val tokenKey = scanner.nextLine()
//      scanner.close()
//      requestToken = authIntf.getAccessToken(token, new Verifier(tokenKey))
//      println("Authentication success")
//    } else {
//      requestToken = new Token(token, secret)
//    }
//
//    val auth = authIntf.checkToken(requestToken)
//    println(" Token: " + requestToken.getToken());
//    println(" Secret: " + requestToken.getSecret());
//    println(" Id: " + auth.getUser().getId())
//    println(" Realname: " + auth.getUser().getRealName())
//    println(" Username: " + auth.getUser().getUsername())
//    println(" Permission: " + auth.getPermission().getType())
//
//    auth
//  }

}

