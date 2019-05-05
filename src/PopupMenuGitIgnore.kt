import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

class PopupMenuGitIgnore : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let {
            val gitIgnorePath = it.basePath + File.separator + ".gitignore"
            val highlightedItems = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext)?.asList()
            val gitIgnoreFile = File(gitIgnorePath)
            if (!gitIgnoreFile.exists()) {
                createGitIgnoreFile(gitIgnoreFile)
            }
            writeHighlightedItemsToGitIgnore(gitIgnorePath, highlightedItems)
        }
    }

    private fun createGitIgnoreFile(gitIgnoreFile: File) {
        val application = ApplicationManager.getApplication()
        application.runWriteAction {
            try {
                gitIgnoreFile.createNewFile()
            } catch (ioex: IOException) {
                Messages.showErrorDialog(ioex.message, "Could not create .gitignore file")
            }
        }
    }

    private fun writeHighlightedItemsToGitIgnore(gitIgnorePath: String, highlightedItems: List<VirtualFile>?) {
        try {
            BufferedWriter(FileWriter(gitIgnorePath, true)).use { writer ->
                highlightedItems?.forEach { virtualFile ->
                    val gitignoreEntry = getGitignoreEntry(virtualFile)
                    writer.write(gitignoreEntry)
                }
            }
        } catch (ioException: IOException) {
            Messages.showErrorDialog(ioException.message, "Could not access gitignore")
        }

    }

    private fun getGitignoreEntry(highlightedItem: VirtualFile): String {
        return (if (highlightedItem.isDirectory) highlightedItem.presentableName + File.separator else highlightedItem.presentableName) + System.lineSeparator()
    }

    override fun update(e: AnActionEvent) {
        val highlightedItems = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext)?.asList()?.map { it.name }
        highlightedItems?.let {
            if (it.contains(".gitignore"))
                e.presentation.isEnabled = false
            else
                e.presentation.text = "Add " + highlightedItems.size + " to gitignore"
        }
    }
}
