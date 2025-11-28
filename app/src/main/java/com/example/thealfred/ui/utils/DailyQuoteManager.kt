package com.example.thealfred.ui.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object DailyQuoteManager {

    private const val PREFS_NAME = "alfred_quotes"
    private const val KEY_CURRENT_QUOTE = "daily_quote"
    private const val KEY_QUOTE_DATE = "quote_date"
    private const val KEY_USED_INDICES = "used_indices"

    private val ALL_QUOTES = listOf(
        "Вы не потерпели неудачу. Вы просто нашли еще один способ, который не сработает. Теперь к следующему.",
        "Тьма — Ваш союзник, Мастер Брюс. Но никогда не позволяйте ей стать Вашим оправданием.",
        "Отличие героя от обывателя, сэр, в том, что герой всегда выполняет свой список дел.",
        "Не позволяйте шуму города отвлекать Вас от голоса долга. Наденьте маску и сосредоточьтесь.",
        "Ваша самая большая сила, сэр, в том, что Вы всегда возвращаетесь домой. Не забывайте об этом.",
        "Мастерство не терпит посредственности. Идите и покажите, что означает настоящая подготовка.",
        "Я полагаю, Вы помните, что даже самый мрачный день в Готэме длится всего 24 часа. Идите и завершите его.",
        "Не позволяйте чувствам влиять на Вашу миссию. Эмоции — это роскошь, которую Вы себе позволить не можете.",
        "Всякая маленькая победа — это кирпич в стене, защищающей Готэм. Начните с первого кирпича.",
        "Надежда, сэр, — это последняя вещь, которую Вы должны позволить себе потерять.",
        "Мир устроен просто, Мастер Брюс: Вы либо работаете над ним, либо позволяете ему работать над Вами.",
        "Не ищите легкий путь, сэр. Ищите самый эффективный. Они редко совпадают.",
        "Вы не потерпели неудачу. Вы просто доказали, что Вам нужна более прочная броня. Технический вопрос.",
        "Ваша воля — это последнее оружие. Не позволяйте никому его отобрать.",
        "Противник ждет Вашего срыва. Не доставляйте ему этого удовольствия.",
        "Ваше прошлое не определяет Ваш настоящий долг. Урок усвоен. Двигайтесь дальше.",
        "Страх — это всего лишь подсказка. Он говорит Вам, где Ваши слабости, чтобы Вы могли их укрепить.",
        "Я обучил Вас быть лучшим. Не разочаровывайте ни себя, ни своего тренера, сэр.",
        "Мы уже проходили это, сэр. Исход всегда один: Вы побеждаете.",
        "Забудьте о себе. Помните о цели. Это все, что имеет значение."
    )

    fun getQuote(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val savedDate = prefs.getString(KEY_QUOTE_DATE, null)

        // Если дата совпадает, просто возвращаем сохраненную цитату
        if (today == savedDate) {
            return prefs.getString(KEY_CURRENT_QUOTE, ALL_QUOTES.first())!!
        }

        // --- Логика выбора новой цитаты на новый день ---

        val usedIndicesString = prefs.getString(KEY_USED_INDICES, "")!!
        val usedIndices = usedIndicesString.split(',').filter { it.isNotEmpty() }.map { it.toInt() }.toMutableSet()

        var availableIndices = ALL_QUOTES.indices.filter { !usedIndices.contains(it) }

        // Если все цитаты были использованы, сбрасываем список
        if (availableIndices.isEmpty()) {
            usedIndices.clear()
            availableIndices = ALL_QUOTES.indices.toList()
        }

        val newQuoteIndex = availableIndices.random()
        val newQuote = ALL_QUOTES[newQuoteIndex]
        usedIndices.add(newQuoteIndex)

        // Сохраняем все на новый день
        prefs.edit()
            .putString(KEY_CURRENT_QUOTE, newQuote)
            .putString(KEY_QUOTE_DATE, today)
            .putString(KEY_USED_INDICES, usedIndices.joinToString(","))
            .apply()

        return newQuote
    }
}