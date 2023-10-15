package com.keevol.launchctl.generator

import com.keevol.javafx.KFXApplication
import com.keevol.javafx.controls.launchctl._
import com.keevol.javafx.controls.{CloseActionable, KList, KStatusBar, KTaskSpinner}
import com.keevol.javafx.utils._
import com.keevol.launchctl.generator.utils.KVTemplateNodes._
import com.keevol.utils.Files
import fr.brouillard.oss.cssfx.CSSFX
import io.vertx.core.impl.ConcurrentHashSet
import javafx.concurrent.Task
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Button, Hyperlink, SplitPane, Tooltip}
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

  val lcLabelNodeTemplate = createNode(LaunchdConfigKeys.Label.value())
  val lcRunAtLoadNodeTemplate = createNode(LaunchdConfigKeys.RunAtLoad.value())
  val lcKeepAliveNodeTemplate = createNode(LaunchdConfigKeys.KeepAlive.value())
  val lcProgramNodeTemplate = createNode(LaunchdConfigKeys.Program.value())
  val lcProgramArgumentsNodeTemplate = createNode(LaunchdConfigKeys.ProgramArgs.value())
  val lcWorkingDirNodeTemplate = createNode(LaunchdConfigKeys.WorkingDirectory.value())
  val lcUsernameNodeTemplate = createNode(LaunchdConfigKeys.Username.value())
  val lcOutPathNodeTemplate = createNode(LaunchdConfigKeys.StandardOutputPath.value())
  val lcErrPathNodeTemplate = createNode(LaunchdConfigKeys.StandardErrorPath.value())
  val lcManualEditNodeTemplate = createNode(LaunchdConfigKeys.Custom.value())

  val nodeEditCache: ConcurrentHashSet[String] = new ConcurrentHashSet[String]()

  val composerList = new KList("Drop Node Below To Compose", new Insets(20))
  composerList.setSpacing(20)
  composerList.setPadding(new Insets(20))

  val nodeCloseAction = (node: Node) => composerList.getChildren.remove(node)

  val nodeCreators = new ConcurrentHashMap[String, Callable[Node]]()
  nodeCreators.put(LaunchdConfigKeys.Label.value(), () => removeOnClose(new LabelNode("")))
  nodeCreators.put(LaunchdConfigKeys.RunAtLoad.value(), () => removeOnClose(new RunAtLoadNode()))
  nodeCreators.put(LaunchdConfigKeys.KeepAlive.value(), () => removeOnClose(new KeepAliveNode()))
  nodeCreators.put(LaunchdConfigKeys.Program.value(), () => removeOnClose(new ProgramNode("")))
  nodeCreators.put(LaunchdConfigKeys.ProgramArgs.value(), () => removeOnClose(new ProgramArgumentsNode(Array[String]())))
  nodeCreators.put(LaunchdConfigKeys.WorkingDirectory.value(), () => removeOnClose(new WorkingDirectoryNode("")))
  nodeCreators.put(LaunchdConfigKeys.Username.value(), () => removeOnClose(new UserNameNode("")))
  nodeCreators.put(LaunchdConfigKeys.StandardOutputPath.value(), () => removeOnClose(new StandardOutPathNode("")))
  nodeCreators.put(LaunchdConfigKeys.StandardErrorPath.value(), () => removeOnClose(new StandardErrorPathNode("")))
  nodeCreators.put(LaunchdConfigKeys.Custom.value(), () => removeOnClose(new CustomEditNode()))

  val loadFromTemplateTask = () => {
    composerList.clearList()
    nodeEditCache.clear()
    addNodeWithInterceptor(LaunchdConfigKeys.Label.value())
    addNodeWithInterceptor(LaunchdConfigKeys.RunAtLoad.value())
    addNodeWithInterceptor(LaunchdConfigKeys.KeepAlive.value())
    addNodeWithInterceptor(LaunchdConfigKeys.Program.value())
    addNodeWithInterceptor(LaunchdConfigKeys.ProgramArgs.value())
    addNodeWithInterceptor(LaunchdConfigKeys.WorkingDirectory.value())
    addNodeWithInterceptor(LaunchdConfigKeys.Username.value())
    addNodeWithInterceptor(LaunchdConfigKeys.StandardOutputPath.value())
    addNodeWithInterceptor(LaunchdConfigKeys.StandardErrorPath.value())
  }


  val copyAction = new Runnable {
    override def run(): Unit = {
      // TODO copy plist content to clipboard
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
      lcManualEditNodeTemplate
    )
    ScrollPanes.wrap(listBox)
  }

  def layoutMainZone(): Node = {
    val splitPane = new SplitPane()

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
        } else {
          PopMessage.show(s"no node creator for ${nodeType}", splitPane.getScene)
        }
        true
      }
    }

    val rightLayout = new StackPane()
    rightLayout.getChildren.add(Labels.title("plist Preview ZONE"))
    splitPane.getItems.addAll(composerList.putInScrollPane(), rightLayout)
    splitPane.setDividerPositions(0.5f, 0.5f)

    splitPane
  }

  private def layoutFooter(): Node = {
    val creditLabel = new Hyperlink("©福强出品")
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

    val newEditButton = new Button("", Icons.fromImage("/icons/new_edit.png"))
    newEditButton.setTooltip(new Tooltip("Start A New Configuration Edit"))
    newEditButton.setOnAction(_ => {
      composerList.clearList()
    })
    layout.getChildren.add(newEditButton)

    val loadFromTemplateButton = new Button("", Icons.fromImage("/icons/load_from_template.png"))
    loadFromTemplateButton.setTooltip(new Tooltip("Load From Template"))
    loadFromTemplateButton.setOnAction(_ => {
      loadFromTemplateTask.apply()
    })
    layout.getChildren.add(loadFromTemplateButton)

    val copyButton = new Button("", Icons.fromImage("/icons/copy_to_clipboard.png"))
    copyButton.setTooltip(new Tooltip("Copy to Clipboard \n(Shortcut Key also Available)"))

    //    Keys.on(, new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN))(copyAction)
    copyButton.setOnAction(_ => copyAction.run())
    layout.getChildren.addAll(Paddings.hPadding(), copyButton)

    layout
  }

  private def addNodeWithInterceptor(nodeType: String): Unit = {
    composerList.addToList(nodeCreators.get(nodeType).call())
    // custom node can be added multiple times, so ignore to keep it in cache
    if (!StringUtils.equalsIgnoreCase(nodeType, LaunchdConfigKeys.Custom.value())) {
      nodeEditCache.add(nodeType)
    }
  }

  private def removeOnClose(node: CloseActionable): Node = {
    node.setOnClose(_ => {
      composerList.getChildren.remove(node)
      if (node.isInstanceOf[CustomKeyValueNode]) {
        nodeEditCache.remove(node.asInstanceOf[CustomKeyValueNode].keyName)
      }
    })
    node.asInstanceOf[Node]
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