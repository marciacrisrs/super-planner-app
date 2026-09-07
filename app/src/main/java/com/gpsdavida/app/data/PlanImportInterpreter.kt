package com.gpsdavida.app.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

class PlanImportInterpreter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    data class ProposalItem(val title: String, val durationMinutes: Int, val uncertain: Boolean)
    data class Proposal(val displayName: String, val items: List<ProposalItem>, val uncertainReason: String?)

    fun interpret(uri: Uri): Proposal {
        val name = uri.toString().substringAfterLast('/').ifBlank { "Documento importado" }
        val mime = context.contentResolver.getType(uri).orEmpty()
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return Proposal(name, emptyList(), "Não foi possível ler o arquivo.")
        val text = when {
            mime == "text/plain" -> bytes.toString(Charsets.UTF_8)
            mime.contains("wordprocessingml") -> extractDocx(bytes)
            mime == "application/pdf" -> extractSimplePdf(bytes)
            else -> ""
        }
        val lines = text.lines()
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.length >= 3 }
            .distinct()
            .take(30)
        val items = lines.map { line ->
            val duration = Regex("(\\d{1,3})\\s*(min|mins|minutos)", RegexOption.IGNORE_CASE)
                .find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 30
            ProposalItem(line.take(120), duration, duration == 30)
        }
        return Proposal(
            name,
            items,
            if (text.isBlank()) "Não foi possível extrair texto automaticamente; revise o documento manualmente." else null,
        )
    }

    private fun extractDocx(bytes: ByteArray): String =
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == "word/document.xml") {
                    return zip.readBytes().toString(Charsets.UTF_8).replace(Regex("<[^>]+>"), " ")
                }
            }
            ""
        }

    private fun extractSimplePdf(bytes: ByteArray): String {
        val raw = bytes.toString(Charsets.ISO_8859_1)
        return Regex("\\(([^()]*)\\)\\s*Tj").findAll(raw).joinToString(" ") { it.groupValues[1] }
    }
}
