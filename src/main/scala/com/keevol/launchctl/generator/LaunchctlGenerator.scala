package com.keevol.launchctl.generator

import com.keevol.javafx.KFXApplication
import com.keevol.javafx.controls.{KStatusBar, KTaskSpinner}
import com.keevol.javafx.utils.{DnD, Images, KTaskExecutor, Labels, PopMessage, ScrollPanes}
import com.keevol.launchctl.generator.utils.KVTemplateNodeGraphics._
import fr.brouillard.oss.cssfx.CSSFX
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Hyperlink, ScrollPane, SplitPane}
import javafx.scene.image.ImageView
import javafx.scene.input.{DataFormat, TransferMode}
import javafx.scene.layout._
import javafx.scene.{Node, Scene}
import javafx.stage.Stage
import org.slf4j.LoggerFactory

class LaunchctlGenerator extends KFXApplication {

  private val logger = LoggerFactory.getLogger(classOf[LaunchctlGenerator])

  val spinner = new KTaskSpinner()
  val taskExecutor = new KTaskExecutor(spinner)


  val lcLabelNodeTemplate = createNode("Label")
  val lcRunAtLoadNodeTemplate = createNode("RunAtLoad")
  val lcKeepAliveNodeTemplate = createNode("KeepAlive")
  val lcProgramNodeTemplate = createNode("Program")
  val lcProgramArgumentsNodeTemplate = createNode("ProgramArguments")
  val lcWorkingDirNodeTemplate = createNode("WorkingDirectory")
  val lcUsernameNodeTemplate = createNode("UserName")
  val lcOutPathNodeTemplate = createNode("StandardOutPath")
  val lcErrPathNodeTemplate = createNode("StandardErrorPath")
  val lcManualEditNodeTemplate = createNode("ManualEdit(Custom)")

  def layoutStage(primaryStage: Stage): Unit = {

    primaryStage.setTitle("Launchd plist Composer")
    primaryStage.getIcons.add(Images.fromClassPath("/images/lc_logo.jpg"))

    val layout = new BorderPane()
    layout.setTop(layoutHeader())
    layout.setLeft(layoutNodeTemplates())
    layout.setCenter(layoutMainZone())
    layout.setBottom(layoutFooter())

    val scene = new Scene(layout)
    scene.getStylesheets.add("/css/style.css")
    primaryStage.setScene(scene)
    CSSFX.start()
  }

  def layoutNodeTemplates(): Node = {
    val listBox = new VBox(20)
    listBox.setAlignment(Pos.CENTER_LEFT)
    listBox.setPadding(new Insets(20))
    listBox.getChildren.addAll(
      lcLabelNodeTemplate,
      lcRunAtLoadNodeTemplate,
      lcKeepAliveNodeTemplate,
      lcProgramNodeTemplate,
      lcProgramArgumentsNodeTemplate,
      lcWorkingDirNodeTemplate,
      lcUsernameNodeTemplate,
      lcOutPathNodeTemplate,
      lcErrPathNodeTemplate,
      lcManualEditNodeTemplate,
      Labels.default("xxx"),
      Labels.default("aaa"),
      Labels.default("bbb"),
      Labels.default("444")
    )
    ScrollPanes.wrap(listBox)
  }

  def layoutMainZone(): Node = {
    val splitPane = new SplitPane()

    val leftLayout = new StackPane()
    leftLayout.getChildren.add(Labels.title("Block编排区"))
    leftLayout.setId("left-zone")
    DnD.dropTo(leftLayout) { dragboard =>
      val nodeType = dragboard.getContent(DataFormat.PLAIN_TEXT).toString
      PopMessage.show(nodeType, splitPane.getScene)
    }

    val rightLayout = new StackPane()
    rightLayout.getChildren.add(Labels.title("Right XML ZONE"))
    splitPane.getItems.addAll(leftLayout, rightLayout)
    splitPane.setDividerPositions(0.5f, 0.5f)

    splitPane
  }

  private def layoutFooter(): Node = {
    val creditLabel = new Hyperlink("©福强出品")
    creditLabel.setOnMouseClicked(e => {
      getHostServices.showDocument("https://afoo.me")
    })
    new KStatusBar(leftItems = Array(creditLabel), rightItems = Array(spinner))
  }

  private def layoutHeader(): HBox = {
    val layout = new HBox(20)
    layout.setPadding(new Insets(20))
    layout.setAlignment(Pos.CENTER_LEFT)
    val logo = new ImageView(Images.fromClassPath("/images/lc_banner.jpg"))
    logo.setFitHeight(46)
    logo.setPreserveRatio(true)
    layout.getChildren.add(logo)
    layout
  }
}


object LaunchctlGenerator {
  private val logger = LoggerFactory.getLogger(classOf[LaunchctlGenerator])

  def main(args: Array[String]): Unit = {
    logger.info("bootstrap LaunchctlGenerator...")
    KFXApplication.launch(classOf[LaunchctlGenerator], args)
  }
}

object LaunchctlGeneratorIDELauncher {
  def main(args: Array[String]): Unit = {
    LaunchctlGenerator.main(args)
  }
}