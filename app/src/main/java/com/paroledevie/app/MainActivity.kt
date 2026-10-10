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
import android.view.MotionEvent
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private val books = listOf("Genèse","Exode","Lévitique","Nombres","Deutéronome","Josué","Juges","Ruth","1 Samuel","2 Samuel","1 Rois","2 Rois","1 Chroniques","2 Chroniques","Esdras","Néhémie","Esther","Job","Psaumes","Proverbes","Ecclésiaste","Cantique des cantiques","Ésaïe","Jérémie","Lamentations","Ézéchiel","Daniel","Osée","Joël","Amos","Abdias","Jonas","Michée","Nahum","Habacuc","Sophonie","Aggée","Zacharie","Malachie","Matthieu","Marc","Luc","Jean","Actes","Romains","1 Corinthiens","2 Corinthiens","Galates","Éphésiens","Philippiens","Colossiens","1 Thessaloniciens","2 Thessaloniciens","1 Timothée","2 Timothée","Tite","Philémon","Hébreux","Jacques","1 Pierre","2 Pierre","1 Jean","2 Jean","3 Jean","Jude","Apocalypse")
    private val chapterCounts = intArrayOf(50,40,27,36,34,24,21,4,31,24,22,25,29,36,10,13,10,42,150,31,12,8,66,52,5,48,12,14,3,9,1,4,7,3,3,3,2,14,4,28,16,24,21,28,16,16,13,6,6,4,4,5,3,6,4,3,1,13,5,5,3,5,1,1,1,22)
    private val forest = Color.rgb(24,66,51)
    private val forestLight = Color.rgb(37,91,69)
    private val gold = Color.rgb(211,169,91)
    private val cream = Color.rgb(249,247,240)
    private val ink = Color.rgb(43,48,43)
    private val muted = Color.rgb(115,119,109)
    private val pageSize = 7
    private var bookIndex = 42
    private var chapter = 3
    private var pageIndex = 0
    private var activeScreen = "home"
    private var currentTitle = "Accueil"
    private var currentSubtitle = "Un moment avec la Parole"
    private var testament = 0
    private var highlightedVerse = 0
    private lateinit var screenHost: FrameLayout
    private lateinit var titleView: TextView
    private lateinit var subtitleView: TextView
    private val prefs by lazy { getSharedPreferences("parole_de_vie", MODE_PRIVATE) }
    private data class Verse(val number: Int, val text: String)
    private data class SearchHit(val bookIndex: Int, val chapter: Int, val verse: Int, val text: String)
    private val bibleVerses: Map<String,List<Verse>> by lazy { loadBibleVerses() }

    private fun loadBibleVerses(): Map<String,List<Verse>> {
        val json = assets.open("bible.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val data = org.json.JSONArray(json)
        val grouped = mutableMapOf<String,MutableList<Verse>>()
        for (i in 0 until data.length()) {
            val row = data.getJSONObject(i)
            val id = row.optInt("book",0)
            if (id !in 1..books.size) continue
            val ch = row.optInt("chapter",0)
            val no = row.optInt("verse",0)
            val text = row.optString("text","").trim()
            if (ch < 1 || no < 1 || text.isEmpty()) continue
            grouped.getOrPut(books[id-1] + "|" + ch) { mutableListOf() }.add(Verse(no,text))
        }
        return grouped.mapValues { it.value.sortedBy { verse -> verse.number } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = forest
        window.navigationBarColor = forest
        bookIndex = prefs.getInt("last_book",42).coerceIn(0,books.lastIndex)
        chapter = prefs.getInt("last_chapter",3).coerceIn(1,chapterCounts[bookIndex])
        pageIndex = prefs.getInt("last_page",0).coerceAtLeast(0)
        showShell()
        showHome()
    }

    private fun showShell() {
        val shell = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(cream) }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20),dp(12),dp(20),dp(12))
            setBackgroundColor(Color.WHITE)
        }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        brand.addView(TextView(this).apply {
            text = "✦"; textSize = 23f; gravity = Gravity.CENTER; setTextColor(gold); background = shape(forest,14)
        },LinearLayout.LayoutParams(dp(42),dp(42)))
        val names = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(11),0,0,0) }
        names.addView(label("PAROLE DE VIE",16f,forest,true))
        names.addView(label("LA BIBLE, À TON RYTHME",9f,muted,true))
        brand.addView(names,LinearLayout.LayoutParams(0,-2,1f))
        brand.addView(makeTextButton("⌕") { showSearch() },LinearLayout.LayoutParams(dp(46),dp(42)))
        top.addView(brand)
        titleView = label(currentTitle,22f,ink,true).apply { setPadding(0,dp(12),0,0) }
        subtitleView = label(currentSubtitle,12f,muted,false)
        top.addView(titleView); top.addView(subtitleView)
        shell.addView(top,LinearLayout.LayoutParams(-1,-2))
        screenHost = FrameLayout(this)
        shell.addView(screenHost,LinearLayout.LayoutParams(-1,0,1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            setPadding(dp(4),dp(5),dp(4),dp(5)); setBackgroundColor(Color.WHITE); elevation = dp(8).toFloat()
        }
        listOf(Triple("⌂","Accueil","home"),Triple("▤","Bible","reader"),Triple("⌕","Recherche","search"),Triple("☆","Enregistrés","saved"),Triple("⚙","Réglages","settings")).forEach { item ->
            val tab = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                setPadding(dp(2),dp(5),dp(2),dp(5))
                setOnClickListener { when(item.third) {
                    "home" -> showHome()
                    "reader" -> showReader()
                    "search" -> showSearch()
                    "saved" -> showFavorites()
                    "settings" -> showSettings()
                } }
            }
            tab.addView(TextView(this).apply { text=item.first; textSize=20f; setTextColor(if(activeScreen==item.third) forest else muted); gravity=Gravity.CENTER })
            tab.addView(TextView(this).apply {
                text=item.second; textSize=10f; typeface=if(activeScreen==item.third) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if(activeScreen==item.third) forest else muted); gravity=Gravity.CENTER
            })
            nav.addView(tab,LinearLayout.LayoutParams(0,-2,1f))
        }
        shell.addView(nav,LinearLayout.LayoutParams(-1,-2))
        setContentView(shell)
    }

    private fun setScreen(name:String,title:String,subtitle:String) {
        activeScreen=name; currentTitle=title; currentSubtitle=subtitle; showShell()
    }
    private fun body() = LinearLayout(this).apply {
        orientation=LinearLayout.VERTICAL; setPadding(dp(18),dp(14),dp(18),dp(22)); setBackgroundColor(cream)
    }
    private fun installBody(view:View) { screenHost.removeAllViews(); screenHost.addView(view,FrameLayout.LayoutParams(-1,-1)) }
    private fun scrollBody(body:LinearLayout) = ScrollView(this).apply { isFillViewport=true; addView(body) }

    private fun showHome() {
        setScreen("home","Accueil","Un moment avec la Parole")
        val b=body()
        val hero=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(22),dp(22),dp(22),dp(22)); background=shapeGradient(forest,forestLight,24) }
        hero.addView(label("POUR AUJOURD’HUI",10f,Color.rgb(238,213,157),true))
        hero.addView(label("« Ta parole est une lampe à mes pieds, et une lumière sur mon sentier. »",22f,Color.WHITE,true).apply {
            setLineSpacing(dp(3).toFloat(),1f); setPadding(0,dp(14),0,dp(10))
        })
        hero.addView(label("Psaume 119:105 · Louis Segond 1910",11f,Color.rgb(226,232,220),false))
        hero.addView(makeButton("Ouvrir la Bible  →",gold,forest) {
            bookIndex=books.indexOf("Psaumes"); chapter=119; pageIndex=0; showReader()
        },LinearLayout.LayoutParams(-1,dp(48)).apply { topMargin=dp(18) })
        b.addView(hero,fullWidth())
        b.addView(sectionTitle("Reprendre ma lecture"),fullWidth().apply { topMargin=dp(24) })
        val resume=card()
        resume.addView(label(books[bookIndex]+" "+chapter,22f,forest,true))
        resume.addView(label("Retrouve ta lecture là où tu l’as laissée.",13f,muted,false).apply { setPadding(0,dp(6),0,dp(14)) })
        resume.addView(makeButton("Continuer la lecture",forest,Color.WHITE) { showReader() },fullWidth())
        b.addView(resume,fullWidth().apply { topMargin=dp(10) })
        b.addView(sectionTitle("Explorer la Bible"),fullWidth().apply { topMargin=dp(22) })
        b.addView(makeButton("Ancien Testament  ·  39 livres",forest,Color.WHITE) { testament=0; bookIndex=0; chapter=1; pageIndex=0; showReader() },fullWidth().apply { topMargin=dp(8) })
        b.addView(makeButton("Nouveau Testament  ·  27 livres",forestLight,Color.WHITE) { testament=1; bookIndex=39; chapter=1; pageIndex=0; showReader() },fullWidth().apply { topMargin=dp(8) })
        b.addView(makeButton("Rechercher un passage  ⌕",Color.WHITE,forest) { showSearch() },fullWidth().apply { topMargin=dp(18) })
        installBody(scrollBody(b))
    }

    private fun showReader() {
        setScreen("reader","Lire la Bible","Ancien et Nouveau Testament · lecture hors ligne")
        val b=body()
        val testamentRow=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        testamentRow.addView(makeButton("Ancien Testament",if(bookIndex<39) forest else Color.WHITE,if(bookIndex<39) Color.WHITE else forest) {
            testament=0; if(bookIndex>=39){bookIndex=0;chapter=1};pageIndex=0;showReader()
        },LinearLayout.LayoutParams(0,dp(44),1f).apply { rightMargin=dp(6) })
        testamentRow.addView(makeButton("Nouveau Testament",if(bookIndex>=39) forest else Color.WHITE,if(bookIndex>=39) Color.WHITE else forest) {
            testament=1;if(bookIndex<39){bookIndex=39;chapter=1};pageIndex=0;showReader()
        },LinearLayout.LayoutParams(0,dp(44),1f))
        b.addView(testamentRow)
        val pickers=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL;setPadding(0,dp(10),0,dp(10)) }
        pickers.addView(makeButton(books[bookIndex]+" ▾",Color.WHITE,forest) { chooseBook() },LinearLayout.LayoutParams(0,dp(46),1.4f).apply { rightMargin=dp(6) })
        pickers.addView(makeButton("Chapitre $chapter ▾",Color.WHITE,forest) { chooseChapter() },LinearLayout.LayoutParams(0,dp(46),1f))
        b.addView(pickers)
        val verses=bibleVerses[books[bookIndex]+"|"+chapter].orEmpty()
        val maxPage=((verses.size-1).coerceAtLeast(0))/pageSize
        pageIndex=pageIndex.coerceIn(0,maxPage)
        val pageCard=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(22),dp(20),dp(20));background=shape(Color.WHITE,20);minimumHeight=dp(290)
        }
        pageCard.addView(label("LA SAINTE BIBLE",9f,gold,true))
        pageCard.addView(label(books[bookIndex],25f,forest,true).apply { setPadding(0,dp(7),0,0) })
        pageCard.addView(label("Chapitre $chapter",13f,muted,false).apply { setPadding(0,0,0,dp(12)) })
        val start=pageIndex*pageSize
        val end=(start+pageSize).coerceAtMost(verses.size)
        if(verses.isEmpty()) pageCard.addView(label("Aucun verset disponible pour ce chapitre.",15f,muted,false))
        else for(i in start until end) {
            val verse=verses[i]
            val row=LinearLayout(this).apply {
                orientation=LinearLayout.HORIZONTAL;gravity=Gravity.TOP;setPadding(dp(8),dp(8),dp(8),dp(8))
                if(verse.number==highlightedVerse) background=shape(Color.rgb(246,232,202),10)
                isClickable=true
                setOnClickListener { verseActions(books[bookIndex]+"|"+chapter+"|"+verse.number,books[bookIndex]+" "+chapter+":"+verse.number,verse.text) }
            }
            row.addView(label(verse.number.toString(),12f,forest,true).apply { setPadding(0,dp(2),dp(10),0) })
            row.addView(label(verse.text,17f,ink,false).apply { setLineSpacing(dp(5).toFloat(),1f) },LinearLayout.LayoutParams(0,-2,1f))
            pageCard.addView(row,fullWidth())
        }
        val gesture=android.view.GestureDetector(this,object:android.view.GestureDetector.SimpleOnGestureListener(){
            override fun onFling(e1:MotionEvent?,e2:MotionEvent,velocityX:Float,velocityY:Float):Boolean {
                if(e1==null || kotlin.math.abs(e1.x-e2.x)<dp(55) || kotlin.math.abs(velocityX)<dp(100) || kotlin.math.abs(velocityX)<kotlin.math.abs(velocityY)) return false
                if(e1.x>e2.x) movePage(1) else movePage(-1)
                return true
            }
        })
        pageCard.setOnTouchListener { _,event -> gesture.onTouchEvent(event) }
        b.addView(pageCard,fullWidth().apply { topMargin=dp(4) })
        val pageCount=(maxPage+1).coerceAtLeast(1)
        b.addView(label("PAGE ${pageIndex+1} / $pageCount  ·  Fais glisser la page pour continuer",10f,muted,true).apply {
            gravity=Gravity.CENTER;setPadding(0,dp(12),0,dp(8))
        },fullWidth())
        val pageNav=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        pageNav.addView(makeButton("← Page",Color.WHITE,forest) { movePage(-1) },LinearLayout.LayoutParams(0,dp(46),1f).apply { rightMargin=dp(6) })
        pageNav.addView(makeButton("Page →",forest,Color.WHITE) { movePage(1) },LinearLayout.LayoutParams(0,dp(46),1f))
        b.addView(pageNav,fullWidth())
        b.addView(makeButton(if(isChapterFavorite()) "★ Chapitre enregistré" else "☆ Enregistrer ce chapitre",Color.rgb(235,239,229),forest) {
            toggleFavorite();showReader()
        },fullWidth().apply { topMargin=dp(10) })
        b.addView(label("Astuce : touche un verset pour le copier ou l’enregistrer.",12f,muted,false).apply {
            gravity=Gravity.CENTER;setPadding(0,dp(12),0,dp(8))
        })
        installBody(scrollBody(b));savePosition()
    }

    private fun movePage(direction:Int) {
        val verses=bibleVerses[books[bookIndex]+"|"+chapter].orEmpty()
        val maxPage=((verses.size-1).coerceAtLeast(0))/pageSize
        if(direction>0 && pageIndex<maxPage) pageIndex++
        else if(direction<0 && pageIndex>0) pageIndex--
        else if(direction>0){moveChapter(1);return}
        else if(direction<0){moveChapter(-1);return}
        highlightedVerse=0;showReader()
    }

    private fun chooseBook() {
        val start=if(testament==1 || bookIndex>=39) 39 else 0
        val end=if(start==0) 39 else books.size
        AlertDialog.Builder(this).setTitle(if(start==0) "Ancien Testament" else "Nouveau Testament")
            .setItems(books.subList(start,end).toTypedArray()) { _,which ->
                bookIndex=start+which;chapter=1;pageIndex=0;highlightedVerse=0;showReader()
            }.show()
    }
    private fun chooseChapter() {
        AlertDialog.Builder(this).setTitle("Choisir un chapitre")
            .setItems((1..chapterCounts[bookIndex]).map{"Chapitre $it"}.toTypedArray()) { _,which ->
                chapter=which+1;pageIndex=0;highlightedVerse=0;showReader()
            }.show()
    }
    private fun moveChapter(direction:Int) {
        chapter+=direction
        if(chapter>chapterCounts[bookIndex]) { if(bookIndex<books.lastIndex){bookIndex++;chapter=1}else chapter=chapterCounts[bookIndex] }
        else if(chapter<1) { if(bookIndex>0){bookIndex--;chapter=chapterCounts[bookIndex]}else chapter=1 }
        pageIndex=if(direction<0) ((bibleVerses[books[bookIndex]+"|"+chapter].orEmpty().size-1).coerceAtLeast(0))/pageSize else 0
        highlightedVerse=0;showReader()
    }

    private fun showSearch() {
        setScreen("search","Recherche biblique","Un mot ou une référence, puis ouverture directe du passage")
        val b=body()
        val field=EditText(this).apply {
            hint="Ex. amour, Jean 3:16, Psaume 23";textSize=16f;singleLine=true
            setPadding(dp(14),dp(12),dp(14),dp(12));background=shape(Color.WHITE,14)
        }
        b.addView(field,fullWidth())
        b.addView(makeButton("Rechercher dans les 66 livres",forest,Color.WHITE) { runSearch(field.text.toString().trim(),b) },fullWidth().apply { topMargin=dp(10) })
        b.addView(label("Les résultats ouvrent le verset dans la page de lecture, sans onglet séparé.",12f,muted,false).apply {
            setPadding(dp(2),dp(12),dp(2),dp(10))
        })
        installBody(scrollBody(b))
    }

    private fun runSearch(query:String,body:LinearLayout) {
        if(query.isBlank()){toast("Entre un mot ou une référence.");return}
        val hits=mutableListOf<SearchHit>()
        val ref=Regex("^(.+?)\\s+(\\d+)(?::(\\d+))?$").find(query)
        if(ref!=null) {
            val wantedBook=ref.groupValues[1].trim()
            val wantedChapter=ref.groupValues[2].toIntOrNull()
            val wantedVerse=ref.groupValues[3].toIntOrNull()
            val index=books.indexOfFirst{it.equals(wantedBook,true)}
            if(index>=0 && wantedChapter!=null) {
                bibleVerses[books[index]+"|"+wantedChapter].orEmpty().filter{wantedVerse==null || it.number==wantedVerse}.forEach {
                    hits.add(SearchHit(index,wantedChapter,it.number,it.text))
                }
            }
        }
        if(hits.isEmpty()) {
            loop@ for(i in books.indices) for(ch in 1..chapterCounts[i]) for(verse in bibleVerses[books[i]+"|"+ch].orEmpty()) {
                if(verse.text.contains(query,true)) {
                    hits.add(SearchHit(i,ch,verse.number,verse.text))
                    if(hits.size>=150) break@loop
                }
            }
        }
        if(hits.isEmpty()) {
            body.addView(card().apply {
                addView(label("Aucun résultat trouvé",18f,forest,true))
                addView(label("Essaie un autre mot ou une référence comme Jean 3:16.",14f,muted,false).apply { setPadding(0,dp(8),0,0) })
            },fullWidth().apply { topMargin=dp(14) })
            return
        }
        body.addView(label("${hits.size}${if(hits.size>=150) "+" else ""} résultat(s) · touche un passage pour l’ouvrir",14f,forest,true),fullWidth().apply { topMargin=dp(18) })
        hits.forEach { hit ->
            val refText=books[hit.bookIndex]+" "+hit.chapter+":"+hit.verse
            val result=card().apply {
                isClickable=true
                setOnClickListener {
                    bookIndex=hit.bookIndex;chapter=hit.chapter
                    val rows=bibleVerses[books[bookIndex]+"|"+chapter].orEmpty()
                    pageIndex=(rows.indexOfFirst{it.number==hit.verse}.coerceAtLeast(0))/pageSize
                    highlightedVerse=hit.verse;showReader()
                }
            }
            result.addView(label(refText,14f,forest,true))
            result.addView(label(hit.text,15f,ink,false).apply { setPadding(0,dp(7),0,0);setLineSpacing(dp(3).toFloat(),1f) })
            body.addView(result,fullWidth().apply { topMargin=dp(8) })
        }
    }

    private fun showFavorites() {
        setScreen("saved","Mes passages","Versets et chapitres enregistrés sur cet appareil")
        val b=body()
        val chapters=prefs.getStringSet("favorite_chapters",emptySet())?.toList().orEmpty().sorted()
        val verses=prefs.getStringSet("favorite_verses",emptySet())?.toList().orEmpty().sorted()
        b.addView(sectionTitle("Chapitres enregistrés"),fullWidth())
        if(chapters.isEmpty()) b.addView(emptyMessage("Aucun chapitre enregistré pour le moment."),fullWidth().apply{topMargin=dp(8)})
        chapters.forEach { key ->
            val p=key.split("|")
            if(p.size>=2) {
                val index=books.indexOf(p[0]);val ch=p[1].toIntOrNull()
                if(index>=0 && ch!=null) b.addView(makeButton(p[0]+" "+ch+"  →",Color.WHITE,forest) {
                    bookIndex=index;chapter=ch;pageIndex=0;showReader()
                },fullWidth().apply{topMargin=dp(7)})
            }
        }
        b.addView(sectionTitle("Versets enregistrés"),fullWidth().apply{topMargin=dp(22)})
        if(verses.isEmpty()) b.addView(emptyMessage("Touche un verset pendant la lecture pour l’enregistrer."),fullWidth().apply{topMargin=dp(8)})
        verses.forEach { key ->
            val p=key.split("|")
            if(p.size>=3) {
                val index=books.indexOf(p[0]);val ch=p[1].toIntOrNull();val no=p[2].toIntOrNull()
                if(index>=0 && ch!=null && no!=null) {
                    val text=bibleVerses[books[index]+"|"+ch].orEmpty().firstOrNull{it.number==no}?.text.orEmpty()
                    val item=card().apply { setOnClickListener {
                        bookIndex=index;chapter=ch
                        pageIndex=(bibleVerses[books[index]+"|"+ch].orEmpty().indexOfFirst{it.number==no}.coerceAtLeast(0))/pageSize
                        highlightedVerse=no;showReader()
                    } }
                    item.addView(label(books[index]+" "+ch+":"+no,14f,forest,true))
                    item.addView(label(text,14f,ink,false).apply{setPadding(0,dp(5),0,0)})
                    b.addView(item,fullWidth().apply{topMargin=dp(8)})
                }
            }
        }
        installBody(scrollBody(b))
    }

    private fun showSettings() {
        setScreen("settings","Réglages","Personnalise ton expérience de lecture")
        val b=body()
        val panel=card()
        panel.addView(label("Une lecture à ton rythme",19f,forest,true))
        panel.addView(label("Parole de Vie fonctionne hors ligne. Ta position de lecture et tes favoris restent sur cet appareil.",14f,muted,false).apply {
            setPadding(0,dp(8),0,dp(14));setLineSpacing(dp(3).toFloat(),1f)
        })
        panel.addView(makeButton("Reprendre à ${books[bookIndex]} $chapter",forest,Color.WHITE) { showReader() },fullWidth())
        b.addView(panel,fullWidth())
        b.addView(sectionTitle("Pistes d’évolution"),fullWidth().apply{topMargin=dp(22)})
        listOf("Taille du texte et mode nuit","Plans de lecture et suivi personnel","Notes privées et surlignage par couleur","Audio biblique hors ligne").forEach {
            b.addView(card().apply{addView(label(it,14f,ink,false))},fullWidth().apply{topMargin=dp(8)})
        }
        b.addView(label("Ces fonctions sont proposées comme prochaines étapes ; elles ne sont pas encore activées.",12f,muted,false).apply{setPadding(0,dp(12),0,0)})
        installBody(scrollBody(b))
    }

    private fun verseActions(key:String,reference:String,verseText:String) {
        val options=arrayOf(if(isVerseFavorite(key)) "Retirer des passages enregistrés" else "Enregistrer ce verset","Copier le verset")
        AlertDialog.Builder(this).setTitle(reference).setItems(options){_,which->
            when(which) {
                0 -> {
                    val set=prefs.getStringSet("favorite_verses",emptySet())?.toMutableSet()?:mutableSetOf()
                    if(!set.add(key))set.remove(key)
                    prefs.edit().putStringSet("favorite_verses",set).apply()
                    showReader();toast("Passages enregistrés mis à jour.")
                }
                1 -> {
                    val clipboard=getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(reference,"$reference — $verseText"))
                    toast("Verset copié.")
                }
            }
        }.show()
    }
    private fun toggleFavorite() {
        val key=books[bookIndex]+"|"+chapter
        val set=prefs.getStringSet("favorite_chapters",emptySet())?.toMutableSet()?:mutableSetOf()
        if(!set.add(key))set.remove(key)
        prefs.edit().putStringSet("favorite_chapters",set).apply()
        toast("Chapitres enregistrés mis à jour.")
    }
    private fun isChapterFavorite()=prefs.getStringSet("favorite_chapters",emptySet())?.contains(books[bookIndex]+"|"+chapter)==true
    private fun isVerseFavorite(key:String)=prefs.getStringSet("favorite_verses",emptySet())?.contains(key)==true
    private fun savePosition(){prefs.edit().putInt("last_book",bookIndex).putInt("last_chapter",chapter).putInt("last_page",pageIndex).apply()}
    private fun sectionTitle(value:String)=label(value,19f,ink,true).apply{setPadding(0,dp(12),0,dp(8))}
    private fun emptyMessage(value:String)=card().apply{addView(label(value,14f,muted,false))}
    private fun card()=LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(16),dp(16),dp(16));background=shape(Color.WHITE,17);elevation=dp(1).toFloat()
    }
    private fun label(value:String,size:Float,color:Int,bold:Boolean)=TextView(this).apply{
        text=value;textSize=size;setTextColor(color);if(bold)typeface=Typeface.create("sans-serif",Typeface.BOLD)
    }
    private fun makeButton(value:String,bg:Int,fg:Int,action:()->Unit)=Button(this).apply{
        text=value;textSize=12f;isAllCaps=false;typeface=Typeface.DEFAULT_BOLD;setTextColor(fg)
        background=shape(bg,13);stateListAnimator=null;setPadding(dp(7),0,dp(7),0);setOnClickListener{action()}
    }
    private fun makeTextButton(value:String,action:()->Unit)=TextView(this).apply{
        text=value;textSize=14f;typeface=Typeface.DEFAULT_BOLD;setTextColor(forest);gravity=Gravity.CENTER
        setPadding(dp(6),dp(6),dp(6),dp(6));background=shape(Color.rgb(239,237,226),13);setOnClickListener{action()}
    }
    private fun shape(color:Int,radius:Int)=GradientDrawable().apply{setColor(color);cornerRadius=dp(radius).toFloat()}
    private fun shapeGradient(top:Int,bottom:Int,radius:Int)=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(top,bottom)).apply{cornerRadius=dp(radius).toFloat()}
    private fun fullWidth()=LinearLayout.LayoutParams(-1,-2)
    private fun dp(value:Int)=(value*resources.displayMetrics.density).toInt()
    private fun toast(message:String)=Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}
