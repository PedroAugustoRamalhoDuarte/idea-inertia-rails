package inertiarails

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons.FileTypes.Any_type
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.awt.event.MouseEvent

import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallImpl

val INERTIA_PAGES_ROOT = "app/frontend/pages"

class ControllerLineMarkerProvider : LineMarkerProvider, GutterIconNavigationHandler<PsiElement> {
  override fun getLineMarkerInfo(element: PsiElement) =
      if ((element is RCallImpl) && element.text.contains("render inertia:")) {
        LineMarkerInfo(
            element,
            element.textRange,
            Any_type,
            { "Find usages of this page" },
            this,
            GutterIconRenderer.Alignment.CENTER,
            { "Find usages of this page" }
        )
      } else {
        null
      }


  override fun navigate(e: MouseEvent, psiFile: PsiElement) {
    if (DumbService.getInstance(psiFile.project).isDumb) {
      return
    }

    val inertiaPageName = psiFile.text.split("render inertia:")[1].split(",")[0].trim(' ', '"', '\'')

    val directory = inertiaPageName.substringBeforeLast('/')
    val filename = inertiaPageName.substringAfterLast('/')

    // TODO: Support other files types
    val files = FilenameIndex.getVirtualFilesByName("$filename.jsx", GlobalSearchScope.allScope(psiFile.project))

    if (files.isNotEmpty()) {
      val matchingFile = files.firstOrNull { it.parent?.path?.endsWith(directory) == true }

      if (matchingFile != null) {
        val fileEditorManager = FileEditorManager.getInstance(psiFile.project)
        fileEditorManager.openFile(matchingFile, true)
      } else {
        // Handle case where file in specified directory was not found
      }
    }
  }
}