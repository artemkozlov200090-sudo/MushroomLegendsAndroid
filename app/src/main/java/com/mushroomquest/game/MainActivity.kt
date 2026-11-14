package com.mushroomquest.game

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("mushroom_save", MODE_PRIVATE)
        setContentView(GameView(this, prefs))
    }
}

private class GameView(context: Context, private val prefs: SharedPreferences) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var level = prefs.getInt("level", 1)
    private var xp = prefs.getInt("xp", 0)
    private var gold = prefs.getInt("gold", 35)
    private var power = prefs.getInt("power", 12)
    private var weapon = prefs.getInt("weapon", 0)
    private var pet = prefs.getInt("pet", 0)
    private var stage = prefs.getInt("stage", 1)
    private var enemyHp = prefs.getInt("enemyHp", 42)
    private var message = "Грибочек готов к приключению!"
    private var tab = 0

    private val bg = 0xFF15233B.toInt()
    private val panel = 0xFF243957.toInt()
    private val cream = 0xFFFFF1D0.toInt()
    private val green = 0xFF79D78B.toInt()
    private val orange = 0xFFFFB45C.toInt()
    private val red = 0xFFFF7777.toInt()

    private fun paint(color: Int, size: Float = 24f, bold: Boolean = false) {
        p.color = color
        p.textSize = size
        p.typeface = if (bold) Typeface.create("sans-serif-rounded", Typeface.BOLD)
            else Typeface.create("sans-serif", Typeface.NORMAL)
        p.style = Paint.Style.FILL
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, size: Float = 24f,
                     color: Int = cream, bold: Boolean = false) {
        paint(color, size, bold)
        c.drawText(s, x, y, p)
    }

    private fun box(c: Canvas, x: Float, y: Float, w: Float, h: Float,
                    color: Int, radius: Float = 22f) {
        paint(color)
        c.drawRoundRect(RectF(x, y, x + w, y + h), radius, radius, p)
    }

    private fun center(c: Canvas, s: String, x: Float, y: Float, size: Float,
                       color: Int = cream, bold: Boolean = false) {
        paint(color, size, bold)
        p.textAlign = Paint.Align.CENTER
        c.drawText(s, x, y, p)
        p.textAlign = Paint.Align.LEFT
    }

    private fun xpNeed() = 30 + (level - 1) * 20
    private fun enemyMax() = 42 + (stage - 1) * 17

    private fun saveProgress() {
        prefs.edit()
            .putInt("level", level).putInt("xp", xp).putInt("gold", gold)
            .putInt("power", power).putInt("weapon", weapon).putInt("pet", pet)
            .putInt("stage", stage).putInt("enemyHp", enemyHp).apply()
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat()
        val h = height.toFloat()
        c.drawColor(bg)

        text(c, "MUSHROOM QUEST", 22f, 42f, 19f, green, true)
        text(c, "Ур. $level", 22f, 82f, 27f, cream, true)
        text(c, "Монеты: $gold", w - 160f, 42f, 17f, orange, true)
        text(c, "Сила: $power", w - 160f, 78f, 17f, cream, true)
        box(c, 20f, 100f, w - 40f, 13f, panel, 8f)
        box(c, 20f, 100f, (w - 40f) * (xp.toFloat() / xpNeed()), 13f, green, 8f)
        text(c, "Опыт: $xp / ${xpNeed()}", 22f, 137f, 14f)

        val sceneY = 158f
        val sceneH = h * 0.37f
        box(c, 20f, sceneY, w - 40f, sceneH, 0xFF31566A.toInt(), 28f)
        paint(0xFF477D69.toInt())
        c.drawOval(-40f, sceneY + sceneH * .55f, w * .62f, sceneY + sceneH + 25f, p)
        c.drawOval(w * .40f, sceneY + sceneH * .52f, w + 50f, sceneY + sceneH + 25f, p)
        center(c, "ЛЕС ГРИБОВ • ЭТАП $stage", w / 2, sceneY + 34f, 15f, cream, true)
        center(c, "🍄", w * .28f, sceneY + sceneH * .58f, 70f)
        center(c, "Твой герой", w * .28f, sceneY + sceneH * .75f, 13f)
        center(c, if (stage % 3 == 0) "🐌" else "👾", w * .73f, sceneY + sceneH * .58f, 62f)
        center(c, "Враг", w * .73f, sceneY + sceneH * .75f, 13f)
        box(c, w * .55f, sceneY + sceneH * .80f, w * .35f, 10f, 0xFF613E50.toInt(), 7f)
        box(c, w * .55f, sceneY + sceneH * .80f,
            w * .35f * (enemyHp.toFloat() / max(1, enemyMax())), 10f, red, 7f)
        center(c, "HP $enemyHp/${enemyMax()}", w * .73f, sceneY + sceneH * .80f + 28f, 12f)

        val actionY = sceneY + sceneH + 16f
        box(c, 20f, actionY, w - 40f, 58f, orange, 18f)
        center(c, "⚔  АТАКОВАТЬ", w / 2, actionY + 37f, 21f, 0xFF382B29.toInt(), true)
        text(c, message, 24f, actionY + 85f, 14f, cream)

        val navY = h - 190f
        val labels = listOf("ГЕРОЙ", "СУМКА", "ПИТОМЕЦ")
        for (i in 0..2) {
            val bw = (w - 52f) / 3f
            box(c, 20f + i * (bw + 6f), navY, bw, 45f, if (tab == i) green else panel, 14f)
            center(c, labels[i], 20f + i * (bw + 6f) + bw / 2, navY + 29f, 12f,
                if (tab == i) bg else cream, true)
        }

        val cardY = navY + 57f
        box(c, 20f, cardY, w - 40f, 112f, panel, 20f)
        when (tab) {
            0 -> {
                text(c, "ПРОКАЧКА ГЕРОЯ", 36f, cardY + 29f, 15f, green, true)
                text(c, "Сила: $power", 36f, cardY + 57f, 16f)
                text(c, "Улучшить силу • 20 монет", 36f, cardY + 86f, 14f, orange, true)
            }
            1 -> {
                text(c, "ЭКИПИРОВКА", 36f, cardY + 29f, 15f, green, true)
                text(c, listOf("Старый посох", "Лесной клинок", "Кристальный гриб")[weapon], 36f, cardY + 58f, 16f)
                text(c, "Нажми, чтобы сменить предмет", 36f, cardY + 86f, 13f, orange)
            }
            else -> {
                text(c, "ТВОЙ ПИТОМЕЦ", 36f, cardY + 29f, 15f, green, true)
                text(c, listOf("Светлячок", "Лесной кролик", "Мини-дракон")[pet], 36f, cardY + 58f, 16f)
                text(c, "Нажми, чтобы выбрать следующего", 36f, cardY + 86f, 13f, orange)
            }
        }
        text(c, "Побеждай врагов и открывай новые этапы!", 22f, h - 22f, 12f, 0xFFB9C8D8.toInt())
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val w = width.toFloat()
        val h = height.toFloat()
        val sceneH = h * .37f
        val actionY = 158f + sceneH + 16f
        val navY = h - 190f
        val cardY = navY + 57f
        val x = e.x
        val y = e.y

        if (y >= actionY && y <= actionY + 58f) {
            val hit = max(1, power + pet * 2 + weapon * 3)
            enemyHp -= hit
            if (enemyHp <= 0) {
                val reward = 12 + stage * 3
                gold += reward
                xp += 18 + stage * 4
                message = "Победа! +$reward монет и опыт"
                stage++
                enemyHp = enemyMax()
                while (xp >= xpNeed()) {
                    xp -= xpNeed()
                    level++
                    power += 3
                    message = "Новый уровень! Сила +3"
                }
            } else {
                message = "Удар на $hit! Враг ещё держится."
            }
            saveProgress()
            invalidate()
            return true
        }

        if (y >= navY && y <= navY + 45f) {
            val bw = (w - 52f) / 3f
            tab = min(2, max(0, ((x - 20f) / (bw + 6f)).toInt()))
            invalidate()
            return true
        }

        if (y >= cardY && y <= cardY + 112f) {
            when (tab) {
                0 -> if (gold >= 20) {
                    gold -= 20
                    power += 5
                    message = "Сила увеличена на 5!"
                } else message = "Нужно ещё 20 монет."
                1 -> {
                    weapon = (weapon + 1) % 3
                    message = "Экипировка изменена!"
                }
                2 -> {
                    pet = (pet + 1) % 3
                    message = "Новый питомец выбран!"
                }
            }
            saveProgress()
            invalidate()
            return true
        }
        return true
    }
}
