package com.ainvar.flickeruploader

import scalafx.scene.control._
import scalafx.scene.input.{KeyCode, KeyCodeCombination, KeyCombination}
import scalafx.stage.DirectoryChooser

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

//case class DialogTextData(tableName:String, connString:String, filePath:String)
case class DialogTextData(praticaId:String)

object Main extends JFXApp {
  // UI build

  // Do something with command line arguments here we print them
  // Named arguments have a form: --name=value
  // For instance, "--output=alpha beta" is interpreted as named argument "output"
  // with value "alpha" and unnamed parameter "beta".
  println("Command line arguments:\n" +
    "  unnamed: " + parameters.unnamed.mkString("[", ", ", "]") + "\n" +
    "  named  : " + parameters.named.mkString("[", ", ", "]"))

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

      val contextMenu = new ContextMenu(new MenuItem("Context 1"), new MenuItem("Context 2"), new MenuItem("Context 3"))
      label.contextMenu = contextMenu

//      content = List(menuBar, menuButton, splitMenuButton, label)
      content = List(menuBar, label, btnUpload)

      exitItem.onAction = _ => sys.exit(0)

      openItem.onAction = _ => {
        val dc = new DirectoryChooser(){
          title="Select the folder that contain all the pictures to upload"
        }

        val selectedFolder = dc.showDialog(stage)

//        val fc = new FileChooser
//        val selectedFile = fc.showOpenDialog(stage)
        label.text = "Ready: " + selectedFolder
      }

    }
  }
}

