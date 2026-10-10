package com.paroledevie.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

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

    private val forest = Color.rgb(24, 66, 51)
    private val forestLight = Color.rgb(37, 91, 69)
    private val gold = Color.rgb(211, 169, 91)
    private val cream = Color.rgb(249, 247, 240)
    private val ink = Color.rgb(43, 48, 43)
    private val muted = Color.rgb(115, 119, 109)
    private var bookIndex = 42
    private var chapter = 3
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var heading: TextView
    private lateinit var favoriteButton: Button
    private val prefs by lazy { getSharedPreferences("parole_de_vie", MODE_PRIVATE) }

    // Extraits de démonstration temporaires. La Bible complète du PDF n'est pas encore intégrée.
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
        window.statusBarColor = forest
        window.navigationBarColor = forest
        window.decorView.systemUiVisibility = 0
        render()
    }

    private fun render() {
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(cream)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(26))
            setBackgroundColor(cream)
        }
        scroll.addView(root)
        setContentView(scroll)

        val brandRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val mark = TextView(this).apply {
            text = "✦"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(gold)
            background = shape(forest, 16)
        }
        brandRow.addView(mark, LinearLayout.LayoutParams(dp(48), dp(48)))
        val brandText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        brandText.addView(label("PAROLE DE VIE", 17f, forest, true))
        brandText.addView(label("UN MOMENT AVEC LA PAROLE", 9f, muted, true))
        brandRow.addView(brandText, LinearLayout.LayoutParams(0, -2, 1f))
        brandRow.addView(makeTextButton("⌕") { search() }, LinearLayout.LayoutParams(dp(48), dp(46)))
        brandRow.addView(makeTextButton("☆") { showFavorites() }, LinearLayout.LayoutParams(dp(48), dp(46)))
        root.addView(brandRow)

        val greeting = label("Bonjour, prends un instant pour toi.", 14f, muted, false)
        greeting.setPadding(dp(2), dp(20), dp(2), dp(12))
        root.addView(greeting)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(22))
            background = shapeGradient(forest, forestLight, 24)
        }
        hero.addView(label("LA PAROLE DU JOUR", 10f, Color.rgb(238, 213, 157), true))
        val heroQuote = label("« Ta parole est une lampe à mes pieds, et une lumière sur mon sentier. »", 22f, Color.WHITE, true)
        heroQuote.setLineSpacing(dp(3).toFloat(), 1f)
        heroQuote.setPadding(0, dp(14), 0, dp(10))
        hero.addView(heroQuote)
        hero.addView(label("Psaume 119:105  ·  Louis Segond 1910", 11f, Color.rgb(226, 232, 220), false))
        val readToday = makeButton("Commencer la lecture  →", forest, Color.WHITE) {
            bookIndex = books.indexOf("Psaumes")
            chapter = 119.coerceAtMost(chapterCounts[bookIndex])
            render()
        }
        readToday.background = shape(gold, 14)
        readToday.setTextColor(forest)
        hero.addView(readToday, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(18) })
        root.addView(hero, fullWidth().apply { topMargin = dp(4) })

        val sectionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(24), 0, dp(10))
        }
        sectionRow.addView(label("Reprendre la lecture", 20f, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
        sectionRow.addView(makeTextButton("Choisir ↗") { chooseBook() })
        root.addView(sectionRow)

        val readingCard = card()
        readingCard.orientation = LinearLayout.VERTICAL
        readingCard.addView(label("ANCIEN & NOUVEAU TESTAMENT", 10f, muted, true))
        heading = label(books[bookIndex] + "  " + chapter, 23f, forest, true)
        heading.setPadding(0, dp(8), 0, dp(4))
        readingCard.addView(heading)
        readingCard.addView(label("Choisis un livre ou un chapitre pour explorer la Bible.", 13f, muted, false))
        val selectorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(16), 0, 0)
        }
        selectorRow.addView(makeButton("Livre : " + books[bookIndex], forest, Color.WHITE) { chooseBook() },
            LinearLayout.LayoutParams(0, dp(48), 1.5f).apply { rightMargin = dp(6) })
        selectorRow.addView(makeButton("Chapitre " + chapter, Color.rgb(241, 237, 225), forest) { chooseChapter() },
            LinearLayout.LayoutParams(0, dp(48), 1f))
        readingCard.addView(selectorRow)
        root.addView(readingCard, fullWidth().apply { topMargin = dp(14) })

        val chapterRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(24), 0, dp(10))
        }
        chapterRow.addView(label("Lecture biblique", 20f, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
        chapterRow.addView(makeTextButton("☆ Favoris") { showFavorites() })
        root.addView(chapterRow)

        val note = TextView(this).apply {
            text = "VERSION ACTUELLE : aperçu de démonstration"
            textSize = 10f
            letterSpacing = 0.06f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(127, 87, 33))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = shape(Color.rgb(246, 232, 202), 12)
        }
        root.addView(note, fullWidth().apply { bottomMargin = dp(8) })

        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, fullWidth())
        favoriteButton = makeButton("☆ Ajouter le chapitre aux favoris", forest, Color.WHITE) { toggleFavorite() }
        root.addView(favoriteButton, fullWidth().apply { topMargin = dp(10) })

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(16), 0, dp(8))
        }
        nav.addView(makeButton("← Précédent", Color.rgb(239, 235, 223), forest) { moveChapter(-1) },
            LinearLayout.LayoutParams(0, dp(48), 1f).apply { rightMargin = dp(6) })
        nav.addView(makeButton("Suivant →", forest, Color.WHITE) { moveChapter(1) },
            LinearLayout.LayoutParams(0, dp(48), 1f))
        root.addView(nav)
        val footer = label("Une interface simple pour lire, méditer et garder ses versets préférés.", 11f, muted, false)
        footer.gravity = Gravity.CENTER
        footer.setPadding(dp(10), dp(18), dp(10), dp(8))
        root.addView(footer)
        refreshChapter()
    }

    private fun refreshChapter() {
        heading.text = books[bookIndex] + " " + chapter
        content.removeAllViews()
        val verses = demoVerses[books[bookIndex] + "|" + chapter]
        if (verses.isNullOrEmpty()) {
            val empty = card()
            empty.orientation = LinearLayout.VERTICAL
            empty.addView(label("Ce chapitre arrive bientôt", 17f, forest, true))
            val msg = label("La navigation et les favoris sont déjà disponibles. Le texte complet du PDF Louis Segond 1910 reste à importer dans l'application.", 14f, muted, false)
            msg.setPadding(0, dp(8), 0, 0)
            msg.setLineSpacing(dp(4).toFloat(), 1f)
            empty.addView(msg)
            content.addView(empty, fullWidth())
        } else {
            verses.forEach { row ->
                val parts = row.split("|", limit = 2)
                val number = parts[0]
                val text = parts.getOrElse(1) { "" }
                val key = books[bookIndex] + "|" + chapter + "|" + number
                val verseCard = card()
                verseCard.orientation = LinearLayout.HORIZONTAL
                verseCard.gravity = Gravity.TOP
                val numberView = TextView(this).apply {
                    this.text = number.padStart(2, '0')
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(forest)
                    gravity = Gravity.CENTER
                    background = shape(Color.rgb(235, 239, 229), 10)
                }
                verseCard.addView(numberView, LinearLayout.LayoutParams(dp(36), dp(34)).apply { rightMargin = dp(12) })
                val verseText = TextView(this).apply {
                    this.text = text + if (isVerseFavorite(key)) "  ★" else ""
                    textSize = 17f
                    setTextColor(ink)
                    setLineSpacing(dp(5).toFloat(), 1f)
                }
                verseCard.addView(verseText, LinearLayout.LayoutParams(0, -2, 1f))
                verseCard.setOnClickListener { verseActions(key, books[bookIndex] + " " + chapter + ":" + number, text) }
                content.addView(verseCard, fullWidth().apply { bottomMargin = dp(8) })
            }
        }
        favoriteButton.text = if (isChapterFavorite()) "★ Retirer ce chapitre des favoris" else "☆ Ajouter ce chapitre aux favoris"
    }

    private fun chooseBook() {
        AlertDialog.Builder(this).setTitle("Choisir un livre")
            .setItems(books.toTypedArray()) { _, which -> bookIndex = which; chapter = 1; render() }
            .show()
    }

    private fun chooseChapter() {
        val items = (1..chapterCounts[bookIndex]).map { "Chapitre " + it }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Choisir un chapitre")
            .setItems(items) { _, which -> chapter = which + 1; refreshChapter() }.show()
    }

    private fun moveChapter(direction: Int) {
        chapter += direction
        if (chapter > chapterCounts[bookIndex]) {
            if (bookIndex < books.lastIndex) { bookIndex++; chapter = 1 }
            else chapter = chapterCounts[bookIndex]
        } else if (chapter < 1) {
            if (bookIndex > 0) { bookIndex--; chapter = chapterCounts[bookIndex] }
            else chapter = 1
        }
        render()
    }

    private fun search() {
        val input = EditText(this).apply { hint = "Mot, référence ou livre" }
        AlertDialog.Builder(this).setTitle("Rechercher dans les extraits")
            .setView(input).setNegativeButton("Annuler", null)
            .setPositiveButton("Rechercher") { _, _ ->
                val query = input.text.toString().trim()
                if (query.isEmpty()) { toast("Entre un mot ou une référence."); return@setPositiveButton }
                val results = mutableListOf<Pair<String, String>>()
                demoVerses.forEach { (reference, rows) ->
                    rows.forEach { line ->
                        val parts = line.split("|", limit = 2)
                        val refParts = reference.split("|")
                        val fullRef = refParts[0] + " " + refParts.getOrElse(1) { "1" } + ":" + parts[0]
                        if (line.contains(query, true) || reference.contains(query, true)) results.add(fullRef to parts.getOrElse(1) { "" })
                    }
                }
                if (results.isEmpty()) {
                    AlertDialog.Builder(this).setTitle("Aucun résultat")
                        .setMessage("Aucun résultat dans les extraits présents dans cette version.")
                        .setPositiveButton("OK", null).show()
                } else {
                    AlertDialog.Builder(this).setTitle("Résultats : " + results.size)
                        .setItems(results.map { it.first + "\n" + it.second }.toTypedArray()) { _, which -> openReference(results[which].first) }
                        .show()
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
        val options = arrayOf(if (isVerseFavorite(key)) "Retirer des favoris" else "Ajouter aux favoris", "Copier le verset")
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
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(reference, reference + " — " + verseText))
                    toast("Verset copié.")
                }
            }
        }.show()
    }

    private fun toggleFavorite() {
        val key = books[bookIndex] + "|" + chapter
        val saved = prefs.getStringSet("favorite_chapters", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (!saved.add(key)) saved.remove(key)
        prefs.edit().putStringSet("favorite_chapters", saved).apply()
        refreshChapter()
        toast("Favoris mis à jour.")
    }

    private fun showFavorites() {
        val chapters = prefs.getStringSet("favorite_chapters", emptySet())?.toList().orEmpty()
        val verses = prefs.getStringSet("favorite_verses", emptySet())?.toList().orEmpty()
        val items = (chapters.map { "Chapitre|" + it } + verses.map { "Verset|" + it }).sorted()
        if (items.isEmpty()) {
            AlertDialog.Builder(this).setTitle("Mes favoris")
                .setMessage("Tes versets et chapitres favoris apparaîtront ici.")
                .setPositiveButton("OK", null).show()
            return
        }
        AlertDialog.Builder(this).setTitle("Mes favoris")
            .setItems(items.map { it.substringAfter("|") }.toTypedArray()) { _, which ->
                val parts = items[which].substringAfter("|").split("|")
                val index = books.indexOf(parts[0])
                if (index >= 0) {
                    bookIndex = index
                    chapter = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(1, chapterCounts[index]) ?: 1
                    render()
                }
            }.show()
    }

    private fun isChapterFavorite() =
        prefs.getStringSet("favorite_chapters", emptySet())?.contains(books[bookIndex] + "|" + chapter) == true

    private fun isVerseFavorite(key: String) =
        prefs.getStringSet("favorite_verses", emptySet())?.contains(key) == true

    private fun card() = LinearLayout(this).apply {
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = shape(Color.WHITE, 18)
        elevation = dp(1).toFloat()
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    private fun makeButton(value: String, backgroundColor: Int, textColor: Int, action: () -> Unit) =
        Button(this).apply {
            text = value
            textSize = 12f
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColor)
            background = shape(backgroundColor, 14)
            stateListAnimator = null
            setPadding(dp(8), 0, dp(8), 0)
            setOnClickListener { action() }
        }

    private fun makeTextButton(value: String, action: () -> Unit) = TextView(this).apply {
        text = value
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(forest)
        gravity = Gravity.CENTER
        setPadding(dp(6), dp(6), dp(6), dp(6))
        background = shape(Color.rgb(239, 237, 226), 14)
        setOnClickListener { action() }
    }

    private fun shape(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun shapeGradient(top: Int, bottom: Int, radius: Int) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, intArrayOf(top, bottom)
    ).apply { cornerRadius = dp(radius).toFloat() }

    private fun fullWidth() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
