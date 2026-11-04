package com.pkdjpro.app

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.res.ColorStateList
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private val red = Color.rgb(255, 51, 77)
    private val bg = Color.rgb(9, 11, 16)
    private val panel = Color.rgb(22, 26, 35)
    private val white = Color.WHITE
    private val players = arrayOfNulls<MediaPlayer>(2)
    private val titles = arrayOf("Deck A — Empty", "Deck B — Empty")
    private val status = arrayOfNulls<TextView>(2)
    private val playButtons = arrayOfNulls<Button>(2)
    private val seekBars = arrayOfNulls<SeekBar>(2)
    private val eqs = arrayOfNulls<Equalizer>(2)
    private var crossfade = 50
    private var pitch = intArrayOf(100, 100)
    private var cue = intArrayOf(0, 0)
    private val pickCodes = intArrayOf(101, 102)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildUi()
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 18, 16, 24)
        }
        scroll.addView(page)
        setContentView(scroll)

        page.addView(label("DESMOND DJ PRO", 27, red, true))
        page.addView(label("MIX • CREATE • ENJOY  |  OFFLINE ANDROID DJ STUDIO", 10, Color.LTGRAY, true))
        page.addView(space(16))
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        top.addView(metric("2", "DECKS"), weight())
        top.addView(metric("OFFLINE", "MODE"), weight())
        top.addView(metric("PRO", "MIXER"), weight())
        page.addView(top)
        page.addView(space(14))

        for (i in 0..1) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 14, 14, 14)
                background = rounded(panel, 18)
            }
            val deckTitle = label(if (i == 0) "DECK A" else "DECK B", 17, if (i == 0) red else Color.CYAN, true)
            card.addView(deckTitle)
            status[i] = label(titles[i], 13, white, true)
            card.addView(status[i])
            val choose = button("＋  LOAD TRACK FROM PHONE")
            choose.setOnClickListener { openPicker(i) }
            card.addView(choose)
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
            playButtons[i] = button("▶  PLAY")
            playButtons[i]?.setOnClickListener { toggle(i) }
            row.addView(playButtons[i], LinearLayout.LayoutParams(0, 48, 1f))
            val cueButton = button("SET CUE")
            cueButton.setOnClickListener { players[i]?.let { cue[i] = it.currentPosition; toast("Cue saved") } }
            row.addView(cueButton, LinearLayout.LayoutParams(0, 48, 1f))
            val jump = button("CUE ▶")
            jump.setOnClickListener { players[i]?.seekTo(cue[i]) }
            row.addView(jump, LinearLayout.LayoutParams(0, 48, 1f))
            card.addView(row)
            seekBars[i] = SeekBar(this).apply {
                max = 1000
                progressTintList = ColorStateList.valueOf(if (i == 0) red else Color.CYAN)
                thumbTintList = progressTintList
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) {
                        if (user) players[i]?.let { if (it.duration > 0) it.seekTo((it.duration * p / 1000f).roundToInt()) }
                    }
                    override fun onStartTrackingTouch(s: SeekBar?) {}
                    override fun onStopTrackingTouch(s: SeekBar?) {}
                })
            }
            card.addView(seekBars[i])
            card.addView(label("PITCH / TEMPO", 11, Color.LTGRAY, true))
            val pitchBar = SeekBar(this).apply {
                max = 40; progress = 20
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) {
                        pitch[i] = 80 + p
                        if (user) applyPitch(i)
                    }
                    override fun onStartTrackingTouch(s: SeekBar?) {}
                    override fun onStopTrackingTouch(s: SeekBar?) {}
                })
            }
            card.addView(pitchBar)
            page.addView(card)
            page.addView(space(14))
        }

        val mixer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(14, 14, 14, 14)
            background = rounded(panel, 18)
        }
        mixer.addView(label("MASTER MIXER", 18, white, true))
        mixer.addView(label("CROSSFADER  •  A  ← →  B", 12, Color.LTGRAY, true))
        val cross = SeekBar(this).apply {
            max = 100; progress = 50
            progressTintList = ColorStateList.valueOf(red)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) { crossfade = p; applyVolumes() }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        mixer.addView(cross)
        mixer.addView(label("3-BAND EQ  •  BASS / MID / TREBLE", 12, Color.LTGRAY, true))
        for (deck in 0..1) {
            mixer.addView(label(if (deck == 0) "DECK A EQ" else "DECK B EQ", 11, white, true))
            val bands = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (band in 0..2) {
                val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
                col.addView(label(listOf("BASS", "MID", "TREBLE")[band], 10, Color.LTGRAY, true))
                val seek = SeekBar(this).apply {
                    max = 100; progress = 50
                    rotation = -90f
                    layoutParams = LinearLayout.LayoutParams(110, 42)
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) {
                            if (user) setEq(deck, band, p)
                        }
                        override fun onStartTrackingTouch(s: SeekBar?) {}
                        override fun onStopTrackingTouch(s: SeekBar?) {}
                    })
                }
                col.addView(seek)
                bands.addView(col, LinearLayout.LayoutParams(0, 118, 1f))
            }
            mixer.addView(bands)
        }
        page.addView(mixer)
        page.addView(space(12))
        page.addView(label("TIP: Load audio files stored on your phone. Desmond DJ Pro works offline.", 12, Color.LTGRAY, false))
        page.addView(label("EARLY BUILD • Advanced scratching, STEMS, video mixing and samplers are planned updates.", 11, Color.GRAY, false))
    }

    private fun openPicker(deck: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        startActivityForResult(intent, pickCodes[deck])
    }

    @Deprecated("Deprecated by Android but retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data?.data == null) return
        val deck = when (requestCode) { 101 -> 0; 102 -> 1; else -> return }
        val uri: Uri = data.data!!
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {}
        try {
            players[deck]?.release()
            eqs[deck]?.release()
            val mp = MediaPlayer()
            mp.setDataSource(this, uri)
            mp.setOnPreparedListener {
                players[deck] = it
                status[deck]?.text = queryName(uri)
                it.start()
                playButtons[deck]?.text = "Ⅱ  PAUSE"
                try { eqs[deck] = Equalizer(0, it.audioSessionId).apply { enabled = true } } catch (_: Exception) {}
                applyVolumes()
                toast("Track loaded on Deck ${if (deck == 0) "A" else "B"}")
            }
            mp.setOnCompletionListener { playButtons[deck]?.text = "▶  PLAY" }
            mp.prepareAsync()
        } catch (e: Exception) { toast("Could not load this audio file") }
    }

    private fun queryName(uri: Uri): String {
        var name = "Loaded audio track"
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = c.getString(idx) ?: name
            }
        }
        return name
    }

    private fun toggle(i: Int) {
        val p = players[i] ?: run { toast("Load a track first"); return }
        if (p.isPlaying) { p.pause(); playButtons[i]?.text = "▶  PLAY" }
        else { p.start(); playButtons[i]?.text = "Ⅱ  PAUSE"; applyVolumes() }
    }

    private fun applyVolumes() {
        val a = (100 - crossfade) / 100f
        val b = crossfade / 100f
        players[0]?.setVolume(a, a)
        players[1]?.setVolume(b, b)
    }

    private fun applyPitch(i: Int) {
        try {
            val p = players[i] ?: return
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                val params = p.playbackParams
                params.speed = pitch[i] / 100f
                p.playbackParams = params
            }
        } catch (_: Exception) { toast("Tempo adjustment unavailable for this file") }
    }

    private fun setEq(deck: Int, band: Int, value: Int) {
        try {
            val eq = eqs[deck] ?: return
            if (eq.numberOfBands < 3) return
            val actualBand = when (band) { 0 -> 0; 1 -> eq.numberOfBands / 2; else -> eq.numberOfBands - 1 }
            val range = eq.bandLevelRange
            val level = (range[0] + (range[1] - range[0]) * value / 100).toShort()
            eq.setBandLevel(actualBand.toShort(), level)
        } catch (_: Exception) {}
    }

    private fun metric(value: String, caption: String): View {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(8, 12, 8, 12); background = rounded(panel, 12) }
        box.addView(label(value, 17, red, true)); box.addView(label(caption, 9, Color.LTGRAY, true)); return box
    }
    private fun label(s: String, size: Int, color: Int, bold: Boolean): TextView =
        TextView(this).apply { text = s; textSize = size.toFloat(); setTextColor(color); if (bold) setTypeface(null, Typeface.BOLD); setPadding(2, 5, 2, 5) }
    private fun button(s: String): Button = Button(this).apply {
        text = s; textSize = 11f; setTextColor(white); background = rounded(Color.rgb(42, 47, 60), 10)
        isAllCaps = false
    }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = radius.toFloat() }
    private fun space(h: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, h) }
    private fun weight() = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(3, 0, 3, 0) }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        players.forEach { it?.release() }
        eqs.forEach { it?.release() }
        super.onDestroy()
    }
}
