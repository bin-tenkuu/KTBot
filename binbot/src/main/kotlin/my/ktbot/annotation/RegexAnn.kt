package my.ktbot.annotation

import org.intellij.lang.annotations.Language

annotation class RegexAnn(@Language("RegExp") val pattern: String, vararg val option: RegexOption = [])
