package com.mallowigi.imageicon

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

class ImageIconIndex : FileBasedIndexExtension<String, String>() {
    private val myIndexer: DataIndexer<String, String, FileContent> = ImageIconIndexer()

    private val myValueExternalizer: DataExternalizer<String> = object : DataExternalizer<String> {
        override fun save(out: DataOutput, value: String) {
            // Convert string to bytes
            val bytes = value.toByteArray(StandardCharsets.UTF_8)
            // Write length of byte array
            out.writeInt(bytes.size)
            // Write byte array
            out.write(bytes)
        }

        override fun read(input: DataInput): String {
            // Read length of byte array
            val length = input.readInt()
            // Read the bytes
            val bytes = ByteArray(length)
            input.readFully(bytes)
            // Convert back to a string
            return String(bytes, StandardCharsets.UTF_8)
        }
    }

    private val myInputFilter = FileBasedIndex.InputFilter { file: VirtualFile ->
        isValidImagePath(file) && file.extension in ImageConverterFactory.SUPPORTED_EXTENSIONS
    }

    override fun getName(): ID<String, String> = NAME

    override fun getInputFilter(): FileBasedIndex.InputFilter = myInputFilter

    override fun dependsOnFileContent(): Boolean = false

    override fun getIndexer(): DataIndexer<String, String, FileContent> = myIndexer

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): DataExternalizer<String> = myValueExternalizer

    override fun getVersion(): Int = VERSION

    internal class ImageIconIndexer : DataIndexer<String, String, FileContent> {
        override fun map(inputData: FileContent): MutableMap<String, String> {
            // val project = inputData.psiFile.project
            // if (DumbService.isDumb(project)) return Collections.emptyMap()

            val file = inputData.file
            when {
                !isValidImagePath(file)                                       -> return Collections.emptyMap()
                file.isDirectory                                              -> return Collections.emptyMap()
                file.extension !in ImageConverterFactory.SUPPORTED_EXTENSIONS -> return Collections.emptyMap()
                else                                                          -> {
                    val converter = ImageConverterFactory.create(file.name) ?: return Collections.emptyMap()

                    val base64: String? = try {
                        val imageWrapper = converter.getImageWrapper(file) ?: return Collections.emptyMap()
                        converter.toBase64(imageWrapper)
                    } catch (e: IOException) {
                        thisLogger().warn(e.message)
                        null
                    }

                    return base64?.let { mutableMapOf(file.path to it) } ?: Collections.emptyMap()
                }
            }
        }
    }

    @Suppress("CompanionObjectInExtension")
    companion object {
        val NAME = ID.create<String, String>("com.mallowigi.imageicon.imageIndex")
        const val VERSION = 1

        private fun isValidImagePath(virtualFile: VirtualFile): Boolean {
            val canonicalPath = virtualFile.canonicalFile?.canonicalPath
            return canonicalPath != null && !canonicalPath.contains(".jar")
        }
    }
}
