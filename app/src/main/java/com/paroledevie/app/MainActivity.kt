package com.paroledevie.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 36, 24, 28)
        }

        val title = TextView(this).apply {
            text = "Parole de Vie"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        page.addView(title, fullWidth())

        val subtitle = TextView(this).apply {
            text = "La Bible, simplement."
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }
        page.addView(subtitle, fullWidth())

        val notice = TextView(this).apply {
            text = "Édition choisie : Parole de Vie (PDV)\n\nLe texte biblique complet sera intégré uniquement après vérification et obtention des autorisations nécessaires. Cette première version ne contient pas encore le texte protégé."
            textSize = 16f
            setLineSpacing(6f, 1f)
        }
        page.addView(notice, fullWidth())

        val status = TextView(this).apply {
            text = "\nPremière étape du projet\nApplication Android native Kotlin\nCompilation automatisée par GitHub Actions"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }
        page.addView(status, fullWidth())

        val scroll = ScrollView(this).apply { addView(page) }
        setContentView(scroll)
    }

    private fun fullWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
}
