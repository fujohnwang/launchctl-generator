package com.keevol.launchctl.generator

import com.keevol.javafx.KFXApplication
import com.keevol.javafx.controls.launchctl._
import com.keevol.javafx.controls.{CloseActionable, KList, KStatusBar, KTaskSpinner}
import com.keevol.javafx.utils._
import com.keevol.launchctl.generator.utils.KVTemplateNodes._
import com.keevol.utils.Files
import fr.brouillard.oss.cssfx.CSSFX
import io.vertx.core.impl.ConcurrentHashSet
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.concurrent.Task
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Button, Hyperlink, SplitPane, TextArea, Tooltip}
import javafx.scene.image.ImageView
import javafx.scene.input.{DataFormat, KeyCode, KeyCodeCombination, KeyCombination}
import javafx.scene.layout._
import javafx.scene.{Node, Scene}
import javafx.stage.Stage
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

import java.util.concurrent.{Callable, ConcurrentHashMap}

class LaunchctlGenerator extends KFXApplication {

  private val logger = LoggerFactory.getLogger(classOf[LaunchctlGenerator])

  val spinner = new KTaskSpinner()
  val taskExecutor = new KTaskExecutor(spinner)

  val lcLabelNodeTemplate = createTemplateNode(LaunchdConfigKeys.Label.value())
  val lcRunAtLoadNodeTemplate = createTemplateNode(LaunchdConfigKeys.RunAtLoad.value())
  val lcKeepAliveNodeTemplate = createTemplateNode(LaunchdConfigKeys.KeepAlive.value())
  val lcProgramNodeTemplate = createTemplateNode(LaunchdConfigKeys.Program.value())
  val lcProgramArgumentsNodeTemplate = createTemplateNode(LaunchdConfigKeys.ProgramArgs.value())
  val lcWorkingDirNodeTemplate = createTemplateNode(LaunchdConfigKeys.WorkingDirectory.value())
  val lcUsernameNodeTemplate = createTemplateNode(LaunchdConfigKeys.Username.value())
  val lcOutPathNodeTemplate = createTemplateNode(LaunchdConfigKeys.StandardOutputPath.value())
  val lcErrPathNodeTemplate = createTemplateNode(LaunchdConfigKeys.StandardErrorPath.value())
  val lcIntervalNodeTemplate = createTemplateNode(LaunchdConfigKeys.StartInterval.value())
  val lcCalendarIntervalNodeTemplate = createTemplateNode(LaunchdConfigKeys.StartCalendarInterval.value())
  val lcManualEditNodeTemplate = createTemplateNode(LaunchdConfigKeys.Custom.value())

  val nodeEditCache: ConcurrentHashSet[String] = new ConcurrentHashSet[String]()

  val composerList = new KList("Drop Node Below To Compose", new Insets(20))
  composerList.setSpacing(20)
  composerList.setPadding(new Insets(20))

  val nodeCloseAction = (node: Node) => composerList.getChildren.remove(node)

  val nodeCreators = new ConcurrentHashMap[String, Callable[Node]]()
  nodeCreators.put(LaunchdConfigKeys.Label.value(), () => createNode(new LabelNode("")))
  nodeCreators.put(LaunchdConfigKeys.RunAtLoad.value(), () => createNode(new RunAtLoadNode()))
  nodeCreators.put(LaunchdConfigKeys.KeepAlive.value(), () => createNode(new KeepAliveNode()))
  nodeCreators.put(LaunchdConfigKeys.Program.value(), () => createNode(new ProgramNode("")))
  nodeCreators.put(LaunchdConfigKeys.ProgramArgs.value(), () => createNode(new ProgramArgumentsNode(Array[String]())))
  nodeCreators.put(LaunchdConfigKeys.WorkingDirectory.value(), () => createNode(new WorkingDirectoryNode("")))
  nodeCreators.put(LaunchdConfigKeys.Username.value(), () => createNode(new UserNameNode("")))
  nodeCreators.put(LaunchdConfigKeys.StandardOutputPath.value(), () => createNode(new StandardOutPathNode("")))
  nodeCreators.put(LaunchdConfigKeys.StandardErrorPath.value(), () => createNode(new StandardErrorPathNode("")))
  nodeCreators.put(LaunchdConfigKeys.StartInterval.value(), () => createNode(new IntervalConfigNode()))
  nodeCreators.put(LaunchdConfigKeys.StartCalendarInterval.value(), () => createNode(new CalendarIntervalConfigNode()))
  nodeCreators.put(LaunchdConfigKeys.Custom.value(), () => createNode(new CustomEditNode()))


  val loadFromTemplateTask = (jobTemplate: Boolean) => {
    composerList.clearList()
    nodeEditCache.clear()

    addNodeWithInterceptor(LaunchdConfigKeys.Label.value())
    addNodeWithInterceptor(LaunchdConfigKeys.RunAtLoad.value())
    addNodeWithInterceptor(LaunchdConfigKeys.KeepAlive.value())
    addNodeWithInterceptor(LaunchdConfigKeys.Program.value())
    addNodeWithInterceptor(LaunchdConfigKeys.ProgramArgs.value())
    if (jobTemplate) {
      addNodeWithInterceptor(LaunchdConfigKeys.StartInterval.value())
    }
    addNodeWithInterceptor(LaunchdConfigKeys.WorkingDirectory.value())
    addNodeWithInterceptor(LaunchdConfigKeys.Username.value())
    addNodeWithInterceptor(LaunchdConfigKeys.StandardOutputPath.value())
    addNodeWithInterceptor(LaunchdConfigKeys.StandardErrorPath.value())
  }

  val plistXmlDisplay: TextArea = new TextArea()


  val copyAction = new Runnable {
    override def run(): Unit = {
      //  copy plist content to clipboard
      Clipboards.put(plistXmlDisplay.getText)
      PopMessage.show("plist content copied successfully.")
    }
  }


  override def registerGlobalKeys(stage: Stage): Unit = {
    Keys.on(stage, new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN))(copyAction)
  }

  def layoutStage(primaryStage: Stage): Unit = {

    primaryStage.setTitle("Launchd plist Composer")
    primaryStage.getIcons.add(Images.fromClassPath("/images/lc_logo.jpg"))
    primaryStage.setMinWidth(1024)
    primaryStage.setMinHeight(768)

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
      lcIntervalNodeTemplate,
      lcCalendarIntervalNodeTemplate,
      lcManualEditNodeTemplate
    )
    ScrollPanes.wrap(listBox)
  }

  def layoutMainZone(): Node = {
    val splitPane = new SplitPane()

    val composerListScrollPane = composerList.putInScrollPane()

    DnD.dropTo(composerList) { dragboard =>
      val nodeType = dragboard.getContent(DataFormat.PLAIN_TEXT).toString
      logger.debug(s"get nodeType from dragboard: $nodeType")
      if (nodeEditCache.contains(nodeType)) {
        val warningMessage = s"Node of type >>> ${nodeType} <<< can only be added once."
        logger.info(warningMessage)
        PopMessage.show(warningMessage)
        false
      } else {
        if (nodeCreators.containsKey(nodeType)) {
          addNodeWithInterceptor(nodeType)
          composerListScrollPane.setVvalue(1.0)
        } else {
          PopMessage.show(s"no node creator for ${nodeType}", splitPane.getScene)
        }
        true
      }
    }

    val rightLayout = new StackPane()
    rightLayout.getChildren.add(plistXmlDisplay)
    splitPane.getItems.addAll(composerListScrollPane, rightLayout)
    splitPane.setDividerPositions(0.5f, 0.5f)

    splitPane
  }

  private def layoutFooter(): Node = {
    val creditLabel = new Hyperlink("©福强出品 (A Fuqiang Production)")
    creditLabel.setOnMouseClicked(e => {
      getHostServices.showDocument("https://afoo.me")
    })
    new KStatusBar(centerItems = Array(creditLabel), rightItems = Array(spinner))
  }

  private def layoutHeader(): HBox = {
    val layout = new HBox(20)
    layout.setPadding(new Insets(20))
    layout.setAlignment(Pos.CENTER_LEFT)
    val logo = new ImageView(Images.fromClassPath("/images/lc_banner.jpg"))
    logo.setFitHeight(46)
    logo.setPreserveRatio(true)
    layout.getChildren.add(logo)

    val newEditButton = new Button("New", Icons.fromImage("/icons/new_edit.png"))
    newEditButton.setTooltip(new Tooltip("Start A New Configuration Edit"))
    newEditButton.setOnAction(_ => {
      confirmBeforeClearWorkingCopy()
      assemblePlist() // refresh
    })
    layout.getChildren.add(newEditButton)

    val loadFromTemplateButton = new Button("New from service template", Icons.fromImage("/icons/load_from_template.png"))
    loadFromTemplateButton.setTooltip(new Tooltip("Load From Template"))
    loadFromTemplateButton.setOnAction(_ => {
      confirmBeforeClearWorkingCopy()
      loadFromTemplateTask.apply(false)
    })
    layout.getChildren.add(loadFromTemplateButton)

    val loadCalendarScheduleTemplateButton = new Button("New from schedule template", Icons.fromImage("/icons/calendar-load.png"))
    loadCalendarScheduleTemplateButton.setTooltip(new Tooltip("Load Calendar Schedule Template Compose"))
    loadCalendarScheduleTemplateButton.setOnAction(_ => {
      confirmBeforeClearWorkingCopy()
      loadFromTemplateTask(true)
    })
    layout.getChildren.add(loadCalendarScheduleTemplateButton)

    val copyButton = new Button("Copy to clipboard", Icons.fromImage("/icons/copy_to_clipboard.png"))
    copyButton.setTooltip(new Tooltip("Copy to Clipboard \n(Shortcut Key also Available)"))

    //    Keys.on(, new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN))(copyAction)
    copyButton.setOnAction(_ => copyAction.run())
    layout.getChildren.addAll(Paddings.hPadding(), copyButton)

    layout
  }

  private def confirmBeforeClearWorkingCopy(): Unit = {
    if (!composerList.isEmpty()) {
      if (Alerts.confirm("discard current working copy?!")) {
        composerList.clearList()
      }
    }
  }

  private def addNodeWithInterceptor(nodeType: String): Unit = {
    composerList.addToList(nodeCreators.get(nodeType).call())
    // custom node can be added multiple times, so ignore to keep it in cache
    if (!StringUtils.equalsIgnoreCase(nodeType, LaunchdConfigKeys.Custom.value())) {
      nodeEditCache.add(nodeType)
    }
    assemblePlist()
  }

  private def createNode(node: Node): Node = {
    node.asInstanceOf[PlistxmlGenerator].addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = assemblePlist()
    })
    node.asInstanceOf[CloseActionable].setOnClose(_ => {
      composerList.getChildren.remove(node)
      if (node.isInstanceOf[CustomKeyValueNode]) {
        nodeEditCache.remove(node.asInstanceOf[CustomKeyValueNode].keyName)
      }
      assemblePlist()
    })
    node.asInstanceOf[Node]
  }

  /**
   * several time points we should refresh the generated result:
   * 1. when node added to composer
   * 2. when node removed from composer
   * 3. when node content changed.
   */
  private def assemblePlist(): Unit = {
    val plistXmlBuilder = new java.lang.StringBuilder
    plistXmlBuilder.append(
      """<?xml version="1.0" encoding="UTF-8"?>
        |<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
        |<plist version="1.0">
        |<dict>
        |""".stripMargin)
    composerList.getItems().forEach(node => plistXmlBuilder.append(node.asInstanceOf[PlistxmlGenerator].toPlistXml()))
    plistXmlBuilder.append(
      """</dict>
        |</plist>
        |""".stripMargin)
    plistXmlDisplay.setText(plistXmlBuilder.toString)
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