package com.paroledevie.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Typeface
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val books = listOf(
        "Genèse", "Exode", "Lévitique", "Nombres", "Deutéronome", "Josué", "Juges", "Ruth",
        "1 Samuel", "2 Samuel", "1 Rois", "2 Rois", "1 Chroniques", "2 Chroniques", "Esdras",
        "Néhémie", "Esther", "Job", "Psaumes", "Proverbes", "Ecclésiaste", "Cantique des cantiques",
        "Ésaïe", "Jérémie", "Lamentations", "Ézéchiel", "Daniel", "Osée", "Joël", "Amos",
        "Abdias", "Jonas", "Michée", "Nahum", "Habacuc", "Sophonie", "Aggée", "Zacharie",
        "Malachie", "Matthieu", "Marc", "Luc", "Jean", "Actes", "Romains", "1 Corinthiens",
        "2 Corinthiens", "Galates", "Éphésiens", "Philippiens", "Colossiens", "1 Thessaloniciens",
        "2 Thessaloniciens", "1 Timothée", "2 Timothée", "Tite", "Philémon", "Hébreux", "Jacques",
        "1 Pierre", "2 Pierre", "1 Jean", "2 Jean", "3 Jean", "Jude", "Apocalypse"
    )
    private val chapterCounts = intArrayOf(
        50,40,27,36,34,24,21,4,31,24,22,25,29,36,10,13,10,42,150,31,12,8,66,52,5,48,12,
        14,3,9,1,4,7,3,3,3,2,14,4,28,16,24,21,28,16,16,13,6,6,4,4,5,3,6,4,3,1,13,5,5,3,5,1,1,1,22
    )

    private var bookIndex = 42
    private var chapter = 3
    private lateinit var page: LinearLayout
    private lateinit var heading: TextView
    private lateinit var content: LinearLayout
    private lateinit var favoriteButton: Button
    private val prefs by lazy { getSharedPreferences("parole_de_vie", MODE_PRIVATE) }

    // Quelques extraits publics servent uniquement à tester l'interface.
    // Ce n'est pas le texte PDV et ce n'est pas une Bible complète.
    private val demoVerses = mapOf(
        "Genèse|1" to listOf(
            "1|Au commencement, Dieu créa les cieux et la terre.",
            "2|La terre était informe et vide; il y avait des ténèbres à la surface de l'abîme, et l'esprit de Dieu se mouvait au-dessus des eaux.",
            "3|Dieu dit: Que la lumière soit! Et la lumière fut.",
            "4|Dieu vit que la lumière était bonne; et Dieu sépara la lumière d'avec les ténèbres."
        ),
        "Psaumes|23" to listOf(
            "1|L'Éternel est mon berger: je ne manquerai de rien.",
            "2|Il me fait reposer dans de verts pâturages, Il me dirige près des eaux paisibles.",
            "3|Il restaure mon âme, Il me conduit dans les sentiers de la justice, à cause de son nom."
        ),
        "Jean|3" to listOf(
            "16|Car Dieu a tant aimé le monde qu'il a donné son Fils unique, afin que quiconque croit en lui ne périsse point, mais qu'il ait la vie éternelle.",
            "17|Dieu, en effet, n'a pas envoyé son Fils dans le monde pour qu'il juge le monde, mais pour que le monde soit sauvé par lui."
        ),
        "Matthieu|5" to listOf(
            "3|Heureux les pauvres en esprit, car le royaume des cieux est à eux!",
            "4|Heureux les affligés, car ils seront consolés!",
            "9|Heureux ceux qui procurent la paix, car ils seront appelés fils de Dieu!"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 22, 20, 24)
            setBackgroundColor(Color.rgb(250, 248, 242))
        }

        val title = TextView(this).apply {
            text = "Parole de Vie"
            textSize = 29f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(34, 82, 60))
            gravity = Gravity.CENTER
        }
        page.addView(title, fullWidth())

        val subtitle = TextView(this).apply {
            text = "Lire • chercher • garder ses versets"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 18)
            setTextColor(Color.DKGRAY)
        }
        page.addView(subtitle, fullWidth())

        val selectors = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        selectors.addView(makeButton("Livre : ${books[bookIndex]}") { chooseBook() }, LinearLayout.LayoutParams(0, dp(48), 2f))
        selectors.addView(makeButton("Chap. $chapter") { chooseChapter() }, LinearLayout.LayoutParams(0, dp(48), 1f))
        page.addView(selectors, fullWidth())

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(makeButton("🔎 Rechercher") { search() }, LinearLayout.LayoutParams(0, dp(48), 1f))
        actions.addView(makeButton("★ Favoris") { showFavorites() }, LinearLayout.LayoutParams(0, dp(48), 1f))
        page.addView(actions, fullWidth())

        heading = TextView(this).apply {
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(34, 82, 60))
            setPadding(0, 22, 0, 8)
        }
        page.addView(heading, fullWidth())

        val warning = TextView(this).apply {
            text = "MODE DÉMONSTRATION — extraits bibliques provisoires, pas le texte PDV complet."
            textSize = 12f
            setTextColor(Color.rgb(130, 75, 20))
            setPadding(10, 10, 10, 10)
            setBackgroundColor(Color.rgb(255, 238, 205))
        }
        page.addView(warning, fullWidth())

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 12)
        }
        page.addView(content, fullWidth())

        favoriteButton = makeButton("★ Ajouter ce chapitre aux favoris") { toggleFavorite() }
        page.addView(favoriteButton, fullWidth())

        val navigation = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        navigation.addView(makeButton("← Précédent") { moveChapter(-1) }, LinearLayout.LayoutParams(0, dp(48), 1f))
        navigation.addView(makeButton("Suivant →") { moveChapter(1) }, LinearLayout.LayoutParams(0, dp(48), 1f))
        page.addView(navigation, fullWidth())

        val footer = TextView(this).apply {
            text = "Lecture hors ligne des extraits de démonstration. Le texte PDV sera ajouté après validation des droits."
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }
        page.addView(footer, fullWidth())

        val scroll = ScrollView(this).apply { addView(page) }
        setContentView(scroll)
        refreshChapter()
    }

    private fun refreshChapter() {
        val book = books[bookIndex]
        heading.text = "$book $chapter"
        content.removeAllViews()
        val verses = demoVerses["$book|$chapter"]
        if (verses.isNullOrEmpty()) {
            val message = TextView(this).apply {
                text = "Le texte de ce chapitre n'est pas encore intégré dans cette version de démonstration.\n\nLa navigation fonctionne déjà. Pour afficher toute la Bible PDV hors ligne, il faudra intégrer un fichier autorisé pour cette utilisation."
                textSize = 16f
                setLineSpacing(5f, 1f)
                setPadding(4, 8, 4, 20)
                setTextColor(Color.DKGRAY)
            }
            content.addView(message, fullWidth())
        } else {
            verses.forEach { row ->
                val parts = row.split("|", limit = 2)
                val verseNumber = parts[0]
                val verseText = parts.getOrElse(1) { "" }
                val key = "$book|$chapter|$verseNumber"
                val verse = TextView(this).apply {
                    text = "$verseNumber   $verseText" + if (isVerseFavorite(key)) "  ★" else ""
                    textSize = 18f
                    setTextColor(Color.rgb(45, 45, 45))
                    setLineSpacing(5f, 1f)
                    setPadding(8, 12, 8, 12)
                    setOnClickListener { verseActions(key, "$book $chapter:$verseNumber", verseText) }
                }
                content.addView(verse, fullWidth())
                val divider = View(this).apply { setBackgroundColor(Color.rgb(225, 220, 208)) }
                content.addView(divider, LinearLayout.LayoutParams(-1, dp(1)))
            }
        }
        favoriteButton.text = if (isChapterFavorite()) "★ Retirer ce chapitre des favoris" else "☆ Ajouter ce chapitre aux favoris"
    }

    private fun chooseBook() {
        AlertDialog.Builder(this)
            .setTitle("Choisir un livre")
            .setItems(books.toTypedArray()) { _, which ->
                bookIndex = which
                chapter = 1
                render()
            }
            .show()
    }

    private fun chooseChapter() {
        val chapters = (1..chapterCounts[bookIndex]).map { "Chapitre $it" }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Choisir un chapitre")
            .setItems(chapters) { _, which ->
                chapter = which + 1
                refreshChapter()
            }.show()
    }

    private fun moveChapter(direction: Int) {
        chapter += direction
        if (chapter > chapterCounts[bookIndex]) {
            if (bookIndex < books.lastIndex) {
                bookIndex++
                chapter = 1
            } else chapter = chapterCounts[bookIndex]
        } else if (chapter < 1) {
            if (bookIndex > 0) {
                bookIndex--
                chapter = chapterCounts[bookIndex]
            } else chapter = 1
        }
        render()
    }

    private fun search() {
        val input = EditText(this).apply { hint = "Mot, référence ou livre" }
        AlertDialog.Builder(this).setTitle("Rechercher dans les extraits disponibles")
            .setView(input)
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Rechercher") { _, _ ->
                val query = input.text.toString().trim()
                if (query.isEmpty()) {
                    toast("Entre un mot ou une référence.")
                    return@setPositiveButton
                }
                val results = mutableListOf<Pair<String, String>>()
                demoVerses.forEach { (reference, verses) ->
                    verses.forEach { line ->
                        val parts = line.split("|", limit = 2)
                        val referenceParts = reference.split("|")
                        val fullRef = "${referenceParts[0]} ${referenceParts.getOrElse(1) { "1" }}:${parts[0]}"
                        if (line.contains(query, ignoreCase = true) || reference.contains(query, ignoreCase = true)) {
                            results.add(fullRef to parts.getOrElse(1) { "" })
                        }
                    }
                }
                if (results.isEmpty()) {
                    AlertDialog.Builder(this).setTitle("Aucun résultat")
                        .setMessage("Aucun résultat dans les extraits de démonstration. La recherche complète sera disponible avec le texte biblique autorisé.")
                        .setPositiveButton("OK", null).show()
                } else {
                    AlertDialog.Builder(this).setTitle("Résultats : ${results.size}")
                        .setItems(results.map { "${it.first}\n${it.second}" }.toTypedArray()) { _, which ->
                            openReference(results[which].first)
                        }.show()
                }
            }.show()
    }

    private fun openReference(reference: String) {
        val parts = reference.split(" ")
        val book = parts.dropLast(1).joinToString(" ")
        val chapterNumber = parts.lastOrNull()?.substringBefore(":")?.toIntOrNull()
        val index = books.indexOf(book)
        if (index >= 0 && chapterNumber != null) {
            bookIndex = index
            chapter = chapterNumber.coerceIn(1, chapterCounts[index])
            render()
        }
    }

    private fun verseActions(key: String, reference: String, verseText: String) {
        val options = arrayOf(
            if (isVerseFavorite(key)) "Retirer des favoris" else "Ajouter aux favoris",
            "Copier le verset"
        )
        AlertDialog.Builder(this).setTitle(reference).setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val saved = prefs.getStringSet("favorite_verses", emptySet())?.toMutableSet() ?: mutableSetOf()
                    if (!saved.add(key)) saved.remove(key)
                    prefs.edit().putStringSet("favorite_verses", saved).apply()
                    refreshChapter()
                    toast("Favoris mis à jour.")
                }
                1 -> {
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(reference, "$reference — $verseText"))
                    toast("Verset copié.")
                }
            }
        }.show()
    }

    private fun toggleFavorite() {
        val key = "${books[bookIndex]}|$chapter"
        val saved = prefs.getStringSet("favorite_chapters", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (!saved.add(key)) saved.remove(key)
        prefs.edit().putStringSet("favorite_chapters", saved).apply()
        refreshChapter()
        toast("Favoris mis à jour.")
    }

    private fun showFavorites() {
        val chapterFavorites = prefs.getStringSet("favorite_chapters", emptySet())?.toList().orEmpty()
        val verseFavorites = prefs.getStringSet("favorite_verses", emptySet())?.toList().orEmpty()
        val options = (chapterFavorites.map { "Chapitre|$it" } + verseFavorites.map { "Verset|$it" }).sorted()
        if (options.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Mes favoris")
                .setMessage("Tu n'as pas encore de favori. Appuie sur un verset ou ajoute un chapitre aux favoris.")
                .setPositiveButton("OK", null).show()
            return
        }
        AlertDialog.Builder(this).setTitle("Mes favoris")
            .setItems(options.map { it.substringAfter("|") }.toTypedArray()) { _, which ->
                val item = options[which]
                val value = item.substringAfter("|")
                val parts = value.split("|")
                val index = books.indexOf(parts[0])
                if (index >= 0) {
                    bookIndex = index
                    chapter = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, chapterCounts[index]) ?: 1
                    render()
                }
            }.show()
    }

    private fun isChapterFavorite() =
        prefs.getStringSet("favorite_chapters", emptySet())?.contains("${books[bookIndex]}|$chapter") == true

    private fun isVerseFavorite(key: String) =
        prefs.getStringSet("favorite_verses", emptySet())?.contains(key) == true

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 12f
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun fullWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
