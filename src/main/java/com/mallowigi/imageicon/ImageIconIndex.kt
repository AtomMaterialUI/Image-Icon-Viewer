package com.mallowigi.imageicon

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import javax.imageio.ImageIO.read
import javax.imageio.ImageIO.write
import javax.swing.Icon
import javax.swing.ImageIcon

class ImageIconIndex : FileBasedIndexExtension<String, Icon>() {
    private val myIndexer: DataIndexer<String, Icon, FileContent> = ImageIconIndexer()

    private val myValueExternalizer: DataExternalizer<Icon> = object : DataExternalizer<Icon> {
        override fun save(out: DataOutput, value: Icon) {
            if (value is ImageIcon && value.image != null) {
                val outputStream = ByteArrayOutputStream()
                try {
                    // Cast value.image to BufferedImage (required for ImageIO)
                    val bufferedImage = value.image as? BufferedImage
                        ?: throw IOException("Icon's image is not a BufferedImage")

                    // Write the image as PNG into the outputStream
                    write(bufferedImage, "PNG", outputStream)

                    // Convert the image bytes to a byte array
                    val imageBytes = outputStream.toByteArray()

                    // Write the byte array length first
                    out.writeInt(imageBytes.size)

                    // Write the actual bytes
                    out.write(imageBytes)
                } catch (e: IOException) {
                    throw IOException("Failed to serialize the Icon", e)
                } finally {
                    outputStream.close()
                }
            } else {
                throw UnsupportedOperationException("Only ImageIcon with BufferedImage is supported for serialization")
            }
        }

        override fun read(input: DataInput): Icon {
            val size = input.readInt() // Read the length of the byte array

            // Read the image bytes
            val imageBytes = ByteArray(size)
            input.readFully(imageBytes)

            // Convert the byte array back to a BufferedImage
            val inputStream = ByteArrayInputStream(imageBytes)
            val bufferedImage = read(inputStream)
                ?: throw IOException("Failed to deserialize Icon image")

            return ImageIcon(bufferedImage)
        }
    }

    private val myInputFilter = FileBasedIndex.InputFilter { file: VirtualFile ->
        isValidImagePath(file) && file.extension in ImageConverterFactory.SUPPORTED_EXTENSIONS
    }

    override fun getName(): ID<String, Icon> = NAME

    override fun getInputFilter(): FileBasedIndex.InputFilter = myInputFilter

    override fun dependsOnFileContent(): Boolean = false

    override fun getIndexer(): DataIndexer<String, Icon, FileContent> = myIndexer

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): DataExternalizer<Icon> = myValueExternalizer

    override fun getVersion(): Int = VERSION

    internal class ImageIconIndexer : DataIndexer<String, Icon, FileContent> {
        override fun map(inputData: FileContent): MutableMap<String, Icon> {
            // val project = inputData.psiFile.project
            // if (DumbService.isDumb(project)) return Collections.emptyMap()

            val file = inputData.file
            when {
                !isValidImagePath(file)                                       -> return Collections.emptyMap()
                file.isDirectory                                              -> return Collections.emptyMap()
                file.extension !in ImageConverterFactory.SUPPORTED_EXTENSIONS -> return Collections.emptyMap()
                else                                                          -> {
                    val converter = ImageConverterFactory.create(file.name) ?: return Collections.emptyMap()

                    val icon: Icon? = try {
                        converter.convert(file, file.canonicalPath)
                    } catch (e: IOException) {
                        thisLogger().warn(e.message)
                        null
                    }

                    return icon?.let { mutableMapOf(file.path to it) } ?: Collections.emptyMap()
                }
            }
        }
    }

    companion object {
        val NAME = ID.create<String, Icon>("com.mallowigi.imageicon.imageIndex")
        const val VERSION = 1

        private fun isValidImagePath(virtualFile: VirtualFile): Boolean {
            val canonicalPath = virtualFile.canonicalFile?.canonicalPath
            return canonicalPath != null && !canonicalPath.contains(".jar")
        }
    }
}
