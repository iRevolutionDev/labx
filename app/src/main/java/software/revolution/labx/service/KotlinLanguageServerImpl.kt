package software.revolution.labx.service

import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.DocumentRangeFormattingParams
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.FoldingRange
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import software.revolution.labx.util.logInfo
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

/**
 * **TEMPORARY KOTLIN LANGUAGE SERVER IMPLEMENTATION**
 */
class KotlinLanguageServerImpl : LanguageServer, LanguageClientAware {

    private var client: LanguageClient? = null
    private val textDocumentService = KotlinTextDocumentService(this)
    private val workspaceService = KotlinWorkspaceService()

    private val documents = mutableMapOf<String, String>()

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
        logInfo("Kotlin Language Server initializing...")

        val capabilities = ServerCapabilities()
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full)

        val completionOptions = CompletionOptions()
        completionOptions.triggerCharacters = listOf(".", "@", ":", "$")
        completionOptions.resolveProvider = true
        capabilities.completionProvider = completionOptions

        capabilities.setHoverProvider(true)
        capabilities.setDocumentFormattingProvider(true)
        capabilities.setDocumentSymbolProvider(true)
        capabilities.setDefinitionProvider(true)

        val result = InitializeResult(capabilities)
        return CompletableFuture.completedFuture(result)
    }

    override fun initialized(params: InitializedParams) {
        logInfo("Kotlin Language Server initialized")
    }

    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }

    override fun getWorkspaceService(): WorkspaceService {
        return workspaceService
    }

    override fun connect(client: LanguageClient) {
        this.client = client
        logInfo("Kotlin Language Server connected to client")
    }

    override fun shutdown(): CompletableFuture<Any> {
        logInfo("Kotlin Language Server shutting down")
        return CompletableFuture.completedFuture(null)
    }

    override fun exit() {
        logInfo("Kotlin Language Server exiting")
    }

    fun addDocument(uri: String, content: String) {
        documents[uri] = content
        analyzeDocument(uri, content)
    }

    fun updateDocument(uri: String, content: String) {
        documents[uri] = content
        analyzeDocument(uri, content)
    }

    fun removeDocument(uri: String) {
        documents.remove(uri)
        client?.publishDiagnostics(PublishDiagnosticsParams(uri, emptyList()))
    }

    fun getDocumentContent(uri: String): String? {
        return documents[uri]
    }

    /**
     * Basic analysis of a Kotlin document to provide diagnostics
     */
    private fun analyzeDocument(uri: String, content: String) {
        val diagnostics = mutableListOf<Diagnostic>()

        val lines = content.lines()

        var openBraces = 0
        var openParens = 0

        lines.forEachIndexed { lineIndex, line ->
            openBraces += line.count { it == '{' } - line.count { it == '}' }
            openParens += line.count { it == '(' } - line.count { it == ')' }

            if (line.contains("var") || line.contains("val")) {
                if (line.contains("=") && !line.contains(":") && line.contains(".") &&
                    !line.contains("\"") && !line.contains("'")
                ) {
                    val diagnostic = Diagnostic(
                        Range(
                            Position(lineIndex, 0),
                            Position(lineIndex, line.length)
                        ),
                        "Consider adding explicit type for this expression",
                        DiagnosticSeverity.Warning,
                        "kotlin"
                    )
                    diagnostics.add(diagnostic)
                }
            }
        }

        if (openBraces > 0) {
            val diagnostic = Diagnostic(
                Range(
                    Position(lines.size - 1, 0),
                    Position(lines.size - 1, 0)
                ),
                "Unclosed braces in document: $openBraces",
                DiagnosticSeverity.Error,
                "kotlin"
            )
            diagnostics.add(diagnostic)
        }

        if (openParens > 0) {
            val diagnostic = Diagnostic(
                Range(
                    Position(lines.size - 1, 0),
                    Position(lines.size - 1, 0)
                ),
                "Unclosed parentheses in document: $openParens",
                DiagnosticSeverity.Error,
                "kotlin"
            )
            diagnostics.add(diagnostic)
        }

        client?.publishDiagnostics(PublishDiagnosticsParams(uri, diagnostics))
    }

    /**
     * Text document service implementation for Kotlin
     */
    inner class KotlinTextDocumentService(private val server: KotlinLanguageServerImpl) :
        TextDocumentService {

        override fun didOpen(params: DidOpenTextDocumentParams) {
            val document = params.textDocument
            server.addDocument(document.uri, document.text)
        }

        override fun didChange(params: DidChangeTextDocumentParams) {
            val uri = params.textDocument.uri
            val changes = params.contentChanges
            if (changes.isNotEmpty()) {
                server.updateDocument(uri, changes[0].text)
            }
        }

        override fun didClose(params: DidCloseTextDocumentParams) {
            val uri = params.textDocument.uri
            server.removeDocument(uri)
        }

        override fun didSave(params: DidSaveTextDocumentParams) {
            val uri = params.textDocument.uri
            params.text?.let { text ->
                server.updateDocument(uri, text)
            }
        }

        override fun completion(params: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
            val uri = params.textDocument.uri
            val content =
                server.getDocumentContent(uri) ?: return CompletableFuture.completedFuture(
                    Either.forLeft(emptyList())
                )

            val position = params.position
            val line = content.lines().getOrNull(position.line) ?: ""
            val prefix = line.substring(0, position.character)

            val items = mutableListOf<CompletionItem>()

            if (prefix.endsWith(".")) {
                addKotlinMethodCompletions(items)
            } else if (prefix.endsWith("::")) {
                addKotlinMemberReferenceCompletions(items)
            } else {
                addKotlinKeywordCompletions(items)
                addKotlinTypeCompletions(items)
            }

            return CompletableFuture.completedFuture(Either.forLeft(items))
        }

        private fun addKotlinMethodCompletions(items: MutableList<CompletionItem>) {
            val methods = listOf(
                "toString()", "equals(other: Any?)", "hashCode()",
                "apply {}", "also {}", "let {}", "run {}", "with(obj) {}"
            )

            methods.forEach { method ->
                val item = CompletionItem(method)
                item.kind = CompletionItemKind.Method
                items.add(item)
            }

            val stringMethods = listOf(
                "length", "isEmpty()", "isNotEmpty()", "substring(startIndex: Int)",
                "substring(startIndex: Int, endIndex: Int)",
                "indexOf(string: String)", "lastIndexOf(string: String)",
                "replace(oldChar: Char, newChar: Char)", "trim()",
                "lowercase()", "uppercase()", "split(delimiter: String)"
            )

            stringMethods.forEach { method ->
                val item = CompletionItem(method)
                item.kind = CompletionItemKind.Method
                items.add(item)
            }

            val collectionMethods = listOf(
                "filter {}", "map {}", "forEach {}", "find {}",
                "firstOrNull {}", "count()", "any {}", "all {}",
                "none {}", "sortedBy {}", "joinToString()"
            )

            collectionMethods.forEach { method ->
                val item = CompletionItem(method)
                item.kind = CompletionItemKind.Method
                items.add(item)
            }
        }

        private fun addKotlinMemberReferenceCompletions(items: MutableList<CompletionItem>) {
            val memberRefs = listOf(
                "class", "javaClass", "toString", "equals", "hashCode"
            )

            memberRefs.forEach { ref ->
                val item = CompletionItem(ref)
                item.kind = CompletionItemKind.Reference
                items.add(item)
            }
        }

        private fun addKotlinTypeCompletions(items: MutableList<CompletionItem>) {
            val types = listOf(
                "String", "Int", "Boolean", "Long", "Double", "Float",
                "List<>", "Map<,>", "Set<>", "MutableList<>", "MutableMap<,>", "MutableSet<>",
                "ArrayList<>", "HashMap<,>", "HashSet<>", "Any", "Nothing",
                "Sequence<>", "Pair<,>", "Triple<,,>", "Array<>", "Function<>"
            )

            types.forEach { type ->
                val item = CompletionItem(type)
                item.kind = CompletionItemKind.Class
                items.add(item)
            }
        }

        private fun addKotlinKeywordCompletions(items: MutableList<CompletionItem>) {
            val keywords = listOf(
                "val", "var", "fun", "class", "object", "interface", "enum", "sealed",
                "data", "inline", "typealias", "suspend", "return", "if", "else", "when",
                "while", "for", "do", "break", "continue", "as", "is", "in", "!in", "by",
                "lazy", "get()", "set()", "init", "companion", "constructor", "override",
                "private", "protected", "public", "internal", "import", "package"
            )

            keywords.forEach { keyword ->
                val item = CompletionItem(keyword)
                item.kind = CompletionItemKind.Keyword
                items.add(item)
            }
        }

        override fun hover(params: HoverParams): CompletableFuture<Hover> {
            val uri = params.textDocument.uri
            val content =
                server.getDocumentContent(uri) ?: return CompletableFuture.completedFuture(null)

            val position = params.position
            val lines = content.lines()
            if (position.line >= lines.size) {
                return CompletableFuture.completedFuture(null)
            }

            val line = lines[position.line]
            val wordRange = findWordAtPosition(line, position.character)
            if (wordRange == null) {
                return CompletableFuture.completedFuture(null)
            }

            val word = line.substring(wordRange.start.character, wordRange.end.character)

            val hoverContent = when (word) {
                "val" -> "**val** - Declares a read-only property or local variable.\n\n" +
                        "```kotlin\nval name: String = \"example\"\n```"

                "var" -> "**var** - Declares a mutable property or local variable.\n\n" +
                        "```kotlin\nvar count: Int = 0\n```"

                "fun" -> "**fun** - Declares a function.\n\n" +
                        "```kotlin\nfun functionName(param: Type): ReturnType { ... }\n```"

                "class" -> "**class** - Declares a class.\n\n" +
                        "```kotlin\nclass ClassName(param: Type) { ... }\n```"

                "object" -> "**object** - Declares a singleton object.\n\n" +
                        "```kotlin\nobject ObjectName { ... }\n```"

                "interface" -> "**interface** - Declares an interface.\n\n" +
                        "```kotlin\ninterface InterfaceName { ... }\n```"

                "data" -> "**data** - Creates a data class with automatically generated equals(), hashCode(), toString(), and copy() methods.\n\n" +
                        "```kotlin\ndata class User(val name: String, val age: Int)\n```"

                "suspend" -> "**suspend** - Marks a function or lambda as suspending (can be used with coroutines).\n\n" +
                        "```kotlin\nsuspend fun doSomething() { ... }\n```"

                else -> null
            }

            if (hoverContent != null) {
                val hover = Hover()
                hover.contents = Either.forRight(MarkupContent("markdown", hoverContent))
                hover.range = wordRange
                return CompletableFuture.completedFuture(hover)
            }

            return CompletableFuture.completedFuture(null)
        }

        private fun findWordAtPosition(line: String, character: Int): Range? {
            if (character >= line.length || character < 0) {
                return null
            }

            var start = character
            while (start > 0 && isPartOfIdentifier(line[start - 1])) {
                start--
            }

            var end = character
            while (end < line.length && isPartOfIdentifier(line[end])) {
                end++
            }

            if (start == end) {
                return null
            }

            return Range(
                Position(0, start),
                Position(0, end)
            )
        }

        private fun isPartOfIdentifier(c: Char): Boolean {
            return c.isLetterOrDigit() || c == '_'
        }

        override fun formatting(params: DocumentFormattingParams): CompletableFuture<List<TextEdit>> {
            val uri = params.textDocument.uri
            val content = server.getDocumentContent(uri)
                ?: return CompletableFuture.completedFuture(emptyList())

            val lines = content.lines()
            val formattedLines = mutableListOf<String>()

            var indentLevel = 0

            lines.forEach { line ->
                val trimmedLine = line.trim()

                if (trimmedLine.startsWith("}") || trimmedLine.startsWith(")")) {
                    indentLevel = (indentLevel - 1).coerceAtLeast(0)
                }

                val indent = "    ".repeat(indentLevel)
                formattedLines.add(indent + trimmedLine)

                if (trimmedLine.endsWith("{")) {
                    indentLevel++
                }
            }

            val formattedText = formattedLines.joinToString("\n")

            val textEdit = TextEdit(
                Range(
                    Position(0, 0),
                    Position(lines.size, 0)
                ),
                formattedText
            )

            return CompletableFuture.completedFuture(listOf(textEdit))
        }

        override fun documentSymbol(params: DocumentSymbolParams): CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> {
            val uri = params.textDocument.uri
            val content = server.getDocumentContent(uri)
                ?: return CompletableFuture.completedFuture(emptyList())

            val symbols = mutableListOf<Either<SymbolInformation, DocumentSymbol>>()

            val lines = content.lines()
            val classPattern =
                Pattern.compile("\\s*(data)?\\s*(class|object|interface|enum class)\\s+([A-Za-z0-9_]+)")
            val functionPattern = Pattern.compile("\\s*(fun)\\s+([A-Za-z0-9_]+)\\s*\\(")
            val propertyPattern =
                Pattern.compile("\\s*(val|var)\\s+([A-Za-z0-9_]+)\\s*(?::\\s*([A-Za-z0-9_<>]+))?")

            for (i in lines.indices) {
                val line = lines[i]

                val classMatcher = classPattern.matcher(line)
                if (classMatcher.find()) {
                    val classType = classMatcher.group(2) ?: "class"
                    val className = classMatcher.group(3)

                    val kind = when (classType) {
                        "interface" -> SymbolKind.Interface
                        "object" -> SymbolKind.Class
                        "enum class" -> SymbolKind.Enum
                        else -> SymbolKind.Class
                    }

                    val classSymbol = DocumentSymbol(
                        className,
                        kind,
                        Range(
                            Position(i, classMatcher.start(3)),
                            Position(i, classMatcher.end(3))
                        ),
                        Range(
                            Position(i, classMatcher.start(3)),
                            Position(i, classMatcher.end(3))
                        ),
                        null,
                        mutableListOf()
                    )

                    symbols.add(Either.forRight(classSymbol))
                }

                val functionMatcher = functionPattern.matcher(line)
                if (functionMatcher.find()) {
                    val functionName = functionMatcher.group(2)

                    val functionSymbol = DocumentSymbol(
                        functionName,
                        SymbolKind.Function,
                        Range(
                            Position(i, functionMatcher.start(2)),
                            Position(i, functionMatcher.end(2))
                        ),
                        Range(
                            Position(i, functionMatcher.start(2)),
                            Position(i, functionMatcher.end(2))
                        ),
                        null,
                        mutableListOf()
                    )

                    symbols.add(Either.forRight(functionSymbol))
                }

                val propertyMatcher = propertyPattern.matcher(line)
                if (propertyMatcher.find()) {
                    val propertyName = propertyMatcher.group(2)
                    val propertyType = propertyMatcher.group(3) ?: ""
                    val propertySymbol = DocumentSymbol(
                        propertyName,
                        SymbolKind.Property,
                        Range(
                            Position(i, propertyMatcher.start(2)),
                            Position(i, propertyMatcher.end(2))
                        ),
                        Range(
                            Position(i, propertyMatcher.start(2)),
                            Position(i, propertyMatcher.end(2))
                        ),
                        propertyType,
                        mutableListOf()
                    )

                    symbols.add(Either.forRight(propertySymbol))
                }
            }

            return CompletableFuture.completedFuture(symbols)
        }

        override fun definition(position: DefinitionParams?): CompletableFuture<Either<List<Location>, List<LocationLink>>> {
            return CompletableFuture.completedFuture(Either.forLeft(emptyList()))
        }

        override fun references(params: ReferenceParams?): CompletableFuture<List<Location>> {
            return CompletableFuture.completedFuture(emptyList())
        }

        override fun codeAction(params: CodeActionParams?): CompletableFuture<List<Either<Command, CodeAction>>> {
            return CompletableFuture.completedFuture(emptyList())
        }

        override fun documentHighlight(params: DocumentHighlightParams?): CompletableFuture<List<DocumentHighlight>> {
            return CompletableFuture.completedFuture(emptyList())
        }

        override fun rangeFormatting(params: DocumentRangeFormattingParams?): CompletableFuture<List<TextEdit>> {
            return CompletableFuture.completedFuture(emptyList())
        }

        override fun onTypeFormatting(params: DocumentOnTypeFormattingParams?): CompletableFuture<List<TextEdit>> {
            return CompletableFuture.completedFuture(emptyList())
        }

        override fun rename(params: RenameParams?): CompletableFuture<WorkspaceEdit> {
            return CompletableFuture.completedFuture(WorkspaceEdit())
        }

        //        override fun prepareRename(params: org.eclipse.lsp4j.PrepareRenameParams?): CompletableFuture<Either<org.eclipse.lsp4j.Range, org.eclipse.lsp4j.PrepareRenameResult>> {
//            return CompletableFuture.completedFuture(null)
//        }
//
        override fun foldingRange(params: FoldingRangeRequestParams?): CompletableFuture<List<FoldingRange>> {
            return CompletableFuture.completedFuture(emptyList())
        }
    }

    /**
     * Workspace service implementation for Kotlin
     */
    inner class KotlinWorkspaceService : WorkspaceService {
        override fun didChangeConfiguration(params: DidChangeConfigurationParams) {}

        override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams) {}

        override fun executeCommand(params: ExecuteCommandParams?): CompletableFuture<Any> {
            return CompletableFuture.completedFuture(null)
        }

//        override fun symbol(params: org.eclipse.lsp4j.WorkspaceSymbolParams?): CompletableFuture<List<SymbolInformation>> {
//            return CompletableFuture.completedFuture(emptyList())
//        }
    }
}