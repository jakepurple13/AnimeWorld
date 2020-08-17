package com.programmersbox.anime_sources.utils

import org.jsoup.Jsoup

internal fun String.toJsoup() = Jsoup.connect(this).get()
