package com.keevol.launchctl.generator.controls

import com.keevol.javafx.utils.{DnD, Labels}
import javafx.geometry.{Insets, Pos}
import javafx.scene.{Cursor, Node}
import javafx.scene.image.ImageView
import javafx.scene.input.{ClipboardContent, Dragboard, TransferMode}
import javafx.scene.layout.VBox

/**
 * {{{
 *   _                                    _
 *  | |                                  | |
 *  | | __   ___    ___  __   __   ___   | |
 *  | |/ /  / _ \  / _ \ \ \ / /  / _ \  | |
 *  |   <  |  __/ |  __/  \ V /  | (_) | | |
 *  |_|\_\  \___|  \___|   \_/    \___/  |_|
 * }}}
 *
 * KEEp eVOLution!
 *
 * @author fq@keevol.com
 * @since 2017.5.12
 *
 *        Copyright 2017 © 杭州福强科技有限公司版权所有
 *        [[https://www.keevol.com]]
 */

class KVTemplateNode(graphic: Node, label: String) extends VBox {
  setPadding(new Insets(5))
  setSpacing(7)
  setAlignment(Pos.CENTER)

  getStyleClass.add("kv-template-node")

  getChildren.addAll(graphic, Labels.default(label))

  DnD.onDrag(this) { dragboard =>
    val content = new ClipboardContent()
    content.putString(label)
    dragboard.setContent(content)
    dragboard.setDragView(graphic.asInstanceOf[ImageView].getImage)
  }
}




